package pagoflex.usuarios.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import pagoflex.usuarios.dto.ActualizarUsuarioRequest;
import pagoflex.usuarios.dto.CambiarContrasenaRequest;
import pagoflex.usuarios.dto.LoginRequest;
import pagoflex.usuarios.dto.LoginResponse;
import pagoflex.usuarios.dto.RecuperarRequest;
import pagoflex.usuarios.dto.RegistroRequest;
import pagoflex.usuarios.dto.UsuarioResponse;
import pagoflex.usuarios.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Autenticación JWT, registro, recuperación de cuenta y perfil. "
        + "Roles: ADMIN, GESTOR y USUARIO.")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
            description = "Valida correo y contraseña y devuelve un JWT Bearer. "
                    + "Tras 5 intentos fallidos consecutivos la cuenta se bloquea (423) "
                    + "hasta que un ADMIN la desbloquee o se recupere con preguntas secretas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto, devuelve el token y los datos del usuario"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (correo con formato incorrecto o campos vacíos)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas; el mensaje indica los intentos restantes", content = @Content),
            @ApiResponse(responseCode = "423", description = "Cuenta bloqueada por demasiados intentos fallidos", content = @Content)
    })
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar usuario (público)",
            description = "Crea una cuenta con rol USUARIO. La contraseña se guarda con BCrypt y las "
                    + "respuestas secretas con SHA-256 (normalizadas a minúsculas y sin espacios en los bordes).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (por ejemplo, contraseña sin la complejidad exigida)", content = @Content),
            @ApiResponse(responseCode = "409", description = "El correo ya está registrado", content = @Content)
    })
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest req) {
        return service.registrar(req);
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Listar usuarios",
            description = "Devuelve todos los usuarios. Con `buscar` filtra por coincidencia parcial "
                    + "en nombre o correo, sin distinguir mayúsculas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuarios (puede venir vacía)"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content)
    })
    public List<UsuarioResponse> listar(
            @Parameter(description = "Texto a buscar en nombre o correo", example = "emilia")
            @RequestParam(required = false) String buscar) {
        return service.listar(buscar);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener un usuario por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public UsuarioResponse obtener(
            @Parameter(description = "Id del usuario", example = "3") @PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualizar perfil",
            description = "Actualiza solo los campos enviados (los que vienen nulos se ignoran). "
                    + "No cambia correo, rol ni contraseña.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil actualizado"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public UsuarioResponse actualizar(
            @Parameter(description = "Id del usuario", example = "3") @PathVariable Long id,
            @RequestBody ActualizarUsuarioRequest req) {
        return service.actualizar(id, req);
    }

    @PutMapping("/{id}/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cambiar contraseña",
            description = "Requiere la contraseña actual y una nueva con mínimo 8 caracteres, "
                    + "mayúscula, minúscula, número y carácter especial.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contraseña cambiada"),
            @ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta o nueva sin la complejidad exigida", content = @Content),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public void cambiarContrasena(
            @Parameter(description = "Id del usuario", example = "3") @PathVariable Long id,
            @Valid @RequestBody CambiarContrasenaRequest req) {
        service.cambiarContrasena(id, req);
    }

    @PostMapping("/recuperar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Recuperar cuenta con preguntas secretas (público)",
            description = "Si las dos respuestas coinciden, fija la nueva contraseña, "
                    + "reinicia los intentos fallidos y desbloquea la cuenta.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contraseña restablecida y cuenta desbloqueada"),
            @ApiResponse(responseCode = "400", description = "Respuestas incorrectas o nueva contraseña sin la complejidad exigida", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con ese correo", content = @Content)
    })
    public void recuperar(@Valid @RequestBody RecuperarRequest req) {
        service.recuperar(req);
    }

    @PutMapping("/{id}/desbloquear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Desbloquear cuenta (solo ADMIN)",
            description = "Quita el bloqueo y reinicia el contador de intentos fallidos.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cuenta desbloqueada"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no es ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public void desbloquear(
            @Parameter(description = "Id del usuario a desbloquear", example = "3") @PathVariable Long id) {
        service.desbloquear(id);
    }

    @PutMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Subir foto de perfil",
            description = "Envía la imagen como multipart/form-data en el campo `foto`. Tamaño máximo: 5 MB.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Foto guardada"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public void subirFoto(
            @Parameter(description = "Id del usuario", example = "3") @PathVariable Long id,
            @Parameter(description = "Archivo de imagen (máx. 5 MB)") @RequestParam("foto") MultipartFile archivo)
            throws Exception {
        service.guardarFoto(id, archivo.getBytes());
    }

    @GetMapping(value = "/{id}/foto", produces = MediaType.IMAGE_JPEG_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener foto de perfil",
            description = "Devuelve la imagen en bytes (image/jpeg).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagen del usuario",
                    content = @Content(mediaType = MediaType.IMAGE_JPEG_VALUE,
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado o sin foto", content = @Content)
    })
    public ResponseEntity<byte[]> obtenerFoto(
            @Parameter(description = "Id del usuario", example = "3") @PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerFoto(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar usuario (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no es ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public void eliminar(
            @Parameter(description = "Id del usuario a eliminar", example = "3") @PathVariable Long id) {
        service.eliminar(id);
    }
}
