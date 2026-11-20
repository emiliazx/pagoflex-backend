package pagoflex.pagos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import pagoflex.pagos.dto.PagoRequest;
import pagoflex.pagos.dto.PagoResponse;
import pagoflex.pagos.service.PagoService;

@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos", description = "Registro e historial de pagos. Un USUARIO solo accede a sus propios pagos; "
        + "ADMIN y GESTOR pueden consultar los de cualquiera.")
@SecurityRequirement(name = "bearerAuth")
public class PagoController {

    private final PagoService service;

    public PagoController(PagoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un pago",
            description = "Guarda el pago con la fecha y hora actuales. Si no se envía `metodo`, se usa TARJETA; "
                    + "el método se guarda en mayúsculas. Este servicio solo registra el pago: "
                    + "no actualiza el estado de la deuda (eso se hace en el servicio de deudas).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pago registrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (monto, ids o método fuera de rango)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Intento de registrar un pago a nombre de otro usuario", content = @Content)
    })
    public PagoResponse registrar(@Valid @RequestBody PagoRequest req) {
        return service.registrar(req);
    }

    @GetMapping
    @Operation(summary = "Listar pagos de un usuario",
            description = "Ordenados del más reciente al más antiguo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial de pagos (puede venir vacío)"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Intento de ver pagos de otro usuario", content = @Content)
    })
    public List<PagoResponse> listar(
            @Parameter(description = "Id del usuario", example = "3", required = true) @RequestParam Long usuarioId) {
        return service.listar(usuarioId);
    }

    @GetMapping("/deuda/{deudaId}")
    @Operation(summary = "Listar pagos de una deuda",
            description = "Ordenados del más reciente al más antiguo. Un USUARIO solo ve los pagos que hizo él; "
                    + "ADMIN y GESTOR ven todos. No devuelve 403: los pagos ajenos simplemente se omiten.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagos de la deuda (puede venir vacío)"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content)
    })
    public List<PagoResponse> porDeuda(
            @Parameter(description = "Id de la deuda", example = "1") @PathVariable Long deudaId) {
        return service.porDeuda(deudaId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un pago")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago encontrado"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "El pago pertenece a otro usuario", content = @Content),
            @ApiResponse(responseCode = "404", description = "El pago no existe", content = @Content)
    })
    public PagoResponse obtener(
            @Parameter(description = "Id del pago", example = "12") @PathVariable Long id) {
        return service.obtener(id);
    }
}
