# Postman — Endpoints por microservicio

Esta guía documenta **todos los endpoints** de la API: método, URL, **qué roles se necesitan**, **qué enviar en el body** (cuando aplica) y ejemplos. Si un endpoint dispara un evento **RabbitMQ** o **Kafka**, en las columnas **Rabbit** y **Kafka** se indica el evento/cola/topic y el output esperado (log). Para levantar servicios e infraestructura ver [GUIA-EJECUCION.md](GUIA-EJECUCION.md). Pruebas paso a paso: [PRUEBAS-RABBITMQ.md](PRUEBAS-RABBITMQ.md), [PRUEBAS-KAFKA.md](PRUEBAS-KAFKA.md).

---

## Cómo obtener el token (Keycloak)

Casi todos los endpoints de Miembros, Clases, Entrenadores y Equipos requieren **JWT**. Primero obtén un token y luego úsalo como `Authorization: Bearer <token>`.

### En Postman

1. Nueva petición: **POST**  
   **URL:** `http://localhost:8080/realms/gimnasio/protocol/openid-connect/token`

2. **Body** → **x-www-form-urlencoded**:

   | Key             | Value                                                |
   |-----------------|------------------------------------------------------|
   | `grant_type`    | `password`                                           |
   | `client_id`     | `clase-service`                                      |
   | `client_secret` | `4caMaKDTbeQsvko8BDSvrqdnFnOSYjnt`                   |
   | `username`      | `admin1` (o el usuario que tengas en Keycloak)       |
   | `password`      | `password` (contraseña del usuario)                   |

3. **Send**. En la respuesta JSON copia el valor de **`access_token`**.

4. En el resto de peticiones:
   - **Authorization** → Type: **Bearer Token** → pega el token, **o**
   - **Headers** → `Authorization` = `Bearer <access_token>`.

### Usuarios de prueba (según roles)

Si creaste usuarios como en [KEYCLOAK-QUE-CREAR.md](KEYCLOAK-QUE-CREAR.md):

| Usuario       | Contraseña | Rol asignado  | Uso típico                          |
|---------------|------------|---------------|-------------------------------------|
| `admin1`      | `password` | ADMIN         | Acceso a todos los endpoints        |
| `entrenador1` | `password` | TRAINER       | Probar permisos de entrenador       |
| `miembro1`    | `password` | MEMBER        | Probar permisos de miembro (lectura, inscripción) |

Cambia `username` y `password` en la petición de token según el rol con el que quieras probar.

### Leyenda en las tablas

- **Público:** no requiere token.
- **ADMIN, TRAINER, MEMBER:** requiere **Authorization: Bearer &lt;token&gt;** y que el usuario tenga **al menos uno** de los roles indicados (dependiendo del endpoint).
- **Rabbit:** si el endpoint publica o está relacionado con RabbitMQ, se indica la cola/evento y qué esperar (p. ej. en log de Notificaciones). "—" = no aplica.
- **Kafka:** si el endpoint publica a Kafka, se indica el topic y qué esperar (p. ej. en log de Notificaciones). "—" = no aplica.

---

## Base URLs (local)

| Servicio       | Base URL             | Puerto |
|----------------|----------------------|--------|
| Miembros       | http://localhost:8081 | 8081   |
| Clases         | http://localhost:8082 | 8082   |
| Entrenadores   | http://localhost:8083 | 8083   |
| Equipos        | http://localhost:8084 | 8084   |
| Notificaciones | http://localhost:8085 | 8085   |

---

## Health check (todos los servicios)

**Público** — no requieren token.

| Servicio      | Método | URL |
|---------------|--------|-----|
| Miembros      | GET    | http://localhost:8081/actuator/health |
| Clases        | GET    | http://localhost:8082/actuator/health |
| Entrenadores  | GET    | http://localhost:8083/actuator/health |
| Equipos       | GET    | http://localhost:8084/actuator/health |
| Notificaciones| GET    | http://localhost:8085/actuator/health |

Respuesta esperada (200): `{"status":"UP"}` (o similar con detalles).

---

