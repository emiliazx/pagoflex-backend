package pagoflex.ticket.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TicketResponse {
    private Long id;
    private Long usuarioId;
    private String titulo;
    private String descripcion;
    private String estado;
    private Boolean resuelto;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaCierre;
}