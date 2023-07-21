cd /D "%~dp0"

docker-compose -f ../../.docker/docker-compose.yml -f ../../.docker/docker-compose.override.yml build
call DeployDevHelm.bat
@pause