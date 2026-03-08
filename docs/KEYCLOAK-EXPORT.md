# Keycloak — Importar el realm del gimnasio

La configuración de Keycloak para este proyecto se hace **importando** el archivo de export del realm. No es necesario crear nada a mano.

---

## Archivo a importar

- **`docs/realm-export-gimnasio.json`**

---

## Qué contiene ese JSON

El archivo es un export del realm **gimnasio** e incluye:

| Elemento | Contenido |
|----------|------------|
| **Realm** | `gimnasio` (configuración del realm, tiempos de sesión, etc.) |
| **Roles de realm** | `ROLE_ADMIN`, `ROLE_TRAINER`, `ROLE_MEMBER` y roles por defecto (`default-roles-gimnasio`, `offline_access`, `uma_authorization`) |
| **Clientes** | `clase-service`, `miembro-service`, `entrenador-service`, `equipo-service` (cada uno con client secret, Direct access grants habilitado para obtener token con usuario/contraseña), más los clientes estándar de Keycloak (`account`, `account-console`, `admin-cli`, `broker`, `realm-management`, `security-admin-console`) |
| **Usuarios** | Cuentas de servicio de cada cliente y usuarios de prueba `admin1`, `entrenador1`, `miembro1` (todos con contraseña `password`) |

Los **client secrets** de los cuatro servicios están en el export; los microservicios y el environment de Postman/Newman ya están configurados con los mismos valores en sus `application.properties` y en las variables de entorno.

---

## Cómo importar en Keycloak

1. **Keycloak** debe estar levantado (por ejemplo con `docker-compose up -d`) y accesible en http://localhost:8080.
2. Inicia sesión en la **consola de administración** (usuario `admin`, contraseña `admin`).
3. Desplegable superior izquierdo: elige el realm **master**.
4. Menú izquierdo: **Realm settings**.
5. Pestaña **Action** (arriba a la derecha) → **Import**.
6. **Browse** y selecciona el fichero **`docs/realm-export-gimnasio.json`**.
7. Revisa las opciones de importación:
   - **If a realm exists:** **Skip** o **Overwrite** según quieras conservar o reemplazar un realm `gimnasio` existente.
   - **If a resource exists:** **Skip** o **Overwrite** según prefieras.
8. Pulsa **Import**.

Tras la importación, el realm **gimnasio** aparecerá en el desplegable de realms. Entra en él y comprueba en **Clients** y **Realm roles** que están los clientes y roles anteriores. A partir de ahí puedes obtener un token (por ejemplo con el cliente `clase-service`) y probar la API tal como se indica en [EndPoints-y-autorizacion.md](EndPoints-y-autorizacion.md).
