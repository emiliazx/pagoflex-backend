package pagoflex.deudas.controller;

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

import pagoflex.deudas.dto.CuotaResponse;
import pagoflex.deudas.dto.GenerarCuotasRequest;
import pagoflex.deudas.security.JwtService;
import pagoflex.deudas.service.CuotaService;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CuotaController.class)
@AutoConfigureMockMvc(addFilters = false)
class CuotaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CuotaService cuotaService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void generar_ConDatosValidos_Retorna201Created() throws Exception {
        GenerarCuotasRequest req = GenerarCuotasRequest.builder()
                .deudaId(1L)
                .usuarioId(2L)
                .cantidadCuotas(3)
                .primerVencimiento("2026-12-01")
                .build();

        CuotaResponse c1 = CuotaResponse.builder()
                .id(10L).deudaId(1L).usuarioId(2L).numero(1)
                .monto(new BigDecimal("100.00")).vencimiento("2026-12-01").estado("PENDIENTE").build();

        when(cuotaService.generar(any(GenerarCuotasRequest.class))).thenReturn(List.of(c1));

        mockMvc.perform(post("/api/deudas/cuotas/generar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].numero").value(1));
    }

    @Test
    void generar_ConFechaInvalida_Retorna400BadRequest() throws Exception {
        GenerarCuotasRequest reqInvalido = GenerarCuotasRequest.builder()
                .deudaId(1L)
                .usuarioId(2L)
                .cantidadCuotas(3)
                .primerVencimiento("01/12/2026")
                .build();

        mockMvc.perform(post("/api/deudas/cuotas/generar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void porDeuda_Retorna200YLista() throws Exception {
        CuotaResponse c1 = CuotaResponse.builder().id(1L).deudaId(5L).numero(1).estado("PENDIENTE").build();
        when(cuotaService.porDeuda(5L)).thenReturn(List.of(c1));

        mockMvc.perform(get("/api/deudas/5/cuotas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deudaId").value(5L));
    }

    @Test
    void porUsuario_Retorna200YLista() throws Exception {
        CuotaResponse c1 = CuotaResponse.builder().id(1L).usuarioId(2L).estado("PENDIENTE").build();
        when(cuotaService.porUsuario(2L, "PENDIENTE")).thenReturn(List.of(c1));

        mockMvc.perform(get("/api/deudas/cuotas")
                        .param("usuarioId", "2")
                        .param("estado", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioId").value(2L));
    }

    @Test
    void pagar_Retorna200() throws Exception {
        CuotaResponse res = CuotaResponse.builder().id(8L).estado("PAGADA").build();
        when(cuotaService.pagar(8L)).thenReturn(res);

        mockMvc.perform(put("/api/deudas/cuotas/8/pagar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PAGADA"));
    }

    @Test
    void siguiente_Retorna200() throws Exception {
        CuotaResponse res = CuotaResponse.builder().id(9L).numero(2).estado("PENDIENTE").build();
        when(cuotaService.siguiente(5L)).thenReturn(res);

        mockMvc.perform(get("/api/deudas/5/cuotas/siguiente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(2));
    }
}