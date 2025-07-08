package com.mycompany.snake_ladder.model;

/**
 * Representa uma casa no tabuleiro do jogo Cobras e Escadas.
 * Cada casa pode ser uma casa normal, uma cobra ou uma escada.
 * 
 * @author Snake_Ladder Team
 * @version 1.0
 */
public class Casas {
    /**
     * Identificador único da casa no tabuleiro.
     */
    private final int idCasa;
    
    /**
     * Casa de destino quando o jogador cai nesta casa.
     * Se for uma casa normal, destino == idCasa.
     * Se for cobra/escada, destino != idCasa.
     */
    private int destino;

    /**
     * Construtor que cria uma casa normal.
     * 
     * @param idCasa ID da casa no tabuleiro
     */
    public Casas(int idCasa) {
        this.idCasa = idCasa;
        this.destino = idCasa; // Casa normal: destino é ela mesma
    }

    /**
     * Obtém o ID da casa.
     * 
     * @return ID da casa
     */
    public int getIdCasa() {
        return idCasa;
    }

    /**
     * Obtém o destino da casa.
     * 
     * @return ID da casa de destino
     */
    public int getDestino() {
        return destino;
    }

    /**
     * Define o destino da casa.
     * Usado para configurar cobras e escadas.
     * 
     * @param destino ID da casa de destino
     */
    public void setDestino(int destino) {
        this.destino = destino;
    }

    /**
     * Verifica se esta casa é uma cobra ou escada.
     * 
     * @return true se for cobra/escada, false se for casa normal
     */
    public boolean isCobraOuEscada() {
        return destino != idCasa;
    }
}
