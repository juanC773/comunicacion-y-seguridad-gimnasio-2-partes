# Servidor de Descubrimiento — Eureka Server

## ¿Qué es este proyecto?

Este proyecto implementa el **servidor de registro y descubrimiento de servicios** usando Netflix Eureka, integrado a través de Spring Cloud. Es la pieza de infraestructura base de la arquitectura de microservicios del gimnasio.

## Función en la arquitectura

En una arquitectura de microservicios, los servicios necesitan encontrarse entre sí sin depender de direcciones IP o puertos hardcodeados. Eureka actúa como un **directorio centralizado**:

1. Cada microservicio, al arrancar, se **registra** en Eureka con su nombre lógico (ej: `ENTRENADOR-SERVICE`) y su dirección física.
2. Cuando un microservicio necesita llamar a otro, **consulta a Eureka** por el nombre lógico y obtiene la dirección actual.
3. Si hay múltiples instancias del mismo servicio, Eureka las lista y el cliente puede hacer **balanceo de carga**.

## ¿Por qué es un proyecto independiente?

- **Debe arrancar primero**, antes que cualquier microservicio de negocio. Un proyecto separado hace explícita esta dependencia de arranque.
- **No tiene lógica de dominio**. Mezclar infraestructura de descubrimiento con reglas de negocio violaría el principio de responsabilidad única.
- **Ciclo de vida propio**: puede actualizarse, reiniciarse o escalarse sin afectar al resto del sistema.

## Estructura del proyecto

```
src/main/java/co/analisys/gimnasio/
└── DescubrimientoApplication.java  — @SpringBootApplication + @EnableEurekaServer
src/main/resources/
└── application.properties          — Configuración del servidor (puerto 8761)
```

## Configuración relevante

```properties
server.port=8761
eureka.client.register-with-eureka=false   # El servidor no se registra a sí mismo
eureka.client.fetch-registry=false          # No consume el registry, lo sirve
```

## Ejecución

```bash
bash mvnw spring-boot:run
```

Puerto: `8761` | Dashboard: [http://localhost:8761](http://localhost:8761)

## Dashboard

Una vez en ejecución, el dashboard de Eureka en `http://localhost:8761` mostrará en tiempo real todos los microservicios registrados con su estado (`UP` / `DOWN`).
