# Levanta Eureka + 5 microservicios del gimnasio (cada uno en su propia ventana).
# Requiere: docker-compose up -d  (Keycloak, RabbitMQ, Zookeeper, Kafka, Kafka UI)
# Ejecutar desde la raíz del proyecto: .\levantar-todo.ps1

$root = (Get-Location).Path

Write-Host "Iniciando Eureka (8761)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$root'; Write-Host 'Eureka Server' -ForegroundColor Yellow; .\servidor-descubrimiento-mcrs\mvnw.cmd -f servidor-descubrimiento-mcrs\pom.xml spring-boot:run"

Write-Host "Esperando 15 s a que Eureka arranque..." -ForegroundColor Gray
Start-Sleep -Seconds 15

Write-Host "Iniciando Entrenadores (8083)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$root'; Write-Host 'Entrenadores' -ForegroundColor Yellow; .\microservicio-entrenadores-mcrs\mvnw.cmd -f microservicio-entrenadores-mcrs\pom.xml spring-boot:run"

Write-Host "Iniciando Miembros (8081)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$root'; Write-Host 'Miembros' -ForegroundColor Yellow; .\microservicio-miembros-mcrs\mvnw.cmd -f microservicio-miembros-mcrs\pom.xml spring-boot:run"

Write-Host "Iniciando Equipos (8084)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$root'; Write-Host 'Equipos' -ForegroundColor Yellow; .\microservicio-equipos-mcrs\mvnw.cmd -f microservicio-equipos-mcrs\pom.xml spring-boot:run"

Write-Host "Iniciando Clases (8082)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$root'; Write-Host 'Clases' -ForegroundColor Yellow; .\microservicio-clases-mcrs\mvnw.cmd -f microservicio-clases-mcrs\pom.xml spring-boot:run"

Write-Host "Iniciando Notificaciones (8085)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$root'; Write-Host 'Notificaciones' -ForegroundColor Yellow; .\microservicio-notificaciones-mcrs\mvnw.cmd -f microservicio-notificaciones-mcrs\pom.xml spring-boot:run"

Write-Host "Listo. Se abrieron 6 ventanas. Espera ~1 minuto y revisa:" -ForegroundColor Green
Write-Host "  Eureka:      http://localhost:8761" -ForegroundColor Yellow
Write-Host "  Keycloak:    http://localhost:8080  (admin/admin)" -ForegroundColor Yellow
Write-Host "  RabbitMQ UI: http://localhost:15672 (guest/guest)" -ForegroundColor Yellow
Write-Host "  Kafka UI:    http://localhost:8090" -ForegroundColor Yellow
