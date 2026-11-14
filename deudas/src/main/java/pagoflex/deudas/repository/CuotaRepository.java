package pagoflex.deudas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pagoflex.deudas.model.Cuota;

public interface CuotaRepository extends JpaRepository<Cuota, Long> {

    List<Cuota> findByDeudaIdOrderByNumeroAsc(Long deudaId);

    List<Cuota> findByUsuarioIdOrderByVencimientoAscIdAsc(Long usuarioId);

    void deleteByDeudaId(Long deudaId);
}
