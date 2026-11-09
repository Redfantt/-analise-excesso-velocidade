# Análise de excesso de velocidade

Aplicação Java + Angular que recebe CSV, identifica episódios de excesso e monta um ranking. Tudo em memória, sem banco ou cadastro.

Referências: [enunciado original](ENUNCIADO.md) e [decisões técnicas](DECISOES.md).

## Requisitos

JDK 17+ (validado com 21), Maven 3.6.3+, npm e Node 20.19+ na linha 20, 22.12+ na linha 22 ou 24.x. Projeto: Spring Boot 3.5.16 e Angular 21.2.26. Compatibilidade: [Spring Boot](https://docs.spring.io/spring-boot/3.5/system-requirements.html) e [Angular](https://angular.dev/reference/versions).

## Executar

Terminal 1, a partir da raiz:

```powershell
cd backend
mvn -s maven-settings.xml "-Dmaven.repo.local=../.cache/m2" spring-boot:run
```

Terminal 2, a partir da raiz:

```powershell
cd frontend
npm ci --cache ../.cache/npm
npm start
```

Abra http://127.0.0.1:4200 e envie `leituras.csv`. O proxy Angular encaminha `/api` a `127.0.0.1:8080`; mantenha ambos rodando. Ctrl+C encerra os processos.

A configuração Maven usa Central e cache nesta pasta. npm instala somente dependências locais. Nenhuma configuração global é alterada. O Rollup usa sua distribuição oficial WebAssembly porque a política Windows da máquina de desenvolvimento bloqueou o módulo nativo.

## Testes e builds

Na pasta `backend/`:

```powershell
mvn -s maven-settings.xml "-Dmaven.repo.local=../.cache/m2" clean verify
java -jar target/analise-frota-1.0.0.jar
```

Na pasta `frontend/`:

```powershell
npm test
npm run build
```

São 37 casos no backend e 4 na tela. Execute Maven em `backend/`: o teste integrado usa o CSV da raiz. O frontend gera `frontend/dist/frota/browser`. O proxy é de desenvolvimento; para hospedar o build, configure um servidor HTTP que encaminhe `/api` ao backend.

## API

`POST /api/analises`, multipart/form-data, campo **arquivo**. CSV UTF-8 com cabeçalho obrigatório:

```text
placa;dataHora;velocidadeKmh;limiteKmh
ABC1D23;2026-10-05T08:00:00;72;60
```

Na raiz, em PowerShell:

```powershell
curl.exe -F "arquivo=@leituras.csv" http://127.0.0.1:8080/api/analises
```

Resposta para o exemplo de uma leitura:

```json
{
  "resumo": { "linhasLidas": 1, "linhasValidas": 1, "totalEpisodios": 1 },
  "episodios": [{
    "placa": "ABC1D23", "inicio": "2026-10-05T08:00:00", "fim": "2026-10-05T08:00:00",
    "duracaoSegundos": 0, "velocidadeMaximaKmh": 72, "excessoMaximoPercentual": 20.0, "gravidade": "LEVE"
  }],
  "ranking": [{ "placa": "ABC1D23", "totalPontos": 1, "velocidadeMaximaKmh": 72, "totalEpisodios": 1 }],
  "linhasInvalidas": []
}
```

Erros retornam JSON com `mensagem`: HTTP 400 para arquivo ausente/vazio/cabeçalho incorreto ou codificação inválida, 413 para arquivo acima de 5 MB e 415 para conteúdo que não seja multipart. Linhas inválidas são reportadas na resposta 200 quando o arquivo foi aceito.

## Resultado do CSV fornecido

43 linhas de dados, 35 válidas, 8 inválidas e 12 episódios. Linhas inválidas: 4, 9, 12, 21, 22, 27, 38 e 41, com cabeçalho na linha 1.

| Posição | Placa | Pontos | Máxima em episódio (km/h) | Episódios |
|---|---|---|---|---|
| 1 | GHI3J45 | 6 | 100 | 2 |
| 2 | ABC1D23 | 6 | 80 | 2 |
| 3 | DEF2G34 | 6 | 78 | 2 |
| 4 | YZA9B01 | 5 | 95 | 1 |
| 5 | MNO5P67 | 4 | 72,5 | 2 |
| 6 | STU7V89 | 3 | 90 | 1 |
| 7 | VWX8Y90 | 3 | 74 | 1 |
| 8 | JKL4M56 | 1 | 70 | 1 |

O filtro de gravidade afeta somente a tabela de episódios; resumo e ranking representam o arquivo completo.

## Organização

- `backend/.../csv`: parser, validação e diagnóstico de linhas.
- `backend/.../analise`: episódios, classificação, ranking e DTOs.
- `backend/.../api`: endpoint e tratamento de erros HTTP.
- `frontend/src/app/app.*`: tela principal e estado.
- `frontend/src/app/analise.service.ts`: chamada HTTP; `analise.model.ts`: contrato.
- `frontend/src/app/episodios.ts`: tabela e filtro; `ranking.ts`: ranking; `data-local.pipe.ts`: datas sem conversão de fuso.
