package br.com.frota.analise;

import br.com.frota.csv.CsvParser;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.*;

class AnaliseServiceTest {
    private final AnaliseService service = new AnaliseService();

    private AnaliseResponse analisar(String linhas) {
        var csv = "placa;dataHora;velocidadeKmh;limiteKmh\n" + linhas;
        return service.analisar(new CsvParser().parse(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))));
    }

    @Test void exatamenteCincoAcimaNaoEExcesso() {
        var resultado = analisar("ABC;2026-10-05T08:00:00;65;60\nABC;2026-10-05T08:01:00;66;60");
        assertThat(resultado.episodios()).hasSize(1);
        assertThat(resultado.episodios().get(0).inicio().toString()).isEqualTo("2026-10-05T08:01");
        assertThat(resultado.episodios().get(0).duracaoSegundos()).isZero();
    }

    @Test void ordenaLeiturasEMantemCincoMinutosMasSeparaCincoMinutosEUmSegundo() {
        var resultado = analisar("ABC;2026-10-05T08:10:01;78;60\nABC;2026-10-05T08:05:00;75;60\nABC;2026-10-05T08:00:00;75;60");
        assertThat(resultado.episodios()).extracting(Episodio::duracaoSegundos).containsExactly(300L, 0L);
        assertThat(resultado.ranking().get(0).totalPontos()).isEqualTo(6);
    }

    @Test void leituraNormalEncerraSemEstenderDuracao() {
        var resultado = analisar("ABC;2026-10-05T08:00:00;70;60\nABC;2026-10-05T08:01:00;60;60\nABC;2026-10-05T08:02:00;70;60");
        assertThat(resultado.episodios()).extracting(Episodio::duracaoSegundos).containsExactly(0L, 0L);
    }

    @Test void percentualMaximoIndependeDaMaiorVelocidade() {
        var resultado = analisar("ABC;2026-10-05T08:00:00;80;60\nABC;2026-10-05T08:01:00;62;40");
        var episodio = resultado.episodios().get(0);
        assertThat(episodio.velocidadeMaximaKmh()).isEqualByComparingTo("80");
        assertThat(episodio.excessoMaximoPercentual()).isEqualByComparingTo("55.0");
        assertThat(episodio.gravidade()).isEqualTo(Gravidade.GRAVISSIMA);
    }

    @ParameterizedTest @CsvSource({"120,LEVE,1,20.0", "120.04,LEVE,1,20.0", "120.05,GRAVE,3,20.1", "150,GRAVE,3,50.0", "150.05,GRAVISSIMA,5,50.1"})
    void classificaComArredondamentoAntesDaGravidade(String velocidade, Gravidade gravidade, int pontos, String percentual) {
        var resultado = analisar("ABC;2026-10-05T08:00:00;" + velocidade + ";100");
        assertThat(resultado.episodios().get(0).gravidade()).isEqualTo(gravidade);
        assertThat(resultado.episodios().get(0).excessoMaximoPercentual()).isEqualByComparingTo(percentual);
        assertThat(resultado.ranking().get(0).totalPontos()).isEqualTo(pontos);
    }

    @Test void desempataPorVelocidadeEDepoisPlacaSemIncluirVeiculoNormal() {
        var resultado = analisar("ZZZ;2026-10-05T08:00:00;72;60\nBBB;2026-10-05T08:00:00;96;80\nAAA;2026-10-05T08:00:00;96;80\nNORMAL;2026-10-05T08:00:00;60;60");
        assertThat(resultado.ranking()).extracting(ItemRanking::placa).containsExactly("AAA", "BBB", "ZZZ");
    }

    @Test void mantemOrdemOriginalQuandoHorariosEmpatamEDuplicatasValidas() {
        var resultado = analisar("ABC;2026-10-05T08:00:00;72;60\nABC;2026-10-05T08:00:00;72;60\nABC;2026-10-05T08:00:00;60;60\nABC;2026-10-05T08:00:00;72;60");
        assertThat(resultado.resumo().linhasValidas()).isEqualTo(4);
        assertThat(resultado.episodios()).hasSize(2);
    }

    @Test void linhaInvalidaNaoEncerraECalculaDuracaoNaViradaDoDia() {
        var resultado = analisar("ABC;2026-10-05T23:58:00;72;60\nlinha quebrada\nABC;2026-10-06T00:01:00;90;60\nABC;2026-10-06T00:02:00;40;60");
        assertThat(resultado.episodios()).hasSize(1);
        assertThat(resultado.episodios().get(0).duracaoSegundos()).isEqualTo(180);
        assertThat(resultado.linhasInvalidas()).hasSize(1);
    }

    @Test void arquivoSemExcessoRetornaListasVazias() {
        var resultado = analisar("ABC;2026-10-05T08:00:00;60;60");
        assertThat(resultado.episodios()).isEmpty();
        assertThat(resultado.ranking()).isEmpty();
        assertThat(resultado.resumo().totalEpisodios()).isZero();
    }
}
