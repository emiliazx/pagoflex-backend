package pagoflex.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Prueba (imagen) adjunta a un ticket")
public class AdjuntoResponse {
    @Schema(description = "Id del adjunto", example = "7")
    private Long id;

    @Schema(description = "Id del ticket", example = "4")
    private Long ticketId;

    @Schema(description = "Nombre original del archivo", example = "comprobante.jpg")
    private String nombreOriginal;

    @Schema(description = "Tipo de contenido", example = "image/jpeg")
    private String tipoContenido;

    @Schema(description = "Tamaño en bytes", example = "184320")
    private long tamano;

    @Schema(description = "Ruta relativa desde donde se descarga la imagen", example = "/api/tickets/4/adjuntos/7")
    private String url;

    @Schema(description = "Fecha en que se subió")
    private LocalDateTime fecha;
}
