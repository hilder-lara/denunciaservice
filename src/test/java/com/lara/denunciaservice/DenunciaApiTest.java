package com.lara.denunciaservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lara.denunciaservice.repository.DenunciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DenunciaApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired DenunciaRepository repository;

    @BeforeEach
    void limpiar() {
        repository.deleteAll();
    }

    @Test
    void cincoEndpointsConservanDatosYCambianEstado() throws Exception {
        JsonNode creada = crear("01234567", "  robo  ");
        int id = creada.get("id").asInt();
        assertEquals(1, creada.get("estado").asInt());
        assertEquals("ROBO", creada.get("tipo").asText());
        assertEquals("01234567", creada.get("dni").asText());
        assertEquals("2026-10-28T09:30:00", creada.get("fecha").asText());

        mvc.perform(get("/denuncias/").param("offset", "0").param("limit", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(id)));
        mvc.perform(get("/denuncias/{id}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.descripcion", is("Hecho ficticio para la prueba")));
        mvc.perform(get("/denuncias/ciudadano/{dni}", "01234567"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(put("/denuncias/{id}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado", is(0)))
                .andExpect(jsonPath("$.dni", is("01234567")))
                .andExpect(jsonPath("$.tipo", is("ROBO")))
                .andExpect(jsonPath("$.fecha", is("2026-10-28T09:30:00")))
                .andExpect(jsonPath("$.direccion", is("Av. Prueba 100")))
                .andExpect(jsonPath("$.descripcion", is("Hecho ficticio para la prueba")));
        // Repetir PUT mantiene la denuncia atendida: es idempotente.
        mvc.perform(put("/denuncias/{id}", id)).andExpect(status().isOk()).andExpect(jsonPath("$.estado", is(0)));
        mvc.perform(get("/denuncias/{id}", id)).andExpect(status().isOk()).andExpect(jsonPath("$.estado", is(0)));
        assertEquals(0, repository.findById(id).orElseThrow().getEstado());
    }

    @Test
    void paginacionRespetaOffsetExactoYBusquedaRetornaTodas() throws Exception {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            ids.add(crear("12345678", "VANDALISMO").get("id").asInt());
        }
        crear("87654321", "ASALTO");
        mvc.perform(get("/denuncias").param("offset", "0").param("limit", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].id", is(ids.get(0))))
                .andExpect(jsonPath("$[4].id", is(ids.get(4))));
        mvc.perform(get("/denuncias/").param("offset", "5").param("limit", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(ids.get(5))));
        // offset=2 no es múltiplo de limit=5. Debe omitir exactamente dos.
        mvc.perform(get("/denuncias/").param("offset", "2").param("limit", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id", is(ids.get(2))))
                .andExpect(jsonPath("$[4].id", is(ids.get(6))));
        mvc.perform(get("/denuncias/ciudadano/12345678"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(7)));
        mvc.perform(get("/denuncias/ciudadano/00000000"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/denuncias/").param("offset", "1000"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void servidorControlaEstadoEIdentificador() throws Exception {
        Map<String, Object> datos = datos("12345678", "ASALTO");
        datos.put("id", 999999);
        datos.put("estado", 0);
        MvcResult result = mvc.perform(post("/denuncias/").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(datos)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estado", is(1))).andReturn();
        assertNotEquals(999999, mapper.readTree(result.getResponse().getContentAsString()).get("id").asInt());
    }

    @Test
    void rechazaCamposInvalidosYFechasIncorrectas() throws Exception {
        for (String campo : List.of("dni", "fecha", "tipo", "direccion", "descripcion")) {
            Map<String, Object> datos = datos("12345678", "ROBO");
            datos.remove(campo);
            mvc.perform(post("/denuncias/").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(datos)))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors." + campo).exists());
        }
        for (String dni : List.of("123", "ABCDEFGH", "123456789")) {
            mvc.perform(post("/denuncias/").contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(datos(dni, "ROBO"))))
                    .andExpect(status().isBadRequest());
        }
        for (Map.Entry<String, Integer> campo : Map.of("tipo", 31, "direccion", 201, "descripcion", 256).entrySet()) {
            Map<String, Object> datos = datos("12345678", "ROBO");
            datos.put(campo.getKey(), "x".repeat(campo.getValue()));
            mvc.perform(post("/denuncias/").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(datos)))
                    .andExpect(status().isBadRequest());
        }
        Map<String, Object> datos = datos("12345678", "  ");
        mvc.perform(post("/denuncias/").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(datos)))
                .andExpect(status().isBadRequest());
        datos.put("fecha", "28/10/2026");
        mvc.perform(post("/denuncias/").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(datos)))
                .andExpect(status().isBadRequest());
        assertEquals(0, repository.count());
    }

    @Test
    void responde400Y404EnConsultasInvalidas() throws Exception {
        mvc.perform(get("/denuncias/999999")).andExpect(status().isNotFound());
        mvc.perform(put("/denuncias/999999")).andExpect(status().isNotFound());
        mvc.perform(get("/denuncias/0")).andExpect(status().isBadRequest());
        mvc.perform(get("/denuncias/texto")).andExpect(status().isBadRequest());
        mvc.perform(get("/denuncias/ciudadano/123")).andExpect(status().isBadRequest());
        mvc.perform(get("/denuncias/").param("offset", "-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/denuncias/").param("limit", "0")).andExpect(status().isBadRequest());
        mvc.perform(get("/denuncias/").param("limit", "101")).andExpect(status().isBadRequest());
    }

    private JsonNode crear(String dni, String tipo) throws Exception {
        MvcResult result = mvc.perform(post("/denuncias/").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(datos(dni, tipo))))
                .andExpect(status().isCreated()).andExpect(header().exists("Location")).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString());
    }

    private Map<String, Object> datos(String dni, String tipo) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("dni", dni);
        datos.put("fecha", "2026-10-28T09:30:00");
        datos.put("tipo", tipo);
        datos.put("direccion", "Av. Prueba 100");
        datos.put("descripcion", "Hecho ficticio para la prueba");
        return datos;
    }
}
