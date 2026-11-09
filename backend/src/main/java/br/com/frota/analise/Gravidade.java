package br.com.frota.analise;

public enum Gravidade {
    LEVE(1), GRAVE(3), GRAVISSIMA(5);
    private final int pontos;
    Gravidade(int pontos) { this.pontos = pontos; }
    public int pontos() { return pontos; }
}
