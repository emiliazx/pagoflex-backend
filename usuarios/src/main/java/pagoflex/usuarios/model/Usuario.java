package pagoflex.usuarios.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String rut;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(name = "clave_hash", nullable = false)
    private String claveHash;

    @Column(nullable = false)
    private String rol; // ADMIN, GESTOR, USUARIO

    private String pregunta1;
    private String respuesta1Hash;
    private String pregunta2;
    private String respuesta2Hash;

    /** URL de la foto en el servidor (no BLOB) */
    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(nullable = false)
    @Builder.Default
    private int intentosFallidos = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean bloqueado = false;

    /** false = cuenta creada pero no puede entrar hasta que admin/gestor habilite */
    @Column(nullable = false)
    @Builder.Default
    private boolean habilitado = false;


}