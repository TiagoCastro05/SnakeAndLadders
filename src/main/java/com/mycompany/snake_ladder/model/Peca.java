package com.mycompany.snake_ladder.model;

/**
 * Representa uma peça no jogo Cobras e Escadas.
 * Cada peça pertence a um jogador e possui uma posição no tabuleiro.
 * 
 * @author Snake_Ladder Team
 * @version 1.0
 */
public class Peca {
    /**
     * Contador estático para geração automática de IDs únicos.
     */
    private static int contador = 1;
    
    /**
     * Identificador único da peça.
     */
    private final int idPeca;
    
    /**
     * ID da casa onde a peça está localizada no tabuleiro.
     */
    private int idCasa;
    
    /**
     * ID do jogador proprietário desta peça.
     */
    private final int idJogador;

    /**
     * Construtor que cria uma nova peça com posição específica.
     * 
     * @param idJogador ID do jogador proprietário
     * @param idCasa ID da casa inicial da peça
     */
    public Peca(int idJogador, int idCasa) {
        this.idPeca = contador++;
        this.idJogador = idJogador;
        this.idCasa = idCasa;
    }

    /**
     * Construtor que cria uma nova peça na posição inicial (casa 1).
     * 
     * @param idJogador ID do jogador proprietário
     */
    public Peca(int idJogador) {
        this.idPeca = contador++;
        this.idJogador = idJogador;
        this.idCasa = 1; // Começa sempre na casa 1
    }

    /**
     * Obtém o identificador único da peça.
     * 
     * @return ID da peça
     */
    public int getIdPeca() {
        return idPeca;
    }

    /**
     * Obtém o ID da casa onde a peça está localizada.
     * 
     * @return ID da casa atual
     */
    public int getIdCasa() {
        return idCasa;
    }

    /**
     * Define a nova posição da peça no tabuleiro.
     * 
     * @param idCasa ID da nova casa
     */
    public void setIdCasa(int idCasa) {
        this.idCasa = idCasa;
    }

    /**
     * Obtém o ID do jogador proprietário desta peça.
     * 
     * @return ID do jogador
     */
    public int getIdJogador() {
        return idJogador;
    }
}