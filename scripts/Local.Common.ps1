#requires -Version 7.0

Set-StrictMode -Version Latest

function Assert-ExternalCommand {
    param([Parameter(Mandatory)][string]$Name)

    if (-not (Get-Command -Name $Name -ErrorAction SilentlyContinue)) {
        throw "No se encontró '$Name' en PATH. Instalalo antes de continuar."
    }
}

function New-RandomBase64 {
    param([Parameter(Mandatory)][ValidateRange(32, 256)][int]$Bytes)

    $Buffer = [byte[]]::new($Bytes)
    [System.Security.Cryptography.RandomNumberGenerator]::Fill($Buffer)
    return [Convert]::ToBase64String($Buffer)
}

function New-LocalPassword {
    $Raw = New-RandomBase64 -Bytes 32
    return $Raw.Replace('+', 'A').Replace('/', 'B').TrimEnd('=')
}

function Read-DotEnvFile {
    param(
        [Parameter(Mandatory)][string]$Path,
        [switch]$AllowMissing
    )

    if (-not (Test-Path -LiteralPath $Path)) {
        if ($AllowMissing) {
            return @{}
        }
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
        $Value = $Trimmed.Substring($Separator + 1).Trim()
        if ($Value.Length -ge 2) {
            $First = $Value[0]
            $Last = $Value[$Value.Length - 1]
            if (($First -eq '"' -and $Last -eq '"') -or ($First -eq "'" -and $Last -eq "'")) {
                $Value = $Value.Substring(1, $Value.Length - 2)
            }
        }
        $Values[$Name] = $Value
    }

    return $Values
}

function Add-MissingDotEnvEntries {
    param(
        [Parameter(Mandatory)][string]$Path,
        [Parameter(Mandatory)][System.Collections.IDictionary]$Entries
    )

    $Existing = Read-DotEnvFile -Path $Path -AllowMissing
    $Lines = [System.Collections.Generic.List[string]]::new()
    if (Test-Path -LiteralPath $Path) {
        foreach ($Line in Get-Content -LiteralPath $Path) {
            $Lines.Add($Line)
        }
    }

    $Added = [System.Collections.Generic.List[string]]::new()
    foreach ($Key in $Entries.Keys) {
        $Name = [string]$Key
        if ($Existing.ContainsKey($Name)) {
            continue
        }

        if ($Lines.Count -gt 0 -and -not [string]::IsNullOrWhiteSpace($Lines[$Lines.Count - 1])) {
            $Lines.Add('')
        }
        $Lines.Add("$Name=$($Entries[$Key])")
        $Existing[$Name] = [string]$Entries[$Key]
        $Added.Add($Name)
    }

    if ($Added.Count -gt 0) {
        Set-Content -LiteralPath $Path -Value $Lines -Encoding utf8
    }

    return @($Added)
}

function Get-ValidatedPort {
    param(
        [Parameter(Mandatory)][string]$Name,
        [Parameter(Mandatory)]$Value
    )

    $Parsed = 0
    if (-not [int]::TryParse(([string]$Value).Trim(), [ref]$Parsed) -or $Parsed -lt 1 -or $Parsed -gt 65535) {
        throw "$Name debe ser un puerto TCP entre 1 y 65535. Valor recibido: '$Value'."
    }
    return $Parsed
}

function Get-LocalPorts {
    param([Parameter(Mandatory)][hashtable]$Environment)

    $PostgresValue = if ($Environment.ContainsKey('POSTGRES_HOST_PORT')) { $Environment['POSTGRES_HOST_PORT'] } else { '5432' }
    $BackendValue = if ($Environment.ContainsKey('BACKEND_HOST_PORT')) { $Environment['BACKEND_HOST_PORT'] } else { '8081' }
    $FrontendValue = if ($Environment.ContainsKey('FRONTEND_HOST_PORT')) { $Environment['FRONTEND_HOST_PORT'] } else { '8080' }

    $Ports = [pscustomobject]@{
        Postgres = Get-ValidatedPort -Name 'POSTGRES_HOST_PORT' -Value $PostgresValue
        Backend = Get-ValidatedPort -Name 'BACKEND_HOST_PORT' -Value $BackendValue
        Frontend = Get-ValidatedPort -Name 'FRONTEND_HOST_PORT' -Value $FrontendValue
    }

    $Distinct = @($Ports.Postgres, $Ports.Backend, $Ports.Frontend) | Sort-Object -Unique
    if ($Distinct.Count -ne 3) {
        throw 'POSTGRES_HOST_PORT, BACKEND_HOST_PORT y FRONTEND_HOST_PORT deben ser distintos.'
    }

    return $Ports
}

