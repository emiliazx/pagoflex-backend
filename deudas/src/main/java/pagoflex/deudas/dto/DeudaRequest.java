package pagoflex.deudas.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DeudaRequest {

    @NotNull
    @Positive
    private Long usuarioId;

    @NotBlank
    @Size(max = 120)
    private String concepto;

    @NotBlank
    @Size(max = 80)
    private String empresa;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("9999999999.99")
    private BigDecimal monto;

    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Usa el formato yyyy-MM-dd")
    private String vencimiento;

    @Min(1)
    @Max(60)
    private Integer cantidadCuotas;
}
