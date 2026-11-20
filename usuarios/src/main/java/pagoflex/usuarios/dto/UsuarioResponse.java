package pagoflex.usuarios.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String rut;
    private String correo;
    private String rol;
    private String pregunta1;
    private String pregunta2;
    private String fotoUrl;
    private boolean habilitado;
    private boolean bloqueado;
}