# Microservicio de Clases

## Bounded Context: Gestión de Clases

Este microservicio es el propietario exclusivo del dominio de **clases del gimnasio**. Gestiona la programación y consulta de clases, y es el único servicio del sistema que realiza **comunicación entre microservicios**: valida la existencia del entrenador asignado antes de persistir una nueva clase.

## Agregado: Clase

```
Aggregate Root: Clase
│
├── ClaseId (Value Object)
│     └── claseid_value: String
│
├── EntrenadorId (Value Object — referencia externa)
│     └── entrenadorid_value: String
│         IMPORTANTE: Clase no embebe el objeto Entrenador completo.
│         Solo guarda su ID como referencia. Esto mantiene los bounded
│         contexts desacoplados: si el microservicio de Entrenadores cambia
│         su modelo interno, Clases no se ve afectado.
│
├── nombre: String              — Nombre de la clase (ej: "Yoga Matutino")
├── horario: LocalDateTime      — Fecha y hora programada
└── capacidadMaxima: int        — Cupo máximo de la clase

Comportamiento encapsulado en el agregado:
  - actualizarHorario(LocalDateTime nuevoHorario)
  - actualizarCapacidad(int nuevaCapacidad)
```

## Comunicación inter-servicios

Al programar una nueva clase, este servicio **consulta al servicio de Entrenadores** para verificar que el entrenador asignado existe. La comunicación se realiza mediante `RestTemplate` con balanceo de carga via Eureka:

```
POST /clases  →  ClaseService.programarClase()
                      │
                      ├── GET http://ENTRENADOR-SERVICE/entrenadores/{id}/existe
                      │         (resuelto por Eureka → localhost:8083)
                      │
                      ├── entrenador no existe → EntrenadorNoEncontradoException
                      └── entrenador existe    → claseRepository.save(clase)
```

El uso de `http://ENTRENADOR-SERVICE/...` (nombre lógico) en lugar de `http://localhost:8083/...` es fundamental: permite que el sistema funcione aunque el entrenador-service cambie de puerto o tenga múltiples instancias.

## Estructura del proyecto

```
src/main/java/co/analisys/gimnasio/
├── ClaseServiceApplication.java      — Define el bean @LoadBalanced RestTemplate
├── DataLoader.java                   — Precarga: Yoga Matutino (id=1), Spinning Vespertino (id=2)
├── controller/
│   └── ClaseController.java
├── model/
│   ├── Clase.java                    — @Entity, @EmbeddedId, @Embedded EntrenadorId
│   ├── ClaseId.java                  — @Embeddable (value object)
│   └── EntrenadorId.java             — @Embeddable (referencia externa al bounded context)
├── repository/
│   └── ClaseRepository.java          — JpaRepository<Clase, ClaseId>
├── service/
│   └── ClaseService.java             — Lógica de negocio + llamada REST a Entrenadores
└── exception/
    └── EntrenadorNoEncontradoException.java
```

## API REST

| Método | Ruta           | Descripción                                                   |
|--------|----------------|---------------------------------------------------------------|
| GET    | `/clases`      | Listar todas las clases                                       |
| GET    | `/clases/{id}` | Obtener clase por id                                          |
| POST   | `/clases`      | Programar clase (valida entrenador via REST antes de guardar) |

**Ejemplo de body para POST `/clases`:**
```json
{
  "id": {"claseid_value": "3"},
  "nombre": "CrossFit Intensivo",
  "horario": "2026-03-01T10:00:00",
  "capacidadMaxima": 10,
  "entrenadorId": {"entrenadorid_value": "1"}
}
```

## Configuración

```properties
spring.application.name=clase-service
server.port=8082
spring.datasource.url=jdbc:h2:mem:clasesdb
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
```

## Ejecución

```bash
bash mvnw spring-boot:run
```

> **Dependencias de arranque:** requiere que `servidor-descubrimiento-mcrs` y `microservicio-entrenadores-mcrs` estén corriendo antes de procesar requests que creen clases nuevas.
