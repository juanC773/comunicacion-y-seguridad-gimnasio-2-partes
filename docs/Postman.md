# Postman — Endpoints por microservicio

---

## Endpoints que participan en llamadas entre microservicios

| Dónde | Método | Endpoint | Qué hace |
|-------|--------|----------|----------|
| **Clases** | POST | `/clases` | **→ Llama a ENTRENADOR-SERVICE** (`/entrenadores/{id}/existe`) antes de guardar la clase. |
| **Clases** | POST | `/clases/{claseId}/miembros` | **→ Llama a MIEMBRO-SERVICE** (`/miembros/{id}/puede-asistir-clase`) antes de inscribir al miembro. |
| **Entrenadores** | GET | `/entrenadores/{id}/existe` | **← Llamado por CLASE-SERVICE** cuando se programa una clase. |
| **Miembros** | GET | `/miembros/{id}/puede-asistir-clase` | **← Llamado por CLASE-SERVICE** cuando se inscribe un miembro en una clase. |

En Postman puedes nombrar o etiquetar estos con algo como `[INTER-SERVICIO]` para identificarlos rápido.

---

## Base URLs (local):

| Servicio     | Base URL              | Puerto |
|-------------|------------------------|--------|
| Miembros    | http://localhost:8081  | 8081   |
| Clases      | http://localhost:8082  | 8082   |
| Entrenadores| http://localhost:8083  | 8083   |
| Equipos     | http://localhost:8084  | 8084   |

---

## Health check (comprobar que el servicio está arriba)

| Servicio     | Método | URL |
|-------------|--------|-----|
| Miembros    | GET    | http://localhost:8081/actuator/health |
| Clases      | GET    | http://localhost:8082/actuator/health |
| Entrenadores| GET    | http://localhost:8083/actuator/health |
| Equipos     | GET    | http://localhost:8084/actuator/health |

Respuesta esperada (200): `{"status":"UP"}`

---

## 1. Miembros (puerto 8081)

| Método | Endpoint | Descripción | Body (si aplica) |
|--------|----------|-------------|-------------------|
| GET    | /miembros | Listar todos los miembros | — |
| GET    | /miembros/{id} | Obtener miembro por id | — |
| GET    | **/miembros/{id}/puede-asistir-clase** | **🔗 ← Llamado por CLASE-SERVICE** — `{ "puedeAsistir": true/false, "razon": null \| "NO_EXISTE" \| "MEMBRESIA_INACTIVA" }` | — |
| POST   | /miembros | Registrar nuevo miembro | JSON abajo |
| PUT    | /miembros/{id}/email | Actualizar email | Body: `"nuevo@email.com"` (string JSON) |

**Ejemplo POST /miembros:**
```json
{
  "id": { "miembroid_value": "4" },
  "nombre": "Pedro García",
  "email": { "email_value": "pedro@email.com" },
  "fechaInscripcion": "2026-02-23",
  "membresiaActiva": true
}
```

---

## 2. Clases (puerto 8082)

| Método | Endpoint | Descripción | Body (si aplica) |
|--------|----------|-------------|-------------------|
| GET    | /clases | Listar todas las clases | — |
| GET    | /clases/{id} | Obtener clase por id (incluye miembrosInscritos) | — |
| POST   | **/clases** | **🔗 → Llama a ENTRENADOR-SERVICE** — Programar nueva clase (valida que exista el entrenador) | JSON abajo |
| POST   | **/clases/{claseId}/miembros** | **🔗 → Llama a MIEMBRO-SERVICE** — Inscribir miembro (valida que exista y membresía activa) | JSON abajo |

**Ejemplo POST /clases (happy path):**
```json
{
  "id": { "claseid_value": "3" },
  "nombre": "CrossFit Intensivo",
  "horario": "2026-03-01T10:00:00",
  "capacidadMaxima": 10,
  "entrenadorId": { "entrenadorid_value": "1" }
}
```
→ **200 OK** y la clase creada.

