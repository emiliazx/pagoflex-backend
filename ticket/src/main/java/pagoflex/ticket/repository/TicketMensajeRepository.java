package pagoflex.ticket.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import pagoflex.ticket.model.TicketMensaje;

import java.util.List;

public interface TicketMensajeRepository extends JpaRepository<TicketMensaje, Long> {
    List<TicketMensaje> findByTicketIdOrderByFechaAsc(Long ticketId);
}