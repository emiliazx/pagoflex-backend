package pagoflex.deudas.model;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public enum Estado {
    PENDIENTE,
    PAGADA;

    public static Estado leer(String texto) {
        try {
            return valueOf(texto.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado invalido, usa PENDIENTE o PAGADA: " + texto);
        }
    }
}
