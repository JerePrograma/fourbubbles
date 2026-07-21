#requires -Version 7.0
[CmdletBinding()]
param(
    [switch]$Rebuild,
    [switch]$Reset,
    [switch]$SkipOpen
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$RepositoryRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $RepositoryRoot
. (Join-Path $PSScriptRoot 'Local.Common.ps1')
. (Join-Path $PSScriptRoot 'Local.ContainerIdentity.ps1')

$AttemptedStart = $false
$EnvironmentResult = $null

try {
    Assert-ExternalCommand -Name 'docker'

    & docker info *> $null
    if ($LASTEXITCODE -ne 0) {
        throw 'Docker no está disponible. Iniciá Docker Desktop y volvé a ejecutar.'
    }
    & docker compose version *> $null
    if ($LASTEXITCODE -ne 0) {
        throw 'Docker Compose no está disponible como subcomando de Docker.'
    }

    $EnvironmentResult = Initialize-LocalEnvironment `
        -ExamplePath (Join-Path $RepositoryRoot '.env.example') `
        -EnvironmentPath (Join-Path $RepositoryRoot '.env')

    Write-Host 'Validando configuración Docker Compose...'
    & docker compose config --quiet
    if ($LASTEXITCODE -ne 0) {
        throw 'La configuración Docker Compose o .env no es válida.'
    }

    Write-Host 'Comprobando conflictos de puertos antes de construir...'
    Assert-ConfiguredPortsAvailable -Ports $EnvironmentResult.Ports

    if ($Reset) {
        Write-Host 'Eliminando contenedores, redes y volumen PostgreSQL del proyecto...' -ForegroundColor Yellow
        Invoke-ComposeChecked -Arguments @('down', '-v', '--remove-orphans')
    }

    $Arguments = @('up', '-d', '--remove-orphans')
    if ($Rebuild) {
        $Arguments += '--build'
    }

    $AttemptedStart = $true
    Invoke-ComposeChecked -Arguments $Arguments

    Write-Host 'Esperando health real de PostgreSQL...'
    [void](Wait-ComposeServiceHealthy -Service 'postgres' -TimeoutSeconds 180)
    Write-Host 'Esperando readiness real del backend...'
    [void](Wait-ComposeServiceHealthy -Service 'backend' -TimeoutSeconds 240)
    Write-Host 'Esperando health real del frontend...'
    [void](Wait-ComposeServiceHealthy -Service 'frontend' -TimeoutSeconds 120)

    $PostgresPort = Get-ComposePublishedPort -Service 'postgres' -ContainerPort 5432
    $BackendPort = Get-ComposePublishedPort -Service 'backend' -ContainerPort 8080
    $FrontendPort = Get-ComposePublishedPort -Service 'frontend' -ContainerPort 80

    $BackendHealthUri = "http://localhost:$BackendPort/api/actuator/health/readiness"
    $FrontendUri = "http://localhost:$FrontendPort/"

    $Health = Invoke-RestMethod -Uri $BackendHealthUri -Method Get -TimeoutSec 10
    if ($Health.status -ne 'UP') {
        throw "El backend respondió un estado de readiness inesperado: $($Health.status)."
    }

    $Frontend = Invoke-WebRequest -Uri $FrontendUri -Method Get -TimeoutSec 10
    if ($Frontend.StatusCode -ne 200 -or -not $Frontend.Content.Contains('<div id="root"></div>')) {
        throw 'El frontend no devolvió la SPA esperada.'
    }

    Write-Host ''
    Write-Host 'Four Bubbles / Ropa Lista está iniciado y saludable.' -ForegroundColor Green
    Write-Host "Aplicación: http://localhost:$FrontendPort"
    Write-Host "API:        http://localhost:$BackendPort/api"
    Write-Host "Swagger:    http://localhost:$BackendPort/api/swagger-ui.html"
    Write-Host "Salud:      $BackendHealthUri"
    Write-Host "PostgreSQL: localhost:$PostgresPort"

    if ($EnvironmentResult.Created) {
        Write-Host ''
        Write-Host 'Se creó .env con secretos locales aleatorios.' -ForegroundColor Yellow
    }
    elseif ($EnvironmentResult.AddedKeys.Count -gt 0) {
        Write-Host ''
        Write-Host "Se completaron variables faltantes en .env sin reemplazar valores existentes: $($EnvironmentResult.AddedKeys -join ', ')." -ForegroundColor Yellow
    }
    else {
        Write-Host ''
        Write-Host 'Se reutilizó .env sin modificarlo.'
    }

    if (-not [string]::IsNullOrWhiteSpace([string]$EnvironmentResult.GeneratedAdminPassword)) {
        Write-Host ''
        Write-Host 'Credenciales administrativas generadas:' -ForegroundColor Yellow
        Write-Host "Usuario: $($EnvironmentResult.Values['APP_DEV_ADMIN_USERNAME'])"
        Write-Host "Contraseña: $($EnvironmentResult.GeneratedAdminPassword)"
        Write-Host 'Guardá esta contraseña. Cambiar .env no modifica una cuenta ya persistida en PostgreSQL.'
    }

    if (-not $SkipOpen -and $IsWindows) {
        Start-Process "http://localhost:$FrontendPort"
    }
}
catch {
    Write-Host ''
    Write-Host "Inicio local fallido: $($_.Exception.Message)" -ForegroundColor Red

    if ($AttemptedStart) {
        try {
            Show-ComposeDiagnostics -Tail 250
        }
        catch {
            Write-Host "No se pudieron obtener diagnósticos completos: $($_.Exception.Message)" -ForegroundColor Yellow
        }

        Write-Host ''
        Write-Host 'Limpiando el inicio parcial del proyecto sin eliminar el volumen PostgreSQL...' -ForegroundColor Yellow
        & docker compose down --remove-orphans 2>&1 | ForEach-Object { Write-Host $_ }
    }

    throw
}
