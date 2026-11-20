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
}
