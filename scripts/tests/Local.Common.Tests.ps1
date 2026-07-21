#requires -Version 7.0

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$ScriptsRoot = Split-Path -Parent $PSScriptRoot
. (Join-Path $ScriptsRoot 'Local.Common.ps1')
. (Join-Path $ScriptsRoot 'Local.ContainerIdentity.ps1')

function Assert-Equal {
    param(
        [Parameter(Mandatory)]$Expected,
        [Parameter(Mandatory)]$Actual,
        [Parameter(Mandatory)][string]$Message
    )

    if ($Expected -ne $Actual) {
        throw "$Message Esperado='$Expected', actual='$Actual'."
    }
}

function Assert-Throws {
    param(
        [Parameter(Mandatory)][scriptblock]$Action,
        [Parameter(Mandatory)][string]$Message
    )

    $Thrown = $false
    try {
        & $Action
    }
    catch {
        $Thrown = $true
    }
    if (-not $Thrown) {
        throw $Message
    }
}

$TempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("fourbubbles-pwsh-tests-" + [Guid]::NewGuid())
New-Item -ItemType Directory -Path $TempRoot | Out-Null

try {
    Write-Host 'Probando parseo .env...'
    $EnvPath = Join-Path $TempRoot '.env'
    Set-Content -LiteralPath $EnvPath -Encoding utf8 -Value @(
        '# comentario',
        'POSTGRES_PASSWORD=keep-me',
        'VALUE_WITH_EQUALS=a=b=c',
        'QUOTED="hello world"'
    )
    $ParsedEnv = Read-DotEnvFile -Path $EnvPath
    Assert-Equal -Expected 'keep-me' -Actual $ParsedEnv['POSTGRES_PASSWORD'] -Message 'No preservó el secreto existente.'
    Assert-Equal -Expected 'a=b=c' -Actual $ParsedEnv['VALUE_WITH_EQUALS'] -Message 'Cortó un valor que contenía signos igual.'
    Assert-Equal -Expected 'hello world' -Actual $ParsedEnv['QUOTED'] -Message 'No removió comillas externas.'

    Write-Host 'Probando agregado idempotente sin reemplazos...'
    $Added = @(Add-MissingDotEnvEntries -Path $EnvPath -Entries ([ordered]@{
        POSTGRES_PASSWORD = 'must-not-replace'
        FRONTEND_HOST_PORT = '18080'
    }))
    Assert-Equal -Expected 1 -Actual $Added.Count -Message 'La cantidad de claves agregadas no fue la esperada.'
    $ParsedEnv = Read-DotEnvFile -Path $EnvPath
    Assert-Equal -Expected 'keep-me' -Actual $ParsedEnv['POSTGRES_PASSWORD'] -Message 'Se reemplazó un secreto existente.'
    Assert-Equal -Expected '18080' -Actual $ParsedEnv['FRONTEND_HOST_PORT'] -Message 'No agregó la variable faltante.'
    $AddedAgain = @(Add-MissingDotEnvEntries -Path $EnvPath -Entries ([ordered]@{ FRONTEND_HOST_PORT = '9999' }))
    Assert-Equal -Expected 0 -Actual $AddedAgain.Count -Message 'El agregado no fue idempotente.'
    Assert-Equal -Expected '18080' -Actual (Read-DotEnvFile -Path $EnvPath)['FRONTEND_HOST_PORT'] -Message 'La segunda ejecución reemplazó el puerto existente.'

    Write-Host 'Probando normalización de JSON de Compose...'
    Assert-Equal -Expected 0 -Actual @(ConvertFrom-ComposeJson -Json '').Count -Message 'La salida vacía no produjo una colección vacía.'
    Assert-Equal -Expected 1 -Actual @(ConvertFrom-ComposeJson -Json '{"Service":"backend"}').Count -Message 'Un objeto JSON no produjo una colección de un elemento.'
    Assert-Equal -Expected 2 -Actual @(ConvertFrom-ComposeJson -Json '[{"Service":"postgres"},{"Service":"backend"}]').Count -Message 'Un array JSON no conservó sus elementos.'
    $JsonLines = "{`"Service`":`"postgres`"}`n{`"Service`":`"backend`"}"
    Assert-Equal -Expected 2 -Actual @(ConvertFrom-ComposeJson -Json $JsonLines).Count -Message 'JSON por líneas no fue aceptado.'

    Write-Host 'Probando normalización de IDs Docker...'
    Assert-Equal -Expected '0123456789ab' -Actual (ConvertTo-DockerDisplayContainerId -ContainerId '0123456789abcdef0123456789abcdef') -Message 'No normalizó un ID completo al formato de docker ps.'
    Assert-Equal -Expected '0123456789ab' -Actual (ConvertTo-DockerDisplayContainerId -ContainerId '0123456789ab') -Message 'Alteró un ID ya abreviado.'

    Write-Host 'Probando validación de puertos...'
    Assert-Equal -Expected 8080 -Actual (Get-ValidatedPort -Name 'TEST_PORT' -Value '8080') -Message 'Un puerto válido fue rechazado.'
    Assert-Throws -Action { Get-ValidatedPort -Name 'TEST_PORT' -Value '0' } -Message 'Se aceptó el puerto 0.'
    Assert-Throws -Action { Get-ValidatedPort -Name 'TEST_PORT' -Value 'abc' } -Message 'Se aceptó un puerto no numérico.'
    Assert-Throws -Action {
        Get-LocalPorts -Environment @{
            POSTGRES_HOST_PORT = '8080'
            BACKEND_HOST_PORT = '8080'
            FRONTEND_HOST_PORT = '8081'
        }
    } -Message 'Se aceptaron puertos duplicados.'

    Write-Host 'Probando inicialización de .env existente...'
    $ExistingPath = Join-Path $TempRoot 'existing.env'
    $ExamplePath = Join-Path $TempRoot '.env.example'
    $Jwt = [Convert]::ToBase64String([byte[]](0..47))
    Set-Content -LiteralPath $ExistingPath -Encoding utf8 -Value @(
        'POSTGRES_DB=ropalista',
        'POSTGRES_USER=ropalista',
        'POSTGRES_PASSWORD=existing-postgres-secret',
        "JWT_SECRET_BASE64=$Jwt",
        'APP_DEV_ADMIN_USERNAME=admin',
        'APP_DEV_ADMIN_PASSWORD=existing-admin-secret'
    )
    Set-Content -LiteralPath $ExamplePath -Encoding utf8 -Value @(
        'POSTGRES_DB=ropalista',
        'POSTGRES_USER=ropalista',
        'POSTGRES_PASSWORD=change-me',
        'DB_PASSWORD=change-me',
        'JWT_SECRET_BASE64=REPLACE_WITH_AT_LEAST_32_RANDOM_BYTES_ENCODED_AS_BASE64',
        'APP_DEV_ADMIN_USERNAME=admin',
        'APP_DEV_ADMIN_PASSWORD=change-me-now'
    )
    $Initialization = Initialize-LocalEnvironment -ExamplePath $ExamplePath -EnvironmentPath $ExistingPath
    Assert-Equal -Expected $false -Actual $Initialization.Created -Message 'Marcó como nuevo un entorno existente.'
    Assert-Equal -Expected 'existing-postgres-secret' -Actual $Initialization.Values['POSTGRES_PASSWORD'] -Message 'Reemplazó la contraseña PostgreSQL existente.'
    Assert-Equal -Expected 'existing-admin-secret' -Actual $Initialization.Values['APP_DEV_ADMIN_PASSWORD'] -Message 'Reemplazó la contraseña administrativa existente.'
    Assert-Equal -Expected 5432 -Actual $Initialization.Ports.Postgres -Message 'No agregó el puerto PostgreSQL predeterminado.'
    Assert-Equal -Expected 8081 -Actual $Initialization.Ports.Backend -Message 'No agregó el puerto backend predeterminado.'
    Assert-Equal -Expected 8080 -Actual $Initialization.Ports.Frontend -Message 'No agregó el puerto frontend predeterminado.'

    Write-Host ''
    Write-Host 'Pruebas PowerShell exitosas.' -ForegroundColor Green
}
finally {
    Remove-Item -LiteralPath $TempRoot -Recurse -Force -ErrorAction SilentlyContinue
}
