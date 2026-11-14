package pagoflex.deudas.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CuotaResponse {
    private Long id;
    private Long deudaId;
    private Long usuarioId;
    private int numero;
    private BigDecimal monto;
    private String vencimiento;
    private String estado;
    private String fechaPago;
}
