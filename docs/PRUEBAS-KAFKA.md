# Pruebas Kafka — Eventos, endpoints y qué esperar

Guía para comprobar la comunicación asíncrona con **Kafka**. El flujo se describe igual que en [EndPoints-y-autorizacion.md](EndPoints-y-autorizacion.md): **qué entidad/evento es**, **qué endpoint lo dispara**, **qué enviar**, **qué respuesta esperar** y **dónde ver el resultado**. En la guía de endpoints, los que usan Kafka tienen la columna **Kafka** con el topic y el output esperado. Para levantar infraestructura (Docker, Eureka, microservicios) ver [GUIA-EJECUCION.md](GUIA-EJECUCION.md).

---

## Antes de empezar

### 1. Infraestructura

```powershell
# En la raíz del proyecto (donde está docker-compose.yml)
docker-compose up -d
```

- **Keycloak:** http://localhost:8080 (para los endpoints que piden JWT).
- **Kafka:** broker en `localhost:29092` (los microservicios usan este puerto).
- **Kafka UI:** http://localhost:8090 — para ver topics, mensajes y consumer groups (sin credenciales por defecto).

Espera ~30 segundos a que Keycloak arranque. Kafka y Zookeeper suelen estar listos en unos segundos.

### 2. Eureka y microservicios

```powershell
.\levantar-todo.ps1
```

Se abren 6 ventanas (Eureka, Miembros 8081, Clases 8082, Entrenadores 8083, Equipos 8084, Notificaciones 8085). **Kafka** debe estar en marcha (puerto 29092) para que Clases y Notificaciones se conecten correctamente al publicar y consumir. Espera ~1 minuto.

### 3. Token JWT (para inscripción)

El endpoint que dispara el evento Kafka es **POST** `/clases/{claseId}/miembros` (Clases 8082) y requiere **Bearer token**. Obtén el token como en [EndPoints-y-autorizacion.md](EndPoints-y-autorizacion.md#cómo-obtener-el-token-keycloak) (Postman o PowerShell).

---

## Resumen: evento Kafka

| # | Topic / Evento | Endpoint que lo dispara | Body | Respuesta HTTP | Dónde ver el resultado |
|---|----------------|--------------------------|------|----------------|-------------------------|
| 1 | Ocupación de clase — `ocupacion-clases` | **POST** `http://localhost:8082/clases/{claseId}/miembros` | Ver abajo | 200 + clase con `miembrosInscritos` | Log Notificaciones (8085): *"[KAFKA] Ocupación actualizada: clase '...' (id) → actual/max"* |

El **productor** está en **Clases** (8082): al inscribir un miembro, además de publicar a RabbitMQ (inscripción), envía un evento de ocupación al topic Kafka `ocupacion-clases`. El **consumidor** está en **Notificaciones** (8085), grupo `monitoreo-grupo`.

---

## 1. Evento: Ocupación de clase (Kafka)

**Entidad/evento:** Cuando un miembro se inscribe en una clase, Clases actualiza la ocupación y publica un mensaje en el topic Kafka `ocupacion-clases` (claseId, nombre, ocupación actual, capacidad máxima). Notificaciones lo consume y escribe en log.

| Dato | Valor |
|------|--------|
| **Endpoint que lo dispara** | **POST** `http://localhost:8082/clases/{claseId}/miembros` |
| **Roles / token** | Bearer token (ADMIN, TRAINER o MEMBER) |
| **Body** | `{ "miembroid_value": "1" }` (id del miembro a inscribir) |
| **Respuesta esperada** | **200** — JSON de la clase actualizada, con el miembro en `miembrosInscritos` |
| **Dónde ver el resultado** | Ventana del servicio **Notificaciones (8085)** — línea de log: *"[KAFKA] Ocupación actualizada: clase 'NombreClase' (idClase) → ocupacionActual/capacidadMaxima"* |

**Pasos:**

1. Asegúrate de tener al menos una clase (ej. id `1`) y un miembro con membresía activa (ej. id `1`). Si no, créalos con POST /clases y POST /miembros (con token).
2. **POST** `http://localhost:8082/clases/1/miembros` con headers `Authorization: Bearer <token>`, `Content-Type: application/json` y body `{ "miembroid_value": "1" }`.
3. Comprueba respuesta 200. En la ventana de **Notificaciones** deberías ver:
   - Primero el log de **RabbitMQ**: *"Nueva inscripción recibida: miembro X en clase..."*
   - Luego el log de **Kafka**: *"[KAFKA] Ocupación actualizada: clase '...' (1) → 1/10"* (o los valores que correspondan a esa clase).

Si ves el log `[KAFKA] Ocupación actualizada`, el topic `ocupacion-clases` y el listener están bien.

**En Kafka UI (http://localhost:8090):** En *Topics* verás `ocupacion-clases`; en *Consumers* el grupo `monitoreo-grupo`. Puedes inspeccionar mensajes y offsets.

---

## Resumen rápido (acción)

| # | Prueba | Acción principal | Dónde mirar |
|---|--------|-------------------|-------------|
| 1 | Ocupación clase (Kafka) | POST /clases/1/miembros con body `{"miembroid_value":"1"}` (con token) | Log Notificaciones: *"[KAFKA] Ocupación actualizada..."* |

---

## Si algo falla

- **No aparece el log [KAFKA]:** Comprueba que Kafka esté en marcha (`docker-compose ps`, puerto 29092). Revisa que Clases y Notificaciones hayan arrancado sin errores de conexión a Kafka y que la inscripción devuelva 200.
- **Error de conexión a Kafka en arranque:** Verifica que `docker-compose up -d` haya levantado `zookeeper` y `kafka`. En los microservicios, `spring.kafka.bootstrap-servers=localhost:29092` debe coincidir con el puerto expuesto en Docker.
