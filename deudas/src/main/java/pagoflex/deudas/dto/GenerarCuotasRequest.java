package pagoflex.deudas.dto;

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
public class GenerarCuotasRequest {

    @NotNull
    @Positive
    private Long deudaId;

    @NotNull
    @Positive
    private Long usuarioId;

    @NotNull
    @Min(1)
    @Max(60)
    private Integer cantidadCuotas;

    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Usa el formato yyyy-MM-dd")
    private String primerVencimiento;
}
