package pagoflex.deudas.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pagoflex.deudas.dto.CuotaResponse;
import pagoflex.deudas.dto.GenerarCuotasRequest;
import pagoflex.deudas.model.Cuota;
import pagoflex.deudas.model.Deuda;
import pagoflex.deudas.model.Estado;
import pagoflex.deudas.repository.CuotaRepository;
import pagoflex.deudas.repository.DeudaRepository;
import pagoflex.deudas.security.Acceso;
import pagoflex.deudas.util.Fechas;

@Service
public class CuotaService {

    private final DeudaRepository deudas;
    private final CuotaRepository cuotas;

    public CuotaService(DeudaRepository deudas, CuotaRepository cuotas) {
        this.deudas = deudas;
        this.cuotas = cuotas;
    }

    @Transactional
    public List<CuotaResponse> generar(GenerarCuotasRequest req) {
        Acceso.exigirGestor();
        Deuda deuda = buscarDeuda(req.getDeudaId());


        if (req.getCantidadCuotas() < 1 || req.getCantidadCuotas() > 72) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad de cuotas debe ser entre 1 y 72");
}
        if (!deuda.getUsuarioId().equals(req.getUsuarioId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La deuda no pertenece a ese usuario");
        }
        if (deuda.getEstado() == Estado.PAGADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La deuda ya esta pagada");
        }
        List<Cuota> existentes = cuotas.findByDeudaIdOrderByNumeroAsc(deuda.getId());
        boolean hayPagadas = existentes.stream().anyMatch(c -> c.getEstado() == Estado.PAGADA);
        if (hayPagadas) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se pueden regenerar cuotas porque ya hay cuotas pagadas");
        }
        LocalDate primero = Fechas.leer(req.getPrimerVencimiento());
        cuotas.deleteAll(existentes);
        return crearCuotas(deuda, req.getCantidadCuotas(), primero).stream().map(this::aRespuesta).toList();
    }

    @Transactional
    public List<Cuota> crearCuotas(Deuda deuda, int cantidad, LocalDate primero) {
        BigDecimal total = deuda.getMonto();
        BigDecimal base = total.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.DOWN);
        if (base.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto es muy bajo para esa cantidad de cuotas");
        }
        List<Cuota> nuevas = new ArrayList<>();
        BigDecimal acumulado = BigDecimal.ZERO;
        for (int i = 1; i <= cantidad; i++) {
            BigDecimal monto = (i == cantidad) ? total.subtract(acumulado) : base;
            acumulado = acumulado.add(monto);
            nuevas.add(Cuota.builder()
                    .deudaId(deuda.getId())
                    .usuarioId(deuda.getUsuarioId())
                    .numero(i)
                    .monto(monto)
                    .vencimiento(primero.plusMonths(i - 1L))
                    .estado(Estado.PENDIENTE)
                    .build());
        }
        return cuotas.saveAll(nuevas);
    }

    @Transactional(readOnly = true)
    public List<CuotaResponse> porDeuda(Long deudaId) {
        Deuda deuda = buscarDeuda(deudaId);
        Acceso.exigirAcceso(deuda.getUsuarioId());
        return cuotas.findByDeudaIdOrderByNumeroAsc(deudaId).stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public List<CuotaResponse> porUsuario(Long usuarioId, String estado) {
        Acceso.exigirAcceso(usuarioId);
        final Estado filtro = (estado == null || estado.isBlank()) ? null : Estado.leer(estado);
        return cuotas.findByUsuarioIdOrderByVencimientoAscIdAsc(usuarioId).stream()
                .filter(c -> filtro == null || c.getEstado() == filtro)
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional
    public CuotaResponse pagar(Long id) {
        Cuota cuota = cuotas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La cuota no existe"));
        Acceso.exigirAcceso(cuota.getUsuarioId());
        if (cuota.getEstado() == Estado.PENDIENTE) {
            boolean hayAnteriorPendiente = cuotas.findByDeudaIdOrderByNumeroAsc(cuota.getDeudaId()).stream()
                    .anyMatch(c -> c.getEstado() == Estado.PENDIENTE && c.getNumero() < cuota.getNumero());
            if (hayAnteriorPendiente) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Debes pagar primero las cuotas anteriores");
            }
            cuota.setEstado(Estado.PAGADA);
            cuota.setFechaPago(LocalDate.now());
            cuotas.save(cuota);
            boolean quedanPendientes = cuotas.findByDeudaIdOrderByNumeroAsc(cuota.getDeudaId()).stream()
                    .anyMatch(c -> c.getEstado() == Estado.PENDIENTE);
            if (!quedanPendientes) {
                deudas.findById(cuota.getDeudaId()).ifPresent(deuda -> {
                    deuda.setEstado(Estado.PAGADA);
                    deudas.save(deuda);
                });
            }
        }
        return aRespuesta(cuota);
    }

    @Transactional(readOnly = true)
    public CuotaResponse siguiente(Long deudaId) {
        Deuda deuda = buscarDeuda(deudaId);
        Acceso.exigirAcceso(deuda.getUsuarioId());
        return cuotas.findByDeudaIdOrderByNumeroAsc(deudaId).stream()
                .filter(c -> c.getEstado() == Estado.PENDIENTE)
                .findFirst()
                .map(this::aRespuesta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay cuotas pendientes"));
    }

    private Deuda buscarDeuda(Long id) {
        return deudas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La deuda no existe"));
    }

    private CuotaResponse aRespuesta(Cuota c) {
        return CuotaResponse.builder()
                .id(c.getId())
                .deudaId(c.getDeudaId())
                .usuarioId(c.getUsuarioId())
                .numero(c.getNumero())
                .monto(c.getMonto())
                .vencimiento(c.getVencimiento().toString())
                .estado(c.getEstado().name())
                .fechaPago(c.getFechaPago() == null ? null : c.getFechaPago().toString())
                .build();
    }
}
