package pagoflex.pagos.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Pago registrado")
public class PagoResponse {
    @Schema(description = "Id del pago", example = "12")
    private Long id;

    @Schema(description = "Id de la deuda pagada", example = "1")
    private Long deudaId;

    @Schema(description = "Id del usuario que pagó", example = "3")
    private Long usuarioId;

    @Schema(description = "Monto pagado", example = "15330.00")
    private BigDecimal monto;

    @Schema(description = "Fecha y hora del pago (ISO-8601, sin zona)", example = "2026-11-19T14:32:10")
    private String fecha;

    @Schema(description = "Método de pago", example = "TARJETA")
    private String metodo;

    @Schema(description = "Id de la tarjeta usada; null si no se indicó", example = "2", nullable = true)
    private Long tarjetaId;
}
