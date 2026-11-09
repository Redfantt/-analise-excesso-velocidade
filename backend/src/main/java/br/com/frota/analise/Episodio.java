package br.com.frota.analise;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Episodio(String placa, LocalDateTime inicio, LocalDateTime fim, long duracaoSegundos,
                       BigDecimal velocidadeMaximaKmh, BigDecimal excessoMaximoPercentual, Gravidade gravidade) {}
