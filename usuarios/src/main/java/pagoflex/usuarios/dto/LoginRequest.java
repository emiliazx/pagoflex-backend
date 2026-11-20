package pagoflex.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Credenciales de acceso")
public class LoginRequest {
    @NotBlank @Email
    @Schema(description = "Correo registrado (se normaliza a minúsculas)", example = "emilia@pagoflex.cl")
    private String correo;

    @NotBlank
    @Schema(description = "Contraseña de la cuenta", example = "Emilia123$")
    private String contrasena;
}
