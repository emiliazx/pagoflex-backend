package pagoflex.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Cambio de contraseña autenticado")
public class CambiarContrasenaRequest {
    @NotBlank
    @Schema(description = "Contraseña vigente", example = "Emilia123$")
    private String contrasenaActual;

    @NotBlank
    @Size(min = 8, max = 64)
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#._-]).{8,}$",
        message = "La nueva contraseña debe tener mínimo 8 caracteres, mayúscula, minúscula, número y carácter especial"
    )
    @Schema(description = "Mínimo 8 caracteres con mayúscula, minúscula, número y carácter especial (@$!%*?&#._-)",
            example = "NuevaClave456#", minLength = 8, maxLength = 64)
    private String contrasenaNueva;
}
