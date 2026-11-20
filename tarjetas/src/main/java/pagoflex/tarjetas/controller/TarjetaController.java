package pagoflex.tarjetas.controller;

import pagoflex.tarjetas.dto.TarjetaRequest;
import pagoflex.tarjetas.dto.TarjetaResponse;
import pagoflex.tarjetas.service.TarjetaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tarjetas")
@Tag(name = "Tarjetas", description = "Tarjetas guardadas por usuario (solo últimos 4 dígitos, nunca el número completo). "
        + "Máximo 4 tarjetas por usuario y una sola principal.")
public class TarjetaController {
    private final TarjetaService service;
    public TarjetaController(TarjetaService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Agregar tarjeta",
            description = "Guarda una tarjeta del usuario. Si `principal` es true, las demás tarjetas del usuario "
                    + "dejan de ser principales. Si no se envía `marca`, se guarda como \"Tarjeta\".")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarjeta agregada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o el usuario ya tiene el máximo de 4 tarjetas", content = @Content)
    })
    public TarjetaResponse agregar(@Valid @RequestBody TarjetaRequest req) {
        return service.agregar(req);
    }

    @GetMapping
    @Operation(summary = "Listar tarjetas de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarjetas del usuario (puede venir vacía)"),
            @ApiResponse(responseCode = "400", description = "Falta el parámetro usuarioId o es inválido", content = @Content)
    })
    public List<TarjetaResponse> listar(
            @Parameter(description = "Id del usuario", example = "3", required = true) @RequestParam Long usuarioId) {
        return service.listar(usuarioId);
    }

    @PutMapping("/{id}/principal")
    @Operation(summary = "Marcar tarjeta como principal",
            description = "La tarjeta indicada pasa a ser la principal y las demás del mismo usuario dejan de serlo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarjeta marcada como principal"),
            @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada", content = @Content)
    })
    public TarjetaResponse marcarPrincipal(
            @Parameter(description = "Id de la tarjeta", example = "2") @PathVariable Long id) {
        return service.marcarPrincipal(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar tarjeta")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tarjeta eliminada"),
            @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada", content = @Content)
    })
    public void eliminar(
            @Parameter(description = "Id de la tarjeta", example = "2") @PathVariable Long id) {
        service.eliminar(id);
    }
}
