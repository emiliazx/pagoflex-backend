package pagoflex.ticket.service;

import pagoflex.ticket.dto.*;
import pagoflex.ticket.model.Ticket;
import pagoflex.ticket.model.TicketMensaje;
import pagoflex.ticket.repository.TicketMensajeRepository;
import pagoflex.ticket.repository.TicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMensajeRepository mensajeRepository;

    public TicketService(TicketRepository ticketRepository, TicketMensajeRepository mensajeRepository) {
        this.ticketRepository = ticketRepository;
        this.mensajeRepository = mensajeRepository;
    }

    public TicketResponse crear(TicketRequest req) {
        Ticket t = Ticket.builder()
                .usuarioId(req.getUsuarioId())
                .titulo(req.getTitulo())
                .descripcion(req.getDescripcion())
                .estado("ABIERTO")
                .resuelto(null)
                .fechaCreacion(LocalDateTime.now())
                .build();
        return toTicket(ticketRepository.save(t));
    }

    public List<TicketResponse> listarPorUsuario(Long usuarioId) {
        return ticketRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream().map(this::toTicket).collect(Collectors.toList());
    }

    /** Gestor: todos o por estado */
    public List<TicketResponse> listarParaGestor(String estado) {
        List<Ticket> lista = (estado == null || estado.isBlank())
                ? ticketRepository.findAllByOrderByFechaCreacionDesc()
                : ticketRepository.findByEstadoOrderByFechaCreacionDesc(estado);
        return lista.stream().map(this::toTicket).collect(Collectors.toList());
    }

    public TicketResponse obtener(Long id) {
        return toTicket(buscar(id));
    }

    public MensajeResponse agregarMensaje(Long ticketId, MensajeRequest req) {
        Ticket t = buscar(ticketId);
        TicketMensaje m = TicketMensaje.builder()
                .ticketId(ticketId)
                .autorId(req.getAutorId())
                .rolAutor(req.getRolAutor())
                .mensaje(req.getMensaje())
                .fecha(LocalDateTime.now())
                .build();

        // Si escribe gestor o admin, pasar a EN_PROCESO
        if ("GESTOR".equalsIgnoreCase(req.getRolAutor()) || "ADMIN".equalsIgnoreCase(req.getRolAutor())) {
            if ("ABIERTO".equals(t.getEstado())) {
                t.setEstado("EN_PROCESO");
                ticketRepository.save(t);
            }
        }
        return toMensaje(mensajeRepository.save(m));
    }

    public List<MensajeResponse> listarMensajes(Long ticketId) {
        return mensajeRepository.findByTicketIdOrderByFechaAsc(ticketId)
                .stream().map(this::toMensaje).collect(Collectors.toList());
    }

    public TicketResponse cerrar(Long id, CerrarTicketRequest req) {
        Ticket t = buscar(id);
        t.setEstado("CERRADO");
        t.setResuelto(req.getResuelto());
        t.setFechaCierre(LocalDateTime.now());
        return toTicket(ticketRepository.save(t));
    }

    private Ticket buscar(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado"));
    }

    private TicketResponse toTicket(Ticket t) {
        return TicketResponse.builder()
                .id(t.getId()).usuarioId(t.getUsuarioId())
                .titulo(t.getTitulo()).descripcion(t.getDescripcion())
                .estado(t.getEstado()).resuelto(t.getResuelto())
                .fechaCreacion(t.getFechaCreacion()).fechaCierre(t.getFechaCierre())
                .build();
    }

    private MensajeResponse toMensaje(TicketMensaje m) {
        return MensajeResponse.builder()
                .id(m.getId()).ticketId(m.getTicketId())
                .autorId(m.getAutorId()).rolAutor(m.getRolAutor())
                .mensaje(m.getMensaje()).fecha(m.getFecha())
                .build();
    }
}