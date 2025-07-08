package com.mycompany.snake_ladder.model;

import java.util.Random;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;

/**
 * Representa um dado no jogo Cobras e Escadas.
 * Responsável por gerar números aleatórios e animar o lançamento do dado.
 * 
 * @author Snake_Ladder Team
 * @version 1.0
 */
public class Dado {
    /**
     * Gerador de números aleatórios para simular o lançamento do dado.
     */
    private final Random random = new Random();
    
    /**
     * Componente visual do dado na interface JavaFX.
     */
    @FXML private ImageView dadoImage;

    /**
     * Simula o lançamento do dado e retorna um valor aleatório entre 1 e 6.
     * 
     * @return Valor do dado (1-6)
     */
    public int rolarDados() {
        return random.nextInt(6) + 1;
    }
}
