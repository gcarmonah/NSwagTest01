param (
    [string] $namespace,
    [string] $release,
    [string] $valuesPath,
    [string] $buildNumber
)

$ErrorActionPreference = "Stop"

function CheckVariableSubstitution([string]$filesFolder, [string]$filesPattern = "*.config", [string]$excludeFolder = "", [bool]$recurse = $true)
{
    Write-Host "--- Check variable substitution ---"
    if ([string]::IsNullOrEmpty($filesFolder) -or -not (Test-Path "$filesFolder"))
    {
        Write-Host "Path '$filesFolder' does not exist or empty"
        return
    }

    $filesPath = Join-Path -Path $filesFolder -ChildPath $filesPattern
    Write-Host "  Search path: $filesPath"
    Write-Host "  Exclude folder: $excludeFolder"

    if ($recurse)
    {
        $linesWithOctoVars = Get-ChildItem -Path $filesPath -File -Recurse |
            Where {$_.Name -ne "$defaultExcludeConfigFile"} |
            Where {$_.FullName -notlike $excludeFolder}  |
            Select-String -pattern "#{.*}" -AllMatches
    }
    else
    {
        $linesWithOctoVars = Get-ChildItem -Path $filesPath -File |
            Where {$_.Name -ne "$defaultExcludeConfigFile"} |
            Where {$_.FullName -notlike $excludeFolder}  |
            Select-String -pattern "#{.*}" -AllMatches
    }

    if ($linesWithOctoVars)
    {
        Write-Warning "### Error lines ###`n$linesWithOctoVars`n"
        Write-Error "Check variable substitution detected files with not substituted variables ('#{}'). See ### Error lines ###" -Category InvalidResult
        Write-Host "---"
        exit 1
    }
    else
    {
        Write-Host "Check variable substitution: Success";
    }

    Write-Host "---"
}

function LintChart
{
    param(
        [string] $chartPath
    )

    helm lint $chartPath
    if (!$?) { throw; }
}

function DeployHelmChart
{
    param(
        [string] $namespace,
        [string] $release,
        [string] $baseChartPath,
        [string] $chartPath,
        [string] $valuesPath,
        [string] $buildNumber
    )

    if ($valuesPath)
    {
        $valuesPath = "$baseChartPath/$valuesPath"
    }
    else
    {
        $valuesPath = "$chartPath/values.yaml"
    }

    if ($buildNumber)
    {
        helm package $chartPath --version "$buildNumber" --app-version "$buildNumber" --destination "$chartPath"
        $chartName = Split-Path $chartPath -Leaf
        $chartPath = "$chartPath/$chartName-$buildNumber.tgz"
    }

    $upgradeArguments = @();

    $upgradeArguments += "upgrade $release $chartPath";
    $upgradeArguments += "--atomic";
    $upgradeArguments += "--install";
    $upgradeArguments += "--namespace $namespace";
    $upgradeArguments += "--reset-values";
    $upgradeArguments += "--values ""$valuesPath""";
    $upgradeArguments += "--debug";

    $stdErrLog = "stderr.log"
    $stdOutLog = "stdout.log"
    $upgradeProcess = Start-Process -FilePath "helm" -ArgumentList "$upgradeArguments" -RedirectStandardOutput $stdOutLog -RedirectStandardError $stdErrLog -NoNewWindow -PassThru -Wait
    $logs = Get-Content $stdErrLog, $stdOutLog -Raw

    Write-Host $logs

    if ($upgradeProcess.ExitCode -ne 0)
    {
        Write-Error "Helm chart upgrade has failed"
    }
}


$projectName = "amazingapi"
$baseChartPath = "$PSScriptRoot/charts"
$mainChartPath = "$baseChartPath/$projectName"

LintChart $mainChartPath

if ($buildNumber)
{
    CheckVariableSubstitution $PSScriptRoot "*.yaml"
}

$doesNamespaceExist = (kubectl get ns $namespace -o json --ignore-not-found | ConvertFrom-Json).status.phase -eq "Active"

if (!$doesNamespaceExist)
{
    Write-Host "Creating namespace: $namespace"
    kubectl create namespace $namespace
}

kubectl config set-context --current --namespace=$namespace

DeployHelmChart $namespace $release $baseChartPath $mainChartPath $valuesPath $buildNumber
if (!$?) { throw; }