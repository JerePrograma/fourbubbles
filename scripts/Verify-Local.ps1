#requires -Version 7.0
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$RepositoryRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $RepositoryRoot
. (Join-Path $PSScriptRoot 'Local.Common.ps1')

function Assert-True {
    param(
        [Parameter(Mandatory)][bool]$Condition,
        [Parameter(Mandatory)][string]$Message
    )

    if (-not $Condition) {
        throw $Message
    }
}

try {
    Assert-ExternalCommand -Name 'docker'

    & docker info *> $null
    if ($LASTEXITCODE -ne 0) {
        throw 'Docker no está disponible. Iniciá Docker Desktop.'
    }

    $Environment = Read-DotEnvFile -Path (Join-Path $RepositoryRoot '.env')
    Assert-RequiredEnvironment -Environment $Environment

    & docker compose config --quiet
    if ($LASTEXITCODE -ne 0) {
        throw 'La configuración Docker Compose o .env no es válida.'
    }

    Write-Host '1/6 Verificando servicios, contenedores y health...'
    $ComposeServices = @(Get-ComposeServiceObjects)
    Assert-True -Condition ($ComposeServices.Count -gt 0) -Message 'No hay servicios Docker Compose creados.'

    foreach ($ExpectedService in @('postgres', 'backend', 'frontend')) {
        $Matches = @($ComposeServices | Where-Object { [string](Get-ObjectPropertyValue -Object $_ -Name 'Service') -eq $ExpectedService })
        Assert-True -Condition ($Matches.Count -eq 1) -Message "Se esperaba exactamente un contenedor Compose para '$ExpectedService' y se encontraron $($Matches.Count)."

        $State = Get-ComposeServiceState -Service $ExpectedService
        Assert-True -Condition ($State.Status -eq 'running') -Message "El servicio '$ExpectedService' no está ejecutándose: estado=$($State.Status), código=$($State.ExitCode)."
        Assert-True -Condition ($State.Health -eq 'healthy') -Message "El servicio '$ExpectedService' no está saludable: health=$($State.Health)."
    }

    $PostgresPort = Get-ComposePublishedPort -Service 'postgres' -ContainerPort 5432
    $BackendPort = Get-ComposePublishedPort -Service 'backend' -ContainerPort 8080
    $FrontendPort = Get-ComposePublishedPort -Service 'frontend' -ContainerPort 80

    Write-Host '2/6 Verificando readiness del backend...'
    $HealthUri = "http://localhost:$BackendPort/api/actuator/health/readiness"
    $Health = Invoke-RestMethod -Uri $HealthUri -Method Get -TimeoutSec 10
    Assert-True -Condition ($Health.status -eq 'UP') -Message "Estado de readiness inesperado: $($Health.status)."

    Write-Host '3/6 Verificando migraciones Flyway...'
    $PostgresUser = [string]$Environment['POSTGRES_USER']
    $PostgresDatabase = [string]$Environment['POSTGRES_DB']
    $MigrationResult = & docker compose exec -T postgres psql -U $PostgresUser -d $PostgresDatabase -tAc 'select count(*) from flyway_schema_history where success = true;' 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo consultar flyway_schema_history: $($MigrationResult -join [Environment]::NewLine)"
    }
    $MigrationText = ($MigrationResult | Out-String).Trim()
    $MigrationCount = 0
    Assert-True -Condition ([int]::TryParse($MigrationText, [ref]$MigrationCount)) -Message "Flyway devolvió un conteo no numérico: '$MigrationText'."
    Assert-True -Condition ($MigrationCount -ge 8) -Message "Se esperaban al menos 8 migraciones exitosas; se encontraron $MigrationCount."

    Write-Host '4/6 Verificando SPA y proxy Nginx...'
    $FrontendUri = "http://localhost:$FrontendPort/"
    $Frontend = Invoke-WebRequest -Uri $FrontendUri -Method Get -TimeoutSec 10
    Assert-True -Condition ($Frontend.StatusCode -eq 200) -Message "El frontend respondió HTTP $($Frontend.StatusCode)."
    Assert-True -Condition ($Frontend.Content.Contains('<div id="root"></div>')) -Message 'La respuesta del frontend no contiene el punto de montaje esperado.'

    Write-Host '5/6 Verificando que la API rechace acceso anónimo...'
    $Anonymous = Invoke-WebRequest -Uri "http://localhost:$FrontendPort/api/catalog/services" -Method Get -SkipHttpErrorCheck -TimeoutSec 10
    Assert-True -Condition ($Anonymous.StatusCode -in @(401, 403)) -Message "La API protegida respondió HTTP $($Anonymous.StatusCode) sin token; se esperaba 401 o 403."

    Write-Host '6/6 Verificando login y API protegida autenticada...'
    $Username = [string]$Environment['APP_DEV_ADMIN_USERNAME']
    $Password = [string]$Environment['APP_DEV_ADMIN_PASSWORD']

    try {
        $Login = Invoke-RestMethod -Uri "http://localhost:$FrontendPort/api/auth/login" -Method Post -ContentType 'application/json' -Body (@{
            username = $Username
            password = $Password
        } | ConvertTo-Json) -TimeoutSec 15
    }
    catch {
        throw 'No se pudo autenticar con las credenciales actuales de .env. Si la contraseña cambió después de crear la base, restaurá la original o ejecutá Start-Local.ps1 -Reset.'
    }

    Assert-True -Condition ($Login.success -eq $true) -Message 'La respuesta de login no indicó éxito.'
    $AccessToken = [string]$Login.data.accessToken
    Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($AccessToken)) -Message 'El login no devolvió un access token.'

    $Services = Invoke-RestMethod -Uri "http://localhost:$FrontendPort/api/catalog/services" -Method Get -Headers @{ Authorization = "Bearer $AccessToken" } -TimeoutSec 15
    Assert-True -Condition ($Services.success -eq $true) -Message 'La consulta autenticada del catálogo no indicó éxito.'
    $ServiceItems = @($Services.data)
    Assert-True -Condition ($ServiceItems.Count -gt 0) -Message 'El catálogo autenticado no devolvió servicios.'

    Write-Host ''
    Write-Host 'Verificación local exitosa.' -ForegroundColor Green
    Write-Host "Contenedores: postgres, backend y frontend en running/healthy"
    Write-Host "Puertos efectivos: PostgreSQL=$PostgresPort, backend=$BackendPort, frontend=$FrontendPort"
    Write-Host "Readiness: $($Health.status)"
    Write-Host "Migraciones exitosas: $MigrationCount"
    Write-Host "Servicios disponibles: $($ServiceItems.Count)"
    Write-Host 'SPA, proxy, rechazo anónimo, login y API protegida: OK'
}
catch {
    Write-Host ''
    Write-Host "Verificación local fallida: $($_.Exception.Message)" -ForegroundColor Red
    try {
        Show-ComposeDiagnostics -Tail 250
    }
    catch {
        Write-Host "No se pudieron obtener diagnósticos completos: $($_.Exception.Message)" -ForegroundColor Yellow
    }
    throw
}
