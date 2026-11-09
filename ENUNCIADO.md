# Desafio técnico: Dev Júnior Java + Angular

**Prazo:** 2 dias corridos a partir do recebimento.
**Esforço esperado:** cerca de 1 dia de trabalho (4 a 8 horas). Se passar muito disso, pare e entregue o que tiver. Entrega parcial bem explicada vale mais que entrega completa que você não sabe explicar.

## O problema

Uma frota de veículos envia leituras de velocidade do rastreador. O gestor recebe esse arquivo e quer saber **quais veículos estão correndo acima do limite e o quanto isso é grave**.

Olhar leitura por leitura não ajuda: um veículo que passou 3 minutos acima do limite gera várias linhas, mas é uma ocorrência só. Você vai construir uma aplicação que recebe o arquivo e transforma as leituras em **episódios de excesso** e em um **ranking de veículos**.

Não é preciso banco de dados nem cadastro. O arquivo entra, o resultado sai.

## O arquivo

Use o [`leituras.csv`](leituras.csv) que acompanha este enunciado. Formato (separado por `;`, com cabeçalho):

```
placa;dataHora;velocidadeKmh;limiteKmh
ABC1D23;2026-10-05T08:00:00;72;60
```

## Regras de negócio

1. **Linha inválida não derruba o processamento.** Campo faltando, data que não dá para ler, número que não é número ou velocidade negativa: a linha é ignorada e aparece na lista de linhas inválidas, com o **número da linha no arquivo** e o **motivo**.
2. **O arquivo pode vir fora de ordem.** As leituras devem ser analisadas por veículo, em ordem de data e hora.
3. **Leitura em excesso:** `velocidade > limite + 5 km/h`. Exatamente 5 km/h acima do limite ainda não é excesso.
4. **Episódio:** sequência de leituras em excesso consecutivas do mesmo veículo. O episódio termina quando:
   - chega uma leitura que não está em excesso; ou
   - o intervalo até a próxima leitura é **maior que 5 minutos** (o rastreador ficou sem sinal). Exatamente 5 minutos não encerra.
5. Para cada episódio, informe: placa, início, fim, duração em segundos, velocidade máxima, excesso máximo (%) e gravidade.
   - Excesso de uma leitura: `(velocidade - limite) / limite * 100`, arredondado para 1 casa decimal.
   - O excesso máximo é o **maior percentual** entre as leituras do episódio. Ele pode não ser o da leitura de maior velocidade, porque o limite muda ao longo da via.
6. **Gravidade**, de acordo com o excesso máximo:

   | Excesso máximo | Gravidade | Pontos |
   |---|---|---|
   | até 20% | LEVE | 1 |
   | acima de 20% até 50% | GRAVE | 3 |
   | acima de 50% | GRAVISSIMA | 5 |

7. **Ranking de veículos:** total de pontos (maior primeiro). Em caso de empate, ganha a maior velocidade registrada em episódio. Se ainda empatar, ordem alfabética da placa. Veículo sem episódio não aparece.

> O enunciado não cobre todas as situações que existem no arquivo. Quando encontrar uma, tome uma decisão e registre no `DECISOES.md`.

## O que construir

**Backend: Java 17+ e Spring Boot 3+**
- Um endpoint `POST /api/analises` que recebe o arquivo e retorna JSON com: episódios, ranking, linhas inválidas e um resumo (linhas lidas, linhas válidas, total de episódios).
- Arquivo vazio ou sem o cabeçalho esperado retorna **HTTP 400** com uma mensagem clara.
- No mínimo **3 testes automatizados** das regras de negócio. Escolha os casos que você considera mais importantes.

**Frontend: Angular 17+**
- Tela para enviar o arquivo.
- Exibir o resumo, o ranking e a tabela de episódios.
- Filtro da tabela de episódios por gravidade, sem nova chamada ao backend.
- Exibir as linhas inválidas e tratar o erro quando o backend recusar o arquivo.

O visual não precisa ser bonito, só organizado e utilizável. Biblioteca de componentes é opcional.

## O que entregar

Um repositório Git público (GitHub ou GitLab) com:
- O código do backend e do frontend.
- Um `README.md` explicando como rodar.
- Um `DECISOES.md` **curto, escrito por você**, respondendo:
  1. Que situações o enunciado não cobria e o que você decidiu em cada uma?
  2. Qual foi a parte mais difícil e como você resolveu?
  3. O que você faria diferente com mais tempo?
- Histórico de commits ao longo do trabalho, em vez de um único commit no final.

## Sobre ferramentas de IA

Pode usar Google, documentação, Stack Overflow e ferramentas de IA. **Na entrevista técnica você vai explicar o código e fazer alterações nele ao vivo, compartilhando a tela e sem IA.** Use o que quiser, desde que no final o código seja realmente seu: você precisa entender e saber mexer em tudo que entregar.

## O que avaliamos

Regras corretas, código legível, testes que fazem sentido, tratamento de erros e, principalmente, o quanto você entende do que entregou. Não esperamos arquitetura sofisticada: **simples e funcionando é melhor que elaborado.**
