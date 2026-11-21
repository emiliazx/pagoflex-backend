package pagoflex.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Cierre de un ticket")
public class CerrarTicketRequest {

    @NotNull
    @Schema(description = "true si el problema quedó resuelto; false si se cierra sin resolver", example = "true")
    private Boolean resuelto;
}
