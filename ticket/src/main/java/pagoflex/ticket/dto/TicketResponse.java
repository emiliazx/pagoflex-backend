package pagoflex.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Ticket de soporte")
public class TicketResponse {
    @Schema(description = "Id del ticket", example = "4")
    private Long id;

    @Schema(description = "Id del usuario que lo creó", example = "3")
    private Long usuarioId;

    @Schema(description = "Correo del usuario que lo creó", example = "emilia@pagoflex.cl")
    private String usuarioCorreo;

    @Schema(description = "Título", example = "Pagué la cuota 2 y sigue pendiente")
    private String titulo;

    @Schema(description = "Descripción del problema")
    private String descripcion;

    @Schema(description = "Id de la deuda relacionada", nullable = true)
    private Long deudaId;

    @Schema(description = "Texto de la deuda o cuota relacionada", nullable = true)
    private String referencia;

    @Schema(description = "Estado del ticket", allowableValues = {"ABIERTO", "EN_PROCESO", "CERRADO"}, example = "ABIERTO")
    private String estado;

    @Schema(description = "null mientras esté abierto; true o false al cerrarlo", nullable = true)
    private Boolean resuelto;

    @Schema(description = "Fecha de creación")
    private LocalDateTime fechaCreacion;

    @Schema(description = "Fecha de cierre", nullable = true)
    private LocalDateTime fechaCierre;
}
