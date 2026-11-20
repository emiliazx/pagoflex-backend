package pagoflex.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketRequest {
    @NotNull private Long usuarioId;
    @NotBlank private String titulo;
    @NotBlank private String descripcion;
}