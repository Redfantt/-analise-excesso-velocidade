package br.com.frota.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class AnaliseControllerTest {
    @Autowired MockMvc mvc;
    private static final String HEADER = "placa;dataHora;velocidadeKmh;limiteKmh\n";

    @Test void analisaMultipartERetornaContrato() throws Exception {
        var arquivo = new MockMultipartFile("arquivo", "leituras.csv", "text/csv",
            (HEADER + "ABC;2026-10-05T08:00:00;72;60").getBytes(StandardCharsets.UTF_8));
        mvc.perform(multipart("/api/analises").file(arquivo)).andExpect(status().isOk())
            .andExpect(jsonPath("$.resumo.linhasLidas").value(1))
            .andExpect(jsonPath("$.resumo.linhasValidas").value(1))
            .andExpect(jsonPath("$.episodios[0].gravidade").value("LEVE"))
            .andExpect(jsonPath("$.ranking[0].totalPontos").value(1));
    }

    @Test void rejeitaVazio() throws Exception {
        mvc.perform(multipart("/api/analises").file(new MockMultipartFile("arquivo", new byte[0])))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test void rejeitaCabecalhoIncorreto() throws Exception {
        mvc.perform(multipart("/api/analises").file(new MockMultipartFile("arquivo", "errado".getBytes(StandardCharsets.UTF_8))))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test void exigeParteArquivo() throws Exception {
        mvc.perform(multipart("/api/analises")).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test void exigeMultipart() throws Exception {
        mvc.perform(post("/api/analises").contentType("application/json").content("{}"))
            .andExpect(status().isUnsupportedMediaType()).andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test void processaCsvOriginalComResultadoEsperado() throws Exception {
        var arquivo = new MockMultipartFile("arquivo", "leituras.csv", "text/csv", Files.readAllBytes(Path.of("../leituras.csv")));
        mvc.perform(multipart("/api/analises").file(arquivo)).andExpect(status().isOk())
            .andExpect(jsonPath("$.resumo.linhasLidas").value(43))
            .andExpect(jsonPath("$.resumo.linhasValidas").value(35))
            .andExpect(jsonPath("$.resumo.totalEpisodios").value(12))
            .andExpect(jsonPath("$.linhasInvalidas.length()").value(8))
            .andExpect(jsonPath("$.ranking[0].placa").value("GHI3J45"))
            .andExpect(jsonPath("$.ranking[0].totalPontos").value(6))
            .andExpect(jsonPath("$.ranking[1].placa").value("ABC1D23"));
    }
}
