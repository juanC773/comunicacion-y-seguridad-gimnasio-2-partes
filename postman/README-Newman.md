# Pruebas con Newman

Esta carpeta contiene la colección y el environment para ejecutar **todas** las pruebas de los endpoints con **Newman** (CLI de Postman): happy path (rol correcto → 200/201) y unhappy path (sin token → 401, rol sin permiso → 403).

**Tener en cuenta:** En `Gimnasio.postman_environment.json` están las variables `client_id` y `client_secret` (cliente Keycloak `clase-service`). Cuando alguien corre el proyecto en su máquina y monta Keycloak (creando o importando el realm), el **client_secret** que genera Keycloak es distinto al que viene en el JSON. Hay que abrir el environment, copiar el secret desde Keycloak (Clients → clase-service → Credentials) y actualizar el valor de `client_secret` para que las peticiones de token funcionen.

## Requisitos

- **Node.js** (para instalar Newman).
- Keycloak, RabbitMQ, Eureka y los 5 microservicios levantados. Ver **[docs/GUIA-EJECUCION.md](../docs/GUIA-EJECUCION.md)** (Docker, `levantar-todo.ps1`, etc.).
- Usuarios en Keycloak realm `gimnasio`: `admin1`, `entrenador1`, `miembro1` con contraseña `password` y roles ADMIN, TRAINER y MEMBER asignados (ver [KEYCLOAK-EXPORT.md](../docs/KEYCLOAK-EXPORT.md)).

## Instalación de Newman

```bash
npm install -g newman
```

(O en el proyecto: `npm install newman` y luego `npx newman ...`.)

## Ejecutar las pruebas

Desde la **raíz del proyecto** (donde está la carpeta `postman/`):

```bash
newman run postman/Microservicios-Gimnasio-Newman.postman_collection.json -e postman/Gimnasio.postman_environment.json
```

Con reporte HTML opcional:

```bash
newman run postman/Microservicios-Gimnasio-Newman.postman_collection.json -e postman/Gimnasio.postman_environment.json -r html --reporter-html-export postman/newman-report.html
```

## Qué prueba la colección

1. **0 - Auth:** Obtiene tokens para `admin1`, `entrenador1` y `miembro1` y los guarda en el environment (usa `client_id` y `client_secret` del environment).
2. **1 - Sin token (401):** GET /miembros, GET /clases, POST /miembros **sin** header Authorization → se espera 401.
3. **2 - Health:** GET a `/actuator/health` de los 5 servicios (sin token).
4. **3 - Miembros:** Happy: GET /miembros con ADMIN y TRAINER (200), GET /miembros/1 y PUT email con MEMBER (200), POST con ADMIN (201). Unhappy: GET /miembros con MEMBER → 403, POST /miembros con MEMBER → 403.
5. **4 - Clases:** Happy con ADMIN/MEMBER según endpoint; POST /clases/1/miembros (dispara evento RabbitMQ inscripción); PUT /clases/1/horario (dispara evento RabbitMQ cambio horario). Unhappy: POST /clases y PUT /clases/1/horario con MEMBER → 403.
6. **5 - Entrenadores:** Happy con ADMIN/TRAINER/MEMBER; GET /entrenadores/1/existe sin token; unhappy: POST y PUT con MEMBER → 403.
7. **6 - Equipos:** Happy con ADMIN/TRAINER/MEMBER; unhappy: POST y PUT con MEMBER → 403.
8. **7 - Notificaciones (RabbitMQ / DLQ):** GET public/status; POST /simular-pago con body JSON (mensaje a cola → falla → DLQ); GET /pagos-fallidos (comprueba que el mensaje se guardó en memoria). Así se ejercitan los flujos asincrónicos (inscripción, cambio horario, DLQ de pagos).

**Resumen:** Happy path con el rol correcto, unhappy con rol que no puede (403) y sin token (401).

**Variable `client_secret`:** En `postman/Gimnasio.postman_environment.json` está el client secret del cliente `clase-service`. Cuando creas o importas el realm en Keycloak, Keycloak puede generar un secret distinto; en ese caso debes copiar el que te muestre Keycloak (Clients → clase-service → Credentials) y actualizarlo en el environment para que las peticiones de token funcionen.

## Más información

- **Endpoints, roles y body de cada petición:** [docs/EndPoints-y-autorizacion.md](../docs/EndPoints-y-autorizacion.md).
- **Montar todo (Docker, Eureka, Swagger):** [docs/GUIA-EJECUCION.md](../docs/GUIA-EJECUCION.md).
- **Pruebas RabbitMQ (inscripción, DLQ):** [docs/PRUEBAS-RABBITMQ.md](../docs/PRUEBAS-RABBITMQ.md).
