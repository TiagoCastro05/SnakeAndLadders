package com.mycompany.snake_ladder.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controlador para o lobby do cliente no jogo Cobras e Escadas.
 * Gerencia a conexão com o servidor e a entrada do jogador na partida.
 * 
 * @author Snake_Ladder Team
 * @version 1.0
 */
public class LobbyClienteController {
    /**
     * Campo de entrada para o IP do servidor.
     */
    @FXML private TextField campoIP;
    
    /**
     * Campo de entrada para a porta do servidor.
     */
    @FXML private TextField campoPorta;
    
    /**
     * Campo de entrada para o nome do jogador.
     */
    @FXML private TextField campoNome;
    
    /**
     * Botão para começar o jogo.
     */
    @FXML private Button botaoComecar;

    /**
     * Socket de conexão com o servidor.
     */
    private java.net.Socket socket;
    
    /**
     * Leitor de entrada do socket.
     */
    private java.io.BufferedReader socketInput;
    
    /**
     * Escritor de saída do socket.
     */
    private java.io.PrintWriter socketOutput;

    /**
     * Inicia a conexão com o servidor e começa o jogo.
     * Conecta ao servidor, envia o nome do jogador e aguarda o início da partida.
     * Após receber o sinal START, navega para a tela do tabuleiro.
     * 
     * @throws IOException Se ocorrer erro na conexão com o servidor
     * @throws NumberFormatException Se a porta não for um número válido
     */
    @FXML
    public void onComecarJogo() {
        try {
            String ip = campoIP.getText().trim(); // Remove espaços no início e fim
            int porta = Integer.parseInt(campoPorta.getText().trim()); // Remove espaços da porta também
            String nome = campoNome.getText().trim(); // Remove espaços do nome

            // Estabelece conexão com o servidor
            socket = new java.net.Socket(ip, porta);
            socketInput = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
            socketOutput = new java.io.PrintWriter(socket.getOutputStream(), true);

            socketOutput.println(nome); // Envia nome ao servidor

            // Troca para a tela de espera
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/snake_ladder/espera.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) botaoComecar.getScene().getWindow();
                stage.setScene(new Scene(root));

                // Inicia thread para aguardar o sinal START do servidor
                new Thread(() -> {
                    try {
                        String msg = socketInput.readLine();
                        if ("START".equals(msg)) {
                            // Navega para a tela do tabuleiro quando o jogo iniciar
                            javafx.application.Platform.runLater(() -> {
                                try {
                                    FXMLLoader tabuleiroLoader = new FXMLLoader(getClass().getResource("/com/mycompany/snake_ladder/tabuleiro.fxml"));
                                    Parent tabuleiroRoot = tabuleiroLoader.load();
                                    TabuleiroController controller = tabuleiroLoader.getController();
                                    controller.setSocket(socket, socketInput, socketOutput, nome);
                                    stage.setScene(new Scene(tabuleiroRoot));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