## Endpoints que participan en llamadas entre microservicios

| Dónde       | Método | Endpoint | Qué hace |
|-------------|--------|----------|----------|
| Clases      | POST   | `/clases` | Antes de guardar, llama a **Entrenadores** (`GET /entrenadores/{id}/existe`). |
| Clases      | POST   | `/clases/{claseId}/miembros` | Antes de inscribir, llama a **Miembros** (`GET /miembros/{id}/puede-asistir-clase`). |
| Entrenadores| GET    | `/entrenadores/{id}/existe` | Llamado por Clases al programar una clase. **Público.** |
| Miembros    | GET    | `/miembros/{id}/puede-asistir-clase` | Llamado por Clases al inscribir un miembro. Requiere token. |

---

## 1. Miembros (puerto 8081)

Salvo indicado, todos los endpoints requieren **Bearer token (JWT)**.

| Método | Endpoint | Roles | Descripción | Body | Rabbit | Kafka |
|--------|----------|-------|-------------|------|--------|------|
| GET | `/miembros/public/status` | Público | Estado del servicio | — | — | — |
| GET | `/miembros` | ADMIN, TRAINER | Listar todos los miembros | — | — | — |
| GET | `/miembros/{id}` | ADMIN, TRAINER, MEMBER | Obtener miembro por id | — | — | — |
| GET | `/miembros/{id}/puede-asistir-clase` | ADMIN, TRAINER, MEMBER | Usado por Clases: `{ puedeAsistir, razon }` | — | — | — |
| POST | `/miembros` | ADMIN, TRAINER | Registrar nuevo miembro | JSON abajo | — | — |
| PUT | `/miembros/{id}/email` | ADMIN, TRAINER, MEMBER | Actualizar email | String JSON | — | — |

**Body POST /miembros:**

```json
{
  "id": { "miembroid_value": "4" },
  "nombre": "Pedro García",
  "email": { "email_value": "pedro@email.com" },
  "fechaInscripcion": "2026-02-23",
  "membresiaActiva": true
}
```

**Body PUT /miembros/{id}/email:**  
Un string JSON con el nuevo email, p. ej. `"nuevo@email.com"`.

---

## 2. Clases (puerto 8082)

Salvo indicado, todos requieren **Bearer token (JWT)**.

| Método | Endpoint | Roles | Descripción | Body | Rabbit | Kafka |
|--------|----------|-------|-------------|------|--------|------|
| GET | `/clases/public/status` | Público | Estado del servicio | — | — | — |
| GET | `/clases` | ADMIN, TRAINER, MEMBER | Listar todas las clases | — | — | — |
| GET | `/clases/{id}` | ADMIN, TRAINER, MEMBER | Obtener clase por id (incl. miembrosInscritos) | — | — | — |
| POST | `/clases` | ADMIN, TRAINER | Programar clase (valida entrenador) | JSON abajo | — | — |
| POST | `/clases/{claseId}/miembros` | ADMIN, TRAINER, MEMBER | Inscribir miembro (valida en Miembros) | JSON abajo | **Rabbit:** cola `gimnasio.inscripciones`. **Output:** log Notificaciones (8085): *"Nueva inscripción recibida: miembro X en clase..."* | **Kafka:** topic `ocupacion-clases`. **Output:** log Notificaciones: *"[KAFKA] Ocupación actualizada: clase '...' (id) → actual/max"* |
| PUT | `/clases/{id}/horario` | ADMIN, TRAINER | Actualizar horario | JSON abajo | **Rabbit:** exchange `gimnasio.eventos` → cola `notificaciones.cambio-horario-clase`. **Output:** log Notificaciones: *"Cambio de horario de clase: ... de ... a ..."* | — |

**Body POST /clases:**

```json
{
  "id": { "claseid_value": "3" },
  "nombre": "CrossFit Intensivo",
  "horario": "2026-03-01T10:00:00",
  "capacidadMaxima": 10,
  "entrenadorId": { "entrenadorid_value": "1" }
}
```

