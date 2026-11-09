package br.com.frota.csv;

import br.com.frota.api.ArquivoInvalidoException;
import org.springframework.stereotype.Component;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class CsvParser {
    private static final String CABECALHO = "placa;dataHora;velocidadeKmh;limiteKmh";
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss")
        .withResolverStyle(ResolverStyle.STRICT);

    public ResultadoCsv parse(InputStream arquivo) {
        var decoder = StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
        try (var reader = new BufferedReader(new InputStreamReader(arquivo, decoder))) {
            String cabecalho = reader.readLine();
            if (cabecalho != null && cabecalho.startsWith("\uFEFF")) cabecalho = cabecalho.substring(1);
            if (!CABECALHO.equals(cabecalho)) {
                throw new ArquivoInvalidoException("Arquivo vazio ou cabeçalho inválido. Esperado: " + CABECALHO);
            }
            List<Leitura> leituras = new ArrayList<>();
            List<LinhaInvalida> invalidas = new ArrayList<>();
            String linha;
            int numero = 1;
            while ((linha = reader.readLine()) != null) {
                numero++;
                try {
                    leituras.add(parseLinha(linha));
                } catch (IllegalArgumentException e) {
                    invalidas.add(new LinhaInvalida(numero, e.getMessage()));
                }
            }
            return new ResultadoCsv(numero - 1, List.copyOf(leituras), List.copyOf(invalidas));
        } catch (IOException e) {
            throw new ArquivoInvalidoException("Não foi possível ler o arquivo. Envie um CSV em UTF-8.");
        }
    }

    private Leitura parseLinha(String linha) {
        if (linha.isBlank()) throw new IllegalArgumentException("Linha em branco.");
        String[] campos = linha.split(";", -1);
        if (campos.length != 4) throw new IllegalArgumentException("Esperadas 4 colunas separadas por ponto e vírgula.");
        for (int i = 0; i < campos.length; i++) {
            campos[i] = campos[i].strip();
            if (campos[i].isEmpty()) throw new IllegalArgumentException("Campo obrigatório ausente: " + CABECALHO.split(";")[i] + ".");
        }
        LocalDateTime data;
        try {
            data = LocalDateTime.parse(campos[1], DATA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("dataHora inválida. Use yyyy-MM-dd'T'HH:mm:ss.");
        }
        BigDecimal velocidade = numero(campos[2], "velocidadeKmh");
        BigDecimal limite = numero(campos[3], "limiteKmh");
        if (velocidade.signum() < 0) throw new IllegalArgumentException("velocidadeKmh não pode ser negativa.");
        if (limite.signum() <= 0) throw new IllegalArgumentException("limiteKmh deve ser maior que zero.");
        return new Leitura(campos[0].toUpperCase(Locale.ROOT), data, velocidade, limite);
    }

    private BigDecimal numero(String valor, String campo) {
        if (!valor.matches("[+-]?\\d+(?:[.,]\\d+)?")) {
            throw new IllegalArgumentException(campo + " deve ser um número decimal sem separador de milhar.");
        }
        return new BigDecimal(valor.replace(',', '.'));
    }
}
