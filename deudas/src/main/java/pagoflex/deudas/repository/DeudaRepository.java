package pagoflex.deudas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pagoflex.deudas.model.Deuda;

public interface DeudaRepository extends JpaRepository<Deuda, Long> {

    List<Deuda> findByUsuarioIdOrderByVencimientoAscIdAsc(Long usuarioId);
}
