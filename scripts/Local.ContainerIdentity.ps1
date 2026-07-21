#requires -Version 7.0

Set-StrictMode -Version Latest

function ConvertTo-DockerDisplayContainerId {
    param([Parameter(Mandatory)][string]$ContainerId)

    $Normalized = $ContainerId.Trim()
    if ([string]::IsNullOrWhiteSpace($Normalized)) {
        return $null
    }

    # `docker ps --format {{json .}}` exposes the conventional 12-character
    # display ID, while `docker compose ps -q` may return the complete ID.
    if ($Normalized.Length -gt 12) {
        return $Normalized.Substring(0, 12)
    }
    return $Normalized
}

function Get-CurrentComposeContainerIds {
    $Output = & docker compose ps --all -q 2>$null
    if ($LASTEXITCODE -ne 0) {
        return @()
    }

    return @(
        $Output |
            ForEach-Object { ConvertTo-DockerDisplayContainerId -ContainerId ([string]$_) } |
            Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
    )
}
