package br.com.frota.analise;

import br.com.frota.csv.ResultadoCsv;
import br.com.frota.csv.Leitura;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnaliseService {
    private static final BigDecimal TOLERANCIA_KMH = new BigDecimal("5");
    private static final Duration INTERVALO_MAXIMO = Duration.ofMinutes(5);

    public AnaliseResponse analisar(ResultadoCsv csv) {
        var porPlaca = csv.leituras().stream().collect(Collectors.groupingBy(Leitura::placa, TreeMap::new, Collectors.toList()));
        List<Episodio> episodios = new ArrayList<>();
        for (var leituras : porPlaca.values()) {
            // List.sort é estável: horários iguais mantêm a ordem do arquivo.
            leituras.sort(Comparator.comparing(Leitura::dataHora));
            List<Leitura> atual = new ArrayList<>();
            Leitura anterior = null;
            for (Leitura leitura : leituras) {
                boolean semSinal = anterior != null && Duration.between(anterior.dataHora(), leitura.dataHora()).compareTo(INTERVALO_MAXIMO) > 0;
                boolean excesso = leitura.velocidadeKmh().compareTo(leitura.limiteKmh().add(TOLERANCIA_KMH)) > 0;
                if (semSinal || !excesso) fechar(atual, episodios);
                if (excesso) atual.add(leitura);
                anterior = leitura;
            }
            fechar(atual, episodios);
        }
        return new AnaliseResponse(new Resumo(csv.linhasLidas(), csv.leituras().size(), episodios.size()),
            List.copyOf(episodios), ranking(episodios), csv.linhasInvalidas());
    }

    private void fechar(List<Leitura> atual, List<Episodio> episodios) {
        if (atual.isEmpty()) return;
        Leitura primeira = atual.get(0);
        Leitura ultima = atual.get(atual.size() - 1);
        BigDecimal maxima = atual.stream().map(Leitura::velocidadeKmh).max(BigDecimal::compareTo).orElseThrow();
        BigDecimal percentual = atual.stream().map(this::percentual).max(BigDecimal::compareTo).orElseThrow();
        Gravidade gravidade = percentual.compareTo(new BigDecimal("20")) <= 0 ? Gravidade.LEVE
            : percentual.compareTo(new BigDecimal("50")) <= 0 ? Gravidade.GRAVE : Gravidade.GRAVISSIMA;
        episodios.add(new Episodio(primeira.placa(), primeira.dataHora(), ultima.dataHora(),
            Duration.between(primeira.dataHora(), ultima.dataHora()).getSeconds(), maxima, percentual, gravidade));
        atual.clear();
    }

    private BigDecimal percentual(Leitura leitura) {
        return leitura.velocidadeKmh().subtract(leitura.limiteKmh()).multiply(new BigDecimal("100"))
            .divide(leitura.limiteKmh(), 1, RoundingMode.HALF_UP);
    }

    private List<ItemRanking> ranking(List<Episodio> episodios) {
        var grupos = episodios.stream().collect(Collectors.groupingBy(Episodio::placa));
        return grupos.entrySet().stream().map(entry -> {
            var lista = entry.getValue();
            return new ItemRanking(entry.getKey(), lista.stream().mapToInt(e -> e.gravidade().pontos()).sum(),
                lista.stream().map(Episodio::velocidadeMaximaKmh).max(BigDecimal::compareTo).orElseThrow(), lista.size());
        }).sorted(Comparator.comparingInt(ItemRanking::totalPontos).reversed()
            .thenComparing(ItemRanking::velocidadeMaximaKmh, Comparator.reverseOrder())
            .thenComparing(ItemRanking::placa)).toList();
    }
}
