param(
    [string] $dockerRepository,
    [string] $dockerFeed,
    [string] $buildConfiguration,
    [string] $teamcityVersion,
    [string] $buildNumber,
    [string] $octopusPath,
    [string] $octopusUrl,
    [string] $octopusApiKey
)

function BuildAndPublishDockerImages {
    param (
        [string] $solutionDirectory,
        [string] $buildNumber,
        [string] $repository,
        [string] $feed,
        [string] $buildConfiguration,
        [string] $teamcityVersion
    )

    $dockerComposeFilePath = "$solutionDirectory/.docker/docker-compose-deploy.yml"

    $env:BUILD_NUMBER = "$buildNumber"
    $env:REPOSITORY = "$repository"
    $env:FEED = "$feed"
    $env:CONFIGURATION = "$buildConfiguration"
    $env:TEAMCITY_VERSION = "$teamcityVersion"
    docker-compose -f "$dockerComposeFilePath" build --parallel
    if (!$?) { throw; }
    docker-compose -f "$dockerComposeFilePath" push
    if (!$?) { throw; }
}

function CopyAppSettingsToHelmChart {
    param([string] $solutionDirectory, [string] $projectName, [string] $dockerLabel)

    $sourceDirectory = Join-Path $solutionDirectory "src"

    $projectPath = Join-Path $sourceDirectory $projectName

    $filesToCopy = @("appsettings.helm.json", "nlog.helm.json")

    $destinationDirectory = Join-Path $solutionDirectory ".ci-cd\deploy\charts\$dockerLabel\includes\configmap"
    $destinationDirectory = Join-Path $destinationDirectory $projectLabel

    foreach ($file in $filesToCopy) {
        $sourceFile = Join-Path $projectPath $file
        $destinationFile = Join-Path $destinationDirectory $file.Replace(".helm", "")
        if (Test-Path $sourceFile) {
            Write-Host "Copying '$sourceFile' to '$destinationFile'"
            if (!(Test-Path -Path $destinationDirectory)) {
                New-Item -ItemType Directory -Path $destinationDirectory | Out-Null
            }
            Copy-Item $sourceFile -Destination $destinationFile -Force
        }
        else {
            Write-Host "Skipping file '$sourceFile' (does not exist)."
        }
    }
    
}

function CollectOctopusPackages {
    param(
        [string]$solutionDirectory,
        [string]$octopusPath,
        [string]$buildNumber,
        [string]$outputDirectory
    )

    function CollectDeploymentArtifacts {
        param(
            [String]$destination
        )

        if (!(Test-Path $destination)) {
            New-Item -ItemType Directory -Force -Path $destination | Out-Null
        }

        $chartPath = "$solutionDirectory\.ci-cd\deploy\*"

        Write-Host "Copying files from '$chartPath' to '$destination'"

        if (!(Test-Path "$chartPath")) {
            Write-Warning "Folder '$chartPath' not found, skipping"
            continue
        }

        Get-ChildItem -Path $chartPath | ForEach-Object {
            Copy-Item $_ -Destination "$destination" -Recurse -Force;
        }
    }

    $ErrorActionPreference = 'Stop'

    $packagePath = "$solutionDirectory\octopus-package\"
    CollectDeploymentArtifacts $packagePath

    $packages = @(
        @{PackageId = 'OneInc.AmazingApi.Deployment'; PackagePath = $packagePath; Format = 'zip' }
    )
    foreach ($package in $packages) {
        $id = "$($package.PackageId)"
        $path = "$($package.PackagePath)"
        $format = "$($package.Format)"

        Write-Host "Octo pack: packageId = '$id'; packagePath = '$path'"
        if (!(Test-Path "$path")) {
            Write-Warning "Folder '$path' not found, skipped`n "
            continue
        }

        & $octopusPath pack --id "$id" --format "$format" --version "$buildNumber" --basePath "$path" --outFolder "$outputDirectory"
        if ($LASTEXITCODE -ne 0) { throw "Error in octo pack" }
        Write-Host " "
    }
}

function PushOctopusPackages {
    param(
        [string] $octopusPath,
        [string] $octopusUrl,
        [string] $octopusApiKey,
        [string] $octopusPackageOutputDirectory
    )
    
    $packagePath = (Get-ChildItem -Path "$octopusPackageOutputDirectory" -Filter *.zip).FullName
    
    & $octopusPath push --package "$packagePath" --server "$octopusUrl" --apiKey "$octopusApiKey"
    if ($LASTEXITCODE -ne 0) { throw "Error in octo push" }
}

$solutionDirectory = Split-Path (Split-Path $PSScriptRoot)
$octopusPackageOutputDirectory = "$solutionDirectory/Build/OctopusPackages"

BuildAndPublishDockerImages $solutionDirectory $buildNumber $dockerRepository $dockerFeed $buildConfiguration $teamcityVersion
if (!$?) { throw; }

CopyAppSettingsToHelmChart $solutionDirectory "OneInc.AmazingApi.Api" "amazingapi"
if (!$?) { throw; }

CollectOctopusPackages $solutionDirectory $octopusPath $buildNumber $octopusPackageOutputDirectory
if (!$?) { throw; }

PushOctopusPackages $octopusPath $octopusUrl $octopusApiKey $octopusPackageOutputDirectory
if (!$?) { throw; }