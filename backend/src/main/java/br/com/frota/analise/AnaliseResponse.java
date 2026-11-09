package br.com.frota.analise;

import br.com.frota.csv.LinhaInvalida;
import java.util.List;

public record AnaliseResponse(Resumo resumo, List<Episodio> episodios, List<ItemRanking> ranking,
                              List<LinhaInvalida> linhasInvalidas) {}
