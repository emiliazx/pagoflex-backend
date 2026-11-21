package pagoflex.ticket.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

public final class Acceso {

    private Acceso() {
    }

    public static String rolActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return "";
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(rol -> rol.startsWith("ROLE_") ? rol.substring(5) : rol)
                .findFirst()
                .orElse("");
    }

    public static boolean esGestor() {
        return "GESTOR".equals(rolActual());
    }

    public static boolean esAdmin() {
        return "ADMIN".equals(rolActual());
    }

    public static boolean esPersonal() {
        return esGestor() || esAdmin();
    }

    public static Long idActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof Long id) {
            return id;
        }
        return null;
    }

    public static String correoActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }

    public static void exigirGestor() {
        if (!esGestor()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el GESTOR puede hacer esta accion");
        }
    }

    public static void exigirAcceso(Long usuarioId) {
        if (esPersonal()) {
            return;
        }
        Long actual = idActual();
        if (actual == null || !actual.equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a los tickets de otro usuario");
        }
    }
}
