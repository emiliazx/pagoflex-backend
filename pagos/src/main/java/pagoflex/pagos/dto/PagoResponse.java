package pagoflex.pagos.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PagoResponse {
    private Long id;
    private Long deudaId;
    private Long usuarioId;
    private BigDecimal monto;
    private String fecha;
    private String metodo;
    private Long tarjetaId;
}
