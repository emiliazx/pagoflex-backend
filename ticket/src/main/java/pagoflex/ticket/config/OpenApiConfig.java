package pagoflex.ticket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PagoFlex - Ticket Service")
                        .version("1.0")
                        .description("Microservicio de reportes de problemas: el usuario crea un ticket con pruebas "
                                + "(fotos), conversa con el gestor y el gestor lo cierra como resuelto o no resuelto. "
                                + "Usa **Authorize** con el token de POST /api/usuarios/login."))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token obtenido en POST /api/usuarios/login")));
    }
}
