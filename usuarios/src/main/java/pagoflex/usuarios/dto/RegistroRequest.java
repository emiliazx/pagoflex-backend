package pagoflex.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Datos para registrar un usuario nuevo (rol USUARIO)")
public class RegistroRequest {
    @NotBlank
    @Schema(description = "Nombre completo", example = "Cristhian Llontop")
    private String nombre;

    @NotBlank
    @Schema(description = "RUT del usuario", example = "12345678-5")
    private String rut;

    @NotBlank @Email
    @Schema(description = "Correo electrónico, único en el sistema", example = "cristhian@correo.cl")
    private String correo;

    @NotBlank
    @Size(min = 8, max = 64)
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#._-]).{8,}$",
        message = "La contraseña debe tener mínimo 8 caracteres, mayúscula, minúscula, número y carácter especial"
    )
    @Schema(description = "Mínimo 8 caracteres con mayúscula, minúscula, número y carácter especial (@$!%*?&#._-)",
            example = "Clave123$", minLength = 8, maxLength = 64)
    private String contrasena;

    @Schema(description = "Primera pregunta secreta (para recuperar la cuenta)", example = "¿Nombre de tu primera mascota?")
    private String pregunta1;

    @Schema(description = "Respuesta a la primera pregunta", example = "firulais")
    private String respuesta1;

    @Schema(description = "Segunda pregunta secreta", example = "¿Nombre de tu madre?")
    private String pregunta2;

    @Schema(description = "Respuesta a la segunda pregunta", example = "maria")
    private String respuesta2;
}
