package pagoflex.ticket.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MensajeResponse {
    private Long id;
    private Long ticketId;
    private Long autorId;
    private String rolAutor;
    private String mensaje;
    private LocalDateTime fecha;
}