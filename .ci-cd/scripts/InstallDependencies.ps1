$ErrorActionPreference = "Stop"

function Get-App-Installation-Status([string] $command)
{
    try
    {
        Get-Command $command
        return $true;
    }
    catch
    {
        return $false
    }
}

function Install-Choco()
{
    Set-ExecutionPolicy Bypass -Scope Process -Force;
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072;
    iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
}

if (!(Get-App-Installation-Status("choco")))
{
    Install-Choco
}

choco install kubernetes-cli
choco install minikube
choco install kubernetes-helm --version=3.10.3

.\InstallIngressNginx
