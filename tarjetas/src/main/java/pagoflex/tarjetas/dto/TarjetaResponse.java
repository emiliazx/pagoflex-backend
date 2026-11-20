package pagoflex.tarjetas.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Tarjeta guardada")
public class TarjetaResponse {
    @Schema(description = "Id de la tarjeta", example = "2")
    private Long id;

    @Schema(description = "Id del usuario dueño", example = "3")
    private Long usuarioId;

    @Schema(description = "Últimos 4 dígitos", example = "4242")
    private String ultimosDigitos;

    @Schema(description = "Titular", example = "EMILIA ZAMORA")
    private String titular;

    @Schema(description = "Vencimiento MM/YY", example = "08/28")
    private String vencimiento;

    @Schema(description = "Marca", example = "Visa")
    private String marca;

    @Schema(description = "Indica si es la tarjeta principal", example = "true")
    private boolean principal;
}
