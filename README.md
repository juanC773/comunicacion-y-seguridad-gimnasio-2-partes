# Análisis: De Monolito a Microservicios — Sistema de Gestión de Gimnasio

# Integrantes:
- Alejandro Amu (A00395686)
- David Henao (A00394033)
- Alejandro Torres (A00394983)
- Juan Calderón (A00395803)

## 1. Sistema Monolítico Original

El monolito `monilito-gimnasio-mcrs` es una aplicación Spring Boot única que gestiona cuatro dominios de negocio dentro de un mismo despliegue, una misma base de datos y un mismo proceso JVM:

| Entidad     | Atributos principales                                      |
|-------------|-----------------------------------------------------------|
| `Miembro`   | id, nombre, email, fechaInscripcion, membresiaActiva      |
| `Entrenador`| id, nombre, especialidad                                  |
| `Clase`     | id, nombre, horario, capacidadMaxima, entrenador (FK), miembrosInscritos (IDs) |
| `Equipo`    | id, nombre, descripcion, cantidad                         |

Un único `GimnasioService` expone ocho métodos (cuatro `save`, cuatro `findAll`) y un único `GimnasioController` centraliza todos los endpoints bajo `/api/gimnasio`. La relación entre `Clase` y `Entrenador` es un `@ManyToOne` directo, lo que acopla ambos dominios en la misma capa de persistencia.

### Limitaciones del modelo monolítico

- **Acoplamiento fuerte:** un cambio en `Entrenador` obliga a recompilar y redesplegar toda la aplicación.
- **Escalado todo-o-nada:** si la gestión de clases recibe alta demanda, no es posible escalar solo ese módulo.
- **Base de datos compartida:** todas las entidades compiten por la misma conexión; un problema en una tabla puede afectar las demás.
- **Despliegue riesgoso:** cualquier feature pequeño requiere parar y volver a levantar el sistema completo.

---

## 2. Identificación de Contextos Acotados

Aplicando Domain-Driven Design (DDD), se identifican cuatro **Bounded Contexts** naturales dentro del dominio del gimnasio. Cada uno encapsula un conjunto cohesivo de responsabilidades que pertenece a un único equipo conceptual:

```
┌─────────────────────────────────────────────────────────────────┐
│                    Dominio del Gimnasio                         │
│                                                                 │
│  ┌──────────────────┐      ┌───────────────────────────────┐   │
│  │   Gestión de     │      │      Gestión de Clases        │   │
│  │    Miembros      │      │                               │   │
│  │                  │      │  (referencia EntrenadorId,    │   │
│  │  Ciclo de vida   │      │   sin acoplar el objeto)      │   │
│  │  del miembro:    │      │                               │   │
│  │  inscripción,    │      └───────────────────────────────┘   │
│  │  actualización   │                                           │
│  └──────────────────┘      ┌───────────────────────────────┐   │
│                             │    Gestión de Entrenadores    │   │
│  ┌──────────────────┐      │                               │   │
│  │   Gestión de     │      │  Perfil del entrenador,       │   │
│  │    Equipos       │      │  especialidad, disponibilidad │   │
│  │                  │      │                               │   │
│  │  Inventario de   │      └───────────────────────────────┘   │
│  │  equipamiento    │                                           │
│  └──────────────────┘                                           │
└─────────────────────────────────────────────────────────────────┘
```

### ¿Por qué no hace falta un servicio orquestador tipo "Circulación"?

En el sistema de biblioteca, el servicio de **Circulación** era necesario porque el acto de prestar un libro involucraba tres dominios simultáneamente: consultar disponibilidad del libro (Catálogo), crear el préstamo (Circulación) y notificar al usuario (Notificaciones). Era una transacción de negocio que cruzaba múltiples contextos.

En el gimnasio, no existe ninguna operación equivalente. Los cruces entre dominios son: `Clase` valida al `Entrenador` (existencia) y al `Miembro` (existencia y membresía activa) mediante llamadas REST; cada validación es simple y no orquesta varios servicios a la vez. No hay flujo de negocio complejo que justifique un quinto servicio orquestador.

---

## 3. Definición de Microservicios

Cada bounded context se materializa en un microservicio independiente:

### Servicio de Miembros (`miembro-service`, puerto 8081)

**Responsabilidad:** Gestionar el ciclo de vida de los miembros del gimnasio.

- Registro de nuevos miembros
- Consulta de miembros existentes
- Actualización de datos de contacto (email)
- Atributo `membresiaActiva` (boolean) para indicar si puede asistir a clases
- Verificación de si un miembro puede asistir a clase: endpoint `GET /miembros/{id}/puede-asistir-clase` devuelve un DTO `{ puedeAsistir, razon }` con `razon` en `null` (puede), `"NO_EXISTE"` o `"MEMBRESIA_INACTIVA"` (consumido por el servicio de Clases para mensajes específicos)