function Assert-RequiredEnvironment {
    param([Parameter(Mandatory)][hashtable]$Environment)

    foreach ($Name in @('POSTGRES_DB', 'POSTGRES_USER', 'POSTGRES_PASSWORD', 'JWT_SECRET_BASE64', 'APP_DEV_ADMIN_USERNAME', 'APP_DEV_ADMIN_PASSWORD')) {
        if (-not $Environment.ContainsKey($Name) -or [string]::IsNullOrWhiteSpace([string]$Environment[$Name])) {
            throw "$Name no está definido en .env."
        }
    }

    foreach ($Name in @('POSTGRES_PASSWORD', 'APP_DEV_ADMIN_PASSWORD')) {
        $Value = [string]$Environment[$Name]
        if ($Value -in @('change-me', 'change-me-now') -or $Value.StartsWith('REPLACE_', [System.StringComparison]::OrdinalIgnoreCase)) {
            throw "$Name conserva un valor de ejemplo. Reemplazalo por un secreto local real."
        }
    }

    try {
        $JwtBytes = [Convert]::FromBase64String([string]$Environment['JWT_SECRET_BASE64'])
    }
    catch {
        throw 'JWT_SECRET_BASE64 no contiene Base64 válido.'
    }
    if ($JwtBytes.Length -lt 32) {
        throw 'JWT_SECRET_BASE64 debe representar al menos 32 bytes.'
    }

    [void](Get-LocalPorts -Environment $Environment)
}

