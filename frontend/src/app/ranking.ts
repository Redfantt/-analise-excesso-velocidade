import { Component, input } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ItemRanking } from './analise.model';

@Component({
  selector: 'app-ranking', imports: [DecimalPipe],
  template: `
    <section class="panel" aria-labelledby="ranking-titulo">
      <h2 id="ranking-titulo">Ranking de veículos</h2><p>Mais pontos primeiro; desempate por velocidade máxima e placa.</p>
      <div class="table-scroll"><table data-testid="ranking">
        <caption class="sr-only">Veículos com episódios de excesso</caption>
        <thead><tr><th scope="col">Posição</th><th scope="col">Placa</th><th scope="col">Pontos</th><th scope="col">Máxima em episódio (km/h)</th><th scope="col">Episódios</th></tr></thead>
        <tbody>
          @for (item of ranking(); track item.placa) {
            <tr><td>{{ $index + 1 }}</td><th scope="row">{{ item.placa }}</th><td><strong>{{ item.totalPontos }}</strong></td><td>{{ item.velocidadeMaximaKmh | number:'1.0-2' }}</td><td>{{ item.totalEpisodios }}</td></tr>
          } @empty { <tr><td colspan="5">Nenhum veículo com excesso de velocidade.</td></tr> }
        </tbody>
      </table></div>
    </section>
  `
})
export class RankingComponent { readonly ranking = input.required<ItemRanking[]>(); }
