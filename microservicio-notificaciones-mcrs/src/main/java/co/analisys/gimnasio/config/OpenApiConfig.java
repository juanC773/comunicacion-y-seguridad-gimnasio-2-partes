package co.analisys.gimnasio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificacionesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Notificaciones - Gimnasio")
                        .version("v1")
                        .description("Servicio de notificaciones (consumidor RabbitMQ). " +
                                "GET /public/status: estado del servicio. " +
                                "POST /simular-pago: envía un mensaje a la cola de pagos (el procesador falla y el mensaje va a la DLQ)."));
    }
}
