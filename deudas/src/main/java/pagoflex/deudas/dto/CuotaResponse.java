package pagoflex.deudas.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Cuota de una deuda")
public class CuotaResponse {
    @Schema(description = "Id de la cuota", example = "7")
    private Long id;

    @Schema(description = "Id de la deuda a la que pertenece", example = "1")
    private Long deudaId;

    @Schema(description = "Id del usuario dueño", example = "3")
    private Long usuarioId;

    @Schema(description = "Número de cuota (1, 2, 3...)", example = "2")
    private int numero;

    @Schema(description = "Monto de la cuota", example = "15330.00")
    private BigDecimal monto;

    @Schema(description = "Fecha de vencimiento (yyyy-MM-dd)", example = "2027-01-05")
    private String vencimiento;

    @Schema(description = "Estado de la cuota", allowableValues = {"PENDIENTE", "PAGADA"}, example = "PENDIENTE")
    private String estado;

    @Schema(description = "Fecha de pago (yyyy-MM-dd); null mientras esté pendiente", example = "2026-11-19",
            nullable = true)
    private String fechaPago;
}
