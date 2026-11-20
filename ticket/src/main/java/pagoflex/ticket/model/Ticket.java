package pagoflex.ticket.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    /** ABIERTO, EN_PROCESO, CERRADO */
    @Column(nullable = false)
    private String estado;

    /** null = abierto; true/false al cerrar */
    private Boolean resuelto;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaCierre;
}