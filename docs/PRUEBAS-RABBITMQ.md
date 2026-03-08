# Pruebas RabbitMQ — Orden y pasos

Guía para comprobar que la comunicación asíncrona con RabbitMQ funciona correctamente: notificaciones por inscripción a clase, pub/sub por cambio de horario y Dead Letter Queue (DLQ) de pagos. Para levantar infraestructura (Docker, Eureka, microservicios) ver [GUIA-EJECUCION.md](GUIA-EJECUCION.md).

---

## Antes de empezar

### 1. Levantar infraestructura

```powershell
# En la raíz del proyecto (donde está docker-compose.yml)
docker-compose up -d
```

- **Keycloak:** http://localhost:8080 (necesario para los endpoints que piden JWT).
- **RabbitMQ (consola web):** http://localhost:15672  
  - **Usuario:** `guest`  
  - **Contraseña:** `guest`  
  Inicia sesión ahí para revisar colas en la pestaña *Queues*.

Espera ~30 segundos a que Keycloak arranque.

### 2. Levantar Eureka y microservicios

```powershell
.\levantar-todo.ps1
```

Se abren 6 ventanas (Eureka, Miembros 8081, Clases 8082, Entrenadores 8083, Equipos 8084, Notificaciones 8085). Espera ~1 minuto.

### 3. Tener un token JWT (para Clases y Miembros)

Si aún no tienes usuarios en Keycloak, créalos en el realm `gimnasio` y asígnales roles. Luego obtén un token (ej. con usuario que tenga ROLE_TRAINER o ROLE_ADMIN):

```powershell
$resp = Invoke-RestMethod -Uri 'http://localhost:8080/realms/gimnasio/protocol/openid-connect/token' -Method Post -Body @{
  grant_type    = 'password'
  client_id     = 'clase-service'
  client_secret = '4caMaKDTbeQsvko8BDSvrqdnFnOSYjnt'
  username      = 'TU_USUARIO'
  password      = 'TU_PASSWORD'
} -ContentType 'application/x-www-form-urlencoded'
$token = $resp.access_token
```

En Postman: header `Authorization: Bearer <token>`.

---

## Orden de las pruebas

Sigue este orden para no depender de datos que aún no existan.

---

### Prueba 1 — Notificaciones por nueva inscripción (la que se corrigió — antes daba 500)

**Qué se prueba:** Al inscribir un miembro en una clase, el servicio Clases publica un mensaje a la cola `gimnasio.inscripciones` y el servicio Notificaciones lo consume y lo registra en log.

**Qué hacer:**

1. Asegúrate de tener al menos una clase y un miembro con membresía activa (por ejemplo clase id `1`, miembro id `1`). Si no, créalos antes con POST /clases y POST /miembros (con token).
2. Inscribir el miembro en la clase:
   - **POST** `http://localhost:8082/clases/1/miembros`
   - Headers: `Authorization: Bearer <tu_token>`, `Content-Type: application/json`
   - Body (raw JSON): `{ "miembroid_value": "1" }`

**Qué debe pasar:**

- Respuesta **200** con la clase actualizada (el miembro aparece en `miembrosInscritos`).
- En la **ventana del servicio Notificaciones (8085)** debe salir una línea de log parecida a:
  ```text
  [NOTIFICACIÓN] Nueva inscripción recibida: miembro 1 en clase '...' (id: 1), horario: ...
  ```

Si ves ese log, la cola `gimnasio.inscripciones` y el listener están bien.

---

### Prueba 2 — Pub/sub: cambio de horario de clase

**Qué se prueba:** Al actualizar el horario de una clase, Clases publica un evento al exchange `gimnasio.eventos` y Notificaciones lo recibe en la cola `notificaciones.cambio-horario-clase`.

**Qué hacer:**

1. Tener al menos una clase (por ejemplo id `1`).
2. Actualizar el horario:
   - **PUT** `http://localhost:8082/clases/1/horario`
   - Headers: `Authorization: Bearer <tu_token>`, `Content-Type: application/json`
   - Body (raw JSON): `{ "horario": "2026-03-15T18:00:00" }`

**Qué debe pasar:**

- Respuesta **200** con la clase y el nuevo horario.
- En la **ventana de Notificaciones (8085)** debe aparecer algo como:
  ```text
  [NOTIFICACIÓN] Cambio de horario de clase: '...' (id: 1), de ... a 2026-03-15T18:00
  ```

Si ves ese log, el pub/sub de cambio de horario está bien.

---

### Prueba 3 — Dead Letter Queue (DLQ) de pagos (la que no estaba hecha — se implementó nueva)

