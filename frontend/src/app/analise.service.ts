import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AnaliseResponse } from './analise.model';

@Injectable({ providedIn: 'root' })
export class AnaliseService {
  private readonly http = inject(HttpClient);

  analisar(arquivo: File) {
    const form = new FormData();
    form.append('arquivo', arquivo);
    return this.http.post<AnaliseResponse>('/api/analises', form);
  }
}