- **Happy path:** entrenador con id 1 existente → 200 y clase creada.
- **Unhappy:** `entrenadorid_value` inexistente (ej. "100") → 404 "Entrenador no encontrado".

**Body POST /clases/{claseId}/miembros** (inscribir miembro):

```json
{
  "miembroid_value": "1"
}
```

- **Happy:** miembro existe y membresía activa → 200.
- **Unhappy:** miembro no existe (ej. "999") → 404 "El miembro no existe."; membresía inactiva → 404 "No se puede inscribir: la membresía está inactiva."; ya inscrito → 409 "El miembro ya está inscrito en esta clase."

**Body PUT /clases/{id}/horario:**

```json
{
  "horario": "2026-03-01T14:00:00"
}
```

---

## 3. Entrenadores (puerto 8083)

| Método | Endpoint | Roles | Descripción | Body | Rabbit | Kafka |
|--------|----------|-------|-------------|------|--------|------|
| GET | `/entrenadores/public/status` | Público | Estado del servicio | — | — | — |
| GET | `/entrenadores` | ADMIN, TRAINER, MEMBER | Listar todos | — | — | — |
| GET | `/entrenadores/{id}` | ADMIN, TRAINER, MEMBER | Obtener por id | — | — | — |
| GET | `/entrenadores/{id}/existe` | **Público** | ¿Existe el entrenador? (true/false). Usado por Clases. | — | — | — |
| POST | `/entrenadores` | ADMIN, TRAINER | Agregar entrenador | JSON abajo | — | — |
| PUT | `/entrenadores/{id}/especialidad` | ADMIN, TRAINER | Actualizar especialidad | String JSON | — | — |

**Body POST /entrenadores:**

```json
{
  "id": { "entrenadorid_value": "3" },
  "nombre": "Luis Torres",
  "especialidad": { "especialidad_value": "CrossFit" }
}
```

**Body PUT /entrenadores/{id}/especialidad:**  
String JSON, p. ej. `"Funcional"`.

---

## 4. Equipos (puerto 8084)

| Método | Endpoint | Roles | Descripción | Body | Rabbit | Kafka |
|--------|----------|-------|-------------|------|--------|------|
| GET | `/equipos/public/status` | Público | Estado del servicio | — | — | — |
| GET | `/equipos` | ADMIN, TRAINER, MEMBER | Listar todos | — | — | — |
| GET | `/equipos/{id}` | ADMIN, TRAINER, MEMBER | Obtener por id | — | — | — |
| POST | `/equipos` | ADMIN, TRAINER | Agregar equipo | JSON abajo | — | — |
| PUT | `/equipos/{id}/cantidad` | ADMIN, TRAINER | Actualizar cantidad | Número (ej. 25) | — | — |

**Body POST /equipos:**

```json
{
  "id": { "equipoid_value": "3" },
  "nombre": "Cuerda para saltar",
  "descripcion": "Cuerda de velocidad profesional",
  "cantidad": 30
}
```

**Body PUT /equipos/{id}/cantidad:**  
Un número, p. ej. `25`.

---

## 5. Notificaciones (puerto 8085)

**Sin JWT** — este servicio no usa Keycloak.

| Método | Endpoint | Roles | Descripción | Body | Rabbit | Kafka |
|--------|----------|-------|-------------|------|--------|------|
| GET | `/notificaciones/public/status` | Público | Estado del servicio | — | — | — |
| GET | `/notificaciones/pagos-fallidos` | Público | Lista pagos que llegaron a la DLQ (en memoria) | — | **Rabbit:** muestra mensajes consumidos de cola `gimnasio.pagos.dlq`. **Output:** JSON con pagos fallidos (miembroId, concepto, monto, timestamps). | — |
| POST | `/notificaciones/simular-pago` | Público | Envía mensaje a cola de pagos (para probar DLQ) | JSON opcional abajo | **Rabbit:** publica en `gimnasio.pagos` → falla → DLQ `gimnasio.pagos.dlq`. **Output:** log Notificaciones: *"[PAGOS] Procesando pago..."* y *"[DLQ PAGOS] Mensaje de pago fallido recibido..."* | — |

