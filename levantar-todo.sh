#!/usr/bin/env bash
# levantar-todo.sh — Levanta toda la infraestructura y microservicios del gimnasio en Linux.
# Uso: chmod +x levantar-todo.sh && ./levantar-todo.sh
# Requiere: docker, java 17, curl, nc (netcat)

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="$ROOT_DIR/logs"
PIDS=()

# ─── Colores ────────────────────────────────────────────────────────────────
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
GRAY='\033[0;37m'
NC='\033[0m'

info()    { echo -e "${CYAN}[INFO]${NC}  $*"; }
ok()      { echo -e "${GREEN}[OK]${NC}    $*"; }
warn()    { echo -e "${YELLOW}[WAIT]${NC}  $*"; }
error()   { echo -e "${RED}[ERROR]${NC} $*" >&2; }

# ─── Limpieza al salir ───────────────────────────────────────────────────────
cleanup() {
    echo ""
    info "Deteniendo todos los servicios..."
    for pid in "${PIDS[@]}"; do
        if kill -0 "$pid" 2>/dev/null; then
            kill "$pid" 2>/dev/null || true
        fi
    done
    info "Servicios detenidos. Los contenedores Docker siguen corriendo."
    info "Para detener Docker: docker compose -f '$ROOT_DIR/docker-compose.yml' down"
}
trap cleanup INT TERM

# ─── Esperar a que un puerto TCP esté abierto ───────────────────────────────
# wait_for_port <puerto> <nombre> <max_intentos>
wait_for_port() {
    local port="$1"
    local nombre="$2"
    local max="${3:-30}"
    local intentos=0

    warn "Esperando que $nombre esté disponible en localhost:$port ..."
    until (echo > /dev/tcp/localhost/"$port") 2>/dev/null; do
        intentos=$((intentos + 1))
        if [ "$intentos" -ge "$max" ]; then
            error "$nombre no respondió después de $((max * 5))s."
            exit 1
        fi
        echo -e "${GRAY}  ... intento $intentos/$max (esperando 5s)${NC}"
        sleep 5
    done
    ok "$nombre listo."
}

# ─── Esperar a que una URL responda ─────────────────────────────────────────
# wait_for_url <url> <nombre> <max_intentos>
wait_for_url() {
    local url="$1"
    local nombre="$2"
    local max="${3:-30}"
    local intentos=0

    warn "Esperando que $nombre esté disponible en $url ..."
    until curl -sf --max-time 3 "$url" > /dev/null 2>&1; do
        intentos=$((intentos + 1))
        if [ "$intentos" -ge "$max" ]; then
            error "$nombre no respondió después de $((max * 5))s. Revisa $LOG_DIR/"
            exit 1
        fi
        echo -e "${GRAY}  ... intento $intentos/$max (esperando 5s)${NC}"
        sleep 5
    done
    ok "$nombre listo."
}

# ─── Iniciar un microservicio en background ──────────────────────────────────
# start_service <nombre> <puerto> <carpeta>
start_service() {
    local nombre="$1"
    local puerto="$2"
    local carpeta="$3"
    local log="$LOG_DIR/${nombre}.log"
    local dir="$ROOT_DIR/$carpeta"

    if [ ! -f "$dir/mvnw" ]; then
        error "No se encontró mvnw en $dir"
        exit 1
    fi

    info "Iniciando $nombre (puerto $puerto)... → log: logs/${nombre}.log"
    (cd "$dir" && ./mvnw spring-boot:run --no-transfer-progress) > "$log" 2>&1 &
    PIDS+=($!)
}

# ─── Verificar prerequisitos ────────────────────────────────────────────────
check_prereqs() {
    local missing=0
    for cmd in docker java curl; do
        if ! command -v "$cmd" &>/dev/null; then
            error "Falta el comando: $cmd"
            missing=1
        fi
    done
    [ "$missing" -eq 1 ] && exit 1

    local java_version
    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$java_version" -lt 17 ] 2>/dev/null; then
        error "Se requiere Java 17+. Versión detectada: $java_version"
        exit 1
    fi
}

# ════════════════════════════════════════════════════════════════════════════
#  MAIN
# ════════════════════════════════════════════════════════════════════════════

echo -e "${CYAN}"
echo "╔══════════════════════════════════════════════════════╗"
echo "║       Sistema de Gimnasio — Microservicios           ║"
echo "╚══════════════════════════════════════════════════════╝"
echo -e "${NC}"

check_prereqs

mkdir -p "$LOG_DIR"

# Permisos de ejecución en todos los Maven wrappers
chmod +x "$ROOT_DIR"/*/mvnw 2>/dev/null || true

# ── 1. Infraestructura Docker ────────────────────────────────────────────────
info "Levantando Keycloak, RabbitMQ y Kafka con Docker Compose..."
docker compose -f "$ROOT_DIR/docker-compose.yml" up -d
wait_for_url "http://localhost:8080" "Keycloak" 36        # hasta 3 min
wait_for_url "http://localhost:15672" "RabbitMQ" 24       # hasta 2 min
wait_for_port 29092 "Kafka" 30                            # hasta 2.5 min

# ── 2. Eureka Server ─────────────────────────────────────────────────────────
start_service "eureka" 8761 "servidor-descubrimiento-mcrs"
wait_for_url "http://localhost:8761/actuator/health" "Eureka" 36

# ── 3. Microservicios (en paralelo) ──────────────────────────────────────────
start_service "entrenadores"   8083 "microservicio-entrenadores-mcrs"
start_service "miembros"       8081 "microservicio-miembros-mcrs"
start_service "equipos"        8084 "microservicio-equipos-mcrs"
start_service "clases"         8082 "microservicio-clases-mcrs"
start_service "notificaciones" 8085 "microservicio-notificaciones-mcrs"

# ── 4. Resumen ───────────────────────────────────────────────────────────────
echo ""
echo -e "${GREEN}╔══════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  Sistema iniciado — espera ~1 min a que compilen     ║${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "  ${YELLOW}Eureka:${NC}          http://localhost:8761"
echo -e "  ${YELLOW}Keycloak:${NC}        http://localhost:8080  (admin / admin)"
echo -e "  ${YELLOW}RabbitMQ UI:${NC}     http://localhost:15672 (guest / guest)"
echo -e "  ${YELLOW}Kafka:${NC}           localhost:29092  |  Zookeeper: localhost:22181"
echo -e "  ${YELLOW}Kafka UI:${NC}        http://localhost:8090"
echo -e "  ${YELLOW}Miembros:${NC}        http://localhost:8081/swagger-ui.html"
echo -e "  ${YELLOW}Clases:${NC}          http://localhost:8082/swagger-ui.html"
echo -e "  ${YELLOW}Entrenadores:${NC}    http://localhost:8083/swagger-ui.html"
echo -e "  ${YELLOW}Equipos:${NC}         http://localhost:8084/swagger-ui.html"
echo -e "  ${YELLOW}Notificaciones:${NC}  http://localhost:8085/swagger-ui.html"
echo ""
echo -e "  ${GRAY}Logs en: $LOG_DIR/${NC}"
echo -e "  ${GRAY}Presiona Ctrl+C para detener todos los servicios.${NC}"
echo ""

# Esperar hasta Ctrl+C
wait