**Base de datos propia:** `miembrosdb` — ningún otro servicio accede a ella.

---

### Servicio de Clases (`clase-service`, puerto 8082)

**Responsabilidad:** Programar y consultar clases del gimnasio.

- Programación de nuevas clases
- Consulta del calendario de clases
- Inscripción de miembros a una clase (por ID), validando que el miembro exista y tenga membresía activa (comunicación con `MIEMBRO-SERVICE`)
- Validación de que el entrenador asignado existe (comunicación con `ENTRENADOR-SERVICE`)

**Base de datos propia:** `clasesdb`.

**Comunicación saliente:**
- Al crear una clase: `GET http://ENTRENADOR-SERVICE/entrenadores/{id}/existe`. Si no existe, lanza `EntrenadorNoEncontradoException`.
- Al inscribir un miembro en una clase: se comprueba primero si ya está inscrito → `MiembroYaInscritoException` (409). Luego `GET http://MIEMBRO-SERVICE/miembros/{id}/puede-asistir-clase`, que devuelve `{ puedeAsistir, razon }`. Si no puede, lanza `MiembroNoPuedeAsistirException` con mensaje específico según `razon`: "El miembro no existe." o "No se puede inscribir: la membresía está inactiva." Se respeta la capacidad máxima de la clase (409).
Utiliza `@LoadBalanced RestTemplate` para resolver los nombres de servicio vía Eureka.

---

### Servicio de Entrenadores (`entrenador-service`, puerto 8083)

**Responsabilidad:** Gestionar el catálogo de entrenadores y sus especialidades.

- Alta de nuevos entrenadores
- Consulta de entrenadores disponibles
- Actualización de especialidad
- Verificación de existencia (consumido por el servicio de Clases)

**Base de datos propia:** `entrenadoresdb`.

---

### Servicio de Equipos (`equipo-service`, puerto 8084)

**Responsabilidad:** Gestionar el inventario de equipamiento del gimnasio.

- Registro de equipos
- Consulta de stock disponible
- Actualización de cantidades

**Base de datos propia:** `equiposdb`.

---

## 4. Identificación de Agregados

Para cada bounded context se define un **Agregado** siguiendo los principios DDD. Un agregado es un conjunto de objetos del dominio que se tratan como una unidad para efectos de cambios de datos. El **Aggregate Root** es la única entrada al agregado.

### Agregado Miembro

```
Agregado Root: Miembro
│
├── MiembroId           (Value Object — identidad)
│     └── miembroid_value: String
│
├── Email               (Value Object — dato de contacto)
│     └── email_value: String
│
├── fechaInscripcion    (LocalDate — atributo de dominio)
└── membresiaActiva     (boolean — si puede asistir a clases)

Métodos del agregado:
  - actualizarEmail(Email nuevoEmail)
```

### Agregado Entrenador

```
Agregado Root: Entrenador
│
├── EntrenadorId        (Value Object — identidad)
│     └── entrenadorid_value: String
│
└── Especialidad        (Value Object — competencia profesional)
      └── especialidad_value: String

Métodos del agregado:
  - actualizarEspecialidad(Especialidad nueva)
```

### Agregado Clase

```
Agregado Root: Clase
│
├── ClaseId             (Value Object — identidad)
│     └── claseid_value: String
│
├── EntrenadorId        (Value Object — referencia externa, NO embebe Entrenador)
│     └── entrenadorid_value: String
│
├── horario             (LocalDateTime — atributo de dominio)
├── capacidadMaxima     (int — restricción de cupo)
└── miembrosInscritos   (Set<MiembroId> — referencias externas por ID)

Métodos del agregado:
  - actualizarHorario(LocalDateTime nuevoHorario)
  - actualizarCapacidad(int nuevaCapacidad)
  - inscribirMiembro(MiembroId miembroId)

Nota: Clase mantiene referencias a Entrenador y Miembros por ID, nunca por objetos completos.
La validación de existencia y membresía activa se hace vía REST al servicio de Miembros.
```

### Agregado Equipo

```
Agregado Root: Equipo
│
├── EquipoId            (Value Object — identidad)
│     └── equipoid_value: String
│
├── nombre              (String — identificación del equipo)
├── descripcion         (String — detalle)
└── cantidad            (int — stock)

Métodos del agregado:
  - actualizarCantidad(int nueva)
  - actualizarDescripcion(String nueva)
```

---

## 5. Refactorización del Código

### 5.1 Cambios estructurales por microservicio

**De `GimnasioService` (monolito) a servicios individuales:**

El monolito tenía un único `GimnasioService` con todos los repositorios inyectados. Cada microservicio ahora tiene:
- Su propio `XxxService` con solo su repositorio inyectado
- Responsabilidad única (Single Responsibility Principle)
- Métodos que operan solo sobre su agregado

