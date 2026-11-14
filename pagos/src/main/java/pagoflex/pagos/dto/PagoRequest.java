package pagoflex.pagos.dto;

import java.math.BigDecimal;

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
public class PagoRequest {

    @NotNull
    @Positive
    private Long deudaId;

    @NotNull
    @Positive
    private Long usuarioId;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("9999999999.99")
    private BigDecimal monto;

    @Size(max = 20)
    private String metodo;

    @Positive
    private Long tarjetaId;
}
