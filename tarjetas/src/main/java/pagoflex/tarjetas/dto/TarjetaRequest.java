package pagoflex.tarjetas.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class TarjetaRequest {
    @NotNull private Long usuarioId;
    @NotBlank @Size(min = 4, max = 4) private String ultimosDigitos;
    @NotBlank private String titular;
    @NotBlank private String vencimiento;
    private String marca;
    private boolean principal;
}
