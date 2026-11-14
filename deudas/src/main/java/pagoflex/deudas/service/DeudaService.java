package pagoflex.deudas.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pagoflex.deudas.dto.DeudaRequest;
import pagoflex.deudas.dto.DeudaResponse;
import pagoflex.deudas.model.Cuota;
import pagoflex.deudas.model.Deuda;
import pagoflex.deudas.model.Estado;
import pagoflex.deudas.repository.CuotaRepository;
import pagoflex.deudas.repository.DeudaRepository;
import pagoflex.deudas.security.Acceso;
import pagoflex.deudas.util.Fechas;

@Service
public class DeudaService {

    private final DeudaRepository deudas;
    private final CuotaRepository cuotas;
    private final CuotaService cuotaService;

    public DeudaService(DeudaRepository deudas, CuotaRepository cuotas, CuotaService cuotaService) {
        this.deudas = deudas;
        this.cuotas = cuotas;
        this.cuotaService = cuotaService;
    }

    @Transactional
    public DeudaResponse crear(DeudaRequest req) {
        Acceso.exigirGestor();
        Deuda deuda = deudas.save(Deuda.builder()
                .usuarioId(req.getUsuarioId())
                .concepto(req.getConcepto().trim())
                .empresa(req.getEmpresa().trim())
                .monto(redondear(req.getMonto()))
                .vencimiento(Fechas.leer(req.getVencimiento()))
                .estado(Estado.PENDIENTE)
                .build());
        int cantidad = req.getCantidadCuotas() == null ? 1 : req.getCantidadCuotas();
        List<Cuota> creadas = cantidad >= 2
                ? cuotaService.crearCuotas(deuda, cantidad, deuda.getVencimiento())
                : List.of();
        return aRespuesta(deuda, creadas);
    }

