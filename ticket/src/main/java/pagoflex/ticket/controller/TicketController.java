package pagoflex.ticket.controller;

import pagoflex.ticket.dto.*;
import pagoflex.ticket.service.TicketService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Tickets / Reportar problema")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Usuario reporta un problema")
    public TicketResponse crear(@Valid @RequestBody TicketRequest req) {
        return service.crear(req);
    }

    @GetMapping
    @Operation(summary = "Lista: ?usuarioId= para usuario | ?estado=ABIERTO para gestor")
    public List<TicketResponse> listar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String estado) {
        if (usuarioId != null) {
            return service.listarPorUsuario(usuarioId);
        }
        return service.listarParaGestor(estado);
    }

    @GetMapping("/{id}")
    public TicketResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping("/{id}/mensajes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Mensaje usuario o gestor")
    public MensajeResponse mensaje(@PathVariable Long id, @Valid @RequestBody MensajeRequest req) {
        return service.agregarMensaje(id, req);
    }

    @GetMapping("/{id}/mensajes")
    public List<MensajeResponse> mensajes(@PathVariable Long id) {
        return service.listarMensajes(id);
    }

    @PutMapping("/{id}/cerrar")
    @Operation(summary = "Cerrar ticket indicando si se resolvió")
    public TicketResponse cerrar(@PathVariable Long id, @Valid @RequestBody CerrarTicketRequest req) {
        return service.cerrar(id, req);
    }
}