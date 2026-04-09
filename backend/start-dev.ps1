param(
    [int]$Port = 8080,
    [string]$Profile = "default"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Get-JavaMajorVersion {
    param(
        [Parameter(Mandatory = $true)]
        [string]$JavaHome
    )

    $javaExe = Join-Path $JavaHome "bin\\java.exe"
    if (-not (Test-Path $javaExe)) {
        return $null
    }

    $versionOutput = & cmd.exe /d /c "`"$javaExe`" -version 2>&1" | Select-Object -First 1
    if ($versionOutput -match '"(?<version>\d+)(\.[^"]+)?"') {
        return [int]$Matches.version
    }

    return $null
}

$candidateHomes = @(
    $env:QSD_JAVA21_HOME,
    $env:JAVA21_HOME,
    $env:JAVA_HOME,
    "C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot",
    "C:\Program Files\Java\jdk-21"
) | Where-Object { $_ -and (Test-Path $_) } | Select-Object -Unique

$selectedJavaHome = $null
foreach ($candidate in $candidateHomes) {
    $majorVersion = Get-JavaMajorVersion -JavaHome $candidate
    if ($majorVersion -eq 21) {
        $selectedJavaHome = $candidate
        break
    }
}

if (-not $selectedJavaHome) {
    Write-Error @"
未找到可用的 JDK 21。
请先设置以下任一环境变量后重试：
- QSD_JAVA21_HOME
- JAVA21_HOME

当前默认尝试过的目录：
- C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot
- C:\Program Files\Java\jdk-21
"@
}

$env:JAVA_HOME = $selectedJavaHome
$env:Path = "$selectedJavaHome\\bin;$env:Path"

Write-Host "Using JAVA_HOME=$selectedJavaHome"
Write-Host "Starting backend on port $Port with profile '$Profile'"

Push-Location $PSScriptRoot
try {
    & mvn.cmd spring-boot:run "-Dspring-boot.run.profiles=$Profile" "-Dspring-boot.run.jvmArguments=-Dserver.port=$Port"
}
finally {
    Pop-Location
}
