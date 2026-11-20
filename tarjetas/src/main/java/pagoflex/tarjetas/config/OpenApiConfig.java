package pagoflex.tarjetas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PagoFlex - Tarjetas Service")
                        .version("1.0")
                        .description("Microservicio de tarjetas: guarda solo los últimos 4 dígitos, "
                                + "máximo 4 por usuario y una principal."));
    }
}
