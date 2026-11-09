package br.com.frota.analise;

import java.math.BigDecimal;

public record ItemRanking(String placa, int totalPontos, BigDecimal velocidadeMaximaKmh, int totalEpisodios) {}
