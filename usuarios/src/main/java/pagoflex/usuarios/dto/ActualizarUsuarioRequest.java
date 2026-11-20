package pagoflex.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Campos editables del perfil. Los que no se envían (null) no se modifican")
public class ActualizarUsuarioRequest {
    @Schema(description = "Nuevo nombre", example = "Emilia Zamora Pérez")
    private String nombre;

    @Schema(description = "Nuevo RUT", example = "12345678-5")
    private String rut;

    @Schema(description = "Nueva primera pregunta secreta", example = "¿Nombre de tu primera mascota?")
    private String pregunta1;

    @Schema(description = "Nueva respuesta a la primera pregunta", example = "firulais")
    private String respuesta1;

    @Schema(description = "Nueva segunda pregunta secreta", example = "¿Nombre de tu madre?")
    private String pregunta2;

    @Schema(description = "Nueva respuesta a la segunda pregunta", example = "maria")
    private String respuesta2;
}
