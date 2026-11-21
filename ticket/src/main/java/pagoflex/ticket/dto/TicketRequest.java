package pagoflex.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Datos para reportar un problema. El usuario se toma del token")
public class TicketRequest {

    @NotBlank
    @Size(max = 120)
    @Schema(description = "Título corto del problema", example = "Pagué la cuota 2 y sigue pendiente", maxLength = 120)
    private String titulo;

    @NotBlank
    @Size(max = 1000)
    @Schema(description = "Detalle de lo que ocurrió", example = "Pagué ayer con mi tarjeta y la cuota aparece sin pagar.", maxLength = 1000)
    private String descripcion;

    @Schema(description = "Id de la deuda relacionada (opcional)", example = "1", nullable = true)
    private Long deudaId;

    @Size(max = 200)
    @Schema(description = "Texto que identifica la deuda o cuota relacionada (opcional)",
            example = "Cuenta de luz (Enel) - cuota 2 de 3", maxLength = 200, nullable = true)
    private String referencia;
}
