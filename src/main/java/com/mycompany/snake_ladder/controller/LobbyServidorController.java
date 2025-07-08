package com.mycompany.snake_ladder.controller;

import com.mycompany.snake_ladder.model.Jogo;
import com.mycompany.snake_ladder.model.Tabuleiro;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador para o lobby do servidor no jogo Cobras e Escadas.
 * Gerencia as conexões dos clientes, a criação do jogo e a comunicação em rede.
 * 
 * @author Snake_Ladder Team
 * @version 1.0
 */
public class LobbyServidorController {
    /**
     * Campo de entrada para o IP do servidor.
     */
    @FXML private TextField campoIP;
    
    /**
     * Campo de entrada para a porta do servidor.
     */
    @FXML private TextField campoPorta;
    
    /**
     * Lista visual dos jogadores conectados.
     */
    @FXML private ListView<String> listaJogadores;
    
    /**
     * Botão para iniciar o jogo.
     */
    @FXML private Button botaoIniciar;

    /**
     * Lista de leitores de entrada dos clientes conectados.
     */
    private final List<BufferedReader> entradas = new ArrayList<>();
    
    /**
     * Lista de escritores de saída para os clientes conectados.
     */
    private final List<PrintWriter> saidas = new ArrayList<>();
    
    /**
     * Lista de sockets dos clientes conectados.
     */
    private final List<Socket> clientes = new ArrayList<>();
    
    /**
     * Lista dos nomes dos jogadores conectados.
     */
    private final List<String> nomesJogadores = new ArrayList<>();
    
    /**
     * Socket do servidor.
     */
    private ServerSocket serverSocket;
    
    /**
     * Instância do jogo.
     */
    private Jogo jogo;
    
    /**
     * Flag para indicar se o jogo foi finalizado.
     */
    private volatile boolean jogoFinalizado = false;
    
    /**
     * Contador de votos para reiniciar o jogo.
     */
    private int votosReinicio = 0;
    
    /**
     * Contador de votos negativos para reiniciar o jogo.
     */
    private int votosNegativos = 0;

