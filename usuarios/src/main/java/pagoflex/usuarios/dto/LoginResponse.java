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

    @Schema(description = "Indica si el usuario tiene foto de perfil", example = "false")
    private boolean tieneFoto;

    @Schema(description = "Ruta relativa desde donde se descarga la foto de perfil; null si no tiene",
            example = "/api/usuarios/fotos/3-0b0a2f0e-6c1d-4a58-8f6e-2f4f4f0d9a11.jpg", nullable = true)
    private String fotoUrl;

    @Schema(description = "Siempre true en un login exitoso", example = "true")
    private boolean habilitado;
}