**Unhappy path — entrenador inexistente (para demostrar al profesor):**  
Mismo request pero con `"entrenadorId": { "entrenadorid_value": "100" }` (o cualquier id que no exista en Entrenadores).  
→ **404 Not Found** con body:
```json
{
  "error": "Entrenador no encontrado",
  "mensaje": "Entrenador no encontrado con id: 100"
}
```
Así se ve que el servicio de Clases validó contra ENTRENADOR-SERVICE y rechazó la petición de forma semántica (404), no un 500 genérico.

**Ejemplo POST /clases/{claseId}/miembros** (inscribir miembro 1 en clase 1):
- URL: `POST http://localhost:8082/clases/1/miembros`
- Body:
```json
{
  "miembroid_value": "1"
}
```
→ **200 OK** y la clase actualizada con el miembro en `miembrosInscritos`.

**Unhappy path — inscribir miembro (mensajes claros para demostrar):**  
- **No existe:** `"miembroid_value": "999"` → **404**  
  `{"error": "No se puede inscribir al miembro", "mensaje": "El miembro no existe."}`  
- **Membresía inactiva:** `"miembroid_value": "3"` (DataLoader tiene id 3 con membresía false) → **404**  
  `{"error": "No se puede inscribir al miembro", "mensaje": "No se puede inscribir: la membresía está inactiva."}`  
- **Ya inscrito:** inscribir el mismo miembro dos veces (ej. dos POST con `"miembroid_value": "1"` a la misma clase) → **409**  
  `{"error": "Miembro ya inscrito", "mensaje": "El miembro ya está inscrito en esta clase."}`

---

## 3. Entrenadores (puerto 8083)

| Método | Endpoint | Descripción | Body (si aplica) |
|--------|----------|-------------|-------------------|
| GET    | /entrenadores | Listar todos los entrenadores | — |
| GET    | /entrenadores/{id} | Obtener entrenador por id | — |
| GET    | **/entrenadores/{id}/existe** | **🔗 ← Llamado por CLASE-SERVICE** — ¿Existe el entrenador? (true/false) | — |
| POST   | /entrenadores | Agregar nuevo entrenador | JSON abajo |
| PUT    | /entrenadores/{id}/especialidad | Actualizar especialidad | Body: `"Funcional"` (string JSON) |

**Ejemplo POST /entrenadores:**
```json
{
  "id": { "entrenadorid_value": "3" },
  "nombre": "Luis Torres",
  "especialidad": { "especialidad_value": "CrossFit" }
}
```

---

## 4. Equipos (puerto 8084)

| Método | Endpoint | Descripción | Body (si aplica) |
|--------|----------|-------------|-------------------|
| GET    | /equipos | Listar todos los equipos | — |
| GET    | /equipos/{id} | Obtener equipo por id | — |
| POST   | /equipos | Agregar nuevo equipo | JSON abajo |
| PUT    | /equipos/{id}/cantidad | Actualizar cantidad de stock | Body: número, ej. `25` |

**Ejemplo POST /equipos:**
```json
{
  "id": { "equipoid_value": "3" },
  "nombre": "Cuerda para saltar",
  "descripcion": "Cuerda de velocidad profesional",
  "cantidad": 30
}
```

---

## Orden sugerido para probar en Postman

1. **Health:** GET a cada `/actuator/health` (8081, 8082, 8083, 8084).
2. **Entrenadores:** GET /entrenadores, GET /entrenadores/1, GET /entrenadores/1/existe, POST (crear), PUT /entrenadores/3/especialidad.
3. **Miembros:** GET /miembros, GET /miembros/1, POST (crear), PUT /miembros/3/email.
4. **Equipos:** GET /equipos, GET /equipos/1, POST (crear), PUT /equipos/1/cantidad.
5. **Clases:** GET /clases, GET /clases/1, POST (crear con entrenadorId 1). POST /clases/1/miembros con body `{"miembroid_value": "1"}` para inscribir miembro. Probar con miembro 999 o con miembro 3 (membresía inactiva) para ver error. Probar también POST clase con entrenadorId 999 para ver error de entrenador no encontrado.
