import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';
import { AnaliseService } from './analise.service';
import { AnaliseResponse } from './analise.model';
import { EpisodiosComponent } from './episodios';
import { RankingComponent } from './ranking';

@Component({ selector: 'app-root', imports: [EpisodiosComponent, RankingComponent], templateUrl: './app.html' })
export class AppComponent {
  private readonly service = inject(AnaliseService);
  readonly arquivo = signal<File | null>(null);
  readonly carregando = signal(false);
  readonly resultado = signal<AnaliseResponse | null>(null);
  readonly erro = signal('');

  selecionar(event: Event) {
    this.arquivo.set((event.target as HTMLInputElement).files?.[0] ?? null);
    this.erro.set('');
    this.resultado.set(null);
  }

  analisar(event: Event) {
    event.preventDefault();
    const arquivo = this.arquivo();
    if (!arquivo || this.carregando()) return;
    this.resultado.set(null);
    this.erro.set('');
    this.carregando.set(true);
    this.service.analisar(arquivo).pipe(finalize(() => this.carregando.set(false))).subscribe({
      next: resultado => this.resultado.set(resultado),
      error: (erro: HttpErrorResponse) => this.erro.set(erro.status === 0
        ? 'Não foi possível acessar o backend. Verifique se ele está em execução e tente novamente.'
        : erro.error?.mensagem ?? 'Não foi possível analisar o arquivo. Tente novamente.')
    });
  }
}
