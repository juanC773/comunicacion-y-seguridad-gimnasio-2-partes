# Pruebas RabbitMQ — Eventos, endpoints y qué esperar

Guía para comprobar la comunicación asíncrona con RabbitMQ. Cada flujo se describe igual que en [EndPoints-y-autorizacion.md](EndPoints-y-autorizacion.md): **qué entidad/evento es**, **qué endpoint lo dispara**, **qué enviar**, **qué respuesta esperar** y **dónde ver el resultado**. En la guía de endpoints, los que usan Rabbit tienen la columna **Rabbit** con el evento/cola y el output esperado. Para levantar infraestructura (Docker, Eureka, microservicios) ver [GUIA-EJECUCION.md](GUIA-EJECUCION.md).

---

## Antes de empezar

### 1. Infraestructura

```powershell
# En la raíz del proyecto (donde está docker-compose.yml)
docker-compose up -d
```

- **Keycloak:** http://localhost:8080 (para los endpoints que piden JWT).
- **RabbitMQ (consola web):** http://localhost:15672 — usuario `guest`, contraseña `guest`. Revisa colas en la pestaña *Queues*.

Espera ~30 segundos a que Keycloak arranque.

### 2. Eureka y microservicios

```powershell
.\levantar-todo.ps1
```

Se abren 6 ventanas (Eureka, Miembros 8081, Clases 8082, Entrenadores 8083, Equipos 8084, Notificaciones 8085). Espera ~1 minuto.

### 3. Token JWT (para inscripción y cambio de horario)

