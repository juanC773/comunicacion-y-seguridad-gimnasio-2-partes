# Ejecuta todas las pruebas Newman (happy + unhappy path).
# Requiere: newman instalado (npm install -g newman) y servicios + Keycloak levantados.
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$collection = Join-Path $scriptDir "Microservicios-Gimnasio-Newman.postman_collection.json"
$envFile = Join-Path $scriptDir "Gimnasio.postman_environment.json"

if (-not (Test-Path $collection)) { Write-Error "No se encuentra la colección: $collection"; exit 1 }
if (-not (Test-Path $envFile)) { Write-Error "No se encuentra el environment: $envFile"; exit 1 }

Write-Host "Ejecutando Newman..." -ForegroundColor Cyan
newman run $collection -e $envFile
