package br.com.frota.csv;

import java.util.List;

public record ResultadoCsv(int linhasLidas, List<Leitura> leituras, List<LinhaInvalida> linhasInvalidas) {}