    /**
     * Inicializa o controlador do servidor.
     * Cria o servidor socket e aguarda conexões dos clientes.
     */
    @FXML
    public void initialize() {
        botaoIniciar.setDisable(true);

        new Thread(() -> {
            try {
                int porta = Integer.parseInt(campoPorta.getText());
                serverSocket = new ServerSocket(porta);
                System.out.println("Servidor aguardando conexões...");

                while (true) {
                    Socket cliente = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                    PrintWriter out = new PrintWriter(cliente.getOutputStream(), true);
                    String nome = in.readLine(); // Cliente envia o nome logo após conectar
                    nomesJogadores.add(nome);
                    entradas.add(in);
                    saidas.add(out);
                    clientes.add(cliente);
                    System.out.println("Cliente conectado! Nome: " + nome);

                    javafx.application.Platform.runLater(() -> {
                        listaJogadores.getItems().add(nome);
                        botaoIniciar.setDisable(listaJogadores.getItems().size() < 2);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Inicia o jogo após todos os jogadores se conectarem.
     * Cria o tabuleiro, configura o jogo e inicia as threads de comunicação.
     */
    @FXML
    private void onIniciarJogo() {
        System.out.println("Botão Iniciar Jogo pressionado!");
        // Desabilitar o botão para evitar múltiplos cliques
        botaoIniciar.setDisable(true);

        // Tabuleiro aleatório para todos
        Tabuleiro tabuleiro = new Tabuleiro();
        Map<Integer, Integer> cobras = tabuleiro.getCobras();
        Map<Integer, Integer> escadas = tabuleiro.getEscadas();
        jogo = new Jogo(nomesJogadores, tabuleiro);

        // Primeiro, enviar sinal START para todos os clientes migrarem para o tabuleiro
        for (PrintWriter out : saidas) {
            out.println("START");
        }

        // Aguardar um pouco para os clientes processarem a mudança de tela
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Aguarda 1 segundo

                // Enviar mapas para todos os clientes
                for (PrintWriter out : saidas) {
                    out.println("COBRAS:" + cobras.entrySet().stream()
                        .map(e -> e.getKey() + "-" + e.getValue()).collect(Collectors.joining(",")));
                    out.println("ESCADAS:" + escadas.entrySet().stream()
                        .map(e -> e.getKey() + "-" + e.getValue()).collect(Collectors.joining(",")));
                }

                // Enviar estado inicial para todos
                broadcastEstado(0, jogo.getNomeJogadorAtual() + " começa!", false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // Thread para ouvir comandos dos clientes
        new Thread(() -> {
            try {
                while (true) {
                    for (int i = 0; i < clientes.size(); i++) {
                        BufferedReader in = entradas.get(i);
                        if (in.ready()) {
                            String comando = in.readLine();
                            if ("ROLAR_DADO".equals(comando) && jogo.getJogadorAtualIndex() == i && !jogoFinalizado) {
                                int valor = jogo.rolarDado();
                                String status = jogo.moverJogadorAtual(valor);
                                boolean venceu = jogo.jogadorVenceu();
                                if (venceu) {
                                    jogoFinalizado = true;
                                    broadcastEstado(valor, status, true);
                                } else {
                                    if (valor != 6) jogo.passarVez();
                                    broadcastEstado(valor, status, false);
                                }
                            } else if ("REINICIAR_SIM".equals(comando)) {
                                votosReinicio++;
                                if (votosReinicio == nomesJogadores.size()) {
                                    reiniciarJogo();
                                    votosReinicio = 0;
                                    votosNegativos = 0;
                                }
                            } else if ("REINICIAR_NAO".equals(comando)) {
                                votosNegativos++;
                                if (votosNegativos > 0) {
                                    votosReinicio = 0;
                                    votosNegativos = 0;
                                }
                            }
                        }
                    }
                    Thread.sleep(50);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Envia o estado atual do jogo para todos os clientes conectados.
     * Inclui informações sobre turnos, posições das peças, vitórias e status do jogo.
     * 
     * @param dado Valor do dado que foi lançado
     * @param status Mensagem de status do jogo
     * @param fim Indica se o jogo chegou ao fim
     */
    private void broadcastEstado(int dado, String status, boolean fim) {
        for (PrintWriter out : saidas) {
            out.println("ESTADO");
            out.println("vez:" + String.join(",", jogo.getNomesJogadores()));
            out.println("indiceVez:" + jogo.getJogadorAtualIndex());
            StringBuilder posicoes = new StringBuilder("posicoes:");
            for (int i = 0; i < jogo.getNumeroJogadores(); i++) {
                posicoes.append(jogo.getPosicaoJogador(i));
                if (i < jogo.getNumeroJogadores() - 1) posicoes.append(",");
            }
            out.println(posicoes.toString());
            
            // Adicionar informações de vitórias
            StringBuilder vitorias = new StringBuilder("vitorias:");
            for (int i = 0; i < jogo.getNumeroJogadores(); i++) {
                int vitoriasJogador = jogo.getVitoriasJogador(i);
                vitorias.append(vitoriasJogador);
                if (i < jogo.getNumeroJogadores() - 1) vitorias.append(",");
            }
            System.out.println("=== SERVIDOR ENVIANDO VITÓRIAS ===");
            System.out.println("String de vitórias: " + vitorias.toString());
            for (int i = 0; i < jogo.getNumeroJogadores(); i++) {
                System.out.println("Jogador " + i + " (" + jogo.getNomesJogadores().get(i) + ") tem " + jogo.getVitoriasJogador(i) + " vitórias");
            }
            System.out.println("==================================");
            out.println(vitorias.toString());
            
            out.println("dado:" + dado);
            out.println("status:" + status);
            out.println("fim:" + (fim ? "1" : "0"));
            out.flush(); // Garantir que a mensagem seja enviada imediatamente
        }
    }

    /**
     * Reinicia o jogo com um novo tabuleiro aleatório.
     * Reseta todas as posições das peças, cria novas cobras e escadas,
     * e envia o novo estado para todos os clientes.
     * As vitórias dos jogadores são preservadas.
     */
    private void reiniciarJogo() {
        // Resetar votos
        votosReinicio = 0;
        votosNegativos = 0;
        
        // Novo tabuleiro aleatório
        Tabuleiro novoTabuleiro = new Tabuleiro();
        Map<Integer, Integer> cobras = novoTabuleiro.getCobras();
        Map<Integer, Integer> escadas = novoTabuleiro.getEscadas();
        
        // Atualizar apenas o tabuleiro do jogo existente, preservando as vitórias
        jogo.setTabuleiro(novoTabuleiro);
        jogo.reiniciarJogo(); // Resetar posições das peças para casa 1
        jogoFinalizado = false;

        // Enviar novos mapas para todos os clientes
        for (PrintWriter out : saidas) {
            out.println("COBRAS:" + cobras.entrySet().stream()
                .map(e -> e.getKey() + "-" + e.getValue()).collect(Collectors.joining(",")));
            out.println("ESCADAS:" + escadas.entrySet().stream()
                .map(e -> e.getKey() + "-" + e.getValue()).collect(Collectors.joining(",")));
        }

        // Enviar estado inicial
        broadcastEstado(0, jogo.getNomeJogadorAtual() + " começa! Novo jogo iniciado!", false);
    }

    // Removido: gerarCobrasFixas e gerarEscadasFixas (não são mais necessários)
}
