package pagoflex.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Recuperación de cuenta con las dos preguntas secretas")
public class RecuperarRequest {
    @NotBlank @Email
    @Schema(description = "Correo de la cuenta a recuperar", example = "emilia@pagoflex.cl")
    private String correo;

    @NotBlank
    @Schema(description = "Respuesta a la primera pregunta (no distingue mayúsculas)", example = "firulais")
    private String respuesta1;

    @NotBlank
    @Schema(description = "Respuesta a la segunda pregunta (no distingue mayúsculas)", example = "maria")
    private String respuesta2;

    @NotBlank
    @Schema(description = "Nueva contraseña: mínimo 8 caracteres con mayúscula, minúscula, número y carácter especial",
            example = "NuevaClave456#")
    private String nuevaContrasena;
}
