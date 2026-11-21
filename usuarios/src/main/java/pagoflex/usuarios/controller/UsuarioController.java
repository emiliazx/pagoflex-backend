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
import pagoflex.usuarios.security.Acceso;
import pagoflex.usuarios.service.UsuarioService;
import pagoflex.usuarios.storage.AlmacenFotos;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Autenticación JWT, registro, recuperación de cuenta y perfil. "
        + "Roles: ADMIN, GESTOR y USUARIO.")
public class UsuarioController {

    private final UsuarioService service;
    private final AlmacenFotos almacenFotos;

    public UsuarioController(UsuarioService service, AlmacenFotos almacenFotos) {
        this.service = service;
        this.almacenFotos = almacenFotos;
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
            @ApiResponse(responseCode = "403", description = "La cuenta aún no fue habilitada por un gestor", content = @Content),
            @ApiResponse(responseCode = "423", description = "Cuenta bloqueada por demasiados intentos fallidos", content = @Content)
    })
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar usuario (público)",
            description = "Crea una cuenta con rol USUARIO que queda pendiente: no puede iniciar sesión hasta que un GESTOR la habilite. La contraseña se guarda con BCrypt y las "
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

    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('GESTOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Listar cuentas pendientes de aprobación (solo GESTOR)",
            description = "Usuarios registrados que todavía no pueden iniciar sesión.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuentas pendientes (puede venir vacía)"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no es GESTOR", content = @Content)
    })
    public List<UsuarioResponse> pendientes() {
        return service.listarPendientes();
    }

    @PutMapping("/{id}/habilitar")
    @PreAuthorize("hasRole('GESTOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Habilitar una cuenta (solo GESTOR)",
            description = "Desde este momento el usuario puede iniciar sesión.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta habilitada"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no es GESTOR", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public UsuarioResponse habilitar(
            @Parameter(description = "Id del usuario a habilitar", example = "5") @PathVariable Long id) {
        return service.habilitar(id);
    }

    @PutMapping("/{id}/deshabilitar")
    @PreAuthorize("hasRole('GESTOR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Deshabilitar una cuenta (solo GESTOR)",
            description = "Impide nuevos inicios de sesión. Solo aplica a cuentas con rol USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta deshabilitada"),
            @ApiResponse(responseCode = "400", description = "La cuenta no tiene rol USUARIO", content = @Content),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no es GESTOR", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public UsuarioResponse deshabilitar(
            @Parameter(description = "Id del usuario a deshabilitar", example = "5") @PathVariable Long id) {
        return service.deshabilitar(id);
    }

    @PutMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Subir foto de perfil",
            description = "Envía la imagen (JPG o PNG, máx. 5 MB) como multipart/form-data en el campo `foto`. "
                    + "El archivo se guarda en el servidor del microservicio y en la base de datos solo queda su URL, "
                    + "que se devuelve en `fotoUrl`.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Foto guardada; devuelve el usuario con su nueva fotoUrl"),
            @ApiResponse(responseCode = "400", description = "Archivo vacío, demasiado grande o que no es una imagen", content = @Content),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Intento de cambiar la foto de otro usuario", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public UsuarioResponse subirFoto(
            @Parameter(description = "Id del usuario", example = "3") @PathVariable Long id,
            @Parameter(description = "Archivo de imagen (máx. 5 MB)") @RequestParam("foto") MultipartFile archivo) {
        Acceso.exigirAcceso(id);
        service.obtener(id);
        String url = almacenFotos.guardar(id, archivo);
        String anterior = service.cambiarFoto(id, url);
        almacenFotos.eliminar(anterior);
        return service.obtener(id);
    }

    @GetMapping("/fotos/{archivo}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Descargar una foto de perfil por su URL",
            description = "Es la ruta que se guarda en `fotoUrl`. Devuelve la imagen en bytes (image/jpeg o image/png).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagen del usuario",
                    content = @Content(mediaType = MediaType.IMAGE_JPEG_VALUE,
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "La foto pertenece a otro usuario", content = @Content),
            @ApiResponse(responseCode = "404", description = "La foto no existe", content = @Content)
    })
    public ResponseEntity<byte[]> descargarFoto(
            @Parameter(description = "Nombre del archivo, tal como aparece al final de fotoUrl",
                    example = "3-0b0a2f0e-6c1d-4a58-8f6e-2f4f4f0d9a11.jpg") @PathVariable String archivo) {
        Acceso.exigirAcceso(almacenFotos.propietario(archivo));
        byte[] datos = almacenFotos.leer(archivo);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(almacenFotos.tipoContenido(archivo)))
                .body(datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Eliminar usuario (solo ADMIN)",
            description = "También borra su foto de perfil del servidor.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
            @ApiResponse(responseCode = "401", description = "Falta el token o es inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no es ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public void eliminar(
            @Parameter(description = "Id del usuario a eliminar", example = "3") @PathVariable Long id) {
        String foto = service.obtener(id).getFotoUrl();
        service.eliminar(id);
        almacenFotos.eliminar(foto);
    }
}
