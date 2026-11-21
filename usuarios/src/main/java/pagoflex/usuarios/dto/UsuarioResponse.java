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
@Schema(description = "Datos públicos de un usuario (nunca incluye contraseña ni respuestas secretas)")
public class UsuarioResponse {
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

    @Schema(description = "Primera pregunta secreta", example = "¿Nombre de tu primera mascota?")
    private String pregunta1;

    @Schema(description = "Segunda pregunta secreta", example = "¿Nombre de tu madre?")
    private String pregunta2;

    @Schema(description = "Indica si el usuario tiene foto de perfil", example = "true")
    private boolean tieneFoto;

    @Schema(description = "Ruta relativa desde donde se descarga la foto de perfil; null si no tiene",
            example = "/api/usuarios/fotos/3-0b0a2f0e-6c1d-4a58-8f6e-2f4f4f0d9a11.jpg", nullable = true)
    private String fotoUrl;

    @Schema(description = "false mientras un gestor no haya habilitado la cuenta", example = "true")
    private boolean habilitado;
}
