# Decisões

## 1. Situações não cobertas

- Placas: remover espaços externos e converter para maiúsculas; sem máscara não solicitada.
- Decimal: aceitar vírgula ou ponto, sem milhar. Limite zero/negativo: invalidar para evitar divisão por zero e dados incoerentes.
- Datas: somente `yyyy-MM-dd'T'HH:mm:ss`, estritamente; datas brasileiras e impossíveis são inválidas.
- Branco: linha inválida. Contar todas as linhas de dados sem cabeçalho e preservar número físico. Terminador final não cria linha extra.
- Duplicatas: manter válidas. Horários iguais mantêm ordem do arquivo. Linha inválida não encerra episódio.
- Duração: primeira até última leitura em excesso; episódio isolado dura zero. Não estender até leitura normal.
- Percentual: BigDecimal, HALF_UP, uma casa em cada leitura antes do máximo e da gravidade.
- Somente cabeçalho: resultado vazio. Aceitar BOM UTF-8. Cabeçalho exato com quatro colunas. Limitar upload a 5 MB.

## 2. Parte mais difícil e solução

O ponto de maior atenção foi montar os episódios com os limites corretos e separar velocidade máxima de percentual máximo. Uma leitura de 80 km/h em limite de 60 tem excesso de 33,3%; outra de 62 km/h em limite de 40 tem excesso de 55%. Portanto, procurar somente a leitura mais rápida classificaria o episódio incorretamente. A solução calcula os dois máximos separadamente depois de agrupar por placa e ordenar por horário. Testes cobrem esse caso, os intervalos de 300/301 segundos e a tolerância de exatamente 5 km/h.

## 3. O que faria com mais tempo

As próximas melhorias seriam tornar tolerância e intervalo configuráveis e ampliar os testes automatizados de navegador para upload, filtro e recuperação após erro. Para arquivos maiores, seria necessário rever a ordenação em memória; essa otimização ficou fora do escopo para manter a solução simples.
