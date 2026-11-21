package pagoflex.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Mensaje dentro de un ticket. El autor y su rol se toman del token")
public class MensajeRequest {

    @NotBlank
    @Size(max = 1000)
    @Schema(description = "Texto del mensaje", example = "Adjunté el comprobante de mi banco.", maxLength = 1000)
    private String mensaje;
}
