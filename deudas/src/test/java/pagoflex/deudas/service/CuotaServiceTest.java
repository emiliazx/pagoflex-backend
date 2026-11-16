package pagoflex.deudas.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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

import pagoflex.deudas.dto.CuotaResponse;
import pagoflex.deudas.dto.GenerarCuotasRequest;
import pagoflex.deudas.model.Cuota;
import pagoflex.deudas.model.Deuda;
import pagoflex.deudas.model.Estado;
import pagoflex.deudas.repository.CuotaRepository;
import pagoflex.deudas.repository.DeudaRepository;
import pagoflex.deudas.security.Acceso;

@ExtendWith(MockitoExtension.class)
class CuotaServiceTest {

    @Mock
    private DeudaRepository deudaRepository;

    @Mock
    private CuotaRepository cuotaRepository;

    @InjectMocks
    private CuotaService cuotaService;

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
    void generar_CuandoEsValido_CreaCuotasAjustandoCentavosEnLaUltima() {
        Deuda deuda = Deuda.builder()
                .id(1L)
                .usuarioId(10L)
                .monto(new BigDecimal("100.00"))
                .estado(Estado.PENDIENTE)
                .build();

        GenerarCuotasRequest req = GenerarCuotasRequest.builder()
                .deudaId(1L)
                .usuarioId(10L)
                .cantidadCuotas(3)
                .primerVencimiento("2026-12-01")
                .build();

        when(deudaRepository.findById(1L)).thenReturn(Optional.of(deuda));
        when(cuotaRepository.findByDeudaIdOrderByNumeroAsc(1L)).thenReturn(List.of());
        when(cuotaRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<CuotaResponse> res = cuotaService.generar(req);

        assertEquals(3, res.size());
        assertEquals(new BigDecimal("33.33"), res.get(0).getMonto());
        assertEquals(new BigDecimal("33.33"), res.get(1).getMonto());
        assertEquals(new BigDecimal("33.34"), res.get(2).getMonto());
        assertEquals("2026-12-01", res.get(0).getVencimiento());
        assertEquals("2027-02-01", res.get(2).getVencimiento());
        accesoMock.verify(Acceso::exigirGestor);
    }

    @Test
    void generar_CuandoDeudaNoPerteneceAUsuario_LanzaBadRequest() {
        Deuda deuda = Deuda.builder().id(1L).usuarioId(99L).estado(Estado.PENDIENTE).build();
        GenerarCuotasRequest req = GenerarCuotasRequest.builder()
                .deudaId(1L).usuarioId(10L).cantidadCuotas(2).primerVencimiento("2026-12-01").build();

        when(deudaRepository.findById(1L)).thenReturn(Optional.of(deuda));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> cuotaService.generar(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void generar_CuandoYaHayCuotasPagadas_LanzaConflict() {
        Deuda deuda = Deuda.builder().id(1L).usuarioId(10L).estado(Estado.PENDIENTE).build();
        Cuota pagada = Cuota.builder().id(5L).estado(Estado.PAGADA).build();
        GenerarCuotasRequest req = GenerarCuotasRequest.builder()
                .deudaId(1L).usuarioId(10L).cantidadCuotas(2).primerVencimiento("2026-12-01").build();

        when(deudaRepository.findById(1L)).thenReturn(Optional.of(deuda));
        when(cuotaRepository.findByDeudaIdOrderByNumeroAsc(1L)).thenReturn(List.of(pagada));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> cuotaService.generar(req));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void pagar_CuandoHayCuotaAnteriorPendiente_LanzaConflict() {
        Cuota cuota1 = Cuota.builder().id(1L).deudaId(10L).usuarioId(5L).numero(1).estado(Estado.PENDIENTE).build();
        Cuota cuota2 = Cuota.builder().id(2L).deudaId(10L).usuarioId(5L).numero(2).estado(Estado.PENDIENTE).build();

        when(cuotaRepository.findById(2L)).thenReturn(Optional.of(cuota2));
        when(cuotaRepository.findByDeudaIdOrderByNumeroAsc(10L)).thenReturn(List.of(cuota1, cuota2));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> cuotaService.pagar(2L));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void pagar_CuandoEsUltimaCuotaPendiente_MarcaCuotaYDeudaComoPagadas() {
        Cuota cuota1 = Cuota.builder().id(1L).deudaId(10L).usuarioId(5L).numero(1)
                .monto(new BigDecimal("50.00")).vencimiento(LocalDate.now()).estado(Estado.PAGADA).build();
        Cuota cuota2 = Cuota.builder().id(2L).deudaId(10L).usuarioId(5L).numero(2)
                .monto(new BigDecimal("50.00")).vencimiento(LocalDate.now()).estado(Estado.PENDIENTE).build();
        Deuda deuda = Deuda.builder().id(10L).usuarioId(5L).estado(Estado.PENDIENTE).build();

        when(cuotaRepository.findById(2L)).thenReturn(Optional.of(cuota2));
        when(cuotaRepository.findByDeudaIdOrderByNumeroAsc(10L)).thenReturn(List.of(cuota1, cuota2));
        when(deudaRepository.findById(10L)).thenReturn(Optional.of(deuda));

        CuotaResponse res = cuotaService.pagar(2L);

        assertEquals("PAGADA", res.getEstado());
        assertNotNull(res.getFechaPago());
        assertEquals(Estado.PAGADA, deuda.getEstado());
        verify(deudaRepository).save(deuda);
    }

    @Test
    void siguiente_CuandoNoHayPendientes_LanzaNotFound() {
        Deuda deuda = Deuda.builder().id(10L).usuarioId(5L).build();
        Cuota pagada = Cuota.builder().id(1L).estado(Estado.PAGADA).build();

        when(deudaRepository.findById(10L)).thenReturn(Optional.of(deuda));
        when(cuotaRepository.findByDeudaIdOrderByNumeroAsc(10L)).thenReturn(List.of(pagada));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> cuotaService.siguiente(10L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}