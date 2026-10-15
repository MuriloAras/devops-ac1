[CmdletBinding()]
param(
    [string]$JenkinsWar = (Join-Path $env:USERPROFILE 'Jenkins\jenkins.war'),
    [string]$JenkinsHome = (Join-Path $env:USERPROFILE '.jenkins'),
    [string]$JavaHome,
    [int]$Port = 9090
)
$ErrorActionPreference = 'Stop'
try {
    $existing = Invoke-WebRequest "http://localhost:$Port/api/json" -TimeoutSec 3
    if ($existing.Headers['X-Jenkins']) {
        Write-Output "Jenkins ja esta ativo em http://localhost:$Port/."
        return
    }
} catch { }
if (-not (Test-Path -LiteralPath $JenkinsWar -PathType Leaf)) {
    throw 'jenkins.war nao encontrado. Informe -JenkinsWar com o arquivo da instalacao Jenkins.'
}
if (-not $JavaHome) {
    $javaPath = (Get-Command java.exe -ErrorAction Stop).Source
    $JavaHome = Split-Path (Split-Path $javaPath -Parent) -Parent
}
$javaExecutable = Join-Path $JavaHome 'bin\java.exe'
$javaVersion = (& $javaExecutable -version 2>&1 | Out-String)
if ($javaVersion -notmatch 'version "21\.') {
    throw 'Este laboratorio usa JDK 21. Informe -JavaHome apontando ao JDK 21.'
}
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$logRoot = Join-Path $projectRoot '.local'
New-Item -ItemType Directory -Force $logRoot | Out-Null
$env:JAVA_HOME = $JavaHome
$env:JENKINS_HOME = $JenkinsHome
$env:PATH = "$JavaHome\bin;$env:PATH"
$jenkinsProcess = Start-Process -FilePath $javaExecutable -ArgumentList @(
    '-Dhudson.plugins.git.GitSCM.ALLOW_LOCAL_CHECKOUT=true',
    '-jar', "`"$JenkinsWar`"", "--httpPort=$Port", '--httpListenAddress=127.0.0.1'
) -WindowStyle Hidden -PassThru `
    -RedirectStandardOutput (Join-Path $logRoot 'jenkins.stdout.log') `
    -RedirectStandardError (Join-Path $logRoot 'jenkins.stderr.log')
$jenkinsProcess.Id | Set-Content (Join-Path $logRoot 'jenkins.pid')
Write-Output "Jenkins iniciado com JDK 21; PID $($jenkinsProcess.Id). Aguarde a inicializacao em http://localhost:$Port/."
