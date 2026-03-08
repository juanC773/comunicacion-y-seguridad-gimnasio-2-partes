package co.analisys.gimnasio.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";
    private static final String ROLES_DESC = "Los endpoints (excepto /public/status) requieren JWT Bearer. " +
            "Roles: ROLE_ADMIN, ROLE_TRAINER, ROLE_MEMBER. " +
            "GET /miembros, /{id}, /{id}/puede-asistir-clase: ADMIN, TRAINER o MEMBER. " +
            "POST /miembros: ADMIN o TRAINER. " +
            "PUT /{id}/email: ADMIN, TRAINER o MEMBER.";

    @Bean
    public OpenAPI miembrosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Miembros - Gimnasio")
                        .version("v1")
                        .description(ROLES_DESC))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH,
                                new SecurityScheme()
                                        .name(BEARER_AUTH)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
