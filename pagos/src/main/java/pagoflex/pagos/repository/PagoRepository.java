package pagoflex.pagos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pagoflex.pagos.model.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByUsuarioIdOrderByFechaDescIdDesc(Long usuarioId);

    List<Pago> findByDeudaIdOrderByFechaDescIdDesc(Long deudaId);
}
