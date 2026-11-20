package pagoflex.pagos.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Datos para registrar un pago")
public class PagoRequest {

    @NotNull
    @Positive
    @Schema(description = "Id de la deuda que se paga", example = "1")
    private Long deudaId;

    @NotNull
    @Positive
    @Schema(description = "Id del usuario que paga (debe ser el del token, salvo ADMIN/GESTOR)", example = "3")
    private Long usuarioId;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("9999999999.99")
    @Schema(description = "Monto pagado (se redondea a 2 decimales)", example = "15330.00")
    private BigDecimal monto;

    @Size(max = 20)
    @Schema(description = "Método de pago; si no se envía se usa TARJETA. Se guarda en mayúsculas",
            example = "TARJETA", maxLength = 20)
    private String metodo;

    @Positive
    @Schema(description = "Id de la tarjeta usada (opcional, referencia al servicio de tarjetas)", example = "2")
    private Long tarjetaId;
}
