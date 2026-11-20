package pagoflex.pagos.config;

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
                        .title("PagoFlex - Pagos Service")
                        .version("1.0")
                        .description("Microservicio de pagos protegido con JWT: registro e historial. "
                                + "Cada USUARIO accede solo a sus propios pagos. "
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
