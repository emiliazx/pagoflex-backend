package pagoflex.ticket.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_mensajes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TicketMensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "autor_id", nullable = false)
    private Long autorId;

    @Column(name = "rol_autor", nullable = false)
    private String rolAutor;

    @Column(nullable = false, length = 1000)
    private String mensaje;

    @Column(nullable = false)
    private LocalDateTime fecha;
}