# Microservicio de Equipos

## Bounded Context: Gestión de Equipos

Este microservicio es el propietario exclusivo del dominio de **equipamiento del gimnasio**. Gestiona el inventario de equipos disponibles: su registro, consulta y actualización de stock.

Este servicio es completamente autónomo. No depende de ningún otro microservicio del sistema y ningún otro servicio lo consulta actualmente.

## Agregado: Equipo

```
Aggregate Root: Equipo
│
├── EquipoId (Value Object)
│     └── equipoid_value: String
│         Identidad explícita del dominio, independiente de la estrategia
│         de generación de IDs de la base de datos.
│
├── nombre: String          — Nombre del equipo (ej: "Mancuernas")
├── descripcion: String     — Detalle del equipo
└── cantidad: int           — Unidades disponibles en inventario

Comportamiento encapsulado en el agregado:
  - actualizarCantidad(int nuevaCantidad)
  - actualizarDescripcion(String nuevaDescripcion)
```

## Estructura del proyecto

```
src/main/java/co/analisys/gimnasio/
├── EquipoServiceApplication.java
├── DataLoader.java               — Precarga: Mancuernas (id=1), Bicicleta estática (id=2)
├── controller/
│   └── EquipoController.java
├── model/
│   ├── Equipo.java               — @Entity, @EmbeddedId
│   └── EquipoId.java             — @Embeddable (value object)
├── repository/
│   └── EquipoRepository.java     — JpaRepository<Equipo, EquipoId>
└── service/
    └── EquipoService.java
```

## API REST

| Método | Ruta                       | Descripción                        |
|--------|----------------------------|------------------------------------|
| GET    | `/equipos`                 | Listar todos los equipos           |
| GET    | `/equipos/{id}`            | Obtener equipo por id              |
| POST   | `/equipos`                 | Agregar nuevo equipo               |
| PUT    | `/equipos/{id}/cantidad`   | Actualizar cantidad de stock       |

**Ejemplo de body para POST `/equipos`:**
```json
{
  "id": {"equipoid_value": "3"},
  "nombre": "Cuerda para saltar",
  "descripcion": "Cuerda de velocidad profesional",
  "cantidad": 30
}
```

## Configuración

```properties
spring.application.name=equipo-service
server.port=8084
spring.datasource.url=jdbc:h2:mem:equiposdb
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
```

## Ejecución

```bash
bash mvnw spring-boot:run
```

> Requiere que el servidor Eureka esté corriendo en el puerto `8761`.
