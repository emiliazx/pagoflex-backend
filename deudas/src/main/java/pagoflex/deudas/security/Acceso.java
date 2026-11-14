package pagoflex.deudas.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

public final class Acceso {

    private Acceso() {
    }

    public static boolean esPersonal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(rol -> rol.equals("ROLE_ADMIN") || rol.equals("ROLE_GESTOR"));
    }

    public static boolean esGestor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(rol -> rol.equals("ROLE_GESTOR"));
    }

    public static Long idActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof Long id) {
            return id;
        }
        return null;
    }

    public static void exigirPersonal() {
        if (!esPersonal()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo ADMIN o GESTOR pueden hacer esta accion");
        }
    }

    public static void exigirGestor() {
        if (!esGestor()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el GESTOR puede crear, editar o eliminar deudas");
        }
    }

    public static void exigirAcceso(Long usuarioId) {
        if (esPersonal()) {
            return;
        }
        Long actual = idActual();
        if (actual == null || !actual.equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a los datos de otro usuario");
        }
    }
}
