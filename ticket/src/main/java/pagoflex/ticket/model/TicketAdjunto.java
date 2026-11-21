package pagoflex.ticket.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_adjuntos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TicketAdjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(nullable = false)
    private String archivo;

    @Column(name = "nombre_original")
    private String nombreOriginal;

    @Column(name = "tipo_contenido")
    private String tipoContenido;

    private long tamano;

    @Column(nullable = false)
    private LocalDateTime fecha;
}
