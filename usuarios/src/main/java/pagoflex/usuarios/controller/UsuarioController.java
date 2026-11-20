package pagoflex.usuarios.controller;

import pagoflex.usuarios.dto.*;
import pagoflex.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest req) {
        return service.registrar(req);
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public List<UsuarioResponse> listar(@RequestParam(required = false) String buscar) {
        return service.listar(buscar);
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Usuarios pendientes de habilitar")
    public List<UsuarioResponse> listarPendientes() {
        return service.listarPendientes();
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public UsuarioResponse actualizar(@PathVariable Long id, @RequestBody ActualizarUsuarioRequest req) {
        return service.actualizar(id, req);
    }

    @PutMapping("/{id}/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    public void cambiarContrasena(@PathVariable Long id, @Valid @RequestBody CambiarContrasenaRequest req) {
        service.cambiarContrasena(id, req);
    }

    @PutMapping("/{id}/habilitar")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Habilitar cuenta (admin/gestor)")
    public UsuarioResponse habilitar(@PathVariable Long id) {
        return service.habilitar(id);
    }

    @PutMapping("/{id}/deshabilitar")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @SecurityRequirement(name = "bearerAuth")
    public UsuarioResponse deshabilitar(@PathVariable Long id) {
        return service.deshabilitar(id);
    }

    @PutMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Subir foto → guarda archivo y URL en BD")
    public UsuarioResponse subirFoto(@PathVariable Long id, @RequestParam("foto") MultipartFile archivo) {
        return service.guardarFoto(id, archivo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}