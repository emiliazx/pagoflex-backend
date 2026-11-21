package pagoflex.ticket.service;

import pagoflex.ticket.dto.AdjuntoResponse;
import pagoflex.ticket.dto.CerrarTicketRequest;
import pagoflex.ticket.dto.MensajeRequest;
import pagoflex.ticket.dto.MensajeResponse;
import pagoflex.ticket.dto.TicketRequest;
import pagoflex.ticket.dto.TicketResponse;
import pagoflex.ticket.model.Ticket;
import pagoflex.ticket.model.TicketAdjunto;
import pagoflex.ticket.model.TicketMensaje;
import pagoflex.ticket.repository.TicketAdjuntoRepository;
import pagoflex.ticket.repository.TicketMensajeRepository;
import pagoflex.ticket.repository.TicketRepository;
import pagoflex.ticket.security.Acceso;
import pagoflex.ticket.storage.AlmacenAdjuntos;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {

    public record ArchivoAdjunto(byte[] datos, String tipoContenido) {
    }

    private static final int MAX_ADJUNTOS = 5;
    private static final String ABIERTO = "ABIERTO";
    private static final String EN_PROCESO = "EN_PROCESO";
    private static final String CERRADO = "CERRADO";

    private final TicketRepository ticketRepository;
    private final TicketMensajeRepository mensajeRepository;
    private final TicketAdjuntoRepository adjuntoRepository;
    private final AlmacenAdjuntos almacen;

    public TicketService(TicketRepository ticketRepository,
                         TicketMensajeRepository mensajeRepository,
                         TicketAdjuntoRepository adjuntoRepository,
                         AlmacenAdjuntos almacen) {
        this.ticketRepository = ticketRepository;
        this.mensajeRepository = mensajeRepository;
        this.adjuntoRepository = adjuntoRepository;
        this.almacen = almacen;
    }

    @Transactional
    public TicketResponse crear(TicketRequest req) {
        if (!"USUARIO".equals(Acceso.rolActual())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo los usuarios pueden reportar un problema");
        }
        Long usuarioId = Acceso.idActual();
        if (usuarioId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token sin identificador de usuario");
        }
        Ticket t = Ticket.builder()
                .usuarioId(usuarioId)
                .usuarioCorreo(Acceso.correoActual())
                .titulo(req.getTitulo().trim())
                .descripcion(req.getDescripcion().trim())
                .deudaId(req.getDeudaId())
                .referencia(req.getReferencia() == null || req.getReferencia().isBlank()
                        ? null : req.getReferencia().trim())
                .estado(ABIERTO)
                .resuelto(null)
                .fechaCreacion(LocalDateTime.now())
                .build();
        return toTicket(ticketRepository.save(t));
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> listar(String estado, Long usuarioId) {
        boolean personal = Acceso.esPersonal();
        List<Ticket> base = personal
                ? ticketRepository.findAllByOrderByFechaCreacionDesc()
                : ticketRepository.findByUsuarioIdOrderByFechaCreacionDesc(Acceso.idActual());
        return base.stream()
                .filter(t -> estado == null || estado.isBlank() || t.getEstado().equalsIgnoreCase(estado.trim()))
                .filter(t -> !personal || usuarioId == null || t.getUsuarioId().equals(usuarioId))
                .map(this::toTicket)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketResponse obtener(Long id) {
        Ticket t = buscar(id);
        Acceso.exigirAcceso(t.getUsuarioId());
        return toTicket(t);
    }

    @Transactional
    public MensajeResponse agregarMensaje(Long ticketId, MensajeRequest req) {
        Ticket t = buscar(ticketId);
        exigirParticipante(t);
        if (CERRADO.equals(t.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El ticket ya está cerrado");
        }
        TicketMensaje m = TicketMensaje.builder()
                .ticketId(ticketId)
                .autorId(Acceso.idActual())
                .rolAutor(Acceso.rolActual())
                .mensaje(req.getMensaje().trim())
                .fecha(LocalDateTime.now())
                .build();
        if (Acceso.esGestor() && ABIERTO.equals(t.getEstado())) {
            t.setEstado(EN_PROCESO);
            ticketRepository.save(t);
        }
        return toMensaje(mensajeRepository.save(m));
    }

    @Transactional(readOnly = true)
    public List<MensajeResponse> listarMensajes(Long ticketId) {
        Ticket t = buscar(ticketId);
        Acceso.exigirAcceso(t.getUsuarioId());
        return mensajeRepository.findByTicketIdOrderByFechaAsc(ticketId)
                .stream().map(this::toMensaje).collect(Collectors.toList());
    }

    @Transactional
    public AdjuntoResponse subirAdjunto(Long ticketId, MultipartFile archivo) {
        Ticket t = buscar(ticketId);
        Long actual = Acceso.idActual();
        if (actual == null || !actual.equals(t.getUsuarioId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo quien creó el ticket puede adjuntar pruebas");
        }
        if (CERRADO.equals(t.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El ticket ya está cerrado");
        }
        if (adjuntoRepository.countByTicketId(ticketId) >= MAX_ADJUNTOS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un ticket admite como máximo " + MAX_ADJUNTOS + " fotos");
        }
        AlmacenAdjuntos.Guardado guardado = almacen.guardar(archivo);
        String nombreOriginal = archivo.getOriginalFilename();
        TicketAdjunto a = TicketAdjunto.builder()
                .ticketId(ticketId)
                .archivo(guardado.archivo())
                .nombreOriginal(nombreOriginal == null || nombreOriginal.isBlank() ? "imagen" : nombreOriginal)
                .tipoContenido(guardado.tipoContenido())
                .tamano(guardado.tamano())
                .fecha(LocalDateTime.now())
                .build();
        return toAdjunto(adjuntoRepository.save(a));
    }

    @Transactional(readOnly = true)
    public List<AdjuntoResponse> listarAdjuntos(Long ticketId) {
        Ticket t = buscar(ticketId);
        Acceso.exigirAcceso(t.getUsuarioId());
        return adjuntoRepository.findByTicketIdOrderByFechaAsc(ticketId)
                .stream().map(this::toAdjunto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ArchivoAdjunto descargarAdjunto(Long ticketId, Long adjuntoId) {
        Ticket t = buscar(ticketId);
        Acceso.exigirAcceso(t.getUsuarioId());
        TicketAdjunto a = adjuntoRepository.findById(adjuntoId)
                .filter(x -> x.getTicketId().equals(ticketId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Adjunto no encontrado"));
        return new ArchivoAdjunto(almacen.leer(a.getArchivo()), a.getTipoContenido());
    }

    @Transactional
    public TicketResponse cerrar(Long id, CerrarTicketRequest req) {
        Acceso.exigirGestor();
        Ticket t = buscar(id);
        if (CERRADO.equals(t.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El ticket ya está cerrado");
        }
        t.setEstado(CERRADO);
        t.setResuelto(req.getResuelto());
        t.setFechaCierre(LocalDateTime.now());
        t.setCerradoPorId(Acceso.idActual());
        return toTicket(ticketRepository.save(t));
    }

    private void exigirParticipante(Ticket t) {
        if (Acceso.esAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El administrador solo puede consultar los tickets");
        }
        Acceso.exigirAcceso(t.getUsuarioId());
    }

    private Ticket buscar(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket no encontrado"));
    }

    private TicketResponse toTicket(Ticket t) {
        return TicketResponse.builder()
                .id(t.getId())
                .usuarioId(t.getUsuarioId())
                .usuarioCorreo(t.getUsuarioCorreo())
                .titulo(t.getTitulo())
                .descripcion(t.getDescripcion())
                .deudaId(t.getDeudaId())
                .referencia(t.getReferencia())
                .estado(t.getEstado())
                .resuelto(t.getResuelto())
                .fechaCreacion(t.getFechaCreacion())
                .fechaCierre(t.getFechaCierre())
                .build();
    }

    private MensajeResponse toMensaje(TicketMensaje m) {
        return MensajeResponse.builder()
                .id(m.getId())
                .ticketId(m.getTicketId())
                .autorId(m.getAutorId())
                .rolAutor(m.getRolAutor())
                .mensaje(m.getMensaje())
                .fecha(m.getFecha())
                .build();
    }

    private AdjuntoResponse toAdjunto(TicketAdjunto a) {
        return AdjuntoResponse.builder()
                .id(a.getId())
                .ticketId(a.getTicketId())
                .nombreOriginal(a.getNombreOriginal())
                .tipoContenido(a.getTipoContenido())
                .tamano(a.getTamano())
                .url("/api/tickets/" + a.getTicketId() + "/adjuntos/" + a.getId())
                .fecha(a.getFecha())
                .build();
    }
}
