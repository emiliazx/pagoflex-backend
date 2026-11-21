package pagoflex.deudas.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Datos para crear o actualizar una deuda")
public class DeudaRequest {

    @NotNull
    @Positive
    @Schema(description = "Id del usuario dueño de la deuda (no se puede cambiar al actualizar)", example = "3")
    private Long usuarioId;

    @NotBlank
    @Size(max = 120)
    @Schema(description = "Concepto de la deuda", example = "Cuenta de luz octubre", maxLength = 120)
    private String concepto;

    @NotBlank
    @Size(max = 80)
    @Schema(description = "Empresa acreedora", example = "Enel", maxLength = 80)
    private String empresa;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("9999999999.99")
    @Schema(description = "Monto total (se redondea a 2 decimales)", example = "45990.00")
    private BigDecimal monto;

    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Usa el formato yyyy-MM-dd")
    @Schema(description = "Fecha de vencimiento (o de la primera cuota), formato yyyy-MM-dd", example = "2026-12-05")
    private String vencimiento;

    @Min(1)
    @Max(72)
    @Schema(description = "Cantidad de cuotas (1 a 72). Con 1 o sin valor no se generan cuotas", example = "3",
            minimum = "1", maximum = "72")
    private Integer cantidadCuotas;
}
