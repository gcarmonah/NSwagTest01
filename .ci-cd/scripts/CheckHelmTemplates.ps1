param (
    [string] $namespace,
    [string] $release,
    [string] $valuesPath
)

$chartPath = (Split-Path $PSScriptRoot)  + "/deploy/charts/amazingapi"

if ($valuesPath)
{
    $valuesPath = $chartPath + "/" + $valuesPath
}
else
{
    $valuesPath = $chartPath + "/values.yaml"
}

helm lint $chartPath
helm template $release $chartPath --values ""$valuesPath""