**De entidades simples a agregados DDD:**

```java
// ANTES (monolito) — entidad anémica, solo campos con getters/setters
@Entity
public class Entrenador {
    @Id @GeneratedValue private Long id;
    private String nombre;
    private String especialidad;
}

// DESPUÉS (microservicio) — agregado rico con value objects e identidad explícita
@Entity
public class Entrenador {
    @EmbeddedId private EntrenadorId id;        // identidad como value object
    private String nombre;
    @Embedded private Especialidad especialidad; // concepto del dominio encapsulado

    public void actualizarEspecialidad(Especialidad nueva) { // comportamiento en el modelo
        this.especialidad = nueva;
    }
}
```

**Rotura del @ManyToOne entre Clase y Entrenador:**

```java
// ANTES (monolito) — acoplamiento JPA directo, ambas entidades en la misma BD
@Entity
public class Clase {
    @ManyToOne
    private Entrenador entrenador; // referencia al objeto completo
}

// DESPUÉS (microservicio) — referencia por ID, sin acoplamiento de persistencia
@Entity
public class Clase {
    @Embedded
    @AttributeOverride(name = "entrenadorid_value", column = @Column(name = "entrenador_id"))
    private EntrenadorId entrenadorId; // solo el ID, validación via REST
}
```

### 5.2 Patrón de comunicación inter-servicios

**1. Clases → Entrenadores** (al programar una clase):

```
POST /clases
      │
      ▼
ClaseService.programarClase()
      │
      ├── GET http://ENTRENADOR-SERVICE/entrenadores/{id}/existe
      │         │
      │         ▼
      │   EntrenadorController.existeEntrenador()
      │         │
      │         ▼
      │   EntrenadorService.existeEntrenador() → boolean
      │
      ├── [false] → lanza EntrenadorNoEncontradoException
      └── [true]  → claseRepository.save(clase)
```

**2. Clases → Miembros** (al inscribir un miembro en una clase):

```
POST /clases/{claseId}/miembros
      │
      ▼
ClaseService.inscribirMiembro(claseId, miembroId)
      │
      ├── [ya en clase.getMiembrosInscritos()] → MiembroYaInscritoException (409)
      ├── [capacidad máxima] → IllegalStateException (409)
      │
      ├── GET http://MIEMBRO-SERVICE/miembros/{id}/puede-asistir-clase
      │         │
      │         ▼
      │   MiembroService.puedeAsistirAClase() → { puedeAsistir, razon }
      │         razon: null | "NO_EXISTE" | "MEMBRESIA_INACTIVA"
      │
      ├── [puedeAsistir false] → MiembroNoPuedeAsistirException(mensaje según razon)
      └── [true]  → clase.inscribirMiembro(miembroId); claseRepository.save(clase)
```

---

## 6. Servidor de Descubrimiento Eureka

### ¿Qué es y por qué es un microservicio aparte?

Eureka es un servidor de registro y descubrimiento de servicios desarrollado por Netflix e integrado en Spring Cloud. Su función es actuar como un **"directorio telefónico"** de la arquitectura: cada microservicio, al arrancar, se registra en Eureka con su nombre y su dirección (host:puerto). Cuando un servicio necesita llamar a otro, pregunta a Eureka por la dirección actual en lugar de usar una URL hardcodeada.

El servidor Eureka es un **microservicio independiente** por varias razones:

1. **Ciclo de vida independiente:** Eureka debe estar activo antes que cualquier otro servicio. Tenerlo separado permite arrancarlo primero y sin dependencias.

2. **No tiene lógica de negocio:** Su único propósito es el registro y descubrimiento. Mezclar esta infraestructura con lógica de dominio violaría el principio de responsabilidad única.

3. **Alta disponibilidad diferenciada:** En producción, Eureka puede desplegarse en múltiples instancias sin afectar el despliegue del resto de servicios.

4. **Evolución independiente:** Si se decide migrar a otro mecanismo de descubrimiento (Consul, Kubernetes DNS), solo se cambia este componente.

### Beneficios concretos en este proyecto

| Sin Eureka | Con Eureka |
|---|---|
| `http://localhost:8083/entrenadores/1` | `http://ENTRENADOR-SERVICE/entrenadores/1` |
| Si el puerto cambia, hay que modificar código | Solo cambia la configuración del servicio |
| Imposible tener múltiples instancias del mismo servicio | Eureka balancea automáticamente entre instancias |
| Cada servicio debe conocer la ubicación de los demás | Los servicios solo conocen el nombre lógico |

Con `@LoadBalanced RestTemplate`, Spring Cloud intercepta las llamadas HTTP, consulta a Eureka el nombre `ENTRENADOR-SERVICE` y resuelve la dirección real en tiempo de ejecución. Esto es transparente para el desarrollador.
