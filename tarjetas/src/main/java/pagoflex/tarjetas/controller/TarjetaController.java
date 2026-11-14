package pagoflex.tarjetas.controller;
import pagoflex.tarjetas.dto.TarjetaRequest;
import pagoflex.tarjetas.dto.TarjetaResponse;
import pagoflex.tarjetas.service.TarjetaService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/tarjetas")
@Tag(name = "Tarjetas")
public class TarjetaController {
    private final TarjetaService service;
    public TarjetaController(TarjetaService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TarjetaResponse agregar(@Valid @RequestBody TarjetaRequest req) {
        return service.agregar(req);
    }

    @GetMapping
    public List<TarjetaResponse> listar(@RequestParam Long usuarioId) {
        return service.listar(usuarioId);
    }

    @PutMapping("/{id}/principal")
    public TarjetaResponse marcarPrincipal(@PathVariable Long id) {
        return service.marcarPrincipal(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
