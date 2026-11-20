package pagoflex.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado del login: token JWT y datos básicos del usuario")
public class LoginResponse {

    @Schema(description = "JWT para enviar en el header Authorization (válido 24 h por defecto)",
            example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Tipo de token", example = "Bearer")
    private String tipo;

    @Schema(description = "Id del usuario", example = "3")
    private Long id;

    @Schema(description = "Nombre completo", example = "Emilia Zamora")
    private String nombre;

    @Schema(description = "RUT", example = "12345678-5")
    private String rut;

    @Schema(description = "Correo electrónico", example = "emilia@pagoflex.cl")
    private String correo;

    @Schema(description = "Rol del usuario", allowableValues = {"ADMIN", "GESTOR", "USUARIO"}, example = "USUARIO")
    private String rol;

    /** URL de la foto en el servidor (null si no tiene) */
    @Schema(description = "URL de la foto de perfil en el servidor",
            example = "http://localhost:8081/uploads/usuarios/3.jpg")
    private String fotoUrl;
}