function Initialize-LocalEnvironment {
    param(
        [Parameter(Mandatory)][string]$ExamplePath,
        [Parameter(Mandatory)][string]$EnvironmentPath
    )

    if (-not (Test-Path -LiteralPath $ExamplePath)) {
        throw "No existe $ExamplePath."
    }

    $Created = $false
    $GeneratedAdminPassword = $null
    $AddedKeys = @()

    if (-not (Test-Path -LiteralPath $EnvironmentPath)) {
        $PostgresPassword = New-LocalPassword
        $GeneratedAdminPassword = New-LocalPassword
        $JwtSecret = New-RandomBase64 -Bytes 48

        $Content = Get-Content -LiteralPath $ExamplePath -Raw
        $Content = $Content.Replace('POSTGRES_PASSWORD=change-me', "POSTGRES_PASSWORD=$PostgresPassword")
        $Content = $Content.Replace('DB_PASSWORD=change-me', "DB_PASSWORD=$PostgresPassword")
        $Content = $Content.Replace(
            'JWT_SECRET_BASE64=REPLACE_WITH_AT_LEAST_32_RANDOM_BYTES_ENCODED_AS_BASE64',
            "JWT_SECRET_BASE64=$JwtSecret"
        )
        $Content = $Content.Replace('APP_DEV_ADMIN_PASSWORD=change-me-now', "APP_DEV_ADMIN_PASSWORD=$GeneratedAdminPassword")
        Set-Content -LiteralPath $EnvironmentPath -Value $Content -Encoding utf8
        $Created = $true
    }
    else {
        $Current = Read-DotEnvFile -Path $EnvironmentPath
        $Missing = [ordered]@{}

        foreach ($Pair in @(
            @('COMPOSE_PROJECT_NAME', 'fourbubbles'),
            @('POSTGRES_HOST_PORT', '5432'),
            @('BACKEND_HOST_PORT', '8081'),
            @('FRONTEND_HOST_PORT', '8080'),
            @('POSTGRES_DB', 'ropalista'),
            @('POSTGRES_USER', 'ropalista'),
            @('APP_DEV_ADMIN_USERNAME', 'admin')
        )) {
            if (-not $Current.ContainsKey($Pair[0])) {
                $Missing[$Pair[0]] = $Pair[1]
            }
        }

        if (-not $Current.ContainsKey('POSTGRES_PASSWORD')) {
            $Missing['POSTGRES_PASSWORD'] = New-LocalPassword
        }
        $EffectivePostgresPassword = if ($Current.ContainsKey('POSTGRES_PASSWORD')) {
            [string]$Current['POSTGRES_PASSWORD']
        }
        else {
            [string]$Missing['POSTGRES_PASSWORD']
        }
        if (-not $Current.ContainsKey('DB_PASSWORD')) {
            $Missing['DB_PASSWORD'] = $EffectivePostgresPassword
        }
        if (-not $Current.ContainsKey('JWT_SECRET_BASE64')) {
            $Missing['JWT_SECRET_BASE64'] = New-RandomBase64 -Bytes 48
        }
        if (-not $Current.ContainsKey('APP_DEV_ADMIN_PASSWORD')) {
            $GeneratedAdminPassword = New-LocalPassword
            $Missing['APP_DEV_ADMIN_PASSWORD'] = $GeneratedAdminPassword
        }
        if (-not $Current.ContainsKey('DB_HOST')) { $Missing['DB_HOST'] = 'postgres' }
        if (-not $Current.ContainsKey('DB_PORT')) { $Missing['DB_PORT'] = '5432' }
        if (-not $Current.ContainsKey('DB_NAME')) { $Missing['DB_NAME'] = 'ropalista' }
        if (-not $Current.ContainsKey('DB_USER')) { $Missing['DB_USER'] = 'ropalista' }
        if (-not $Current.ContainsKey('CORS_ALLOWED_ORIGINS')) { $Missing['CORS_ALLOWED_ORIGINS'] = 'http://localhost:5173,http://localhost:8080' }
        if (-not $Current.ContainsKey('APP_TIME_ZONE')) { $Missing['APP_TIME_ZONE'] = 'America/Argentina/Buenos_Aires' }
        if (-not $Current.ContainsKey('VITE_API_BASE_URL')) { $Missing['VITE_API_BASE_URL'] = '/api' }

        $AddedKeys = @(Add-MissingDotEnvEntries -Path $EnvironmentPath -Entries $Missing)
    }

    $Environment = Read-DotEnvFile -Path $EnvironmentPath
    Assert-RequiredEnvironment -Environment $Environment

    return [pscustomobject]@{
        Created = $Created
        GeneratedAdminPassword = $GeneratedAdminPassword
        AddedKeys = @($AddedKeys)
        Values = $Environment
        Ports = Get-LocalPorts -Environment $Environment
    }
}

function ConvertFrom-ComposeJson {
    param([AllowEmptyString()][string]$Json)

    if ([string]::IsNullOrWhiteSpace($Json)) {
        return @()
    }

    $Trimmed = $Json.Trim()
    try {
        $Parsed = ConvertFrom-Json -InputObject $Trimmed -Depth 32
        return @($Parsed)
    }
    catch {
        $Items = [System.Collections.Generic.List[object]]::new()
        foreach ($Line in ($Trimmed -split '\r?\n')) {
            if ([string]::IsNullOrWhiteSpace($Line)) {
                continue
            }
            try {
                $Items.Add((ConvertFrom-Json -InputObject $Line -Depth 32))
            }
            catch {
                throw "Docker Compose devolvió JSON no reconocido: $Line"
            }
        }
        return @($Items)
    }
}

function Get-ComposeServiceObjects {
    $Output = & docker compose ps --all --format json 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo consultar Docker Compose: $($Output -join [Environment]::NewLine)"
    }
    $Raw = ($Output | Out-String).Trim()
    return @(ConvertFrom-ComposeJson -Json $Raw)
}

function Get-ObjectPropertyValue {
    param(
        [Parameter(Mandatory)]$Object,
        [Parameter(Mandatory)][string]$Name
    )

    $Property = $Object.PSObject.Properties[$Name]
    if ($null -eq $Property) {
        return $null
    }
    return $Property.Value
}

