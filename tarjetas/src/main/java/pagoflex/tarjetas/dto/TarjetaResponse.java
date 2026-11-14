package pagoflex.tarjetas.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TarjetaResponse {
    private Long id;
    private Long usuarioId;
    private String ultimosDigitos;
    private String titular;
    private String vencimiento;
    private String marca;
    private boolean principal;
}
