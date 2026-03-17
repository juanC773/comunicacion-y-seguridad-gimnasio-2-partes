# Sistema de Gestión de Gimnasio — Microservicios

Sistema distribuido de gestión de un gimnasio: miembros, clases, entrenadores, equipos y notificaciones. Arquitectura de microservicios con **Spring Boot**, descubrimiento **Eureka**, seguridad **Keycloak (JWT)** y mensajería **RabbitMQ** y **Kafka**.

---

## Resumen del proyecto

- **Miembros** (8081): alta, consulta y actualización de socios; verificación de membresía activa para inscripción a clases.
- **Clases** (8082): programación de clases, inscripción de miembros (valida contra Miembros y Entrenadores vía REST).
- **Entrenadores** (8083): catálogo de entrenadores y especialidades.
- **Equipos** (8084): inventario de equipamiento.
- **Notificaciones** (8085): consumo de eventos vía RabbitMQ (inscripciones, cambio de horario, cola DLQ de pagos) y Kafka (ocupación de clases).

Cada servicio tiene su propia base H2 y se comunica por REST (con JWT propagado) o por colas AMQP. El servicio de Clases orquesta validaciones llamando a Entrenadores y Miembros; las notificaciones se publican de forma asíncrona.

**Integrantes:**
- Alejandro Amu (A00395686)
- David Henao (A00394033)
- Alejandro Torres (A00394983)
- Juan Calderón (A00395803)

---

## Documentación

| Documento | Contenido |
|-----------|------------|
| **[Guía de ejecución](docs/GUIA-EJECUCION.md)** | Requisitos, Docker (Keycloak + RabbitMQ), cómo levantar Eureka y microservicios, ver Eureka y Swagger. |
| **[Keycloak — importar realm y usuarios](docs/KEYCLOAK-EXPORT.md)** | Importar `realm-export-gimnasio.json`; crear usuarios admin1, entrenador1, miembro1 (password y roles). Opción manual. |
| **[Endpoints y autorización](docs/EndPoints-y-autorizacion.md)** | Todos los endpoints por servicio: método, URL, **roles necesarios**, **body** (JSON) y ejemplos. Incluye cómo obtener el token. Columnas Rabbit y Kafka para endpoints que publican eventos. |
| **[Pruebas RabbitMQ](docs/PRUEBAS-RABBITMQ.md)** | Orden y pasos para probar notificaciones por inscripción, cambio de horario y DLQ de pagos. |
| **[Pruebas Kafka](docs/PRUEBAS-KAFKA.md)** | Orden y pasos para probar el evento de ocupación de clase (topic `ocupacion-clases`, Clases → Notificaciones). |
| **[Newman (tests CLI)](postman/README-Newman.md)** | Colección Postman para ejecutar todas las pruebas desde terminal con Newman. |

---

## Arranque rápido

1. **Infraestructura:** `docker-compose up -d` (Keycloak en 8080, RabbitMQ en 5672/15672).
2. **Keycloak:** Importar realm y crear usuarios según [KEYCLOAK-EXPORT.md](docs/KEYCLOAK-EXPORT.md).
3. **Servicios:** `.\levantar-todo.ps1` (Eureka en 8761 y los 5 microservicios en 8081–8085).
4. **Probar:** Token y endpoints ([EndPoints-y-autorizacion.md](docs/EndPoints-y-autorizacion.md)); RabbitMQ en [PRUEBAS-RABBITMQ.md](docs/PRUEBAS-RABBITMQ.md); Kafka en [PRUEBAS-KAFKA.md](docs/PRUEBAS-KAFKA.md); suite automática en [README-Newman.md](postman/README-Newman.md).

Para detalles de requisitos, puertos, Eureka y Swagger, ver **[docs/GUIA-EJECUCION.md](docs/GUIA-EJECUCION.md)**.
