package com.mycompany.snake_ladder.model;

/**
 * Representa um jogador no jogo Cobras e Escadas.
 * Cada jogador possui um identificador único e um nome.
 * 
 * @author Snake_Ladder Team
 * @version 1.0
 */
public class Jogador {
    /**
     * Contador estático para geração automática de IDs únicos.
     */
    private static int contador = 1;
    
    /**
     * Identificador único do jogador.
     */
    private final int idJogador;
    
    /**
     * Nome do jogador.
     */
    private final String nome;

    /**
     * Construtor que cria um novo jogador com ID gerado automaticamente.
     * 
     * @param nome Nome do jogador
     */
    public Jogador(String nome) {
        this.idJogador = contador++;
        this.nome = nome;
    }

    /**
     * Construtor que cria um jogador com ID específico.
     * Usado principalmente para reconstruir objetos recebidos via rede.
     * 
     * @param id ID específico do jogador
     * @param nome Nome do jogador
     */
    public Jogador(int id, String nome) {
        this.idJogador = id;
        this.nome = nome;
    }

    /**
     * Obtém o identificador único do jogador.
     * 
     * @return ID do jogador
     */
    public int getIdJogador() {
        return idJogador;
    }

    /**
     * Obtém o nome do jogador.
     * 
     * @return Nome do jogador
     */
    public String getNome() {
        return nome;
    }
}
