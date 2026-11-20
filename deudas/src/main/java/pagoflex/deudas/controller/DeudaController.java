package pagoflex.deudas.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import pagoflex.deudas.dto.DeudaRequest;
import pagoflex.deudas.dto.DeudaResponse;
import pagoflex.deudas.service.DeudaService;

@RestController
@RequestMapping("/api/deudas")
@Tag(name = "Deudas", description = "Gestión de deudas. Crear, editar y eliminar es exclusivo del rol GESTOR; "
        + "un USUARIO solo ve y paga sus propias deudas; ADMIN y GESTOR pueden consultar las de cualquiera.")
@SecurityRequirement(name = "bearerAuth")
public class DeudaController {

    private final DeudaService service;

    public DeudaController(DeudaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear deuda (solo GESTOR)",
            description = "Crea una deuda en estado PENDIENTE. Si `cantidadCuotas` es 2 o más, genera las cuotas "
                    + "automáticamente: monto repartido en partes iguales (el resto va a la última) y un "
                    + "vencimiento por mes a partir de `vencimiento`.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Deuda creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o monto muy bajo para esa cantidad de cuotas", content = @Content),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Solo el GESTOR puede crear deudas", content = @Content)
    })
    public DeudaResponse crear(@Valid @RequestBody DeudaRequest req) {
        return service.crear(req);
    }

    @GetMapping
    @Operation(summary = "Listar deudas de un usuario",
            description = "Ordenadas por vencimiento. Todos los filtros son opcionales y se combinan. "
                    + "Un USUARIO solo puede consultar su propio `usuarioId`.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de deudas (puede venir vacía)"),
            @ApiResponse(responseCode = "400", description = "Estado o fecha con formato inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Intento de ver datos de otro usuario", content = @Content)
    })
    public List<DeudaResponse> listar(
            @Parameter(description = "Id del usuario dueño de las deudas", example = "3", required = true)
            @RequestParam Long usuarioId,
            @Parameter(description = "Filtrar por estado", example = "PENDIENTE")
            @RequestParam(required = false) String estado,
            @Parameter(description = "Vencimiento desde (inclusive), formato yyyy-MM-dd", example = "2026-11-01")
            @RequestParam(required = false) String desde,
            @Parameter(description = "Vencimiento hasta (inclusive), formato yyyy-MM-dd", example = "2026-12-31")
            @RequestParam(required = false) String hasta,
            @Parameter(description = "Monto mínimo", example = "10000")
            @RequestParam(required = false) BigDecimal montoMin,
            @Parameter(description = "Monto máximo", example = "500000")
            @RequestParam(required = false) BigDecimal montoMax) {
        return service.listar(usuarioId, estado, desde, hasta, montoMin, montoMax);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una deuda",
            description = "Incluye el avance de pago: cuotas pagadas, cuota actual y monto pagado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deuda encontrada"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "La deuda pertenece a otro usuario", content = @Content),
            @ApiResponse(responseCode = "404", description = "La deuda no existe", content = @Content)
    })
    public DeudaResponse obtener(
            @Parameter(description = "Id de la deuda", example = "1") @PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar deuda (solo GESTOR)",
            description = "No se puede cambiar el usuario de la deuda. Si ya hay cuotas pagadas (o la deuda está "
                    + "pagada) solo se pueden editar concepto y empresa. Si cambian monto, vencimiento o cantidad "
                    + "de cuotas, las cuotas se regeneran.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deuda actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o se intentó cambiar el usuario de la deuda", content = @Content),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Solo el GESTOR puede editar deudas", content = @Content),
            @ApiResponse(responseCode = "404", description = "La deuda no existe", content = @Content),
            @ApiResponse(responseCode = "409", description = "La deuda ya tiene pagos: solo concepto y empresa son editables", content = @Content)
    })
    public DeudaResponse actualizar(
            @Parameter(description = "Id de la deuda", example = "1") @PathVariable Long id,
            @Valid @RequestBody DeudaRequest req) {
        return service.actualizar(id, req);
    }

    @PutMapping("/{id}/pagar")
    @Operation(summary = "Pagar la deuda completa",
            description = "Marca la deuda como PAGADA y liquida todas sus cuotas pendientes con la fecha de hoy. "
                    + "Es idempotente: si ya estaba pagada devuelve su estado actual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deuda pagada"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "La deuda pertenece a otro usuario", content = @Content),
            @ApiResponse(responseCode = "404", description = "La deuda no existe", content = @Content)
    })
    public DeudaResponse pagar(
            @Parameter(description = "Id de la deuda", example = "1") @PathVariable Long id) {
        return service.pagar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar deuda y sus cuotas (solo GESTOR)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deuda y cuotas eliminadas"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Solo el GESTOR puede eliminar deudas", content = @Content),
            @ApiResponse(responseCode = "404", description = "La deuda no existe", content = @Content)
    })
    public void eliminar(
            @Parameter(description = "Id de la deuda", example = "1") @PathVariable Long id) {
        service.eliminar(id);
    }
}
