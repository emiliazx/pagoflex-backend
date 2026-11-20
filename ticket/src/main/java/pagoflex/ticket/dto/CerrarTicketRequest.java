package pagoflex.ticket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CerrarTicketRequest {
    @NotNull private Boolean resuelto;
}