# Microservicio de Miembros

## Bounded Context: Gestión de Miembros

Este microservicio es el propietario exclusivo del dominio de **miembros del gimnasio**. Gestiona el ciclo de vida completo de un miembro: su registro, consulta y actualización de datos de contacto.

Ningún otro microservicio accede directamente a la base de datos de miembros ni embebe objetos `Miembro` en sus propios agregados. Si otro servicio necesitara datos de un miembro, lo haría a través de la API REST expuesta aquí.

## Agregado: Miembro

Aplicando Domain-Driven Design, el dominio se organiza en torno al agregado `Miembro`:

```
Aggregate Root: Miembro
│
├── MiembroId (Value Object)
│     └── miembroid_value: String
│         Encapsula la identidad del miembro como un concepto del dominio,
│         en lugar de usar un Long genérico generado por la BD.
│
├── Email (Value Object)
│     └── email_value: String
│         El email no es un String cualquiera; es un concepto del dominio
│         con semántica propia. Encapsularlo permite agregar validaciones
│         en el futuro sin cambiar la firma de los métodos del agregado.
│
└── fechaInscripcion: LocalDate
      Atributo de dominio que registra cuándo ingresó el miembro.

Comportamiento encapsulado en el agregado:
  - actualizarEmail(Email nuevoEmail)
```

## Estructura del proyecto

```
src/main/java/co/analisys/gimnasio/
├── MiembroServiceApplication.java
├── DataLoader.java                    — Precarga: Juan Pérez (id=1), María López (id=2)
├── controller/
│   └── MiembroController.java
├── model/
│   ├── Miembro.java                   — @Entity, @EmbeddedId
│   ├── MiembroId.java                 — @Embeddable (value object)
│   └── Email.java                     — @Embeddable (value object)
├── repository/
│   └── MiembroRepository.java         — JpaRepository<Miembro, MiembroId>
└── service/
    └── MiembroService.java
```

## API REST

| Método | Ruta                    | Descripción                        |
|--------|-------------------------|------------------------------------|
| GET    | `/miembros`             | Listar todos los miembros          |
| GET    | `/miembros/{id}`        | Obtener miembro por id             |
| POST   | `/miembros`             | Registrar nuevo miembro            |
| PUT    | `/miembros/{id}/email`  | Actualizar email del miembro       |

**Ejemplo de body para POST `/miembros`:**
```json
{
  "id": {"miembroid_value": "3"},
  "nombre": "Pedro García",
  "email": {"email_value": "pedro@email.com"},
  "fechaInscripcion": "2026-02-23"
}
```

## Configuración

```properties
spring.application.name=miembro-service
server.port=8081
spring.datasource.url=jdbc:h2:mem:miembrosdb
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
```

## Ejecución

```bash
bash mvnw spring-boot:run
```

> Requiere que el servidor Eureka (`servidor-descubrimiento-mcrs`) esté corriendo en el puerto `8761`.
