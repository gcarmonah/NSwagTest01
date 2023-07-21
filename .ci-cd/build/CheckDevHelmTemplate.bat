set context=docker-desktop
set namespace=amazingapi-dev
set release=amazingapi

kubectl config use-context %context%
@powershell ../scripts/CheckHelmTemplates.ps1 -namespace %namespace% -release %release%