function Get-CurrentComposeContainerIds {
    $Output = & docker compose ps --all -q 2>$null
    if ($LASTEXITCODE -ne 0) {
        return @()
    }
    return @($Output | ForEach-Object { ([string]$_).Trim() } | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
}

function Get-DockerPublishedPortOwners {
    param([Parameter(Mandatory)][int]$Port)

    $Output = & docker ps --format '{{json .}}' 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo consultar los contenedores Docker: $($Output -join [Environment]::NewLine)"
    }

    $Owners = [System.Collections.Generic.List[object]]::new()
    foreach ($Line in $Output) {
        if ([string]::IsNullOrWhiteSpace([string]$Line)) {
            continue
        }
        $Item = ConvertFrom-Json -InputObject ([string]$Line)
        $Ports = [string]$Item.Ports
        if ($Ports -match "(?:^|,\s*)(?:0\.0\.0\.0|127\.0\.0\.1|localhost|\[::\]|\[::1\]|::):$Port->") {
            $Owners.Add([pscustomobject]@{
                Kind = 'container'
                ContainerId = [string]$Item.ID
                Name = [string]$Item.Names
                Image = [string]$Item.Image
                Ports = $Ports
                Pid = $null
                Process = $null
            })
        }
    }
    return @($Owners)
}

function Get-WindowsPortProcessOwners {
    param([Parameter(Mandatory)][int]$Port)

    if (-not $IsWindows -or -not (Get-Command Get-NetTCPConnection -ErrorAction SilentlyContinue)) {
        return @()
    }

    $Owners = [System.Collections.Generic.List[object]]::new()
    $Connections = @(Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue)
    foreach ($Connection in $Connections) {
        $ProcessName = '<desconocido>'
        $ProcessPath = $null
        try {
            $ProcessInfo = Get-Process -Id $Connection.OwningProcess -ErrorAction Stop
            $ProcessName = $ProcessInfo.ProcessName
            $ProcessPath = $ProcessInfo.Path
        }
        catch {
        }
        $Owners.Add([pscustomobject]@{
            Kind = 'process'
            ContainerId = $null
            Name = $null
            Image = $null
            Ports = "$($Connection.LocalAddress):$Port"
            Pid = [int]$Connection.OwningProcess
            Process = if ($ProcessPath) { "$ProcessName ($ProcessPath)" } else { $ProcessName }
        })
    }
    return @($Owners)
}

function Get-PortConflicts {
    param(
        [Parameter(Mandatory)][int]$Port,
        [string[]]$CurrentComposeContainerIds = @()
    )

    $DockerOwners = @(Get-DockerPublishedPortOwners -Port $Port)
    $CurrentSet = @{}
    foreach ($Id in $CurrentComposeContainerIds) {
        $CurrentSet[[string]$Id] = $true
    }

    $ExternalContainers = @($DockerOwners | Where-Object { -not $CurrentSet.ContainsKey([string]$_.ContainerId) })
    if ($ExternalContainers.Count -gt 0) {
        return $ExternalContainers
    }

    if ($DockerOwners.Count -gt 0) {
        return @()
    }

    return @(Get-WindowsPortProcessOwners -Port $Port)
}

function Assert-ConfiguredPortsAvailable {
    param([Parameter(Mandatory)]$Ports)

    $CurrentIds = @(Get-CurrentComposeContainerIds)
    $Definitions = @(
        [pscustomobject]@{ Name = 'PostgreSQL'; Variable = 'POSTGRES_HOST_PORT'; Port = [int]$Ports.Postgres },
        [pscustomobject]@{ Name = 'Backend'; Variable = 'BACKEND_HOST_PORT'; Port = [int]$Ports.Backend },
        [pscustomobject]@{ Name = 'Frontend'; Variable = 'FRONTEND_HOST_PORT'; Port = [int]$Ports.Frontend }
    )

    $Failures = [System.Collections.Generic.List[string]]::new()
    foreach ($Definition in $Definitions) {
        $Conflicts = @(Get-PortConflicts -Port $Definition.Port -CurrentComposeContainerIds $CurrentIds)
        foreach ($Conflict in $Conflicts) {
            if ($Conflict.Kind -eq 'container') {
                $Failures.Add("$($Definition.Name) ($($Definition.Variable)=$($Definition.Port)): contenedor '$($Conflict.Name)', imagen '$($Conflict.Image)', id '$($Conflict.ContainerId)', publicación '$($Conflict.Ports)'.")
            }
            else {
                $Failures.Add("$($Definition.Name) ($($Definition.Variable)=$($Definition.Port)): PID $($Conflict.Pid), proceso '$($Conflict.Process)', escucha '$($Conflict.Ports)'.")
            }
        }
    }

    if ($Failures.Count -gt 0) {
        $Details = $Failures -join [Environment]::NewLine
        throw "Hay puertos ocupados por recursos ajenos a este proyecto:`n$Details`nNo se detuvo nada. Cambiá los puertos en .env o detené manualmente el recurso indicado."
    }
}