**Body POST /notificaciones/simular-pago** (opcional):

```json
{
  "miembroId": "1",
  "concepto": "membresía",
  "monto": 50.00
}
```

Si no envías body, se usan valores por defecto (miembroId "1", concepto "membresía", monto 50.00). Ver [PRUEBAS-RABBITMQ.md](PRUEBAS-RABBITMQ.md) para el flujo completo de la DLQ.

---

## Orden sugerido para probar en Postman

1. **Token:** POST a Keycloak (sección *Cómo obtener el token*) y copiar `access_token`. Usar **Authorization → Bearer Token** en las peticiones que lo requieran.
2. **Health:** GET a cada `/actuator/health` (8081–8085); no requieren token.
3. **Entrenadores (8083):** GET /entrenadores, GET /entrenadores/1, GET /entrenadores/1/existe (sin token), POST (crear), PUT /entrenadores/3/especialidad.
4. **Miembros (8081):** GET /miembros, GET /miembros/1, POST (crear), PUT /miembros/3/email.
5. **Equipos (8084):** GET /equipos, GET /equipos/1, POST (crear), PUT /equipos/1/cantidad.
6. **Clases (8082):** GET /clases, GET /clases/1, POST (crear con entrenadorId 1), POST /clases/1/miembros con body `{"miembroid_value": "1"}` (Rabbit + Kafka: ver log Notificaciones), PUT /clases/1/horario (Rabbit: ver log cambio horario). Probar errores: miembro 999, miembro con membresía inactiva, entrenadorId inexistente.
7. **Notificaciones (8085):** GET /notificaciones/public/status, POST /notificaciones/simular-pago (body opcional; Rabbit: ver log [PAGOS] y [DLQ PAGOS]), GET /notificaciones/pagos-fallidos (muestra lo que llegó a la DLQ). Ver [PRUEBAS-RABBITMQ.md](PRUEBAS-RABBITMQ.md) y [PRUEBAS-KAFKA.md](PRUEBAS-KAFKA.md) para orden y detalles.

---

## Probar todo con Newman (CLI)

En la carpeta **postman/** hay una colección y un environment para ejecutar todas las pruebas desde la terminal:

- **Colección:** `postman/Microservicios-Gimnasio-Newman.postman_collection.json`
- **Environment:** `postman/Gimnasio.postman_environment.json` (URLs y client_secret; editar si cambian usuarios/contraseñas en Keycloak)

Instalar Newman: `npm install -g newman`. Desde la raíz del proyecto:

```bash
newman run postman/Microservicios-Gimnasio-Newman.postman_collection.json -e postman/Gimnasio.postman_environment.json
```

La colección obtiene tokens (admin1, entrenador1, miembro1), ejecuta Health y todos los endpoints: con el rol correcto espera 200/201 (happy) y con rol sin permiso espera 403 (unhappy). Detalle en [postman/README-Newman.md](../postman/README-Newman.md).

---

## Eureka y Swagger

- **Eureka:** Tras levantar los servicios con `.\levantar-todo.ps1`, abre **http://localhost:8761** y comprueba que aparecen registrados los cinco microservicios. Ver [GUIA-EJECUCION.md](GUIA-EJECUCION.md#4-ver-que-todo-está-registrado-en-eureka).

- **Swagger (OpenAPI):** Cada microservicio expone documentación interactiva:
  - Miembros: http://localhost:8081/swagger-ui.html  
  - Clases: http://localhost:8082/swagger-ui.html  
  - Entrenadores: http://localhost:8083/swagger-ui.html  
  - Equipos: http://localhost:8084/swagger-ui.html  
  - Notificaciones: http://localhost:8085/swagger-ui.html  

En Swagger puedes ver los endpoints y, si configuras **Authorize** con `Bearer <token>`, probar los que requieren JWT. OpenAPI JSON: `http://localhost:8XXX/v3/api-docs`. Más detalle en [GUIA-EJECUCION.md](GUIA-EJECUCION.md#5-swagger-openapi-por-servicio).
