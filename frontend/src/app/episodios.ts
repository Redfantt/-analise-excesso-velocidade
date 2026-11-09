import { Component, computed, input, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { Episodio } from './analise.model';
import { DataLocalPipe } from './data-local.pipe';

@Component({
  selector: 'app-episodios',
  imports: [DecimalPipe, DataLocalPipe],
  template: `
    <section class="panel" aria-labelledby="episodios-titulo">
      <div class="section-heading">
        <div><h2 id="episodios-titulo">Episódios de excesso</h2><p>{{ filtrados().length }} de {{ episodios().length }} episódios</p></div>
        <div><label for="gravidade">Gravidade</label>
          <select id="gravidade" [value]="gravidade()" (change)="filtrar($event)">
            <option value="">Todas</option><option value="LEVE">Leve</option><option value="GRAVE">Grave</option><option value="GRAVISSIMA">Gravíssima</option>
          </select>
        </div>
      </div>
      <div class="table-scroll"><table data-testid="episodios">
        <caption class="sr-only">Episódios filtrados por gravidade</caption>
        <thead><tr><th scope="col">Placa</th><th scope="col">Início</th><th scope="col">Fim</th><th scope="col">Duração (s)</th><th scope="col">Máxima (km/h)</th><th scope="col">Excesso máximo</th><th scope="col">Gravidade</th></tr></thead>
        <tbody>
          @for (episodio of filtrados(); track $index) {
            <tr><th scope="row">{{ episodio.placa }}</th><td>{{ episodio.inicio | dataLocal }}</td><td>{{ episodio.fim | dataLocal }}</td>
              <td>{{ episodio.duracaoSegundos }}</td><td>{{ episodio.velocidadeMaximaKmh | number:'1.0-2' }}</td><td>{{ episodio.excessoMaximoPercentual | number:'1.1-1' }}%</td>
              <td><span class="badge" [class]="'badge ' + episodio.gravidade.toLowerCase()">{{ episodio.gravidade === 'GRAVISSIMA' ? 'Gravíssima' : episodio.gravidade === 'GRAVE' ? 'Grave' : 'Leve' }}</span></td>
            </tr>
          } @empty { <tr><td colspan="7">Nenhum episódio para este filtro.</td></tr> }
        </tbody>
      </table></div>
    </section>
  `
})
export class EpisodiosComponent {
  readonly episodios = input.required<Episodio[]>();
  readonly gravidade = signal('');
  readonly filtrados = computed(() => this.episodios().filter(e => !this.gravidade() || e.gravidade === this.gravidade()));
  filtrar(event: Event) { this.gravidade.set((event.target as HTMLSelectElement).value); }
}
