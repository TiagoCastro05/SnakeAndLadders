package com.mycompany.snake_ladder.model;

import java.util.*;

/**
 * Classe principal que controla a lógica do jogo Cobras e Escadas.
 * 
 * Gerencia o estado do jogo, incluindo jogadores, suas peças, turnos,
 * rolagem de dados e condições de vitória.
 * 
 * @author Tiago
 * @version 1.0
 * @since 2025
 */
public class Jogo {
    /** Lista de jogadores participantes */
    private List<Jogador> jogadores;
    
    /** Tabuleiro do jogo com cobras e escadas */
    private Tabuleiro tabuleiro = new Tabuleiro();
    
    /** Dado usado para movimentação */
    private final Dado dado = new Dado();
    
    /** Mapa de peças por ID do jogador */
    private Map<Integer, Peca> pecas;
    
    /** Contador de vitórias por jogador */
    private Map<Integer, Integer> vitorias;
    
    /** Índice do jogador atual */
    private int jogadorAtual = 0;
    
    /** Flag indicando se o jogo terminou */
    private boolean jogoFinalizado = false;

    /**
     * Construtor padrão que cria um jogo com 2 jogadores pré-definidos.
     */
    public Jogo() {
        jogadores = new ArrayList<>();
        jogadores.add(new Jogador("Jogador 1"));
        jogadores.add(new Jogador("Jogador 2"));
        // Para 3 ou 4 jogadores, adicione mais:
        // jogadores.add(new Jogador("Jogador 3"));
        // jogadores.add(new Jogador("Jogador 4"));
        pecas = new HashMap<>();
        vitorias = new HashMap<>();
        for (Jogador j : jogadores) {
            pecas.put(j.getIdJogador(), new Peca(j.getIdJogador()));
            vitorias.put(j.getIdJogador(), 0); // Inicializa contador de vitórias
        }
    }

    /**
     * Construtor que cria um jogo com jogadores personalizados.
     * 
     * @param nomesJogadores Lista com os nomes dos jogadores
     */
    public Jogo(List<String> nomesJogadores) {
        this.jogadores = new ArrayList<>();
        this.pecas = new HashMap<>();
        this.vitorias = new HashMap<>();
        for (int i = 0; i < nomesJogadores.size(); i++) {
            Jogador jogador = new Jogador(i, nomesJogadores.get(i));
            jogadores.add(jogador);
            pecas.put(i, new Peca(i, 1)); // começa na casa 1
            vitorias.put(i, 0); // Inicializa contador de vitórias
        }
        this.jogadorAtual = 0;
        this.jogoFinalizado = false;
    }

    public Jogo(List<String> nomesJogadores, Tabuleiro tabuleiro) {
        this.tabuleiro = tabuleiro;
        this.jogadores = new ArrayList<>();
        this.pecas = new HashMap<>();
        this.vitorias = new HashMap<>();
        for (int i = 0; i < nomesJogadores.size(); i++) {
            Jogador jogador = new Jogador(i, nomesJogadores.get(i));
            jogadores.add(jogador);
            pecas.put(i, new Peca(i, 1)); // começa na casa 1
            vitorias.put(i, 0); // Inicializa contador de vitórias
        }
        this.jogadorAtual = 0;
        this.jogoFinalizado = false;
    }

    public Jogo(Tabuleiro tabuleiro) {
        this.tabuleiro = tabuleiro;
        jogadores = new ArrayList<>();
        jogadores.add(new Jogador("Jogador 1"));
        jogadores.add(new Jogador("Jogador 2"));
        // Para 3 ou 4 jogadores, adicione mais:
        // jogadores.add(new Jogador("Jogador 3"));
        // jogadores.add(new Jogador("Jogador 4"));
        pecas = new HashMap<>();
        for (Jogador j : jogadores) {
            pecas.put(j.getIdJogador(), new Peca(j.getIdJogador()));
        }
    }

    /**
     * Obtém a lista de nomes dos jogadores.
     * 
     * @return Lista com os nomes de todos os jogadores
     */
    public List<String> getNomesJogadores() {
        List<String> nomes = new ArrayList<>();
        for (Jogador j : jogadores) nomes.add(j.getNome());
        return nomes;
    }

    /**
     * Obtém o índice do jogador atual.
     * 
     * @return Índice do jogador atual (0-based)
     */
    public int getJogadorAtualIndex() {
        return jogadorAtual;
    }

