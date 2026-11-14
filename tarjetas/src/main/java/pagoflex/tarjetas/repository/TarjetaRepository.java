package pagoflex.tarjetas.repository;
import pagoflex.tarjetas.model.Tarjeta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface TarjetaRepository extends JpaRepository<Tarjeta, Long> {
    List<Tarjeta> findByUsuarioId(Long usuarioId);
    long countByUsuarioId(Long usuarioId);
}
