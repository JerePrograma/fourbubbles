#requires -Version 7.0
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$RepositoryRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $RepositoryRoot

function Assert-True {
    param(
        [Parameter(Mandatory)][bool]$Condition,
        [Parameter(Mandatory)][string]$Message
    )

    if (-not $Condition) {
        throw $Message
    }
}

function Read-DotEnv {
    param([Parameter(Mandatory)][string]$Path)

    if (-not (Test-Path -LiteralPath $Path)) {
        throw "No existe $Path. Ejecutá primero .\scripts\Start-Local.ps1."
    }

    $Values = @{}
    foreach ($Line in Get-Content -LiteralPath $Path) {
        $Trimmed = $Line.Trim()
        if ([string]::IsNullOrWhiteSpace($Trimmed) -or $Trimmed.StartsWith('#')) {
            continue
        }
        $Separator = $Trimmed.IndexOf('=')
        if ($Separator -lt 1) {
            continue
        }
        $Name = $Trimmed.Substring(0, $Separator).Trim()
        $Value = $Trimmed.Substring($Separator + 1)
        $Values[$Name] = $Value
    }
    return $Values
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw 'Docker no está disponible en PATH.'
}

Write-Host '1/5 Verificando contenedores...'
$ComposePs = & docker compose ps --format json
if ($LASTEXITCODE -ne 0) {
    throw 'No se pudo consultar Docker Compose.'
}
Assert-True -Condition ($ComposePs.Count -gt 0) -Message 'No hay servicios Docker Compose iniciados.'

Write-Host '2/5 Verificando salud del backend...'
$Health = Invoke-RestMethod -Uri 'http://localhost:8081/api/actuator/health' -Method Get -TimeoutSec 10
Assert-True -Condition ($Health.status -eq 'UP') -Message "Estado de salud inesperado: $($Health.status)"

Write-Host '3/5 Verificando migraciones Flyway...'
$MigrationResult = & docker compose exec -T postgres psql -U ropalista -d ropalista -tAc 'select count(*) from flyway_schema_history where success = true;'
if ($LASTEXITCODE -ne 0) {
    throw 'No se pudo consultar flyway_schema_history.'
}
$MigrationCount = [int]($MigrationResult.Trim())
Assert-True -Condition ($MigrationCount -ge 10) -Message "Se esperaban al menos 10 migraciones exitosas; se encontraron $MigrationCount."

Write-Host '4/5 Verificando aplicación web...'
$Frontend = Invoke-WebRequest -Uri 'http://localhost:8080/' -Method Get -TimeoutSec 10
Assert-True -Condition ($Frontend.StatusCode -eq 200) -Message "El frontend respondió HTTP $($Frontend.StatusCode)."
Assert-True -Condition ($Frontend.Content.Contains('<div id="root"></div>')) -Message 'La respuesta del frontend no contiene el punto de montaje esperado.'

Write-Host '5/5 Verificando autenticación y API protegida...'
$Environment = Read-DotEnv -Path (Join-Path $RepositoryRoot '.env')
$Username = if ($Environment.ContainsKey('APP_DEV_ADMIN_USERNAME')) { $Environment['APP_DEV_ADMIN_USERNAME'] } else { 'admin' }
if (-not $Environment.ContainsKey('APP_DEV_ADMIN_PASSWORD') -or [string]::IsNullOrWhiteSpace($Environment['APP_DEV_ADMIN_PASSWORD'])) {
    throw 'APP_DEV_ADMIN_PASSWORD no está definido en .env.'
}

try {
    $Login = Invoke-RestMethod -Uri 'http://localhost:8081/api/auth/login' -Method Post -ContentType 'application/json' -Body (@{
        username = $Username
        password = $Environment['APP_DEV_ADMIN_PASSWORD']
    } | ConvertTo-Json) -TimeoutSec 15
}
catch {
    throw 'No se pudo autenticar con las credenciales actuales de .env. Si cambiaste APP_DEV_ADMIN_PASSWORD después de crear la base, restaurá la contraseña original o recreá el volumen local.'
}

Assert-True -Condition ($Login.success -eq $true) -Message 'La respuesta de login no indicó éxito.'
$AccessToken = [string]$Login.data.accessToken
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($AccessToken)) -Message 'El login no devolvió un access token.'

$Services = Invoke-RestMethod -Uri 'http://localhost:8081/api/catalog/services' -Method Get -Headers @{ Authorization = "Bearer $AccessToken" } -TimeoutSec 15
Assert-True -Condition ($Services.success -eq $true) -Message 'La consulta autenticada del catálogo no indicó éxito.'
Assert-True -Condition ($Services.data.Count -gt 0) -Message 'El catálogo autenticado no devolvió servicios.'

Write-Host ''
Write-Host 'Verificación local exitosa.' -ForegroundColor Green
Write-Host "Salud: $($Health.status)"
Write-Host "Migraciones exitosas: $MigrationCount"
Write-Host "Servicios disponibles: $($Services.data.Count)"
Write-Host 'Frontend, autenticación y API protegida: OK'
