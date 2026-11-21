package pagoflex.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Mensaje de la conversación de un ticket")
public class MensajeResponse {
    @Schema(description = "Id del mensaje", example = "10")
    private Long id;

    @Schema(description = "Id del ticket", example = "4")
    private Long ticketId;

    @Schema(description = "Id de quien escribió", example = "2")
    private Long autorId;

    @Schema(description = "Rol de quien escribió", allowableValues = {"USUARIO", "GESTOR"}, example = "GESTOR")
    private String rolAutor;

    @Schema(description = "Texto del mensaje")
    private String mensaje;

    @Schema(description = "Fecha y hora")
    private LocalDateTime fecha;
}
