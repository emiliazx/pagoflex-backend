package pagoflex.usuarios.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AlmacenFotos {

    public static final String RUTA_PUBLICA = "/api/usuarios/fotos/";

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Pattern NOMBRE_VALIDO =
            Pattern.compile("^(\\d{1,18})-[0-9a-f\\-]{36}\\.(jpg|png)$");

    private final Path carpeta;

    public AlmacenFotos(@Value("${app.uploads.dir:uploads}") String directorio) {
        this.carpeta = Path.of(directorio, "usuarios").toAbsolutePath().normalize();
    }

    public String guardar(Long usuarioId, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debes enviar una imagen");
        }
        if (archivo.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen supera los 5 MB");
        }
        byte[] datos;
        try {
            datos = archivo.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo leer la imagen");
        }
        String extension = extensionDe(datos);
        if (extension == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo debe ser una imagen JPG o PNG");
        }
        String nombre = usuarioId + "-" + UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(carpeta);
            Files.write(carpeta.resolve(nombre), datos);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar la imagen");
        }
        return RUTA_PUBLICA + nombre;
    }

    public byte[] leer(String nombre) {
        Path ruta = resolver(nombre);
        try {
            return Files.readAllBytes(ruta);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto no encontrada");
        }
    }

    public Long propietario(String nombre) {
        Matcher m = NOMBRE_VALIDO.matcher(nombre);
        if (!m.matches()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto no encontrada");
        }
        return Long.valueOf(m.group(1));
    }

    public String tipoContenido(String nombre) {
        return nombre.endsWith(".png") ? "image/png" : "image/jpeg";
    }

    public void eliminar(String url) {
        if (url == null || !url.startsWith(RUTA_PUBLICA)) {
            return;
        }
        String nombre = url.substring(RUTA_PUBLICA.length());
        if (!NOMBRE_VALIDO.matcher(nombre).matches()) {
            return;
        }
        try {
            Files.deleteIfExists(carpeta.resolve(nombre).normalize());
        } catch (IOException e) {
            return;
        }
    }

    private Path resolver(String nombre) {
        if (!NOMBRE_VALIDO.matcher(nombre).matches()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto no encontrada");
        }
        Path ruta = carpeta.resolve(nombre).normalize();
        if (!ruta.startsWith(carpeta)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto no encontrada");
        }
        return ruta;
    }

    private static String extensionDe(byte[] d) {
        if (d.length > 3 && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (d.length > 8 && (d[0] & 0xFF) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G') {
            return "png";
        }
        return null;
    }
}
