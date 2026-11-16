package pagoflex.pagos.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

import pagoflex.pagos.dto.PagoRequest;
import pagoflex.pagos.dto.PagoResponse;
import pagoflex.pagos.model.Pago;
import pagoflex.pagos.repository.PagoRepository;
import pagoflex.pagos.security.Acceso;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @InjectMocks
    private PagoService pagoService;

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
    void registrar_ConMetodoNulo_UsaTarjetaPorDefectoYRedondeaMonto() {
        PagoRequest req = PagoRequest.builder()
                .deudaId(10L)
                .usuarioId(1L)
                .monto(new BigDecimal("150.556"))
                .metodo(null)
                .tarjetaId(5L)
                .build();

        Pago pagoGuardado = Pago.builder()
                .id(100L)
                .deudaId(10L)
                .usuarioId(1L)
                .monto(new BigDecimal("150.56"))
                .fecha(LocalDateTime.of(2026, 11, 16, 12, 0))
                .metodo("TARJETA")
                .tarjetaId(5L)
                .build();

        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoGuardado);

        PagoResponse res = pagoService.registrar(req);

        assertNotNull(res);
        assertEquals(100L, res.getId());
        assertEquals("TARJETA", res.getMetodo());
        assertEquals(new BigDecimal("150.56"), res.getMonto());
        accesoMock.verify(() -> Acceso.exigirAcceso(1L));
        verify(pagoRepository).save(any(Pago.class));
    }

    @Test
    void registrar_ConMetodoPersonalizado_LoFormateaAMayusculas() {
        PagoRequest req = PagoRequest.builder()
                .deudaId(10L)
                .usuarioId(1L)
                .monto(new BigDecimal("50.00"))
                .metodo("  transferencia ")
                .build();

        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> {
            Pago p = inv.getArgument(0);
            p.setId(101L);
            return p;
        });

        PagoResponse res = pagoService.registrar(req);

        assertEquals("TRANSFERENCIA", res.getMetodo());
        assertEquals(101L, res.getId());
    }

    @Test
    void listar_DevuelvePagosDelUsuario() {
        Pago p1 = Pago.builder()
                .id(1L).deudaId(10L).usuarioId(2L)
                .monto(new BigDecimal("20.00"))
                .fecha(LocalDateTime.now())
                .metodo("TARJETA")
                .build();

        when(pagoRepository.findByUsuarioIdOrderByFechaDescIdDesc(2L)).thenReturn(List.of(p1));

        List<PagoResponse> lista = pagoService.listar(2L);

        assertEquals(1, lista.size());
        assertEquals(1L, lista.get(0).getId());
        accesoMock.verify(() -> Acceso.exigirAcceso(2L));
    }

    @Test
    void porDeuda_CuandoNoEsPersonal_FiltraSoloPagosDelUsuarioActual() {
        accesoMock.when(Acceso::esPersonal).thenReturn(false);
        accesoMock.when(Acceso::idActual).thenReturn(1L);

        Pago mio = Pago.builder()
                .id(1L).deudaId(10L).usuarioId(1L)
                .monto(new BigDecimal("30.00")).fecha(LocalDateTime.now()).metodo("TARJETA")
                .build();
        Pago deOtro = Pago.builder()
                .id(2L).deudaId(10L).usuarioId(99L)
                .monto(new BigDecimal("40.00")).fecha(LocalDateTime.now()).metodo("TARJETA")
                .build();

        when(pagoRepository.findByDeudaIdOrderByFechaDescIdDesc(10L)).thenReturn(List.of(mio, deOtro));

        List<PagoResponse> resultado = pagoService.porDeuda(10L);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getUsuarioId());
    }

    @Test
    void porDeuda_CuandoEsPersonal_DevuelveTodosLosPagos() {
        accesoMock.when(Acceso::esPersonal).thenReturn(true);
        accesoMock.when(Acceso::idActual).thenReturn(999L);

        Pago p1 = Pago.builder().id(1L).deudaId(10L).usuarioId(1L).monto(BigDecimal.TEN).fecha(LocalDateTime.now()).metodo("TARJETA").build();
        Pago p2 = Pago.builder().id(2L).deudaId(10L).usuarioId(2L).monto(BigDecimal.TEN).fecha(LocalDateTime.now()).metodo("TARJETA").build();

        when(pagoRepository.findByDeudaIdOrderByFechaDescIdDesc(10L)).thenReturn(List.of(p1, p2));

        List<PagoResponse> resultado = pagoService.porDeuda(10L);

        assertEquals(2, resultado.size());
    }

    @Test
    void obtener_CuandoExiste_RetornaPagoYValidaAcceso() {
        Pago p = Pago.builder()
                .id(5L).deudaId(10L).usuarioId(3L)
                .monto(new BigDecimal("99.90")).fecha(LocalDateTime.now()).metodo("TARJETA")
                .build();

        when(pagoRepository.findById(5L)).thenReturn(Optional.of(p));

        PagoResponse res = pagoService.obtener(5L);

        assertEquals(5L, res.getId());
        accesoMock.verify(() -> Acceso.exigirAcceso(3L));
    }

    @Test
    void obtener_CuandoNoExiste_LanzaNotFound() {
        when(pagoRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> pagoService.obtener(999L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}