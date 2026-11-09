package br.com.frota.csv;

import br.com.frota.api.ArquivoInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.*;

class CsvParserTest {
    private final CsvParser parser = new CsvParser();
    private static final String HEADER = "placa;dataHora;velocidadeKmh;limiteKmh\n";

    private ResultadoCsv parse(String csv) {
        return parser.parse(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
    }

    @Test void normalizaPlacaEAceitaVirgulaDecimal() {
        var resultado = parse("\uFEFF" + HEADER + " abc1d23 ;2026-10-05T08:00:00;72,5;60\n");
        assertThat(resultado.linhasLidas()).isEqualTo(1);
        assertThat(resultado.leituras()).hasSize(1);
        assertThat(resultado.leituras().get(0).placa()).isEqualTo("ABC1D23");
        assertThat(resultado.leituras().get(0).velocidadeKmh()).isEqualByComparingTo("72.5");
    }

    @Test void preservaNumeroFisicoDaLinhaEContinuaAposErro() {
        var resultado = parse(HEADER + "\nABC;2026-10-05T08:00:00;abc;60\nABC;2026-10-05T08:01:00;70;60");
        assertThat(resultado.linhasLidas()).isEqualTo(3);
        assertThat(resultado.leituras()).hasSize(1);
        assertThat(resultado.linhasInvalidas()).extracting(LinhaInvalida::linha).containsExactly(2, 3);
        assertThat(resultado.linhasInvalidas()).allSatisfy(erro -> assertThat(erro.motivo()).isNotBlank());
    }

    @ParameterizedTest @ValueSource(strings = {
        "ABC;2026-02-30T08:00:00;70;60", "ABC;05/10/2026 08:00:00;70;60",
        "ABC;2026-10-05T08:00:00;-1;60", "ABC;2026-10-05T08:00:00;50;0",
        "ABC;2026-10-05T08:00:00;50;-1", "ABC;2026-10-05T08:00:00;;60",
        "ABC;2026-10-05T08:00:00;70", "ABC;2026-10-05T08:00:00;70;60;extra",
        ";2026-10-05T08:00:00;70;60", "ABC;2026-10-05T08:00:00;NaN;60"
    }) void coletaLinhaInvalida(String linha) {
        var resultado = parse(HEADER + linha);
        assertThat(resultado.leituras()).isEmpty();
        assertThat(resultado.linhasInvalidas()).hasSize(1);
        assertThat(resultado.linhasInvalidas().get(0).linha()).isEqualTo(2);
    }

    @Test void aceitaApenasCabecalhoSemInventarLinhaFinal() {
        assertThat(parse(HEADER).linhasLidas()).isZero();
        assertThat(parse(HEADER).leituras()).isEmpty();
    }

    @ParameterizedTest @ValueSource(strings = {"", "\n", "placa,dataHora,velocidadeKmh,limiteKmh", "placa;dataHora;limiteKmh;velocidadeKmh"})
    void rejeitaArquivoVazioOuCabecalhoErrado(String csv) {
        assertThatThrownBy(() -> parse(csv)).isInstanceOf(ArquivoInvalidoException.class).hasMessageContaining("cabeçalho");
    }

    @Test void rejeitaUtf8Corrompido() {
        assertThatThrownBy(() -> parser.parse(new ByteArrayInputStream(new byte[]{(byte) 0xC3, 0x28})))
            .isInstanceOf(ArquivoInvalidoException.class).hasMessageContaining("UTF-8");
    }
}
