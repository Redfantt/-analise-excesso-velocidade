import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AppComponent } from './app';

const resposta = {
  resumo: { linhasLidas: 3, linhasValidas: 2, totalEpisodios: 2 },
  ranking: [{ placa: 'ABC', totalPontos: 6, velocidadeMaximaKmh: 100, totalEpisodios: 2 }],
  episodios: [
    { placa: 'ABC', inicio: '2026-10-05T08:00:00', fim: '2026-10-05T08:00:00', duracaoSegundos: 0, velocidadeMaximaKmh: 72, excessoMaximoPercentual: 20, gravidade: 'LEVE' },
    { placa: 'ABC', inicio: '2026-10-05T09:00:00', fim: '2026-10-05T09:01:00', duracaoSegundos: 60, velocidadeMaximaKmh: 100, excessoMaximoPercentual: 66.7, gravidade: 'GRAVISSIMA' }
  ],
  linhasInvalidas: [{ linha: 4, motivo: 'Data inválida' }]
};

describe('Tela de análise', () => {
  let fixture: ComponentFixture<AppComponent>;
  let http: HttpTestingController;
  let root: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [AppComponent], providers: [provideHttpClient(), provideHttpClientTesting()] }).compileComponents();
    fixture = TestBed.createComponent(AppComponent);
    http = TestBed.inject(HttpTestingController);
    root = fixture.nativeElement;
    fixture.detectChanges();
  });
  afterEach(() => http.verify());

  function enviar() {
    const input = root.querySelector('input[type=file]') as HTMLInputElement;
    Object.defineProperty(input, 'files', { configurable: true, value: [new File(['csv'], 'leituras.csv', { type: 'text/csv' })] });
    input.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    root.querySelector('form')!.dispatchEvent(new Event('submit', { cancelable: true }));
    fixture.detectChanges();
    return http.expectOne('/api/analises');
  }

  it('impede envio sem arquivo e durante carregamento, enviando multipart', () => {
    expect((root.querySelector('button[type=submit]') as HTMLButtonElement).disabled).toBe(true);
    const req = enviar();
    expect(req.request.method).toBe('POST');
    expect(req.request.body.get('arquivo').name).toBe('leituras.csv');
    expect((root.querySelector('button[type=submit]') as HTMLButtonElement).disabled).toBe(true);
    req.flush(resposta);
  });

  it('mostra resultado e filtra episódios localmente sem alterar ranking', () => {
    enviar().flush(resposta);
    fixture.detectChanges();
    expect(root.textContent).toContain('Data inválida');
    expect(root.querySelectorAll('[data-testid=episodios] tbody tr')).toHaveLength(2);
    const filtro = root.querySelector('select')!;
    filtro.value = 'GRAVISSIMA';
    filtro.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(root.querySelectorAll('[data-testid=episodios] tbody tr')).toHaveLength(1);
    expect(root.querySelector('[data-testid=episodios]')!.textContent).toContain('100');
    expect(root.querySelector('[data-testid=ranking]')!.textContent).toContain('ABC');
    http.expectNone('/api/analises');
  });

  it('mostra mensagem do backend e permite nova tentativa', () => {
    enviar().flush({ mensagem: 'Cabeçalho inválido' }, { status: 400, statusText: 'Bad Request' });
    fixture.detectChanges();
    expect(root.querySelector('[role=alert]')!.textContent).toContain('Cabeçalho inválido');
    expect((root.querySelector('button[type=submit]') as HTMLButtonElement).disabled).toBe(false);
    enviar().flush(resposta);
    fixture.detectChanges();
    expect(root.querySelector('[role=alert]')).toBeNull();
    expect(root.querySelector('[data-testid=ranking]')).not.toBeNull();
  });

  it('limpa resultado anterior e explica falha de rede', () => {
    enviar().flush(resposta);
    fixture.detectChanges();
    const req = enviar();
    expect(root.querySelector('[data-testid=ranking]')).toBeNull();
    req.error(new ProgressEvent('error'));
    fixture.detectChanges();
    expect(root.querySelector('[role=alert]')!.textContent).toContain('backend');
    expect((root.querySelector('button[type=submit]') as HTMLButtonElement).disabled).toBe(false);
  });
});
