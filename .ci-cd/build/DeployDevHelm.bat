set context=docker-desktop
set namespace=amazingapi-dev
set release=amazingapi

kubectl config use-context %context%
@powershell ../deploy/DeployHelmChart.ps1 -namespace %namespace% -release %release%