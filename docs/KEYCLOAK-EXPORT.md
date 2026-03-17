# Keycloak — Importar realm y usuarios

En Keycloak hay que **importar** el realm y **crear los usuarios de prueba** (el export no los incluye). Si prefieres no usar el export, más abajo se explica cómo configurar todo a mano.

---

## 1. Importar el realm

1. Keycloak levantado (p. ej. `docker-compose up -d`) en http://localhost:8080.
2. Consola de administración: usuario `admin`, contraseña `admin`.
3. Desplegable superior izquierdo: realm **master**.
4. Menú izquierdo: **Realm settings** → pestaña **Action** → **Import**.
5. Selecciona el archivo **`docs/realm-export-gimnasio.json`**.
6. Opciones: **If a realm exists** → Skip o Overwrite. **If a resource exists** → Skip o Overwrite. Pulsa **Import**.

El realm **gimnasio** quedará creado con clientes, roles y configuración. Los **usuarios no vienen en el export**, hay que crearlos a continuación.

---

## 2. Crear los usuarios de prueba

El archivo `realm-export-gimnasio.json` **no incluye usuarios**. Crea estos tres en el realm `gimnasio`:

| Usuario      | Contraseña | Rol a asignar |
|--------------|------------|----------------|
| `admin1`     | `password` | **ADMIN** (realm role `ROLE_ADMIN`) |
| `entrenador1`| `password` | **TRAINER** (realm role `ROLE_TRAINER`) |
| `miembro1`   | `password` | **MEMBER** (realm role `ROLE_MEMBER`) |

**Pasos por usuario:**

1. En el realm **gimnasio**: **Users** → **Add user**.
2. **Username:** el de la tabla (ej. `admin1`). **Create**.
3. Pestaña **Credentials**: **Set password** → contraseña `password`, desmarca *Temporary* si no quieres que pida cambio.
4. Pestaña **Role mapping**: **Assign role** → filtra por realm roles → asigna el rol correspondiente (`ROLE_ADMIN`, `ROLE_TRAINER` o `ROLE_MEMBER`).

Repite para `entrenador1` y `miembro1`. Con eso puedes obtener tokens y probar la API según [EndPoints-y-autorizacion.md](EndPoints-y-autorizacion.md).

**Client secret:** Al importar (o crear) el realm, Keycloak puede generar un **client secret** distinto para el cliente `clase-service`. Si usas Postman/Newman, actualiza la variable `client_secret` en **`postman/Gimnasio.postman_environment.json`** con el valor que veas en Keycloak (Clients → clase-service → Credentials). Ver [postman/README-Newman.md](../postman/README-Newman.md).

---

## 3. Qué trae el export (realm-export-gimnasio.json)

| Elemento | Contenido |
|---------|-----------|
| **Realm** | `gimnasio` (configuración, tiempos de sesión, etc.) |
| **Roles de realm** | `ROLE_ADMIN`, `ROLE_TRAINER`, `ROLE_MEMBER` y roles por defecto |
| **Clientes** | `clase-service`, `miembro-service`, `entrenador-service`, `equipo-service` (client authentication ON, Direct access grants). Los client secrets pueden variar al importar en otro Keycloak. |
| **Usuarios** | No incluidos; hay que crear `admin1`, `entrenador1`, `miembro1` como en la sección 2. |

---

## 4. Si quieres configurar todo a mano (sin importar)

Si no usas el JSON y creas el realm desde cero:

- **Realm:** nombre `gimnasio`.
- **Roles de realm:** `ROLE_ADMIN`, `ROLE_TRAINER`, `ROLE_MEMBER` (el backend puede mapear también `ADMIN`, `TRAINER`, `MEMBER`).
- **Clientes:** `clase-service`, `miembro-service`, `entrenador-service`, `equipo-service`. En cada uno:
  - **Client authentication:** ON.
  - **Direct access grants:** ON.
  - En **Credentials** copia el client secret; los microservicios lo usan en `application.properties` y Postman en el environment.
- **Usuarios:** `admin1`, `entrenador1`, `miembro1` con contraseña `password` y asignar a cada uno su rol en **Role mapping** (ADMIN, TRAINER, MEMBER respectivamente).

Para obtener token y probar: [EndPoints-y-autorizacion.md](EndPoints-y-autorizacion.md).
