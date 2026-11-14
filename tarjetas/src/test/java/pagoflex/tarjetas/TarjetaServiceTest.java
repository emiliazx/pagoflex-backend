package pagoflex.tarjetas;
import pagoflex.tarjetas.dto.TarjetaRequest;
import pagoflex.tarjetas.dto.TarjetaResponse;
import pagoflex.tarjetas.model.Tarjeta;
import pagoflex.tarjetas.repository.TarjetaRepository;
import pagoflex.tarjetas.service.TarjetaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class TarjetaServiceTest {
    @Mock private TarjetaRepository repository;
    @InjectMocks private TarjetaService service;
    @Test
    void agregarTarjeta() {
        when(repository.countByUsuarioId(3L)).thenReturn(0L);
        when(repository.save(any(Tarjeta.class))).thenAnswer(inv -> {
            Tarjeta t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });
        TarjetaRequest req = new TarjetaRequest();
        req.setUsuarioId(3L);
        req.setUltimosDigitos("1234");
        req.setTitular("Emilia Zamora");
        req.setVencimiento("12/28");
        req.setMarca("Visa");
        req.setPrincipal(true);
        TarjetaResponse res = service.agregar(req);
        assertEquals(1L, res.getId());
        assertTrue(res.isPrincipal());
    }
    @Test
    void maximoTarjetas() {
        when(repository.countByUsuarioId(3L)).thenReturn(4L);
        TarjetaRequest req = new TarjetaRequest();
        req.setUsuarioId(3L);
        req.setUltimosDigitos("9999");
        req.setTitular("X");
        req.setVencimiento("01/30");
        assertThrows(Exception.class, () -> service.agregar(req));
    }
}