    /**
     * Obtém o nome do jogador atual.
     * 
     * @return Nome do jogador atual
     */
    public String getNomeJogadorAtual() {
        return jogadores.get(jogadorAtual).getNome();
    }

    /**
     * Rola o dado e retorna o valor obtido.
     * 
     * @return Valor do dado (1-6)
     */
    public int rolarDado() {
        return dado.rolarDados();
    }

    /**
     * Obtém a posição atual de um jogador.
     * 
     * @param idx Índice do jogador
     * @return Posição da peça do jogador no tabuleiro
     */
    public int getPosicaoJogador(int idx) {
        return pecas.get(jogadores.get(idx).getIdJogador()).getIdCasa();
    }

    /**
     * Obtém o número total de jogadores.
     * 
     * @return Número de jogadores na partida
     */
    public int getNumeroJogadores() {
        return jogadores.size();
    }

    public String moverJogadorAtual(int valorDado) {
    Jogador jogador = jogadores.get(jogadorAtual);
    int origem = pecas.get(jogador.getIdJogador()).getIdCasa();
    int destino = origem + valorDado;
    StringBuilder status = new StringBuilder();

    status.append(jogador.getNome())
          .append(" rolou um ").append(valorDado).append(".");

    // Verificar regra do número exato para ganhar
    if (destino > 100) {
        status.append("\n❌ ").append(jogador.getNome())
              .append(" precisa de exatamente ").append(100 - origem)
              .append(" para ganhar! Fica na casa ").append(origem).append(".");
        // Jogador fica na mesma posição
        return status.toString();
    } else if (destino == 100) {
        // Jogador ganhou!
        pecas.get(jogador.getIdJogador()).setIdCasa(destino);
        // Incrementa as vitórias usando o método apropriado
        adicionarVitoria(jogador.getIdJogador());
        jogoFinalizado = true; // Marcar jogo como finalizado
        status.append("\n🏆 ").append(jogador.getNome()).append(" venceu o jogo!");
        return status.toString();
    } else {
        // Movimento normal
        pecas.get(jogador.getIdJogador()).setIdCasa(destino);
    }

    if (tabuleiro.eCabecaDeCobra(destino)) {
        int cauda = tabuleiro.getCaudaCobra(destino);
        status.append("\n⚠️ ").append(jogador.getNome())
              .append(" caiu numa COBRA! Desce até a casa ").append(cauda).append(".");
        pecas.get(jogador.getIdJogador()).setIdCasa(cauda);
    } else if (tabuleiro.eBaseDeEscada(destino)) {
        int topo = tabuleiro.getTopoEscada(destino);
        status.append("\n🪜 ").append(jogador.getNome())
              .append(" caiu numa escada! Sobe até a casa ").append(topo).append(".");
        pecas.get(jogador.getIdJogador()).setIdCasa(topo);
    }

    if (valorDado == 6) {
        status.append("\n🎲 ").append(jogador.getNome()).append(" rolou um 6 e pode jogar outra vez!");
    }

    return status.toString();
}    
    /**
     * Verifica se uma casa é cabeça de cobra.
     * 
     * @param casa Número da casa a verificar
     * @return true se a casa for cabeça de cobra, false caso contrário
     */
    public boolean eCabecaDeCobra(int casa) { return tabuleiro.eCabecaDeCobra(casa); }
    
    /**
     * Verifica se uma casa é base de escada.
     * 
     * @param casa Número da casa a verificar
     * @return true se a casa for base de escada, false caso contrário
     */
    public boolean eBaseDeEscada(int casa) { return tabuleiro.eBaseDeEscada(casa); }

    /**
     * Obtém o nome do jogador atual.
     * 
     * @return Nome do jogador atual
     */
    public String getJogadorAtual() {
        return jogadores.get(jogadorAtual).getNome();
    }
    
    /**
     * Verifica se o jogo foi finalizado.
     * 
     * @return true se o jogo foi finalizado, false caso contrário
     */
    public boolean isJogoFinalizado() {
        return jogoFinalizado;
    }
    
