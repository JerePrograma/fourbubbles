#requires -Version 7.0
[CmdletBinding()]
param(
    [switch]$Rebuild
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$RepositoryRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $RepositoryRoot

function Assert-Command {
    param([Parameter(Mandatory)][string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "No se encontró '$Name' en PATH. Instalalo antes de continuar."
    }
}

function New-RandomBase64 {
    param([Parameter(Mandatory)][ValidateRange(32, 256)][int]$Bytes)

    $Buffer = [byte[]]::new($Bytes)
    [System.Security.Cryptography.RandomNumberGenerator]::Fill($Buffer)
    return [Convert]::ToBase64String($Buffer)
}

function New-Password {
    $Raw = New-RandomBase64 -Bytes 24
    return $Raw.Replace('+', 'A').Replace('/', 'B').TrimEnd('=')
}

Assert-Command -Name 'docker'

& docker info *> $null
if ($LASTEXITCODE -ne 0) {
    throw 'Docker no está disponible. Iniciá Docker Desktop y volvé a ejecutar.'
}

$EnvExamplePath = Join-Path $RepositoryRoot '.env.example'
$EnvPath = Join-Path $RepositoryRoot '.env'

if (-not (Test-Path -LiteralPath $EnvExamplePath)) {
    throw "No existe $EnvExamplePath"
}

$CreatedEnvironment = $false
$GeneratedAdminPassword = $null

if (-not (Test-Path -LiteralPath $EnvPath)) {
    $PostgresPassword = New-Password
    $GeneratedAdminPassword = New-Password
    $JwtSecret = New-RandomBase64 -Bytes 48

    $Content = Get-Content -LiteralPath $EnvExamplePath -Raw
    $Content = $Content.Replace('POSTGRES_PASSWORD=change-me', "POSTGRES_PASSWORD=$PostgresPassword")
    $Content = $Content.Replace('DB_PASSWORD=change-me', "DB_PASSWORD=$PostgresPassword")
    $Content = $Content.Replace(
        'JWT_SECRET_BASE64=REPLACE_WITH_AT_LEAST_32_RANDOM_BYTES_ENCODED_AS_BASE64',
        "JWT_SECRET_BASE64=$JwtSecret"
    )
    $Content = $Content.Replace('APP_DEV_ADMIN_PASSWORD=change-me-now', "APP_DEV_ADMIN_PASSWORD=$GeneratedAdminPassword")

    Set-Content -LiteralPath $EnvPath -Value $Content -Encoding utf8
    $CreatedEnvironment = $true
}

& docker compose config --quiet
if ($LASTEXITCODE -ne 0) {
    throw 'La configuración Docker Compose o .env no es válida.'
}

$Arguments = @('compose', 'up', '-d')
if ($Rebuild) {
    $Arguments += '--build'
}

& docker @Arguments
if ($LASTEXITCODE -ne 0) {
    throw 'Docker Compose no pudo iniciar el entorno.'
}

$HealthUri = 'http://localhost:8081/api/actuator/health'
$Deadline = (Get-Date).AddMinutes(3)
$Healthy = $false

while ((Get-Date) -lt $Deadline) {
    try {
        $Health = Invoke-RestMethod -Uri $HealthUri -Method Get -TimeoutSec 5
        if ($Health.status -eq 'UP') {
            $Healthy = $true
            break
        }
    }
    catch {
        Start-Sleep -Seconds 3
    }
}

& docker compose ps

if (-not $Healthy) {
    & docker compose logs --tail 200 backend
    throw "El backend no quedó saludable dentro del plazo. Revisá los logs anteriores."
}

Write-Host ''
Write-Host 'Four Bubbles / Ropa Lista está iniciado.' -ForegroundColor Green
Write-Host 'Aplicación: http://localhost:8080'
Write-Host 'Swagger:    http://localhost:8081/api/swagger-ui.html'
Write-Host 'Salud:      http://localhost:8081/api/actuator/health'

if ($CreatedEnvironment) {
    Write-Host ''
    Write-Host 'Credenciales de desarrollo generadas:' -ForegroundColor Yellow
    Write-Host 'Usuario: admin'
    Write-Host "Contraseña: $GeneratedAdminPassword"
    Write-Host 'Guardá esta contraseña. Modificar .env no cambia automáticamente una cuenta ya creada.'
}
else {
    Write-Host ''
    Write-Host 'Se reutilizó el archivo .env existente; no fue modificado.'
}
