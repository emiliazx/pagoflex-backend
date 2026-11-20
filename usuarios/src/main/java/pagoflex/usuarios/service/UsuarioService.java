package pagoflex.usuarios.service;

import pagoflex.usuarios.dto.*;
import pagoflex.usuarios.model.Usuario;
import pagoflex.usuarios.repository.UsuarioRepository;
import pagoflex.usuarios.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private static final int MAX_INTENTOS = 5;

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.upload-dir:uploads/usuarios}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    public UsuarioService(UsuarioRepository repository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // ===================== LOGIN =====================
    @Transactional
    public LoginResponse login(LoginRequest req) {
        String correo = req.getCorreo().toLowerCase(Locale.ROOT).trim();
        Usuario u = repository.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (u.isBloqueado()) {
            throw new ResponseStatusException(HttpStatus.LOCKED,
                    "Cuenta bloqueada por demasiados intentos fallidos.");
        }

        // No entra hasta que admin/gestor habilite
        if (!u.isHabilitado()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Tu cuenta está pendiente de aprobación por un administrador o gestor.");
        }

        if (!passwordEncoder.matches(req.getContrasena(), u.getClaveHash())) {
            u.setIntentosFallidos(u.getIntentosFallidos() + 1);
            if (u.getIntentosFallidos() >= MAX_INTENTOS) {
                u.setBloqueado(true);
            }
            repository.save(u);
            int restantes = MAX_INTENTOS - u.getIntentosFallidos();
            String msg = restantes > 0
                    ? "Credenciales inválidas. Intentos restantes: " + restantes
                    : "Cuenta bloqueada por demasiados intentos fallidos.";
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, msg);
        }

        u.setIntentosFallidos(0);
        repository.save(u);

        String token = jwtService.generarToken(u.getId(), u.getCorreo(), u.getRol());
        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .id(u.getId())
                .nombre(u.getNombre())
                .rut(u.getRut())
                .correo(u.getCorreo())
                .rol(u.getRol())
                .fotoUrl(u.getFotoUrl())
                .build();
    }

    // ===================== REGISTRO =====================
    public UsuarioResponse registrar(RegistroRequest req) {
        String correo = req.getCorreo().toLowerCase(Locale.ROOT).trim();
        if (repository.existsByCorreo(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }

        Usuario u = Usuario.builder()
                .nombre(req.getNombre().trim())
                .rut(req.getRut().trim())
                .correo(correo)
                .claveHash(passwordEncoder.encode(req.getContrasena()))
                .rol("USUARIO")
                .pregunta1(req.getPregunta1())
                .respuesta1Hash(req.getRespuesta1() != null ? hashSimple(normalizar(req.getRespuesta1())) : "")
                .pregunta2(req.getPregunta2())
                .respuesta2Hash(req.getRespuesta2() != null ? hashSimple(normalizar(req.getRespuesta2())) : "")
                .intentosFallidos(0)
                .bloqueado(false)
                .habilitado(false) // pendiente de aprobación
                .build();

        return toResponse(repository.save(u));
    }

    // ===================== HABILITAR / DESHABILITAR =====================
    public UsuarioResponse habilitar(Long id) {
        Usuario u = buscar(id);
        u.setHabilitado(true);
        u.setBloqueado(false);
        u.setIntentosFallidos(0);
        return toResponse(repository.save(u));
    }

    public UsuarioResponse deshabilitar(Long id) {
        Usuario u = buscar(id);
        u.setHabilitado(false);
        return toResponse(repository.save(u));
    }

    public List<UsuarioResponse> listarPendientes() {
        return repository.findAll().stream()
                .filter(u -> !u.isHabilitado())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ===================== CRUD =====================
    public UsuarioResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    public List<UsuarioResponse> listar(String buscar) {
        List<Usuario> lista = (buscar == null || buscar.isBlank())
                ? repository.findAll()
                : repository.findByNombreContainingIgnoreCaseOrCorreoContainingIgnoreCase(buscar, buscar);
        return lista.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest req) {
        Usuario u = buscar(id);
        if (req.getNombre() != null) u.setNombre(req.getNombre().trim());
        if (req.getRut() != null) u.setRut(req.getRut().trim());
        if (req.getPregunta1() != null) u.setPregunta1(req.getPregunta1());
        if (req.getRespuesta1() != null) u.setRespuesta1Hash(hashSimple(normalizar(req.getRespuesta1())));
        if (req.getPregunta2() != null) u.setPregunta2(req.getPregunta2());
        if (req.getRespuesta2() != null) u.setRespuesta2Hash(hashSimple(normalizar(req.getRespuesta2())));
        return toResponse(repository.save(u));
    }

    public void cambiarContrasena(Long id, CambiarContrasenaRequest req) {
        Usuario u = buscar(id);
        if (!passwordEncoder.matches(req.getContrasenaActual(), u.getClaveHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contraseña actual incorrecta");
        }
        u.setClaveHash(passwordEncoder.encode(req.getContrasenaNueva()));
        repository.save(u);
    }

    // ===================== FOTO (archivo + URL en BD) =====================
    public UsuarioResponse guardarFoto(Long id, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Archivo de foto vacío");
        }
        Usuario u = buscar(id);
        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);
            String nombre = id + ".jpg";
            Path destino = dir.resolve(nombre);
            Files.write(destino, archivo.getBytes());

            String url = baseUrl + "/uploads/usuarios/" + nombre;
            u.setFotoUrl(url);
            return toResponse(repository.save(u));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar la foto");
        }
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        repository.deleteById(id);
    }

    // ===================== HELPERS =====================
    private Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private UsuarioResponse toResponse(Usuario u) {
        return UsuarioResponse.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .rut(u.getRut())
                .correo(u.getCorreo())
                .rol(u.getRol())
                .pregunta1(u.getPregunta1())
                .pregunta2(u.getPregunta2())
                .fotoUrl(u.getFotoUrl())
                .habilitado(u.isHabilitado())
                .bloqueado(u.isBloqueado())
                .build();
    }

    public static String hashSimple(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String normalizar(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
}