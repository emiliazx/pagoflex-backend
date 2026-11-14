package pagoflex.tarjetas.model;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "tarjetas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tarjeta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;
    @Column(name = "ultimos_digitos", nullable = false, length = 4)
    private String ultimosDigitos;
    @Column(nullable = false)
    private String titular;
    @Column(nullable = false)
    private String vencimiento; // MM/YY
    private String marca;
    @Column(nullable = false)
    private boolean principal;
}
