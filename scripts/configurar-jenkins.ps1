[CmdletBinding()]
param(
    [string]$JenkinsUrl = 'http://localhost:9090',
    [string]$JobName = 'A2_P2_supercursos_pipeline',
    [string]$RepositoryUrl,
    [string]$Branch = 'codex/praticas-2-3',
    [switch]$AtualizarJob
)
$ErrorActionPreference = 'Stop'
if (-not $RepositoryUrl) {
    $repositoryPath = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
    $RepositoryUrl = ([Uri]($repositoryPath + [IO.Path]::DirectorySeparatorChar)).AbsoluteUri.TrimEnd('/')
}
$JenkinsUrl = $JenkinsUrl.TrimEnd('/')
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$headers = @{}
if ($env:JENKINS_USER -and $env:JENKINS_TOKEN) {
    $auth = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("$($env:JENKINS_USER):$($env:JENKINS_TOKEN)"))
    $headers.Authorization = "Basic $auth"
}
$crumb = Invoke-RestMethod "$JenkinsUrl/crumbIssuer/api/json" -WebSession $session -Headers $headers
$headers[$crumb.crumbRequestField] = $crumb.crumb
$escapedRepo = [Security.SecurityElement]::Escape($RepositoryUrl)
$escapedBranch = [Security.SecurityElement]::Escape($Branch)
$jobXml = @"
<?xml version="1.0" encoding="UTF-8"?>
<flow-definition plugin="workflow-job">
  <description>Super Cursos - praticas 2 e 3: CI/CD, PostgreSQL, Cypress e observabilidade. Origem: https://github.com/MuriloAras/devops-ac1</description>
  <keepDependencies>false</keepDependencies>
  <properties/>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps">
    <scm class="hudson.plugins.git.GitSCM" plugin="git">
      <configVersion>2</configVersion>
      <userRemoteConfigs><hudson.plugins.git.UserRemoteConfig><url>$escapedRepo</url></hudson.plugins.git.UserRemoteConfig></userRemoteConfigs>
      <branches><hudson.plugins.git.BranchSpec><name>$escapedBranch</name></hudson.plugins.git.BranchSpec></branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="empty-list"/>
      <extensions/>
    </scm>
    <scriptPath>Jenkinsfile</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers/>
  <disabled>false</disabled>
</flow-definition>
"@
$encodedJob = [Uri]::EscapeDataString($JobName)
if ($AtualizarJob) {
    $endpoint = "$JenkinsUrl/job/$encodedJob/config.xml"
} else {
    $endpoint = "$JenkinsUrl/createItem?name=$encodedJob"
}
Invoke-WebRequest $endpoint -Method Post -ContentType 'application/xml; charset=utf-8' `
    -Body ([Text.Encoding]::UTF8.GetBytes($jobXml)) -Headers $headers -WebSession $session | Out-Null
Write-Output "Job configurado: $JenkinsUrl/job/$encodedJob/"
Write-Output "SCM: $RepositoryUrl; branch: $Branch; script: Jenkinsfile"
