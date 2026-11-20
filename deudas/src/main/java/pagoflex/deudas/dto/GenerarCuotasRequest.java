package pagoflex.deudas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Datos para generar las cuotas de una deuda")
public class GenerarCuotasRequest {

    @NotNull
    @Positive
    @Schema(description = "Id de la deuda", example = "1")
    private Long deudaId;

    @NotNull
    @Positive
    @Schema(description = "Id del usuario dueño de la deuda (debe coincidir)", example = "3")
    private Long usuarioId;

    @NotNull
    @Min(1)
    @Max(60)
    @Schema(description = "Cantidad de cuotas (1 a 60)", example = "6", minimum = "1", maximum = "60")
    private Integer cantidadCuotas;

    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Usa el formato yyyy-MM-dd")
    @Schema(description = "Vencimiento de la primera cuota (yyyy-MM-dd); las demás vencen mes a mes", example = "2026-12-05")
    private String primerVencimiento;
}