    @Transactional(readOnly = true)
    public List<DeudaResponse> listar(Long usuarioId, String estado, String desde, String hasta,
                                      BigDecimal montoMin, BigDecimal montoMax) {
        Acceso.exigirAcceso(usuarioId);
        final Estado filtroEstado = (estado == null || estado.isBlank()) ? null : Estado.leer(estado);
        final LocalDate fechaDesde = Fechas.leerOpcional(desde);
        final LocalDate fechaHasta = Fechas.leerOpcional(hasta);
        final Map<Long, List<Cuota>> cuotasPorDeuda = cuotas.findByUsuarioIdOrderByVencimientoAscIdAsc(usuarioId).stream()
                .collect(Collectors.groupingBy(Cuota::getDeudaId));
        return deudas.findByUsuarioIdOrderByVencimientoAscIdAsc(usuarioId).stream()
                .filter(d -> filtroEstado == null || d.getEstado() == filtroEstado)
                .filter(d -> fechaDesde == null || !d.getVencimiento().isBefore(fechaDesde))
                .filter(d -> fechaHasta == null || !d.getVencimiento().isAfter(fechaHasta))
                .filter(d -> montoMin == null || d.getMonto().compareTo(montoMin) >= 0)
                .filter(d -> montoMax == null || d.getMonto().compareTo(montoMax) <= 0)
                .map(d -> aRespuesta(d, cuotasPorDeuda.getOrDefault(d.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public DeudaResponse obtener(Long id) {
        Deuda deuda = buscar(id);
        Acceso.exigirAcceso(deuda.getUsuarioId());
        return aRespuesta(deuda, cuotas.findByDeudaIdOrderByNumeroAsc(id));
    }

    @Transactional
    public DeudaResponse actualizar(Long id, DeudaRequest req) {
        Acceso.exigirGestor();
        Deuda deuda = buscar(id);
        if (!deuda.getUsuarioId().equals(req.getUsuarioId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede cambiar el usuario de una deuda");
        }
        BigDecimal nuevoMonto = redondear(req.getMonto());
        LocalDate nuevoVencimiento = Fechas.leer(req.getVencimiento());
        List<Cuota> existentes = cuotas.findByDeudaIdOrderByNumeroAsc(id);

        int actual = existentes.isEmpty() ? 1 : existentes.size();
        int deseada = req.getCantidadCuotas() == null ? actual : req.getCantidadCuotas();
        boolean cambiaMonto = deuda.getMonto().compareTo(nuevoMonto) != 0;
        boolean cambiaVencimiento = !deuda.getVencimiento().equals(nuevoVencimiento);
        boolean cambiaCuotas = deseada != actual;
        boolean hayPagadas = existentes.stream().anyMatch(c -> c.getEstado() == Estado.PAGADA);
        boolean bloqueada = hayPagadas || deuda.getEstado() == Estado.PAGADA;
        boolean cambiaCondiciones = cambiaMonto || cambiaVencimiento || cambiaCuotas;

        if (bloqueada && cambiaCondiciones) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La deuda ya tiene pagos: solo se pueden editar el concepto y la empresa");
        }

        deuda.setConcepto(req.getConcepto().trim());
        deuda.setEmpresa(req.getEmpresa().trim());
        deuda.setMonto(nuevoMonto);
        deuda.setVencimiento(nuevoVencimiento);
        deudas.save(deuda);

        List<Cuota> resultado = existentes;
        if (cambiaCondiciones) {
            cuotas.deleteAll(existentes);
            resultado = deseada >= 2
                    ? cuotaService.crearCuotas(deuda, deseada, nuevoVencimiento)
                    : List.of();
        }
        return aRespuesta(deuda, resultado);
    }

    @Transactional
    public DeudaResponse pagar(Long id) {
        Deuda deuda = buscar(id);
        Acceso.exigirAcceso(deuda.getUsuarioId());
        List<Cuota> todas = cuotas.findByDeudaIdOrderByNumeroAsc(id);
        if (deuda.getEstado() == Estado.PENDIENTE) {
            deuda.setEstado(Estado.PAGADA);
            deudas.save(deuda);
            LocalDate hoy = LocalDate.now();
            for (Cuota cuota : todas) {
                if (cuota.getEstado() == Estado.PENDIENTE) {
                    cuota.setEstado(Estado.PAGADA);
                    cuota.setFechaPago(hoy);
                }
            }
            cuotas.saveAll(todas);
        }
        return aRespuesta(deuda, todas);
    }

    @Transactional
    public void eliminar(Long id) {
        Acceso.exigirGestor();
        Deuda deuda = buscar(id);
        cuotas.deleteByDeudaId(id);
        deudas.delete(deuda);
    }

    private Deuda buscar(Long id) {
        return deudas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La deuda no existe"));
    }

    private BigDecimal redondear(BigDecimal monto) {
        return monto.setScale(2, RoundingMode.HALF_UP);
    }

    private DeudaResponse aRespuesta(Deuda d, List<Cuota> lista) {
        List<Cuota> ordenadas = lista.stream()
                .sorted(Comparator.comparingInt(Cuota::getNumero))
                .toList();
        int total = ordenadas.isEmpty() ? 1 : ordenadas.size();
        int pagadas = (int) ordenadas.stream().filter(c -> c.getEstado() == Estado.PAGADA).count();
        Cuota siguiente = ordenadas.stream()
                .filter(c -> c.getEstado() == Estado.PENDIENTE)
                .findFirst()
                .orElse(null);
        BigDecimal pagado;
        if (ordenadas.isEmpty()) {
            pagado = d.getEstado() == Estado.PAGADA ? d.getMonto() : BigDecimal.ZERO;
        } else {
            pagado = ordenadas.stream()
                    .filter(c -> c.getEstado() == Estado.PAGADA)
                    .map(Cuota::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return DeudaResponse.builder()
                .id(d.getId())
                .usuarioId(d.getUsuarioId())
                .concepto(d.getConcepto())
                .empresa(d.getEmpresa())
                .monto(d.getMonto())
                .vencimiento(d.getVencimiento().toString())
                .estado(d.getEstado().name())
                .cantidadCuotas(total)
                .cuotasPagadas(pagadas)
                .cuotaActual(siguiente == null ? null : siguiente.getNumero())
                .montoCuota(siguiente == null ? d.getMonto() : siguiente.getMonto())
                .vencimientoActual(siguiente == null ? d.getVencimiento().toString() : siguiente.getVencimiento().toString())
                .montoPagado(pagado)
                .build();
    }
}
