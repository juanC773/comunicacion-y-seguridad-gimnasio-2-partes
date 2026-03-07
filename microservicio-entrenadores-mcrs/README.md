# Microservicio de Entrenadores

## Bounded Context: Gestión de Entrenadores

Este microservicio es el propietario exclusivo del dominio de **entrenadores del gimnasio**. Gestiona el catálogo de instructores, sus especialidades y expone un endpoint de verificación de existencia que es consumido por el servicio de Clases.

## Agregado: Entrenador

```
Aggregate Root: Entrenador
│
├── EntrenadorId (Value Object)
│     └── entrenadorid_value: String
│         Identidad explícita del dominio. Otros servicios que necesiten
│         referenciar a un entrenador guardan un EntrenadorId, nunca
│         el objeto Entrenador completo.
│
└── Especialidad (Value Object)
      └── especialidad_value: String
          La especialidad de un entrenador es un concepto del dominio, no
          un String plano. Encapsularla permite enriquecer el modelo
          (validaciones, comparaciones) sin cambiar la API.

Comportamiento encapsulado en el agregado:
  - actualizarEspecialidad(Especialidad nueva)
```

## Rol en la comunicación inter-servicios

El endpoint `GET /entrenadores/{id}/existe` es consumido por el **Servicio de Clases** al momento de programar una nueva clase. Esto permite que el servicio de Clases valide que el entrenador referenciado existe, sin necesidad de acceder directamente a la base de datos de entrenadores.

```
[clase-service]  →  GET /entrenadores/{id}/existe  →  [entrenador-service]
                                                            ↓
                                                     true / false
```

## Estructura del proyecto

```
src/main/java/co/analisys/gimnasio/
├── EntrenadorServiceApplication.java
├── DataLoader.java               — Precarga: Carlos Rodríguez (id=1), Ana Martínez (id=2)
├── controller/
│   └── EntrenadorController.java
├── model/
│   ├── Entrenador.java           — @Entity, @EmbeddedId
│   ├── EntrenadorId.java         — @Embeddable (value object)
│   └── Especialidad.java         — @Embeddable (value object)
├── repository/
│   └── EntrenadorRepository.java — JpaRepository<Entrenador, EntrenadorId>
└── service/
    └── EntrenadorService.java
```

## API REST

| Método | Ruta                              | Descripción                                     |
|--------|-----------------------------------|-------------------------------------------------|
| GET    | `/entrenadores`                   | Listar todos los entrenadores                   |
| GET    | `/entrenadores/{id}`              | Obtener entrenador por id                       |
| GET    | `/entrenadores/{id}/existe`       | Verificar existencia (consumido por Clases)     |
| POST   | `/entrenadores`                   | Agregar nuevo entrenador                        |
| PUT    | `/entrenadores/{id}/especialidad` | Actualizar especialidad del entrenador          |

**Ejemplo de body para POST `/entrenadores`:**
```json
{
  "id": {"entrenadorid_value": "3"},
  "nombre": "Luis Torres",
  "especialidad": {"especialidad_value": "CrossFit"}
}
```

## Configuración

```properties
spring.application.name=entrenador-service
server.port=8083
spring.datasource.url=jdbc:h2:mem:entrenadoresdb
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
```

## Ejecución

```bash
bash mvnw spring-boot:run
```

> Requiere que el servidor Eureka esté corriendo. Se recomienda arrancar este servicio antes que `microservicio-clases-mcrs`.