    /**
     * Reinicia o jogo, resetando as posições das peças para a casa 1.
     * As vitórias dos jogadores são mantidas entre jogos.
     */
    public void reiniciarJogo() {
        jogoFinalizado = false;
        jogadorAtual = 0;
        for (Peca peca : pecas.values()) {
         peca.setIdCasa(1); // Reseta todas as peças para a casa 1
        }
        // Nota: NÃO resetamos as vitórias - elas persistem entre jogos
    }
    
    /**
     * Obtém o mapa de vitórias de todos os jogadores.
     * 
     * @return Mapa onde a chave é o ID do jogador e o valor é o número de vitórias
     */
    public Map<Integer, Integer> getVitorias() {
        return vitorias;
    }
    
    /**
     * Obtém o número de vitórias de um jogador específico.
     * 
     * @param idJogador ID do jogador
     * @return Número de vitórias do jogador
     */
    public int getVitoriasJogador(int idJogador) {
        return vitorias.getOrDefault(idJogador, 0);
    }
    
    /**
     * Adiciona uma vitória para um jogador específico.
     * 
     * @param idJogador ID do jogador que venceu
     */
    public void adicionarVitoria(int idJogador) {
        vitorias.put(idJogador, vitorias.getOrDefault(idJogador, 0) + 1);
    }
    
    /**
     * Define um novo tabuleiro para o jogo.
     * Usado quando o jogo é reiniciado com um novo layout de cobras e escadas.
     * 
     * @param novoTabuleiro O novo tabuleiro a ser usado
     */
    public void setTabuleiro(Tabuleiro novoTabuleiro) {
        this.tabuleiro = novoTabuleiro;
    }
    
    /**
     * Obtém a lista de jogadores.
     * 
     * @return Lista de jogadores
     */
    public List<Jogador> getJogadores() {
        return jogadores;
    }
    
    /**
     * Obtém o tabuleiro do jogo.
     * 
     * @return Tabuleiro do jogo
     */
    public Tabuleiro getTabuleiro() {
        return tabuleiro;
    }
    
    /**
     * Obtém o dado do jogo.
     * 
     * @return Dado utilizado no jogo
     */
    public Dado getDado() {
        return dado;
    }
    
    /**
     * Obtém o mapa de peças dos jogadores.
     * 
     * @return Mapa onde a chave é o ID do jogador e o valor é sua peça
     */
    public Map<Integer, Peca> getPecas() {
        return pecas;
    }
    
    /**
     * Define o jogador atual da partida.
     * 
     * @param jogadorAtual Índice do jogador atual
     */
    public void setJogadorAtual(int jogadorAtual) {
        this.jogadorAtual = jogadorAtual;
    }
    
    /**
     * Define se o jogo foi finalizado.
     * 
     * @param jogoFinalizado true se o jogo foi finalizado, false caso contrário
     */
    public void setJogoFinalizado(boolean jogoFinalizado) {
        this.jogoFinalizado = jogoFinalizado;
    }

    /**
     * Obtém a peça de um jogador específico.
     * 
     * @param idxJogador Índice do jogador
     * @return Peça do jogador
     */
    public Peca getPeca(int idxJogador) {
        return pecas.get(jogadores.get(idxJogador).getIdJogador());
    }

    /**
     * Passa a vez para o próximo jogador.
     */
    public void passarVez() {
        jogadorAtual = (jogadorAtual + 1) % jogadores.size();
    }

    /**
     * Verifica se o jogador atual venceu o jogo.
     * 
     * @return true se o jogador chegou exatamente na casa 100, false caso contrário
     */
    public boolean jogadorVenceu() {
        Jogador jogador = jogadores.get(jogadorAtual);
        return pecas.get(jogador.getIdJogador()).getIdCasa() == 100;
    }

    /**
     * Método estático para criar um jogo a partir de dados recebidos pela rede.
     * 
     * @param vez String com nomes dos jogadores separados por vírgula
     * @param cobrasRecebidas Mapa de cobras recebido
     * @param escadasRecebidas Mapa de escadas recebido
     * @return Nova instância do jogo configurada
     */
    public static Jogo criarJogo(String vez, Map<Integer, Integer> cobrasRecebidas, Map<Integer, Integer> escadasRecebidas) {
        Tabuleiro tabuleiro = new Tabuleiro(cobrasRecebidas, escadasRecebidas);
        String[] nomesJogadores = vez.split(",");
        return new Jogo(Arrays.asList(nomesJogadores), tabuleiro);
    }
}
