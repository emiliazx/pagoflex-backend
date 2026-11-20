package pagoflex.ticket.repository;

import pagoflex.ticket.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
    List<Ticket> findByEstadoOrderByFechaCreacionDesc(String estado);
    List<Ticket> findAllByOrderByFechaCreacionDesc();
}