package pagoflex.deudas.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import pagoflex.deudas.dto.DeudaRequest;
import pagoflex.deudas.dto.DeudaResponse;
import pagoflex.deudas.model.Cuota;
import pagoflex.deudas.model.Deuda;
import pagoflex.deudas.model.Estado;
import pagoflex.deudas.repository.CuotaRepository;
import pagoflex.deudas.repository.DeudaRepository;
import pagoflex.deudas.security.Acceso;

@ExtendWith(MockitoExtension.class)
class DeudaServiceTest {

    @Mock
    private DeudaRepository deudaRepository;

    @Mock
    private CuotaRepository cuotaRepository;

    @Mock
    private CuotaService cuotaService;

    @InjectMocks
    private DeudaService deudaService;

    private MockedStatic<Acceso> accesoMock;

    @BeforeEach
    void setUp() {
        accesoMock = mockStatic(Acceso.class);
    }

    @AfterEach
    void tearDown() {
        accesoMock.close();
    }

    @Test
    void crear_ConVariasCuotas_GuardaDeudaYGeneraCuotas() {
        DeudaRequest req = DeudaRequest.builder()
                .usuarioId(1L)
                .concepto("  Compra Notebook ")
                .empresa("TechStore")
                .monto(new BigDecimal("600.00"))
                .vencimiento("2026-12-10")
                .cantidadCuotas(3)
                .build();

        Deuda guardada = Deuda.builder()
                .id(10L)
                .usuarioId(1L)
                .concepto("Compra Notebook")
                .empresa("TechStore")
                .monto(new BigDecimal("600.00"))
                .vencimiento(LocalDate.of(2026, 12, 10))
                .estado(Estado.PENDIENTE)
                .build();

        Cuota c1 = Cuota.builder().numero(1).monto(new BigDecimal("200.00"))
                .vencimiento(LocalDate.of(2026, 12, 10)).estado(Estado.PENDIENTE).build();

        when(deudaRepository.save(any(Deuda.class))).thenReturn(guardada);
        when(cuotaService.crearCuotas(eq(guardada), eq(3), any(LocalDate.class))).thenReturn(List.of(c1));

        DeudaResponse res = deudaService.crear(req);

        assertEquals(10L, res.getId());
        assertEquals("Compra Notebook", res.getConcepto());
        assertEquals(1, res.getCuotaActual());
        assertEquals(new BigDecimal("200.00"), res.getMontoCuota());
        accesoMock.verify(Acceso::exigirGestor);
    }

    @Test
    void listar_FiltraPorEstadoYRangoDeMonto() {
        Deuda d1 = Deuda.builder().id(1L).usuarioId(2L).concepto("A").empresa("E")
                .monto(new BigDecimal("100.00")).vencimiento(LocalDate.of(2026, 11, 20)).estado(Estado.PENDIENTE).build();
        Deuda d2 = Deuda.builder().id(2L).usuarioId(2L).concepto("B").empresa("E")
                .monto(new BigDecimal("500.00")).vencimiento(LocalDate.of(2026, 11, 25)).estado(Estado.PAGADA).build();

        when(cuotaRepository.findByUsuarioIdOrderByVencimientoAscIdAsc(2L)).thenReturn(List.of());
        when(deudaRepository.findByUsuarioIdOrderByVencimientoAscIdAsc(2L)).thenReturn(List.of(d1, d2));

        List<DeudaResponse> res = deudaService.listar(2L, "PENDIENTE", null, null,
                new BigDecimal("50.00"), new BigDecimal("200.00"));

        assertEquals(1, res.size());
        assertEquals(1L, res.get(0).getId());
    }

    @Test
    void actualizar_CuandoDeudaBloqueadaYCambiaMonto_LanzaConflict() {
        Deuda existente = Deuda.builder()
                .id(5L).usuarioId(1L).concepto("Viejo").empresa("Emp")
                .monto(new BigDecimal("100.00")).vencimiento(LocalDate.of(2026, 12, 1)).estado(Estado.PAGADA).build();

        DeudaRequest req = DeudaRequest.builder()
                .usuarioId(1L).concepto("Nuevo").empresa("Emp")
                .monto(new BigDecimal("200.00")).vencimiento("2026-12-01").cantidadCuotas(1).build();

        when(deudaRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(cuotaRepository.findByDeudaIdOrderByNumeroAsc(5L)).thenReturn(List.of());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> deudaService.actualizar(5L, req));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void pagar_LiquidaDeudaYTodasLasCuotasPendientes() {
        Deuda deuda = Deuda.builder()
                .id(7L).usuarioId(3L).concepto("Servicio").empresa("Luz")
                .monto(new BigDecimal("100.00")).vencimiento(LocalDate.of(2026, 12, 1)).estado(Estado.PENDIENTE).build();
        Cuota c1 = Cuota.builder().id(1L).numero(1).monto(new BigDecimal("50.00"))
                .vencimiento(LocalDate.of(2026, 12, 1)).estado(Estado.PENDIENTE).build();
        Cuota c2 = Cuota.builder().id(2L).numero(2).monto(new BigDecimal("50.00"))
                .vencimiento(LocalDate.of(2027, 1, 1)).estado(Estado.PENDIENTE).build();

        when(deudaRepository.findById(7L)).thenReturn(Optional.of(deuda));
        when(cuotaRepository.findByDeudaIdOrderByNumeroAsc(7L)).thenReturn(List.of(c1, c2));

        DeudaResponse res = deudaService.pagar(7L);

        assertEquals("PAGADA", res.getEstado());
        assertEquals(Estado.PAGADA, c1.getEstado());
        assertEquals(Estado.PAGADA, c2.getEstado());
        verify(cuotaRepository).saveAll(anyList());
    }

    @Test
    void eliminar_EliminaCuotasYDeuda() {
        Deuda deuda = Deuda.builder().id(9L).usuarioId(1L).build();
        when(deudaRepository.findById(9L)).thenReturn(Optional.of(deuda));

        deudaService.eliminar(9L);

        accesoMock.verify(Acceso::exigirGestor);
        verify(cuotaRepository).deleteByDeudaId(9L);
        verify(deudaRepository).delete(deuda);
    }
}