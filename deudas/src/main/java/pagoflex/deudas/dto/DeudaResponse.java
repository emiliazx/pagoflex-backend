package pagoflex.deudas.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DeudaResponse {
    private Long id;
    private Long usuarioId;
    private String concepto;
    private String empresa;
    private BigDecimal monto;
    private String vencimiento;
    private String estado;
    private int cantidadCuotas;
    private int cuotasPagadas;
    private Integer cuotaActual;
    private BigDecimal montoCuota;
    private String vencimientoActual;
    private BigDecimal montoPagado;
}
