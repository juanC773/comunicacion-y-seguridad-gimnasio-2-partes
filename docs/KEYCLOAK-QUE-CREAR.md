# Keycloak — Configuración del realm

**Recomendado:** importar el archivo `docs/realm-export-gimnasio.json` en Keycloak. Ver [KEYCLOAK-EXPORT.md](KEYCLOAK-EXPORT.md) para los pasos y qué contiene ese JSON.

A continuación se describe qué incluye ese export (realm, clientes, roles, usuarios), por si necesitas consultarlo o configurar Keycloak a mano en algún caso puntual.

---

## Qué tiene el realm `gimnasio`

- **Realm:** `gimnasio`
- **Roles de realm:** `ROLE_ADMIN`, `ROLE_TRAINER`, `ROLE_MEMBER`
- **Clientes:** `clase-service`, `miembro-service`, `entrenador-service`, `equipo-service` (cada uno confidential, con Direct access grants)
- **Usuarios:** cuentas de servicio de los clientes y usuarios de prueba `admin1`, `entrenador1`, `miembro1` (contraseña de los tres: `password`)

---

## Si en algún momento configuraras a mano

- **Realm:** nombre `gimnasio`.
- **Clientes:** los cuatro anteriores con Client authentication ON y Direct access grants habilitado; los secrets están en los `application.properties` de cada microservicio y en el environment de Postman.
- **Roles de realm:** `ROLE_ADMIN`, `ROLE_TRAINER`, `ROLE_MEMBER` (el backend puede mapear también `ADMIN`, `TRAINER`, `MEMBER` según la configuración de Spring).
- **Usuarios de prueba (opcional):** por ejemplo `admin1`, `entrenador1`, `miembro1` con contraseña `password` y asignar a cada uno el rol correspondiente en Role mapping.

Para obtener token y probar la API: [EndPoints-y-autorizacion.md](EndPoints-y-autorizacion.md).
