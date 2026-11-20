package pagoflex.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MensajeRequest {
    @NotNull private Long autorId;
    @NotBlank private String rolAutor;
    @NotBlank private String mensaje;
}