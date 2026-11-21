package pagoflex.ticket.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import pagoflex.ticket.dto.AdjuntoResponse;
import pagoflex.ticket.dto.CerrarTicketRequest;
import pagoflex.ticket.dto.MensajeRequest;
import pagoflex.ticket.dto.MensajeResponse;
import pagoflex.ticket.dto.TicketRequest;
import pagoflex.ticket.dto.TicketResponse;
import pagoflex.ticket.service.TicketService;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Tickets", description = "Reportar un problema. El USUARIO crea el ticket y adjunta pruebas; "
        + "el GESTOR responde y lo cierra; el ADMIN solo consulta.")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Reportar un problema (solo USUARIO)",
            description = "Crea un ticket ABIERTO a nombre del usuario del token. Las fotos de prueba se suben después "
                    + "con POST /{id}/adjuntos.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Solo un USUARIO puede reportar problemas", content = @Content)
    })
    public TicketResponse crear(@Valid @RequestBody TicketRequest req) {
        return service.crear(req);
    }

    @GetMapping
    @Operation(summary = "Listar tickets",
            description = "Un USUARIO ve solo los suyos. GESTOR y ADMIN ven todos y pueden filtrar por `estado` "
                    + "(ABIERTO, EN_PROCESO, CERRADO) y por `usuarioId`. Del más reciente al más antiguo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de tickets (puede venir vacía)"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content)
    })
    public List<TicketResponse> listar(
            @Parameter(description = "Filtrar por estado", example = "ABIERTO") @RequestParam(required = false) String estado,
            @Parameter(description = "Solo para GESTOR y ADMIN: filtrar por usuario", example = "3") @RequestParam(required = false) Long usuarioId) {
        return service.listar(estado, usuarioId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket encontrado"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "El ticket pertenece a otro usuario", content = @Content),
            @ApiResponse(responseCode = "404", description = "El ticket no existe", content = @Content)
    })
    public TicketResponse obtener(
            @Parameter(description = "Id del ticket", example = "4") @PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping("/{id}/mensajes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enviar un mensaje en el ticket (USUARIO dueño o GESTOR)",
            description = "El autor y su rol salen del token. Cuando el GESTOR responde un ticket ABIERTO, "
                    + "pasa a EN_PROCESO.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mensaje enviado"),
            @ApiResponse(responseCode = "400", description = "Mensaje vacío o demasiado largo", content = @Content),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "No eres el dueño del ticket ni GESTOR", content = @Content),
            @ApiResponse(responseCode = "404", description = "El ticket no existe", content = @Content),
            @ApiResponse(responseCode = "409", description = "El ticket ya está cerrado", content = @Content)
    })
    public MensajeResponse mensaje(
            @Parameter(description = "Id del ticket", example = "4") @PathVariable Long id,
            @Valid @RequestBody MensajeRequest req) {
        return service.agregarMensaje(id, req);
    }

    @GetMapping("/{id}/mensajes")
    @Operation(summary = "Listar los mensajes de un ticket", description = "Del más antiguo al más reciente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conversación del ticket"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "El ticket pertenece a otro usuario", content = @Content),
            @ApiResponse(responseCode = "404", description = "El ticket no existe", content = @Content)
    })
    public List<MensajeResponse> mensajes(
            @Parameter(description = "Id del ticket", example = "4") @PathVariable Long id) {
        return service.listarMensajes(id);
    }

    @PostMapping(value = "/{id}/adjuntos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adjuntar una foto de prueba (solo el USUARIO dueño)",
            description = "Imagen JPG o PNG de hasta 5 MB en el campo `archivo`; máximo 5 por ticket. "
                    + "El archivo queda en el servidor del microservicio y en la base de datos solo se guardan sus datos.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Adjunto guardado"),
            @ApiResponse(responseCode = "400", description = "Archivo vacío, muy grande, que no es imagen o ticket con 5 fotos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Solo quien creó el ticket puede adjuntar", content = @Content),
            @ApiResponse(responseCode = "404", description = "El ticket no existe", content = @Content),
            @ApiResponse(responseCode = "409", description = "El ticket ya está cerrado", content = @Content)
    })
    public AdjuntoResponse subirAdjunto(
            @Parameter(description = "Id del ticket", example = "4") @PathVariable Long id,
            @Parameter(description = "Imagen de prueba (máx. 5 MB)") @RequestParam("archivo") MultipartFile archivo) {
        return service.subirAdjunto(id, archivo);
    }

    @GetMapping("/{id}/adjuntos")
    @Operation(summary = "Listar los adjuntos de un ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adjuntos del ticket (pueden venir vacíos)"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "El ticket pertenece a otro usuario", content = @Content),
            @ApiResponse(responseCode = "404", description = "El ticket no existe", content = @Content)
    })
    public List<AdjuntoResponse> adjuntos(
            @Parameter(description = "Id del ticket", example = "4") @PathVariable Long id) {
        return service.listarAdjuntos(id);
    }

    @GetMapping("/{id}/adjuntos/{adjuntoId}")
    @Operation(summary = "Descargar un adjunto", description = "Devuelve la imagen en bytes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagen adjunta",
                    content = @Content(mediaType = MediaType.IMAGE_JPEG_VALUE,
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "El ticket pertenece a otro usuario", content = @Content),
            @ApiResponse(responseCode = "404", description = "El ticket o el adjunto no existen", content = @Content)
    })
    public ResponseEntity<byte[]> descargarAdjunto(
            @Parameter(description = "Id del ticket", example = "4") @PathVariable Long id,
            @Parameter(description = "Id del adjunto", example = "7") @PathVariable Long adjuntoId) {
        TicketService.ArchivoAdjunto archivo = service.descargarAdjunto(id, adjuntoId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.tipoContenido()))
                .body(archivo.datos());
    }

    @PutMapping("/{id}/cerrar")
    @Operation(summary = "Cerrar un ticket (solo GESTOR)",
            description = "Lo deja CERRADO e indica si el problema quedó resuelto (true) o se cierra sin resolver (false).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket cerrado"),
            @ApiResponse(responseCode = "400", description = "Falta indicar `resuelto`", content = @Content),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Solo el GESTOR puede cerrar tickets", content = @Content),
            @ApiResponse(responseCode = "404", description = "El ticket no existe", content = @Content),
            @ApiResponse(responseCode = "409", description = "El ticket ya estaba cerrado", content = @Content)
    })
    public TicketResponse cerrar(
            @Parameter(description = "Id del ticket", example = "4") @PathVariable Long id,
            @Valid @RequestBody CerrarTicketRequest req) {
        return service.cerrar(id, req);
    }
}