function Invoke-ComposeChecked {
    param([Parameter(Mandatory)][string[]]$Arguments)

    & docker compose @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Falló: docker compose $($Arguments -join ' ')"
    }
}

function Get-ComposeServiceState {
    param([Parameter(Mandatory)][string]$Service)

    $Ids = @(& docker compose ps --all -q $Service 2>$null | ForEach-Object { ([string]$_).Trim() } | Where-Object { $_ })
    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo resolver el contenedor del servicio '$Service'."
    }
    if ($Ids.Count -ne 1) {
        throw "Se esperaba un contenedor para '$Service' y se encontraron $($Ids.Count)."
    }

    $StateJson = & docker inspect --format '{{json .State}}' $Ids[0] 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo inspeccionar '$Service': $($StateJson -join [Environment]::NewLine)"
    }
    $State = ConvertFrom-Json -InputObject ([string]$StateJson)
    $Health = $null
    $HealthProperty = $State.PSObject.Properties['Health']
    if ($null -ne $HealthProperty -and $null -ne $HealthProperty.Value) {
        $Health = [string]$HealthProperty.Value.Status
    }

    return [pscustomobject]@{
        Service = $Service
        ContainerId = $Ids[0]
        Status = [string]$State.Status
        Health = $Health
        ExitCode = [int]$State.ExitCode
        Error = [string]$State.Error
    }
}

function Wait-ComposeServiceHealthy {
    param(
        [Parameter(Mandatory)][string]$Service,
        [ValidateRange(1, 900)][int]$TimeoutSeconds = 180
    )

    $Deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $LastState = $null
    while ((Get-Date) -lt $Deadline) {
        try {
            $LastState = Get-ComposeServiceState -Service $Service
            if ($LastState.Status -in @('exited', 'dead')) {
                throw "El servicio '$Service' terminó con código $($LastState.ExitCode). $($LastState.Error)"
            }
            if ($LastState.Status -eq 'running' -and $LastState.Health -eq 'healthy') {
                return $LastState
            }
        }
        catch {
            if ($_.Exception.Message -like "El servicio '$Service' terminó*") {
                throw
            }
        }
        Start-Sleep -Seconds 2
    }

    $Description = if ($null -eq $LastState) { 'sin contenedor disponible' } else { "estado=$($LastState.Status), health=$($LastState.Health)" }
    throw "El servicio '$Service' no quedó saludable en $TimeoutSeconds segundos ($Description)."
}

function Get-ComposePublishedPort {
    param(
        [Parameter(Mandatory)][string]$Service,
        [Parameter(Mandatory)][int]$ContainerPort
    )

    $Output = & docker compose port $Service $ContainerPort 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo obtener el puerto publicado de '$Service': $($Output -join [Environment]::NewLine)"
    }
    $Line = @($Output | ForEach-Object { ([string]$_).Trim() } | Where-Object { $_ }) | Select-Object -Last 1
    if ([string]::IsNullOrWhiteSpace($Line) -or $Line -notmatch ':(\d+)$') {
        throw "Formato de puerto no reconocido para '$Service': '$Line'."
    }
    return [int]$Matches[1]
}

function Show-ComposeDiagnostics {
    param([ValidateRange(1, 2000)][int]$Tail = 200)

    Write-Host ''
    Write-Host 'Estado Docker Compose:' -ForegroundColor Yellow
    & docker compose ps --all 2>&1 | ForEach-Object { Write-Host $_ }
    Write-Host ''
    Write-Host "Logs recientes (últimas $Tail líneas):" -ForegroundColor Yellow
    & docker compose logs --tail $Tail postgres backend frontend 2>&1 | ForEach-Object { Write-Host $_ }
}
