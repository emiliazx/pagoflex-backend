package pagoflex.deudas.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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

import pagoflex.deudas.dto.DeudaRequest;
import pagoflex.deudas.dto.DeudaResponse;
import pagoflex.deudas.security.JwtService;
import pagoflex.deudas.service.DeudaService;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(DeudaController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeudaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeudaService deudaService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void crear_ConDatosValidos_Retorna201Created() throws Exception {
        DeudaRequest req = DeudaRequest.builder()
                .usuarioId(1L)
                .concepto("Internet Fibra")
                .empresa("Movistar")
                .monto(new BigDecimal("35.00"))
                .vencimiento("2026-12-15")
                .cantidadCuotas(1)
                .build();

        DeudaResponse res = DeudaResponse.builder()
                .id(20L).usuarioId(1L).concepto("Internet Fibra")
                .monto(new BigDecimal("35.00")).estado("PENDIENTE").build();

        when(deudaService.crear(any(DeudaRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/deudas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20L))
                .andExpect(jsonPath("$.concepto").value("Internet Fibra"));
    }

    @Test
    void crear_ConConceptoVacio_Retorna400BadRequest() throws Exception {
        DeudaRequest reqInvalido = DeudaRequest.builder()
                .usuarioId(1L)
                .concepto("")
                .empresa("Movistar")
                .monto(new BigDecimal("35.00"))
                .vencimiento("2026-12-15")
                .build();

        mockMvc.perform(post("/api/deudas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listar_Retorna200YLista() throws Exception {
        DeudaResponse res = DeudaResponse.builder().id(1L).usuarioId(1L).concepto("Agua").build();
        when(deudaService.listar(1L, null, null, null, null, null)).thenReturn(List.of(res));

        mockMvc.perform(get("/api/deudas").param("usuarioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void obtener_Retorna200() throws Exception {
        DeudaResponse res = DeudaResponse.builder().id(5L).concepto("Luz").build();
        when(deudaService.obtener(5L)).thenReturn(res);

        mockMvc.perform(get("/api/deudas/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L));
    }

    @Test
    void actualizar_Retorna200() throws Exception {
        DeudaRequest req = DeudaRequest.builder()
                .usuarioId(1L).concepto("Luz Editada").empresa("Enel")
                .monto(new BigDecimal("50.00")).vencimiento("2026-12-20").build();

        DeudaResponse res = DeudaResponse.builder().id(5L).concepto("Luz Editada").build();
        when(deudaService.actualizar(eq(5L), any(DeudaRequest.class))).thenReturn(res);

        mockMvc.perform(put("/api/deudas/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concepto").value("Luz Editada"));
    }

    @Test
    void pagar_Retorna200() throws Exception {
        DeudaResponse res = DeudaResponse.builder().id(5L).estado("PAGADA").build();
        when(deudaService.pagar(5L)).thenReturn(res);

        mockMvc.perform(put("/api/deudas/5/pagar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PAGADA"));
    }

    @Test
    void eliminar_Retorna204NoContent() throws Exception {
        doNothing().when(deudaService).eliminar(5L);

        mockMvc.perform(delete("/api/deudas/5"))
                .andExpect(status().isNoContent());
    }
}