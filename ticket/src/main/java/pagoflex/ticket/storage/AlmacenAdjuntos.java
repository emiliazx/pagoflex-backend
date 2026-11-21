package pagoflex.ticket.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AlmacenAdjuntos {

    public record Guardado(String archivo, String tipoContenido, long tamano) {
    }

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Pattern NOMBRE_VALIDO = Pattern.compile("^[0-9a-f\\-]{36}\\.(jpg|png)$");

    private final Path carpeta;

    public AlmacenAdjuntos(@Value("${app.uploads.dir:uploads}") String directorio) {
        this.carpeta = Path.of(directorio, "tickets").toAbsolutePath().normalize();
    }

    public Guardado guardar(MultipartFile archivo) {
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
        String nombre = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(carpeta);
            Files.write(carpeta.resolve(nombre), datos);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar la imagen");
        }
        String tipo = "png".equals(extension) ? "image/png" : "image/jpeg";
        return new Guardado(nombre, tipo, datos.length);
    }

    public byte[] leer(String nombre) {
        if (!NOMBRE_VALIDO.matcher(nombre).matches()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Archivo no encontrado");
        }
        Path ruta = carpeta.resolve(nombre).normalize();
        if (!ruta.startsWith(carpeta)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Archivo no encontrado");
        }
        try {
            return Files.readAllBytes(ruta);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Archivo no encontrado");
        }
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
