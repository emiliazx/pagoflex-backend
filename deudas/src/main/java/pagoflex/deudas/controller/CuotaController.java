package pagoflex.deudas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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
import pagoflex.deudas.dto.CuotaResponse;
import pagoflex.deudas.dto.GenerarCuotasRequest;
import pagoflex.deudas.service.CuotaService;

@RestController
@RequestMapping("/api/deudas")
@Tag(name = "Cuotas", description = "Cuotas de una deuda. Las cuotas se pagan en orden: no se puede pagar "
        + "una cuota si hay una anterior pendiente.")
@SecurityRequirement(name = "bearerAuth")
public class CuotaController {

    private final CuotaService service;

    public CuotaController(CuotaService service) {
        this.service = service;
    }

    @PostMapping("/cuotas/generar")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Generar (o regenerar) cuotas de una deuda (solo GESTOR)",
            description = "Reemplaza las cuotas existentes de la deuda. El monto se reparte en partes iguales "
                    + "(el resto va a la última cuota) con un vencimiento por mes desde `primerVencimiento`. "
                    + "No se permite si la deuda está pagada o ya tiene cuotas pagadas.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuotas generadas"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, la deuda no pertenece a ese usuario o el monto es muy bajo para esa cantidad de cuotas", content = @Content),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Solo el GESTOR puede generar cuotas", content = @Content),
            @ApiResponse(responseCode = "404", description = "La deuda no existe", content = @Content),
            @ApiResponse(responseCode = "409", description = "La deuda ya está pagada o tiene cuotas pagadas", content = @Content)
    })
    public List<CuotaResponse> generar(@Valid @RequestBody GenerarCuotasRequest req) {
        return service.generar(req);
    }

    @GetMapping("/{deudaId}/cuotas")
    @Operation(summary = "Listar cuotas de una deuda",
            description = "Ordenadas por número de cuota.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuotas de la deuda (vacío si la deuda no tiene cuotas)"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "La deuda pertenece a otro usuario", content = @Content),
            @ApiResponse(responseCode = "404", description = "La deuda no existe", content = @Content)
    })
    public List<CuotaResponse> porDeuda(
            @Parameter(description = "Id de la deuda", example = "1") @PathVariable Long deudaId) {
        return service.porDeuda(deudaId);
    }

    @GetMapping("/cuotas")
    @Operation(summary = "Listar cuotas de un usuario",
            description = "Ordenadas por vencimiento. Se puede filtrar por estado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuotas del usuario (puede venir vacía)"),
            @ApiResponse(responseCode = "400", description = "Estado inválido (usa PENDIENTE o PAGADA)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Intento de ver datos de otro usuario", content = @Content)
    })
    public List<CuotaResponse> porUsuario(
            @Parameter(description = "Id del usuario", example = "3", required = true) @RequestParam Long usuarioId,
            @Parameter(description = "Filtrar por estado", example = "PENDIENTE") @RequestParam(required = false) String estado) {
        return service.porUsuario(usuarioId, estado);
    }

    @PutMapping("/cuotas/{id}/pagar")
    @Operation(summary = "Pagar una cuota",
            description = "Marca la cuota como PAGADA con la fecha de hoy. Si era la última pendiente, "
                    + "la deuda pasa a PAGADA. Si la cuota ya estaba pagada devuelve su estado actual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuota pagada"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "La cuota pertenece a otro usuario", content = @Content),
            @ApiResponse(responseCode = "404", description = "La cuota no existe", content = @Content),
            @ApiResponse(responseCode = "409", description = "Hay cuotas anteriores pendientes: debes pagarlas primero", content = @Content)
    })
    public CuotaResponse pagar(
            @Parameter(description = "Id de la cuota", example = "7") @PathVariable Long id) {
        return service.pagar(id);
    }

    @GetMapping("/{deudaId}/cuotas/siguiente")
    @Operation(summary = "Siguiente cuota pendiente de una deuda",
            description = "Devuelve la cuota pendiente con menor número.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Siguiente cuota pendiente"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "La deuda pertenece a otro usuario", content = @Content),
            @ApiResponse(responseCode = "404", description = "La deuda no existe o no tiene cuotas pendientes", content = @Content)
    })
    public CuotaResponse siguiente(
            @Parameter(description = "Id de la deuda", example = "1") @PathVariable Long deudaId) {
        return service.siguiente(deudaId);
    }
}
