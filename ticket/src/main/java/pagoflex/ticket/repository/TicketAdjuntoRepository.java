package pagoflex.ticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pagoflex.ticket.model.TicketAdjunto;

import java.util.List;

public interface TicketAdjuntoRepository extends JpaRepository<TicketAdjunto, Long> {
    List<TicketAdjunto> findByTicketIdOrderByFechaAsc(Long ticketId);
    long countByTicketId(Long ticketId);
}
