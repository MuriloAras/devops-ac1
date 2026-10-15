[CmdletBinding()]
param(
    [string]$JenkinsUrl = 'http://localhost:9090',
    [string]$JobName = 'A2_P2_supercursos_pipeline',
    [ValidateRange(0, [int]::MaxValue)]
    [int]$BuildNumber = 0,
    [string]$Workspace = 'C:\Users\pedro\.jenkins\workspace\A2_P2_supercursos_pipeline',
    [ValidateSet('Auto', 'Workspace', 'Artifacts')]
    [string]$Fonte = 'Auto',
    [string]$OutputRoot,
    [ValidateRange(5, 120)]
    [int]$TimeoutSeconds = 30
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version 2.0
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
if (-not $OutputRoot) { $OutputRoot = Join-Path $projectRoot 'evidencias\a2' }
$OutputRoot = [IO.Path]::GetFullPath($OutputRoot)
$JenkinsUrl = $JenkinsUrl.TrimEnd('/')
$utf8 = New-Object Text.UTF8Encoding($false)
$settings = @{}
$secretValues = New-Object 'System.Collections.Generic.List[string]'
$copiedFiles = New-Object 'System.Collections.Generic.List[object]'

function Read-PropertiesText([string]$Text) {
    $values = @{}
    foreach ($line in ($Text -split '\r?\n')) {
        if ($line -match '^\s*([^#\s=]+)\s*=(.*)$') {
            $value = $Matches[2].Trim()
            if ($value.Length -ge 2 -and (($value.StartsWith('"') -and $value.EndsWith('"')) -or
                    ($value.StartsWith("'") -and $value.EndsWith("'")))) {
                $value = $value.Substring(1, $value.Length - 2)
            }
            $values[$Matches[1]] = $value
        }
    }
    return $values
}

foreach ($configName in @('.env.example', '.env')) {
    $configPath = Join-Path $projectRoot $configName
    if (Test-Path -LiteralPath $configPath -PathType Leaf) {
        $fileSettings = Read-PropertiesText ([IO.File]::ReadAllText($configPath))
        foreach ($key in $fileSettings.Keys) {
            $settings[$key] = $fileSettings[$key]
            if ($key -match '(?i)PASSWORD|TOKEN|SECRET' -and $fileSettings[$key]) {
                $secretValues.Add([string]$fileSettings[$key])
            }
        }
    }
}
foreach ($item in (Get-ChildItem Env:)) {
    if ($settings.ContainsKey($item.Name)) { $settings[$item.Name] = $item.Value }
    if ($item.Name -match '(?i)PASSWORD|TOKEN|SECRET' -and $item.Value) {
        $secretValues.Add([string]$item.Value)
    }
}

function Get-Setting([string]$Name, [string]$Default) {
    if ($settings.ContainsKey($Name) -and $settings[$Name]) { return [string]$settings[$Name] }
    return $Default
}

function Get-ServiceUrl([string]$PortSetting, [int]$DefaultPort) {
    $port = 0
    if (-not [int]::TryParse((Get-Setting $PortSetting ([string]$DefaultPort)), [ref]$port) -or
            $port -lt 1 -or $port -gt 65535) {
        throw "Porta invalida na configuracao $PortSetting."
    }
    return "http://localhost:$port"
}

function Protect-Text([string]$Text) {
    foreach ($secret in ($secretValues | Sort-Object Length -Descending -Unique)) {
        if ($secret.Length -ge 4) { $Text = $Text.Replace($secret, '[REDACTED]') }
    }
    $Text = [regex]::Replace($Text, '(?im)(Using generated security password:\s*)[^\r\n]+', '$1[REDACTED]')
    $Text = [regex]::Replace($Text, '(?is)(<(?:password|secret|apiToken)(?:\s[^>]*)?>).*?(</(?:password|secret|apiToken)>)', '$1[REDACTED]$2')
    $Text = [regex]::Replace($Text, '(?im)^([ \t]*[A-Z0-9_]*(?:PASSWORD|TOKEN|SECRET)[A-Z0-9_]*[ \t]*=[ \t]*).+$', '$1[REDACTED]')
    $Text = [regex]::Replace($Text, '(?i)(https?://)[^/\s:@]+:[^@\s/]+@', '$1[REDACTED]@')
    return $Text
}

function Save-Text([string]$Path, [string]$Text) {
    [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($Path)) | Out-Null
    [IO.File]::WriteAllText($Path, (Protect-Text $Text), $utf8)
}

function Save-Json([string]$Path, $Value) {
    Save-Text $Path ($Value | ConvertTo-Json -Depth 60)
}

function Get-Response([string]$Url, [hashtable]$Headers = @{}) {
    try {
        return Invoke-WebRequest -Uri $Url -Headers $Headers -UseBasicParsing -TimeoutSec $TimeoutSeconds
    } catch {
        $status = 'indisponivel'
        $responseProperty = $_.Exception.PSObject.Properties['Response']
        if ($responseProperty -and $responseProperty.Value) {
            $status = [string][int]$responseProperty.Value.StatusCode
        }
        throw "Falha HTTP ($status) consultando $Url. Verifique o servico e a autenticacao."
    }
}

function Response-Text($Response) {
    if ($Response.Content -is [byte[]]) { return [Text.Encoding]::UTF8.GetString($Response.Content) }
    return [string]$Response.Content
}

function Get-Json([string]$Url, [hashtable]$Headers = @{}) {
    return (Response-Text (Get-Response $Url $Headers)) | ConvertFrom-Json
}

function Encode-Path([string]$Path) {
    return (($Path.Replace('\', '/') -split '/' | ForEach-Object { [Uri]::EscapeDataString($_) }) -join '/')
}

$jenkinsHeaders = @{}
if ($env:JENKINS_USER -and $env:JENKINS_TOKEN) {
    $auth = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("$($env:JENKINS_USER):$($env:JENKINS_TOKEN)"))
    $jenkinsHeaders.Authorization = "Basic $auth"
    $secretValues.Add($auth)
} elseif ($env:JENKINS_USER -or $env:JENKINS_TOKEN) {
    throw 'Defina JENKINS_USER e JENKINS_TOKEN juntos ou remova ambos para acesso anonimo local.'
}
$jobPath = (($JobName -split '/' | ForEach-Object { 'job/' + [Uri]::EscapeDataString($_) }) -join '/')
$jobUrl = "$JenkinsUrl/$jobPath"
$latestBuild = Get-Json "$jobUrl/lastBuild/api/json" $jenkinsHeaders
if ($BuildNumber -eq 0) { $BuildNumber = [int]$latestBuild.number }
if ($BuildNumber -ne [int]$latestBuild.number) {
    throw 'O build solicitado nao e o ultimo do job. Colete o ultimo para associar os servicos ao deploy correto.'
}
$buildUrl = "$jobUrl/$BuildNumber"
$build = $latestBuild
if ($build.building -or $build.result -ne 'SUCCESS') {
    throw "Coleta interrompida: build $BuildNumber ainda esta em execucao ou nao terminou com SUCCESS."
}

$executionRelativePath = 'evidencias/a2/p2/execucao.properties'
$executionText = Response-Text (Get-Response "$buildUrl/artifact/$executionRelativePath" $jenkinsHeaders)
$execution = Read-PropertiesText $executionText
if ($execution['build'] -ne [string]$BuildNumber -or $execution['job'] -ne $JobName -or
        $execution['commit'] -notmatch '^[0-9a-fA-F]{40}$') {
    throw 'A identificacao de checkout arquivada nao corresponde ao job/build ou nao contem commit valido.'
}
$commit = $execution['commit']
$workspaceMatches = $false
if ($Fonte -ne 'Artifacts' -and (Test-Path -LiteralPath $Workspace -PathType Container)) {
    $workspaceExecution = Join-Path $Workspace $executionRelativePath
    if (Test-Path -LiteralPath $workspaceExecution -PathType Leaf) {
        $workspaceProperties = Read-PropertiesText ([IO.File]::ReadAllText($workspaceExecution))
        if ($workspaceProperties['build'] -eq [string]$BuildNumber -and $workspaceProperties['commit'] -eq $commit) {
            if (Get-Command git -ErrorAction SilentlyContinue) {
                try {
                    $workspaceHead = & git -C $Workspace rev-parse HEAD 2>$null
                    $workspaceMatches = $LASTEXITCODE -eq 0 -and ([string]$workspaceHead).Trim() -eq $commit
                } catch { $workspaceMatches = $false }
            }
        }
    }
}
if ($Fonte -eq 'Workspace' -and -not $workspaceMatches) {
    throw 'Workspace ausente ou diferente do checkout do build. Use -Fonte Artifacts para a copia arquivada.'
}
$useWorkspace = $Fonte -ne 'Artifacts' -and $workspaceMatches
$sourceName = if ($useWorkspace) { 'workspace verificado' } else { 'artifacts do build' }
$artifactList = @($build.artifacts)

$captureId = "build-$BuildNumber-" + [DateTime]::UtcNow.ToString('yyyyMMddTHHmmssfffZ')
$p2 = Join-Path $OutputRoot "p2\$captureId"
$p3 = Join-Path $OutputRoot "p3\$captureId"
$cypress = Join-Path $OutputRoot "p3\cypress\$captureId"
$grafana = Join-Path $OutputRoot "grafana\$captureId"
foreach ($directory in @($p2, $p3, $cypress, $grafana)) {
    [IO.Directory]::CreateDirectory($directory) | Out-Null
}

function Copy-EvidenceFiles([string]$Prefix, [string]$Destination, [string]$Pattern) {
    $prefixNormalized = $Prefix.Replace('\', '/').TrimEnd('/') + '/'
    $candidates = @()
    if ($useWorkspace) {
        $sourceDirectory = Join-Path $Workspace $Prefix
        if (Test-Path -LiteralPath $sourceDirectory -PathType Container) {
            $sourceAbsolute = [IO.Path]::GetFullPath($sourceDirectory).TrimEnd('\', '/') + [IO.Path]::DirectorySeparatorChar
            $candidates = @(Get-ChildItem -LiteralPath $sourceDirectory -File -Recurse | ForEach-Object {
                if ($_.FullName.StartsWith($sourceAbsolute, [StringComparison]::OrdinalIgnoreCase) -and
                        -not ($_.Attributes -band [IO.FileAttributes]::ReparsePoint)) {
                    $relative = $_.FullName.Substring($sourceAbsolute.Length).Replace('\', '/')
                    if ($relative -match $Pattern) {
                        [PSCustomObject]@{ Relative = $relative; LocalPath = $_.FullName; ArtifactPath = $null }
                    }
                }
            })
        }
    } else {
        $candidates = @($artifactList | ForEach-Object {
            $relativePath = [string]$_.relativePath
            if ($relativePath.StartsWith($prefixNormalized, [StringComparison]::Ordinal)) {
                $relative = $relativePath.Substring($prefixNormalized.Length)
                if ($relative -match $Pattern -and $relative -notmatch '(^|/)\.\.(/|$)') {
                    [PSCustomObject]@{ Relative = $relative; LocalPath = $null; ArtifactPath = $relativePath }
                }
            }
        })
    }
    foreach ($candidate in $candidates) {
        $destinationPath = Join-Path $Destination $candidate.Relative
        [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($destinationPath)) | Out-Null
        if ($candidate.LocalPath) {
            $bytes = [IO.File]::ReadAllBytes($candidate.LocalPath)
        } else {
            $response = Get-Response "$buildUrl/artifact/$(Encode-Path $candidate.ArtifactPath)" $jenkinsHeaders
            $memory = New-Object IO.MemoryStream
            try {
                if ($response.RawContentStream.CanSeek) { $response.RawContentStream.Position = 0 }
                $response.RawContentStream.CopyTo($memory)
                $bytes = $memory.ToArray()
            } finally { $memory.Dispose() }
        }
        if ([IO.Path]::GetExtension($destinationPath) -match '^\.(png|gif|mp4|ico|woff|woff2|ttf)$') {
            [IO.File]::WriteAllBytes($destinationPath, $bytes)
        } else {
            Save-Text $destinationPath ([Text.Encoding]::UTF8.GetString($bytes))
        }
        $copiedFiles.Add([PSCustomObject]@{
            source = $prefixNormalized + $candidate.Relative
            destination = $destinationPath.Substring($OutputRoot.TrimEnd('\', '/').Length + 1).Replace('\', '/')
            sha256 = (Get-FileHash -LiteralPath $destinationPath -Algorithm SHA256).Hash.ToLowerInvariant()
            bytes = (Get-Item -LiteralPath $destinationPath).Length
        })
    }
    return $candidates.Count
}

function Assert-JUnit([string]$Directory, [int]$MinimumTests = 1) {
    $cases = 0; $failures = 0; $errors = 0; $skipped = 0
    $reports = @(Get-ChildItem -LiteralPath $Directory -Filter '*.xml' -File)
    foreach ($report in $reports) {
        $document = New-Object Xml.XmlDocument
        $document.XmlResolver = $null
        $document.Load($report.FullName)
        $cases += $document.SelectNodes("//*[local-name()='testcase']").Count
        $failures += $document.SelectNodes("//*[local-name()='testcase']/*[local-name()='failure']").Count
        $errors += $document.SelectNodes("//*[local-name()='testcase']/*[local-name()='error']").Count
        $skipped += $document.SelectNodes("//*[local-name()='testcase']/*[local-name()='skipped']").Count
        foreach ($suite in $document.SelectNodes("//*[local-name()='testsuite']")) {
            if (($suite.GetAttribute('failures') -and [int]$suite.GetAttribute('failures') -gt 0) -or
                    ($suite.GetAttribute('errors') -and [int]$suite.GetAttribute('errors') -gt 0)) {
                throw "Relatorio JUnit registra falhas/erros em $Directory."
            }
        }
    }
    if (($cases - $skipped) -lt $MinimumTests -or $failures -gt 0 -or $errors -gt 0) {
        throw "Relatorios JUnit insuficientes ou com falhas em $Directory."
    }
    return [PSCustomObject]@{ reports = $reports.Count; tests = $cases; failures = $failures; errors = $errors; skipped = $skipped }
}

function Assert-UpQuery($Query, [string]$Origin) {
    $results = @($Query.data.result)
    if ($Query.status -ne 'success' -or $results.Count -lt 1) {
        throw "Consulta up sem resultado valido em $Origin."
    }
    foreach ($result in $results) {
        if ($result.metric.job -ne 'supercursos-api' -or [double]$result.value[1] -ne 1) {
            throw "A API nao esta UP (up=1) na consulta de $Origin."
        }
    }
}

try {
    Save-Text (Join-Path $p2 'jenkins\job-config.xml') (Response-Text (Get-Response "$jobUrl/config.xml" $jenkinsHeaders))
    $stages = Get-Json "$buildUrl/wfapi/describe" $jenkinsHeaders
    if (@($stages.stages).Count -lt 1) { throw 'Jenkins nao retornou as etapas do pipeline em wfapi.' }
    Save-Json (Join-Path $p2 'jenkins\stages.json') $stages
    Save-Text (Join-Path $p2 'jenkins\console.txt') (Response-Text (Get-Response "$buildUrl/consoleText" $jenkinsHeaders))
    Save-Text (Join-Path $p2 'jenkins\execucao.properties') $executionText
    Save-Json (Join-Path $p2 'jenkins\build.json') ([ordered]@{
        job = $JobName; number = $BuildNumber; result = $build.result; building = $build.building
        url = "$buildUrl/"; commit = $commit; timestamp = $build.timestamp; duration = $build.duration
        buildDateUtc = [DateTimeOffset]::FromUnixTimeMilliseconds([long]$build.timestamp).ToString('o')
        artifacts = @($artifactList | Select-Object relativePath, fileName)
    })

    if ((Copy-EvidenceFiles 'target/surefire-reports' (Join-Path $p2 'junit\unitarios') '^TEST-[^/]+\.xml$') -lt 1) { throw 'JUnit unitario ausente.' }
    if ((Copy-EvidenceFiles 'target/failsafe-reports' (Join-Path $p2 'junit\integracao') '^TEST-[^/]+\.xml$') -lt 1) { throw 'JUnit de integracao ausente.' }
    if ((Copy-EvidenceFiles 'target/site/jacoco' (Join-Path $p2 'jacoco') '(?i)\.(html|xml|csv|css|js|gif|png|svg)$') -lt 1) { throw 'JaCoCo ausente.' }
    if ((Copy-EvidenceFiles 'target/site' (Join-Path $p2 'pmd') '^pmd\.html$') -ne 1) { throw 'HTML do PMD ausente.' }
    if ((Copy-EvidenceFiles 'target' (Join-Path $p2 'pmd') '^pmd\.xml$') -ne 1) { throw 'XML do PMD ausente.' }
    Copy-EvidenceFiles 'target/site' (Join-Path $p2 'pmd') '(?i)^(css|js|images|img|fonts)/.*\.(css|js|png|gif|svg|ico|woff2?|ttf)$' | Out-Null
    foreach ($required in @('jacoco\index.html', 'jacoco\jacoco.xml')) {
        if (-not (Test-Path -LiteralPath (Join-Path $p2 $required) -PathType Leaf)) { throw "Relatorio obrigatorio ausente: $required." }
    }
    $unitTests = Assert-JUnit (Join-Path $p2 'junit\unitarios')
    $integrationTests = Assert-JUnit (Join-Path $p2 'junit\integracao')

    if ((Copy-EvidenceFiles 'frontend/cypress/results' (Join-Path $cypress 'results') '(?i)\.xml$') -lt 1) { throw 'JUnit Cypress ausente.' }
    $screenshotCount = Copy-EvidenceFiles 'frontend/cypress/screenshots' (Join-Path $cypress 'screenshots') '(?i)\.png$'
    $videoCount = Copy-EvidenceFiles 'frontend/cypress/videos' (Join-Path $cypress 'videos') '(?i)\.mp4$'
    if ($screenshotCount -lt 6 -or $videoCount -lt 1) { throw 'Cypress precisa fornecer seis screenshots e pelo menos um video desta execucao.' }
    $cypressTests = Assert-JUnit (Join-Path $cypress 'results') 6

    $apiUrl = Get-ServiceUrl 'API_PORT' 8082
    $frontendUrl = Get-ServiceUrl 'FRONTEND_PORT' 3002
    $prometheusUrl = Get-ServiceUrl 'PROMETHEUS_PORT' 9092
    $grafanaUrl = Get-ServiceUrl 'GRAFANA_PORT' 3003
    $health = Get-Json "$apiUrl/actuator/health"
    Save-Json (Join-Path $p3 'health.json') $health
    if ($health.status -ne 'UP') { throw 'A API nao respondeu status UP.' }
    $frontHealth = Response-Text (Get-Response "$frontendUrl/health")
    Save-Text (Join-Path $p3 'frontend-health.txt') $frontHealth
    if ($frontHealth.Trim() -ne 'OK') { throw 'O healthcheck do frontend nao respondeu OK.' }
    $front = Get-Response "$frontendUrl/"
    Save-Text (Join-Path $p3 'frontend.html') (Response-Text $front)
    Save-Json (Join-Path $p3 'frontend-status.json') ([ordered]@{ url = "$frontendUrl/"; statusCode = [int]$front.StatusCode })
    if ([int]$front.StatusCode -ne 200) { throw 'O frontend nao respondeu HTTP 200.' }

    $targets = Get-Json "$prometheusUrl/api/v1/targets"
    Save-Json (Join-Path $p3 'prometheus-targets.json') $targets
    $apiTargets = @($targets.data.activeTargets | Where-Object { $_.labels.job -eq 'supercursos-api' })
    if ($targets.status -ne 'success' -or $apiTargets.Count -lt 1 -or @($apiTargets | Where-Object { $_.health -ne 'up' }).Count -gt 0) {
        throw 'O target supercursos-api ainda nao esta UP no Prometheus. Aguarde um scrape e repita a coleta.'
    }
    $query = [Uri]::EscapeDataString('up{job="supercursos-api"}')
    $up = Get-Json "$prometheusUrl/api/v1/query?query=$query"
    Save-Json (Join-Path $p3 'prometheus-up.json') $up
    Assert-UpQuery $up 'Prometheus'

    $grafanaUser = Get-Setting 'GRAFANA_ADMIN_USER' 'admin'
    $grafanaPassword = Get-Setting 'GRAFANA_ADMIN_PASSWORD' 'admin_local'
    $secretValues.Add($grafanaPassword)
    $grafanaAuth = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("${grafanaUser}:$grafanaPassword"))
    $secretValues.Add($grafanaAuth)
    $grafanaHeaders = @{ Authorization = "Basic $grafanaAuth" }
    $grafanaHealth = Get-Json "$grafanaUrl/api/health"
    Save-Json (Join-Path $grafana 'health.json') $grafanaHealth
    if ($grafanaHealth.database -ne 'ok') { throw 'Grafana nao informou banco interno saudavel.' }
    $datasource = Get-Json "$grafanaUrl/api/datasources/uid/prometheus-supercursos" $grafanaHeaders
    Save-Json (Join-Path $grafana 'datasource.json') $datasource
    if ($datasource.uid -ne 'prometheus-supercursos' -or $datasource.type -ne 'prometheus') { throw 'Datasource Grafana inesperado.' }
    $datasourceHealth = Get-Json "$grafanaUrl/api/datasources/uid/prometheus-supercursos/health" $grafanaHeaders
    Save-Json (Join-Path $grafana 'datasource-health.json') $datasourceHealth
    if ($datasourceHealth.status -ne 'OK') { throw 'O datasource Prometheus nao esta saudavel no Grafana.' }
    $dashboard = Get-Json "$grafanaUrl/api/dashboards/uid/supercursos-homol" $grafanaHeaders
    Save-Json (Join-Path $grafana 'dashboard.json') $dashboard
    if ($dashboard.dashboard.uid -ne 'supercursos-homol' -or @($dashboard.dashboard.panels).Count -lt 4) { throw 'Dashboard Grafana ou seus quatro paineis ausentes.' }
    $grafanaUp = Get-Json "$grafanaUrl/api/datasources/proxy/uid/prometheus-supercursos/api/v1/query?query=$query" $grafanaHeaders
    Save-Json (Join-Path $grafana 'query-up.json') $grafanaUp
    Assert-UpQuery $grafanaUp 'Grafana/datasource Prometheus'

    $composeRoot = if ($useWorkspace) { $Workspace } else { $projectRoot }
    $composeFile = Join-Path $composeRoot 'docker-compose.homol.yml'
    if (-not (Test-Path -LiteralPath $composeFile -PathType Leaf)) { throw 'Compose HOMOL nao encontrado para consultar os containers.' }
    $serviceNames = @('postgres', 'pgadmin', 'api', 'frontend', 'prometheus', 'grafana')
    $composeArguments = @('compose', '--project-name', 'supercursos-homol', '--project-directory', $composeRoot, '-f', $composeFile, 'ps', '--all')
    $containerText = & docker @composeArguments @serviceNames 2>&1
    if ($LASTEXITCODE -ne 0) { throw 'docker compose ps falhou. Nenhum ambiente foi alterado.' }
    Save-Text (Join-Path $p3 'containers.txt') ($containerText -join [Environment]::NewLine)
    $containerJson = & docker @composeArguments '--format' 'json' @serviceNames 2>&1
    if ($LASTEXITCODE -ne 0) { throw 'docker compose ps --format json falhou.' }
    $containerJsonText = $containerJson -join [Environment]::NewLine
    $containers = @()
    if ($containerJsonText.TrimStart().StartsWith('[')) { $containers = @($containerJsonText | ConvertFrom-Json) }
    else { $containers = @($containerJson | Where-Object { ([string]$_).Trim() } | ForEach-Object { ([string]$_) | ConvertFrom-Json }) }
    Save-Json (Join-Path $p3 'containers.json') @($containers)
    foreach ($serviceName in $serviceNames) {
        $serviceContainers = @($containers | Where-Object { $_.Service -eq $serviceName })
        if ($serviceContainers.Count -lt 1 -or @($serviceContainers | Where-Object { $_.State -ne 'running' }).Count -gt 0) {
            throw "O servico $serviceName nao esta running no projeto supercursos-homol."
        }
    }

    $finalBuild = Get-Json "$jobUrl/lastBuild/api/json" $jenkinsHeaders
    if ([int]$finalBuild.number -ne $BuildNumber -or $finalBuild.building -or $finalBuild.result -ne 'SUCCESS') {
        throw 'O ultimo build mudou durante a coleta. Repita depois que ele terminar com SUCCESS.'
    }
    $manifest = [ordered]@{
        status = 'VERIFICADO'; collectedAtUtc = [DateTimeOffset]::UtcNow.ToString('o')
        captureId = $captureId; job = $JobName; build = $BuildNumber; commit = $commit
        buildUrl = "$buildUrl/"; buildTimestamp = $build.timestamp; source = $sourceName
        redaction = 'Valores de senha/token conhecidos e senhas geradas foram removidos dos arquivos de texto.'
        junit = [ordered]@{ unitarios = $unitTests; integracao = $integrationTests; cypress = $cypressTests }
        cypressScreenshots = $screenshotCount; cypressVideos = $videoCount
        checks = [ordered]@{ jenkinsSuccess = $true; apiUp = $true; frontendOk = $true; prometheusUp = $true; grafanaUp = $true; composeRunning = $true }
        directories = [ordered]@{ p2 = $p2; p3 = $p3; cypress = $cypress; grafana = $grafana }
        files = @($copiedFiles.ToArray())
    }
    Save-Json (Join-Path $p2 'manifesto-coleta.json') $manifest
    Write-Output "Coleta verificada: build $BuildNumber; commit $commit."
    Write-Output "Manifesto: $(Join-Path $p2 'manifesto-coleta.json')"
    Write-Output "Cypress: $cypress"
    Write-Output "Grafana: $grafana"
} catch {
    $safeFailure = Protect-Text $_.Exception.Message
    Save-Json (Join-Path $p2 'coleta-incompleta.json') ([ordered]@{
        status = 'INCOMPLETA'; collectedAtUtc = [DateTimeOffset]::UtcNow.ToString('o')
        job = $JobName; build = $BuildNumber; commit = $commit; reason = $safeFailure
    })
    throw "Coleta incompleta: $safeFailure Arquivos parciais: $p2"
}
