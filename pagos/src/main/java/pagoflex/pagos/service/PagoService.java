package pagoflex.pagos.service;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pagoflex.pagos.dto.PagoRequest;
import pagoflex.pagos.dto.PagoResponse;
import pagoflex.pagos.model.Pago;
import pagoflex.pagos.repository.PagoRepository;
import pagoflex.pagos.security.Acceso;

@Service
public class PagoService {

    private static final String METODO_POR_DEFECTO = "TARJETA";

    private final PagoRepository pagos;

    public PagoService(PagoRepository pagos) {
        this.pagos = pagos;
    }

    @Transactional
    public PagoResponse registrar(PagoRequest req) {
        Acceso.exigirAcceso(req.getUsuarioId());
        String metodo = (req.getMetodo() == null || req.getMetodo().isBlank())
                ? METODO_POR_DEFECTO
                : req.getMetodo().trim().toUpperCase(Locale.ROOT);
        Pago pago = Pago.builder()
                .deudaId(req.getDeudaId())
                .usuarioId(req.getUsuarioId())
                .monto(req.getMonto().setScale(2, RoundingMode.HALF_UP))
                .fecha(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .metodo(metodo)
                .tarjetaId(req.getTarjetaId())
                .build();
        return aRespuesta(pagos.save(pago));
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listar(Long usuarioId) {
        Acceso.exigirAcceso(usuarioId);
        return pagos.findByUsuarioIdOrderByFechaDescIdDesc(usuarioId).stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> porDeuda(Long deudaId) {
        final boolean personal = Acceso.esPersonal();
        final Long actual = Acceso.idActual();
        return pagos.findByDeudaIdOrderByFechaDescIdDesc(deudaId).stream()
                .filter(p -> personal || p.getUsuarioId().equals(actual))
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagoResponse obtener(Long id) {
        Pago pago = pagos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El pago no existe"));
        Acceso.exigirAcceso(pago.getUsuarioId());
        return aRespuesta(pago);
    }

    private PagoResponse aRespuesta(Pago p) {
        return PagoResponse.builder()
                .id(p.getId())
                .deudaId(p.getDeudaId())
                .usuarioId(p.getUsuarioId())
                .monto(p.getMonto())
                .fecha(p.getFecha().toString())
                .metodo(p.getMetodo())
                .tarjetaId(p.getTarjetaId())
                .build();
    }
}
