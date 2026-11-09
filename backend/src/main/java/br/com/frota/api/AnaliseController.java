package br.com.frota.api;

import br.com.frota.analise.AnaliseResponse;
import br.com.frota.analise.AnaliseService;
import br.com.frota.csv.CsvParser;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/analises")
public class AnaliseController {
    private final CsvParser parser;
    private final AnaliseService service;

    public AnaliseController(CsvParser parser, AnaliseService service) {
        this.parser = parser;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnaliseResponse analisar(@RequestParam("arquivo") MultipartFile arquivo) {
        try {
            return service.analisar(parser.parse(arquivo.getInputStream()));
        } catch (IOException e) {
            throw new ArquivoInvalidoException("Não foi possível abrir o arquivo enviado.");
        }
    }
}
