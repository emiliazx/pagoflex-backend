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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import pagoflex.deudas.dto.CuotaResponse;
import pagoflex.deudas.dto.GenerarCuotasRequest;
import pagoflex.deudas.service.CuotaService;

@RestController
@RequestMapping("/api/deudas")
@Tag(name = "Cuotas", description = "Cuotas de una deuda")
@SecurityRequirement(name = "bearerAuth")
public class CuotaController {

    private final CuotaService service;

    public CuotaController(CuotaService service) {
        this.service = service;
    }

    @PostMapping("/cuotas/generar")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Generar cuotas de una deuda (solo GESTOR)")
    public List<CuotaResponse> generar(@Valid @RequestBody GenerarCuotasRequest req) {
        return service.generar(req);
    }

    @GetMapping("/{deudaId}/cuotas")
    @Operation(summary = "Listar cuotas de una deuda")
    public List<CuotaResponse> porDeuda(@PathVariable Long deudaId) {
        return service.porDeuda(deudaId);
    }

    @GetMapping("/cuotas")
    @Operation(summary = "Listar cuotas de un usuario, con filtro opcional de estado")
    public List<CuotaResponse> porUsuario(@RequestParam Long usuarioId,
                                          @RequestParam(required = false) String estado) {
        return service.porUsuario(usuarioId, estado);
    }

    @PutMapping("/cuotas/{id}/pagar")
    @Operation(summary = "Pagar una cuota")
    public CuotaResponse pagar(@PathVariable Long id) {
        return service.pagar(id);
    }

    @GetMapping("/{deudaId}/cuotas/siguiente")
    @Operation(summary = "Siguiente cuota pendiente de una deuda")
    public CuotaResponse siguiente(@PathVariable Long deudaId) {
        return service.siguiente(deudaId);
    }
}
