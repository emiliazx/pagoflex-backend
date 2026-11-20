package pagoflex.deudas.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Deuda con su avance de pago")
public class DeudaResponse {
    @Schema(description = "Id de la deuda", example = "1")
    private Long id;

    @Schema(description = "Id del usuario dueño", example = "3")
    private Long usuarioId;

    @Schema(description = "Concepto", example = "Cuenta de luz octubre")
    private String concepto;

    @Schema(description = "Empresa acreedora", example = "Enel")
    private String empresa;

    @Schema(description = "Monto total", example = "45990.00")
    private BigDecimal monto;

    @Schema(description = "Vencimiento de la deuda (yyyy-MM-dd)", example = "2026-12-05")
    private String vencimiento;

    @Schema(description = "Estado de la deuda", allowableValues = {"PENDIENTE", "PAGADA"}, example = "PENDIENTE")
    private String estado;

    @Schema(description = "Total de cuotas (1 si la deuda no está dividida)", example = "3")
    private int cantidadCuotas;

    @Schema(description = "Cuotas ya pagadas", example = "1")
    private int cuotasPagadas;

    @Schema(description = "Número de la cuota pendiente actual; null si no quedan pendientes", example = "2",
            nullable = true)
    private Integer cuotaActual;

    @Schema(description = "Monto de la cuota actual (o el monto total si no hay cuotas pendientes)", example = "15330.00")
    private BigDecimal montoCuota;

    @Schema(description = "Vencimiento de la cuota actual (yyyy-MM-dd)", example = "2027-01-05")
    private String vencimientoActual;

    @Schema(description = "Suma de lo ya pagado", example = "15330.00")
    private BigDecimal montoPagado;
}
