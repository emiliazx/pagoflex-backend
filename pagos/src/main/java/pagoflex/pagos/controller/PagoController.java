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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import pagoflex.pagos.dto.PagoRequest;
import pagoflex.pagos.dto.PagoResponse;
import pagoflex.pagos.service.PagoService;

@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos", description = "Registro e historial de pagos")
@SecurityRequirement(name = "bearerAuth")
public class PagoController {

    private final PagoService service;

    public PagoController(PagoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un pago")
    public PagoResponse registrar(@Valid @RequestBody PagoRequest req) {
        return service.registrar(req);
    }

    @GetMapping
    @Operation(summary = "Listar pagos de un usuario")
    public List<PagoResponse> listar(@RequestParam Long usuarioId) {
        return service.listar(usuarioId);
    }

    @GetMapping("/deuda/{deudaId}")
    @Operation(summary = "Listar pagos de una deuda")
    public List<PagoResponse> porDeuda(@PathVariable Long deudaId) {
        return service.porDeuda(deudaId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un pago")
    public PagoResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }
}
