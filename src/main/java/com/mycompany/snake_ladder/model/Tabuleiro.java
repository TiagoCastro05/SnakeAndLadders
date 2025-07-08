package com.mycompany.snake_ladder.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

/**
 * Representa o tabuleiro do jogo Cobras e Escadas.
 * 
 * O tabuleiro é composto por 100 casas numeradas de 1 a 100,
 * com cobras e escadas distribuídas aleatoriamente ou definidas
 * manualmente através dos construtores.
 * 
 * @author Tiago
 * @version 1.0
 * @since 2025
 */
public class Tabuleiro {
    /** Lista das 100 casas do tabuleiro */
    private final List<Casas> casas = new ArrayList<>();
    
    /** Mapa das cobras (cabeça -> cauda) */
    private final Map<Integer, Integer> cobras = new HashMap<>();
    
    /** Mapa das escadas (base -> topo) */
    private final Map<Integer, Integer> escadas = new HashMap<>();

    /**
     * Construtor que cria um tabuleiro com cobras e escadas pré-definidas.
     * 
     * @param cobras Mapa com posições das cobras (cabeça -> cauda)
     * @param escadas Mapa com posições das escadas (base -> topo)
     */
    public Tabuleiro(Map<Integer, Integer> cobras, Map<Integer, Integer> escadas) {
        // Inicializa as 100 casas
        for (int i = 1; i <= 100; i++) {
            casas.add(new Casas(i));
        }
        this.cobras.putAll(cobras);
        this.escadas.putAll(escadas);

        // Marca destinos nas casas de cobra e escada
        for (Map.Entry<Integer, Integer> entry : cobras.entrySet()) {
            getCasa(entry.getKey()).setDestino(entry.getValue());
        }
        for (Map.Entry<Integer, Integer> entry : escadas.entrySet()) {
            getCasa(entry.getKey()).setDestino(entry.getValue());
        }
    }

    /**
     * Construtor padrão que cria um tabuleiro com cobras e escadas aleatórias.
     * Gera 6 cobras e 6 escadas em posições aleatórias válidas.
     */
    public Tabuleiro() {
        // Inicializa as 100 casas
        for (int i = 1; i <= 100; i++) {
            casas.add(new Casas(i));
        }

        Random rand = new Random();
        int numCobras = 6;
        int numEscadas = 6;
        Set<Integer> ocupadas = new HashSet<>();

        // Gerar cobras (sem restrição de cor)
        for (int i = 0; i < numCobras; i++) {
            int cabeca, cauda;
            int tentativas = 0;
            while (true) {
                cabeca = 15 + rand.nextInt(81); // 15 a 95 (evita casas muito próximas do início/fim)
                cauda = cabeca - (5 + rand.nextInt(Math.min(15, cabeca - 10)));
                if (
                    cabeca <= 10 || cabeca >= 96 ||
                    cauda <= 5 || cauda >= 91 ||
                    cauda >= cabeca ||
                    ocupadas.contains(cabeca) ||
                    ocupadas.contains(cauda)
                ) { 
                    tentativas++; 
                    if (tentativas > 200) break; 
                    continue; 
                }
                adicionarCobra(cabeca, cauda);
                ocupadas.add(cabeca);
                ocupadas.add(cauda);
                break;
            }
        }

        // Gerar escadas (sem restrição de cor)
        for (int i = 0; i < numEscadas; i++) {
            int base, topo;
            int tentativas = 0;
            while (true) {
                base = 5 + rand.nextInt(81); // 5 a 85 (deixa espaço para subir)
                topo = base + (5 + rand.nextInt(Math.min(15, 95 - base)));
                if (
                    base <= 5 || base >= 86 ||
                    topo <= 10 || topo >= 96 ||
                    topo <= base ||
                    ocupadas.contains(base) ||
                    ocupadas.contains(topo)
                ) { 
                    tentativas++; 
                    if (tentativas > 200) break; 
                    continue; 
                }
                adicionarEscada(base, topo);
                ocupadas.add(base);
                ocupadas.add(topo);
                break;
            }
        }
    }

    /**
     * Adiciona uma cobra ao tabuleiro.
     * 
     * @param inicio Casa da cabeça da cobra
     * @param destino Casa da cauda da cobra
     */
    private void adicionarCobra(int inicio, int destino) {
        cobras.put(inicio, destino);
        getCasa(inicio).setDestino(destino);
    }

    /**
     * Adiciona uma escada ao tabuleiro.
     * 
     * @param inicio Casa da base da escada
     * @param destino Casa do topo da escada
     */
    private void adicionarEscada(int inicio, int destino) {
        escadas.put(inicio, destino);
        getCasa(inicio).setDestino(destino);
    }

    /**
     * Obtém uma casa específica do tabuleiro.
     * 
     * @param numero Número da casa (1-100)
     * @return Objeto Casas correspondente
     */
    public Casas getCasa(int numero) {
        return casas.get(numero - 1);
    }

    /**
     * Obtém todas as casas do tabuleiro.
     * 
     * @return Lista com todas as 100 casas
     */
    public List<Casas> getCasas() {
        return casas;
    }

    /**
     * Verifica se uma casa é cabeça de cobra.
     * 
     * @param casa Número da casa a verificar
     * @return true se a casa for cabeça de cobra, false caso contrário
     */
    public boolean eCabecaDeCobra(int casa) { return cobras.containsKey(casa); }
    
    /**
     * Verifica se uma casa é base de escada.
     * 
     * @param casa Número da casa a verificar
     * @return true se a casa for base de escada, false caso contrário
     */
    public boolean eBaseDeEscada(int casa) { return escadas.containsKey(casa); }

    /**
     * Obtém o mapa de cobras do tabuleiro.
     * 
     * @return Mapa onde a chave é a cabeça da cobra e o valor é a cauda
     */
    public Map<Integer, Integer> getCobras() {
        return cobras;
    }

    /**
     * Obtém o mapa de escadas do tabuleiro.
     * 
     * @return Mapa onde a chave é a base da escada e o valor é o topo
     */
    public Map<Integer, Integer> getEscadas() {
        return escadas;
    }

    /**
     * Obtém a casa da cauda de uma cobra.
     * 
     * @param cabeca Número da casa da cabeça da cobra
     * @return Número da casa da cauda, ou null se não for cabeça de cobra
     */
    public Integer getCaudaCobra(int cabeca) {
        return cobras.get(cabeca);
    }

    /**
     * Obtém a casa do topo de uma escada.
     * 
     * @param base Número da casa da base da escada
     * @return Número da casa do topo, ou null se não for base de escada
     */
    public Integer getTopoEscada(int base) {
        return escadas.get(base);
    }
}
