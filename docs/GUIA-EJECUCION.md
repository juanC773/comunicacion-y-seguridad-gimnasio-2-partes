# Guía de ejecución — Montar y probar todo

Esta guía explica paso a paso cómo levantar la infraestructura, los microservicios y cómo verificar que todo está en marcha (Eureka, Swagger, Postman, RabbitMQ, Newman).

---

## 1. Requisitos

- **Java 17** (o superior).
- **Maven** (o usar los `mvnw` incluidos en cada módulo).
- **Docker** y **Docker Compose** (para Keycloak y RabbitMQ).
- **Node.js** (opcional, solo si quieres ejecutar Newman: `npm install -g newman`).

---

## 2. Levantar infraestructura (Docker)

En la **raíz del proyecto** (donde está `docker-compose.yml`):

```powershell
docker-compose up -d
```

Esto levanta:

| Servicio  | URL (admin/consola)     | Credenciales   | Uso |
|-----------|-------------------------|----------------|-----|
| **Keycloak**  | http://localhost:8080   | Admin: `admin` / `admin` | Autenticación JWT para la API |
| **RabbitMQ**  | http://localhost:15672  | `guest` / `guest`        | Colas y mensajería (notificaciones, DLQ) |

- **Keycloak:** espera ~30 segundos tras `docker-compose up -d` antes de crear el realm o importar el export.
- **RabbitMQ:** puerto AMQP 5672 para los microservicios; 15672 para la consola web (ver colas, exchanges).

Configuración del realm y clientes en Keycloak: ver [KEYCLOAK-QUE-CREAR.md](KEYCLOAK-QUE-CREAR.md). Importación desde fichero: [KEYCLOAK-EXPORT.md](KEYCLOAK-EXPORT.md).

---

## 3. Levantar Eureka y microservicios

Desde la **raíz del proyecto**:

```powershell
.\levantar-todo.ps1
```

El script abre **6 ventanas** (PowerShell):

1. **Eureka** (puerto **8761**)
2. **Entrenadores** (8083)
3. **Miembros** (8081)
4. **Equipos** (8084)
5. **Clases** (8082)
6. **Notificaciones** (8085)

**Orden importante:** Eureka arranca primero; el script espera 15 s antes de lanzar el resto. Los microservicios se registran en Eureka al iniciar.  
**Requisito:** RabbitMQ debe estar corriendo (puerto 5672) para que Clases y Notificaciones arranquen bien.

Espera ~1 minuto y comprueba que no haya errores en las ventanas.

---

## 4. Ver que todo está registrado en Eureka

1. Abre en el navegador: **http://localhost:8761**
2. En el dashboard de Eureka verás las **Applications** registradas.

Deberías ver algo como:

- **CLASE-SERVICE** (o clase-service)
- **ENTRENADOR-SERVICE** (o entrenador-service)
- **EQUIPO-SERVICE** (o equipo-service)
- **MIEMBRO-SERVICE** (o miembro-service)
- **NOTIFICACIONES** (o el nombre del servicio de notificaciones)

Si un servicio no aparece, revisa la ventana de ese servicio: suele ser error de conexión a Eureka (8761) o a RabbitMQ (5672). La URL base de Eureka está en el `application.properties` de cada microservicio: `eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/`.

---

## 5. Swagger (OpenAPI) por servicio

Cada microservicio expone **Swagger UI** con Springdoc. Una vez levantados:

| Servicio       | Puerto | Swagger UI (interfaz)     | OpenAPI JSON        |
|----------------|--------|---------------------------|----------------------|
| Miembros       | 8081   | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| Clases         | 8082   | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |
| Entrenadores   | 8083   | http://localhost:8083/swagger-ui.html | http://localhost:8083/v3/api-docs |
| Equipos        | 8084   | http://localhost:8084/swagger-ui.html | http://localhost:8084/v3/api-docs |
| Notificaciones | 8085   | http://localhost:8085/swagger-ui.html | http://localhost:8085/v3/api-docs |

En Swagger UI puedes ver todos los endpoints del servicio, parámetros y (si está configurado) la seguridad Bearer JWT. Para probar con token, obtén primero un JWT desde Keycloak (ver [EndPoints-y-autorizacion.md](EndPoints-y-autorizacion.md)) y en Swagger usa **Authorize** con `Bearer <token>`.

---

## 6. Probar la API (Postman)

- **Cómo obtener el token y qué roles usar:** [EndPoints-y-autorizacion.md](EndPoints-y-autorizacion.md).
- En esa guía están **todos los endpoints** por servicio, con:
  - Método y URL
  - **Qué roles se necesitan** (ADMIN, TRAINER, MEMBER) o si es público
  - **Body** en JSON cuando aplica (ejemplos de POST/PUT)

Orden sugerido: token → health → Entrenadores → Miembros → Equipos → Clases → Notificaciones.

---

## 7. Probar RabbitMQ (notificaciones y DLQ)

Pasos concretos y orden de pruebas en **[PRUEBAS-RABBITMQ.md](PRUEBAS-RABBITMQ.md)**:

1. Notificaciones por **nueva inscripción** (POST clase/miembro).
2. **Cambio de horario** de clase (PUT horario).
3. **Dead Letter Queue (DLQ)** de pagos (POST simular-pago, ver log y GET pagos-fallidos).

Ahí se indica qué peticiones hacer, qué ver en consola (Notificaciones) y en la consola de RabbitMQ (http://localhost:15672).

---

## 8. Probar todo con Newman (CLI)

Colección y environment en la carpeta **postman/**:

- **Colección:** `postman/Microservicios-Gimnasio-Newman.postman_collection.json`
- **Environment:** `postman/Gimnasio.postman_environment.json`

Instalar Newman:

```bash
npm install -g newman
```

Desde la **raíz del proyecto**:

```bash
newman run postman/Microservicios-Gimnasio-Newman.postman_collection.json -e postman/Gimnasio.postman_environment.json
```

La colección obtiene tokens (admin1, entrenador1, miembro1), prueba health, todos los endpoints con el rol correcto (happy) y con rol sin permiso o sin token (401/403). Incluye flujos de Notificaciones y DLQ. Detalle en [postman/README-Newman.md](../postman/README-Newman.md).

---

## Resumen de puertos

| Puerto  | Servicio / Herramienta |
|---------|-------------------------|
| 8080    | Keycloak                |
| 8081    | Miembros                |
| 8082    | Clases                  |
| 8083    | Entrenadores            |
| 8084    | Equipos                 |
| 8085    | Notificaciones          |
| 8761    | Eureka                  |
| 5672    | RabbitMQ (AMQP)         |
| 15672   | RabbitMQ (consola web)  |
