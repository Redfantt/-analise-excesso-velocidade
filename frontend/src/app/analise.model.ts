export type Gravidade = 'LEVE' | 'GRAVE' | 'GRAVISSIMA';
export interface Episodio {
  placa: string; inicio: string; fim: string; duracaoSegundos: number;
  velocidadeMaximaKmh: number; excessoMaximoPercentual: number; gravidade: Gravidade;
}
export interface ItemRanking { placa: string; totalPontos: number; velocidadeMaximaKmh: number; totalEpisodios: number; }
export interface AnaliseResponse {
  resumo: { linhasLidas: number; linhasValidas: number; totalEpisodios: number; };
  episodios: Episodio[];
  ranking: ItemRanking[];
  linhasInvalidas: { linha: number; motivo: string; }[];
}