Inscripción y cambio de horario van contra **Clases (8082)** y requieren **Bearer token**. Obtén el token como en [EndPoints-y-autorizacion.md](EndPoints-y-autorizacion.md#cómo-obtener-el-token-keycloak) (Postman o PowerShell). El flujo de **simular pago** y **pagos-fallidos** es contra Notificaciones (8085) y **no** requiere token.

---

## Resumen: eventos RabbitMQ

| # | Evento / Cola | Endpoint que lo dispara | Body | Respuesta HTTP | Dónde ver el resultado |
|---|----------------|--------------------------|------|----------------|-------------------------|
| 1 | Inscripción — `gimnasio.inscripciones` | **POST** `http://localhost:8082/clases/{claseId}/miembros` | Ver abajo | 200 + clase con `miembrosInscritos` | Log Notificaciones (8085): *"Nueva inscripción recibida..."* |
| 2 | Cambio de horario — `gimnasio.eventos` → `notificaciones.cambio-horario-clase` | **PUT** `http://localhost:8082/clases/{id}/horario` | Ver abajo | 200 + clase con nuevo horario | Log Notificaciones: *"Cambio de horario de clase..."* |
| 3 | Pago (simular) — `gimnasio.pagos` → DLQ `gimnasio.pagos.dlq` | **POST** `http://localhost:8085/notificaciones/simular-pago` | Ver abajo (opcional) | 200 + mensaje "Mensaje de pago enviado..." | Log Notificaciones: *"[PAGOS] Procesando..."* y *"[DLQ PAGOS] Mensaje de pago fallido..."*; listar: **GET** `/notificaciones/pagos-fallidos` |

Orden sugerido: **1 → 2 → 3** (la 1 y 2 requieren al menos una clase y un miembro; la 3 no).

---

## 1. Evento: Inscripción a clase

**Entidad/evento:** Cuando un miembro se inscribe en una clase, Clases publica un mensaje en la cola `gimnasio.inscripciones`. Notificaciones lo consume y escribe en log.

| Dato | Valor |
|------|--------|
| **Endpoint que lo dispara** | **POST** `http://localhost:8082/clases/{claseId}/miembros` |
| **Roles / token** | Bearer token (ADMIN, TRAINER o MEMBER) |
| **Body** | `{ "miembroid_value": "1" }` (id del miembro a inscribir) |
| **Respuesta esperada** | **200** — JSON de la clase actualizada, con el miembro en `miembrosInscritos` |
| **Dónde ver el resultado** | Ventana del servicio **Notificaciones (8085)** — línea de log: *"Nueva inscripción recibida: miembro X en clase '...' (id: ...), horario: ..."* |

**Pasos:**

1. Asegúrate de tener al menos una clase (ej. id `1`) y un miembro con membresía activa (ej. id `1`). Si no, créalos con POST /clases y POST /miembros (con token).
2. **POST** `http://localhost:8082/clases/1/miembros` con headers `Authorization: Bearer <token>`, `Content-Type: application/json` y body `{ "miembroid_value": "1" }`.
3. Comprueba respuesta 200 y en la ventana de Notificaciones el log de nueva inscripción.

Si ves ese log, la cola `gimnasio.inscripciones` y el listener están bien.

---

## 2. Evento: Cambio de horario de clase

**Entidad/evento:** Al actualizar el horario de una clase, Clases publica un evento al exchange `gimnasio.eventos`; Notificaciones lo recibe en la cola `notificaciones.cambio-horario-clase` y lo registra en log.

| Dato | Valor |
|------|--------|
| **Endpoint que lo dispara** | **PUT** `http://localhost:8082/clases/{id}/horario` |
| **Roles / token** | Bearer token (ADMIN o TRAINER) |
| **Body** | `{ "horario": "2026-03-15T18:00:00" }` (nueva fecha/hora en ISO) |
| **Respuesta esperada** | **200** — JSON de la clase con el nuevo `horario` |
| **Dónde ver el resultado** | Ventana **Notificaciones (8085)** — log: *"Cambio de horario de clase: '...' (id: ...), de ... a 2026-03-15T18:00"* |

**Pasos:**

1. Ten al menos una clase (ej. id `1`).
2. **PUT** `http://localhost:8082/clases/1/horario` con headers `Authorization: Bearer <token>`, `Content-Type: application/json` y body `{ "horario": "2026-03-15T18:00:00" }`.
3. Comprueba 200 y el log de cambio de horario en Notificaciones.

Si ves ese log, el pub/sub de cambio de horario está bien.

---

## 3. Evento: Pago simulado y Dead Letter Queue (DLQ)

**Entidad/evento:** Se envía un mensaje de “pago” a la cola `gimnasio.pagos`. El consumidor en Notificaciones falla a propósito; el mensaje va a la cola DLQ `gimnasio.pagos.dlq` y otro listener lo recibe y lo registra (y opcionalmente se listan con GET).

| Dato | Valor |
|------|--------|
| **Endpoint que lo dispara** | **POST** `http://localhost:8085/notificaciones/simular-pago` |
| **Roles / token** | Público (no usa Keycloak) |
| **Body** | Opcional. Si no envías body, se usan valores por defecto. |
| **Respuesta esperada** | **200** — mensaje tipo "Mensaje de pago enviado a la cola..." |
| **Dónde ver el resultado** | Log Notificaciones (8085): primero *"[PAGOS] Procesando pago: miembro X - concepto - monto"* y un error (esperado); después *"[DLQ PAGOS] Mensaje de pago fallido recibido (para revisión manual): ..."*. Para listar en memoria: **GET** `http://localhost:8085/notificaciones/pagos-fallidos` → JSON con pagos fallidos. |

**Body POST /notificaciones/simular-pago** (opcional):

```json
{
  "miembroId": "1",
  "concepto": "membresía",
  "monto": 50.00
}
```

Si no envías body, se usan miembroId `"1"`, concepto `"membresía"`, monto `50.00`.

**Pasos:**

1. **POST** `http://localhost:8085/notificaciones/simular-pago` con header `Content-Type: application/json` y body opcional (o vacío).
2. Comprueba 200 y en la ventana de Notificaciones: log de procesamiento de pago, error del listener y luego log de DLQ.
3. **GET** `http://localhost:8085/notificaciones/pagos-fallidos` — debe devolver un JSON con el pago fallido (miembroId, concepto, monto, timestamps).

**En RabbitMQ (http://localhost:15672, guest/guest) → Queues:**  
- `gimnasio.pagos`: tras el POST puede haber 0 mensajes (o 1 unacked un instante); no debe haber tasa de deliver/get disparada (antes indicaba reencolado en bucle; corregido con `default-requeue-rejected=false`).  
- `gimnasio.pagos.dlq`: el listener de DLQ consume el mensaje; la cola puede quedar en 0.

---

## Resumen rápido (acciones)

| # | Prueba | Acción principal | Dónde mirar |
|---|--------|-------------------|-------------|
| 1 | Inscripción | POST /clases/1/miembros con body `{"miembroid_value":"1"}` (con token) | Log Notificaciones: "Nueva inscripción recibida" |
| 2 | Cambio de horario | PUT /clases/1/horario con body `{"horario":"2026-03-15T18:00:00"}` (con token) | Log Notificaciones: "Cambio de horario de clase" |
| 3 | DLQ pagos | POST /notificaciones/simular-pago (body opcional, sin token) | Log: "[PAGOS]" y "[DLQ PAGOS]"; GET /notificaciones/pagos-fallidos |

---

## Si algo falla

- **No aparece el log de inscripción (Prueba 1):** Comprueba que RabbitMQ esté en marcha (docker-compose, puerto 5672), que Clases y Notificaciones hayan arrancado sin errores y que la inscripción devuelva 200.
- **No aparece el log de cambio de horario (Prueba 2):** Comprueba que la clase exista y que el PUT devuelva 200. En RabbitMQ verifica la cola `notificaciones.cambio-horario-clase` y el exchange `gimnasio.eventos`.
- **No aparece el log de DLQ (Prueba 3):** Comprueba que el POST a `/notificaciones/simular-pago` devuelva 200 y que existan las colas `gimnasio.pagos` y `gimnasio.pagos.dlq`. Si `gimnasio.pagos` tiene deliver/get muy alto (bucle), reinicia Notificaciones; debe tener `spring.rabbitmq.listener.simple.default-requeue-rejected=false` en application.properties.
