package pagoflex.pagos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import pagoflex.pagos.dto.PagoRequest;
import pagoflex.pagos.dto.PagoResponse;
import pagoflex.pagos.service.PagoService;
import tools.jackson.databind.ObjectMapper;
import pagoflex.pagos.security.JwtService;

@WebMvcTest(PagoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PagoService pagoService;

    @Test
    void registrar_ConDatosValidos_Retorna201Created() throws Exception {
        PagoRequest req = PagoRequest.builder()
                .deudaId(1L)
                .usuarioId(2L)
                .monto(new BigDecimal("120.50"))
                .metodo("TARJETA")
                .tarjetaId(10L)
                .build();

        PagoResponse res = PagoResponse.builder()
                .id(50L)
                .deudaId(1L)
                .usuarioId(2L)
                .monto(new BigDecimal("120.50"))
                .fecha("2026-11-16T19:00:00")
                .metodo("TARJETA")
                .tarjetaId(10L)
                .build();

        when(pagoService.registrar(any(PagoRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(50L))
                .andExpect(jsonPath("$.monto").value(120.50))
                .andExpect(jsonPath("$.metodo").value("TARJETA"));
    }

    @Test
    void registrar_ConMontoInvalido_Retorna400BadRequest() throws Exception {
        PagoRequest reqInvalido = PagoRequest.builder()
                .deudaId(1L)
                .usuarioId(2L)
                .monto(new BigDecimal("0.00"))
                .build();

        mockMvc.perform(post("/api/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listar_ConUsuarioId_Retorna200YLista() throws Exception {
        PagoResponse res = PagoResponse.builder()
                .id(1L)
                .deudaId(10L)
                .usuarioId(2L)
                .monto(new BigDecimal("75.00"))
                .metodo("TARJETA")
                .build();

        when(pagoService.listar(2L)).thenReturn(List.of(res));

        mockMvc.perform(get("/api/pagos")
                        .param("usuarioId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void porDeuda_Retorna200YLista() throws Exception {
        PagoResponse res = PagoResponse.builder()
                .id(3L)
                .deudaId(15L)
                .usuarioId(2L)
                .monto(new BigDecimal("200.00"))
                .metodo("TARJETA")
                .build();

        when(pagoService.porDeuda(15L)).thenReturn(List.of(res));

        mockMvc.perform(get("/api/pagos/deuda/15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deudaId").value(15L));
    }

    @Test
    void obtener_PorId_Retorna200() throws Exception {
        PagoResponse res = PagoResponse.builder()
                .id(9L)
                .deudaId(15L)
                .usuarioId(2L)
                .monto(new BigDecimal("300.00"))
                .metodo("TARJETA")
                .build();

        when(pagoService.obtener(9L)).thenReturn(res);

        mockMvc.perform(get("/api/pagos/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9L))
                .andExpect(jsonPath("$.monto").value(300.00));
    }
}