**Qué se prueba:** Un mensaje de “pago” se envía a la cola `gimnasio.pagos`. El consumidor falla a propósito; el mensaje va a la cola DLQ `gimnasio.pagos.dlq` y otro listener lo recibe y lo registra.

**Qué hacer:**

1. **POST** `http://localhost:8085/notificaciones/simular-pago`  
   - No hace falta token (el servicio de notificaciones no usa Keycloak).  
   - **Headers:** `Content-Type: application/json`  
   - **Body (raw JSON):**

   ```json
   {
     "miembroId": "1",
     "concepto": "membresía",
     "monto": 50.00
   }
   ```

   Si no envías body, se usan valores por defecto (miembroId "1", concepto "membresía", monto 50.00).

**Qué debe pasar:**

- Respuesta **200** con un mensaje tipo: “Mensaje de pago enviado a la cola...”.
- En la **ventana de Notificaciones (8085)**:
  1. Primero algo como: `[PAGOS] Procesando pago: miembro 1 - membresía - 50.00` y a continuación un error (el listener lanza excepción a propósito).
  2. Después: `[DLQ PAGOS] Mensaje de pago fallido recibido (para revisión manual): miembro 1 - membresía - 50.00 - ...`

Si ves ambos logs (procesamiento que falla y luego consumo desde la DLQ), la DLQ está bien.

**Ver los pagos fallidos guardados:** Los mensajes que llegan a la DLQ se guardan en memoria. Para listarlos: **GET** `http://localhost:8085/notificaciones/pagos-fallidos`. Devuelve un JSON con cada pago (miembroId, concepto, monto, timestamp del pago y cuándo llegó a la DLQ). Así puedes explicar que la DLQ no solo “dice que hubo un error”, sino que conserva el mensaje para revisión o reproceso.

**Qué ver en la consola (Notificaciones 8085):**  
1. Una línea `[PAGOS] Procesando pago: miembro 1 - membresía - 50.00`.  
2. Un error/warn del listener (es esperado: falla a propósito).  
3. Una línea `[DLQ PAGOS] Mensaje de pago fallido recibido (para revisión manual): ...`.  
No debe repetirse sin parar: si ves el mismo log cientos de veces por segundo, era un bucle (ya corregido con `default-requeue-rejected=false`).

**Qué ver en RabbitMQ (http://localhost:15672, usuario `guest` / `guest`) → pestaña *Queues*:**  
- **gimnasio.pagos:** tras el POST, puede haber 0 mensajes (o 1 unacked un instante); no debe tener “deliver/get” disparado (ej. 637/s). Si eso pasaba, el mensaje se reencolaba en bucle; con la corrección el mensaje va a la DLQ y no vuelve.  
- **gimnasio.pagos.dlq:** puede verse 0 o 1 mensaje un momento; el listener de DLQ lo consume y deja la cola en 0.  
Comprobar que ambas colas existan y que no quede un mensaje atascado en `gimnasio.pagos` con tasa de entrega muy alta.

---

## Resumen rápido

| # | Prueba              | Nota | Acción principal                                      | Dónde mirar el resultado                    |
|---|---------------------|------|--------------------------------------------------------|---------------------------------------------|
| 1 | Inscripción         | (corregida — antes 500) | POST /clases/1/miembros con body `{"miembroid_value":"1"}` | Log de Notificaciones: “Nueva inscripción recibida” |
| 2 | Cambio de horario   | — | PUT /clases/1/horario con body `{"horario":"2026-03-15T18:00:00"}` | Log de Notificaciones: “Cambio de horario de clase” |
| 3 | DLQ pagos           | (nueva — no estaba hecha) | POST /notificaciones/simular-pago con body `{"miembroId":"1","concepto":"membresía","monto":50.00}` | Log de Notificaciones: “[PAGOS]” y “[DLQ PAGOS]” |

---

## Si algo falla

- **No aparece el log de inscripción (Prueba 1):** Comprueba que RabbitMQ esté en marcha (docker-compose, puerto 5672) y que Clases y Notificaciones hayan arrancado sin errores. Revisa que la inscripción devuelva 200.
- **No aparece el log de cambio de horario (Prueba 2):** Comprueba que la clase exista y que el PUT devuelva 200. Revisa en RabbitMQ que exista la cola `notificaciones.cambio-horario-clase` y el exchange `gimnasio.eventos`.
- **No aparece el log de DLQ (Prueba 3):** Comprueba que el POST a `/notificaciones/simular-pago` devuelva 200. Revisa en RabbitMQ las colas `gimnasio.pagos` y `gimnasio.pagos.dlq`. Si `gimnasio.pagos` tiene “deliver/get” muy alto (bucle): reinicia el servicio Notificaciones (debe tener `spring.rabbitmq.listener.simple.default-requeue-rejected=false` en application.properties).
