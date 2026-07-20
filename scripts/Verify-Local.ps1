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

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw 'Docker no está disponible en PATH.'
}

Write-Host '1/3 Verificando contenedores...'
$ComposePs = & docker compose ps --format json
if ($LASTEXITCODE -ne 0) {
    throw 'No se pudo consultar Docker Compose.'
}
Assert-True -Condition ($ComposePs.Count -gt 0) -Message 'No hay servicios Docker Compose iniciados.'

Write-Host '2/3 Verificando salud del backend...'
$Health = Invoke-RestMethod -Uri 'http://localhost:8081/api/actuator/health' -Method Get -TimeoutSec 10
Assert-True -Condition ($Health.status -eq 'UP') -Message "Estado de salud inesperado: $($Health.status)"

Write-Host '3/3 Verificando migraciones Flyway...'
$MigrationResult = & docker compose exec -T postgres psql -U ropalista -d ropalista -tAc 'select count(*) from flyway_schema_history where success = true;'

if ($LASTEXITCODE -ne 0) {
    throw 'No se pudo consultar flyway_schema_history.'
}

$MigrationCount = [int]($MigrationResult.Trim())
Assert-True -Condition ($MigrationCount -ge 6) -Message "Se esperaban al menos 6 migraciones exitosas; se encontraron $MigrationCount."

Write-Host ''
Write-Host 'Verificación local exitosa.' -ForegroundColor Green
Write-Host "Salud: $($Health.status)"
Write-Host "Migraciones exitosas: $MigrationCount"
Write-Host 'La autenticación y el flujo funcional deben verificarse desde la UI o Swagger.'
