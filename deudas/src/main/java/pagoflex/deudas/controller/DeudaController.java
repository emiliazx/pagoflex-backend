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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import pagoflex.deudas.dto.DeudaRequest;
import pagoflex.deudas.dto.DeudaResponse;
import pagoflex.deudas.service.DeudaService;

@RestController
@RequestMapping("/api/deudas")
@Tag(name = "Deudas", description = "Gestion de deudas")
@SecurityRequirement(name = "bearerAuth")
public class DeudaController {

    private final DeudaService service;

    public DeudaController(DeudaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear deuda (solo GESTOR)")
    public DeudaResponse crear(@Valid @RequestBody DeudaRequest req) {
        return service.crear(req);
    }

    @GetMapping
    @Operation(summary = "Listar deudas de un usuario con filtros opcionales")
    public List<DeudaResponse> listar(@RequestParam Long usuarioId,
                                      @RequestParam(required = false) String estado,
                                      @RequestParam(required = false) String desde,
                                      @RequestParam(required = false) String hasta,
                                      @RequestParam(required = false) BigDecimal montoMin,
                                      @RequestParam(required = false) BigDecimal montoMax) {
        return service.listar(usuarioId, estado, desde, hasta, montoMin, montoMax);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una deuda")
    public DeudaResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar deuda (solo GESTOR)")
    public DeudaResponse actualizar(@PathVariable Long id, @Valid @RequestBody DeudaRequest req) {
        return service.actualizar(id, req);
    }

    @PutMapping("/{id}/pagar")
    @Operation(summary = "Marcar deuda como pagada y liquidar sus cuotas pendientes")
    public DeudaResponse pagar(@PathVariable Long id) {
        return service.pagar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar deuda y sus cuotas (solo GESTOR)")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
