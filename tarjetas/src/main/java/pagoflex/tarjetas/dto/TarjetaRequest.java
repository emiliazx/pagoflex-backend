package pagoflex.tarjetas.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Datos para guardar una tarjeta")
public class TarjetaRequest {
    @NotNull
    @Schema(description = "Id del usuario dueño de la tarjeta", example = "3")
    private Long usuarioId;

    @NotBlank @Size(min = 4, max = 4)
    @Schema(description = "Últimos 4 dígitos del número (nunca envíes el número completo)", example = "4242",
            minLength = 4, maxLength = 4)
    private String ultimosDigitos;

    @NotBlank
    @Schema(description = "Nombre del titular", example = "EMILIA ZAMORA")
    private String titular;

    @NotBlank
    @Schema(description = "Fecha de vencimiento en formato MM/YY", example = "08/28")
    private String vencimiento;

    @Schema(description = "Marca de la tarjeta; si se omite se guarda \"Tarjeta\"", example = "Visa")
    private String marca;

    @Schema(description = "Si es true, pasa a ser la tarjeta principal del usuario", example = "true")
    private boolean principal;
}
