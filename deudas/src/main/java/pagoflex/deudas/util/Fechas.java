package pagoflex.deudas.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class Fechas {

    private Fechas() {
    }

    public static LocalDate leer(String texto) {
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha invalida, usa el formato yyyy-MM-dd: " + texto);
        }
    }

    public static LocalDate leerOpcional(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return leer(texto);
    }
}
