package com.mycompany.snake_ladder.controller;

import com.mycompany.snake_ladder.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.PathTransition;
import javafx.scene.shape.Path;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.LineTo;
import javafx.util.Duration;
import javafx.scene.layout.Pane;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

/**
 * Controlador principal do tabuleiro do jogo Cobras e Escadas.
 * 
 * Esta classe é responsável por:
 * - Gerenciar a interface gráfica do tabuleiro de jogo
 * - Coordenar as animações das peças e dados
 * - Comunicar com o servidor através de sockets
 * - Atualizar o estado visual do jogo
 * - Processar eventos de interface do usuário
 * 
 * @author Tiago
 * @version 1.0
 * @since 2025
 */
public class TabuleiroController {
    // Elementos da interface FXML
    /** GridPane que representa o tabuleiro do jogo */
    @FXML private GridPane gridTabuleiro;
    
    /** Label que mostra de quem é a vez atual */
    @FXML private Label labelVez;
    
    /** Painel horizontal que exibe informações dos jogadores */
    @FXML private HBox painelJogadores;
    
    /** Painel vertical que exibe o placar de vitórias */
    @FXML private VBox painelVitorias;
    
    /** Painel overlay para desenhar setas e animações */
    @FXML private Pane overlayPane;
    
    /** ImageView que exibe a face atual do dado */
    @FXML private ImageView dadoImage;
    
    /** Botão para rolar o dado */
    @FXML private Button botaoRolar;
    
    /** Label que mostra o resultado do dado */
    @FXML private Label labelDado;
    
    /** Área de texto que mostra o status do jogo */
    @FXML private TextArea areaStatus;

    // Variáveis de estado do jogo
    /** Instância do jogo atual */
    private Jogo jogo;
    
    /** Stream de entrada para comunicação com o servidor */
    private java.io.BufferedReader socketInput;
    
    /** Stream de saída para comunicação com o servidor */
    private java.io.PrintWriter socketOutput;
    
    /** Índice do jogador atual neste cliente (-1 se não definido) */
    private int meuIndice = -1;
    
    /** Flag que indica se uma animação está em execução */
    private boolean animando = false;
    
    /** Nome do jogador deste cliente */
    private String nomeJogador;

    /** Mapa das cobras recebidas do servidor (cabeça -> cauda) */
    private final Map<Integer, Integer> cobrasRecebidas = new HashMap<>();
    
    /** Mapa das escadas recebidas do servidor (base -> topo) */
    private final Map<Integer, Integer> escadasRecebidas = new HashMap<>();

    /**
     * Configura a conexão socket e inicia a comunicação com o servidor.
     * 
     * Este método estabelece a comunicação entre o cliente e o servidor,
     * enviando o nome do jogador e criando uma thread para escutar
     * mensagens do servidor em tempo real.
     * 
     * @param socket Socket de conexão com o servidor
     * @param in Stream de entrada para ler dados do servidor
     * @param out Stream de saída para enviar dados ao servidor
     * @param nome Nome do jogador deste cliente
     */

    public void setSocket(java.net.Socket socket, java.io.BufferedReader in, java.io.PrintWriter out, String nome) {
        this.socketInput = in;
        this.socketOutput = out;
        this.nomeJogador = nome;

        // Envia o nome do jogador assim que o socket é criado
        out.println(nome);

        new Thread(() -> {
            try {
                String linha;
                while ((linha = socketInput.readLine()) != null) {
                    if (linha.startsWith("COBRAS:")) {
                        cobrasRecebidas.clear();
                        String[] pares = linha.substring(7).split(",");
                        for (String par : pares) {
                            if (par.isEmpty()) continue;
                            String[] vals = par.split("-");
                            cobrasRecebidas.put(Integer.parseInt(vals[0]), Integer.parseInt(vals[1]));
                        }
                        continue;
                    }
                    if (linha.startsWith("ESCADAS:")) {
                        escadasRecebidas.clear();
                        String[] pares = linha.substring(8).split(",");
                        for (String par : pares) {
                            if (par.isEmpty()) continue;
                            String[] vals = par.split("-");
                            escadasRecebidas.put(Integer.parseInt(vals[0]), Integer.parseInt(vals[1]));
                        }
                        continue;
                    }
                    if (linha.equals("ESTADO")) {
                        String vez = socketInput.readLine().substring(4);
                        String indiceVezStr = socketInput.readLine().substring(10);
                        int indiceVez = Integer.parseInt(indiceVezStr);
                        String posicoes = socketInput.readLine().substring(9);
                        String vitorias = socketInput.readLine().substring(9);
                        String dado = socketInput.readLine().substring(5);

                        // Definir o índice do jogador atual se ainda não foi definido
                        if (meuIndice == -1) {
                            String[] nomesJogadores = vez.split(",");
                            for (int i = 0; i < nomesJogadores.length; i++) {
                                if (nomesJogadores[i].equals(nomeJogador)) {
                                    meuIndice = i;
                                    break;
                                }
                            }
                        }

                        // Ler status multi-linha até encontrar "fim:"
                        StringBuilder statusBuilder = new StringBuilder();
                        String statusLine = socketInput.readLine();
                        if (statusLine.startsWith("status:")) {
                            statusBuilder.append(statusLine.substring(7));
                        }
                        String fimStr = "";
                        while (true) {
                            socketInput.mark(1000);
                            String nextLine = socketInput.readLine();
                            if (nextLine.startsWith("fim:")) {
                                fimStr = nextLine.substring(4);
                                break;
                            }
                            statusBuilder.append("\n").append(nextLine);
                        }
                        String status = statusBuilder.toString();
                        boolean jogoAcabou = fimStr.equals("1");

                        javafx.application.Platform.runLater(() -> {
                            boolean isReinicio = status.contains("Novo jogo iniciado");
                            
                            if (jogo == null || isReinicio) {
                                Tabuleiro tabuleiro = new Tabuleiro(cobrasRecebidas, escadasRecebidas);
                                String[] nomesJogadores = vez.split(",");
                                jogo = new Jogo(Arrays.asList(nomesJogadores), tabuleiro);
                                if (isReinicio) {
                                    jogo.reiniciarJogo();
                                    animando = false;
                                }
                                atualizarPainelJogadores(indiceVez);
                                atualizarPainelVitorias();
                            }
                            processarEstado(vez, posicoes, vitorias, Integer.parseInt(dado), status, indiceVez, jogoAcabou);
                            desenharTabuleiro();
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Método de ação do botão rolar dado.
     * 
     * Envia um comando ao servidor para rolar o dado, mas apenas se:
     * - Nenhuma animação estiver em execução
     * - A conexão com o servidor estiver ativa
     */
    @FXML
    private void rolarDado() {
        if (!animando && socketOutput != null) {
            socketOutput.println("ROLAR_DADO");
            socketOutput.flush();
        }
    }

    /**
     * Desenha o tabuleiro completo do jogo.
     * 
     * Este método:
     * - Limpa o tabuleiro anterior
     * - Configura o grid 10x10 com casas numeradas de 1 a 100
     * - Posiciona as peças dos jogadores
     * - Marca visualmente as casas com cobras e escadas
     * - Agenda o desenho das setas após o layout estar pronto
     */
    private void desenharTabuleiro() {
        if (jogo == null) return;
        gridTabuleiro.getChildren().clear();
        gridTabuleiro.getColumnConstraints().clear();
        gridTabuleiro.getRowConstraints().clear();

        // Torna o tabuleiro responsivo e adaptável ao tamanho da tela
        gridTabuleiro.setPrefWidth(500);
        gridTabuleiro.setPrefHeight(500);
        gridTabuleiro.setMinWidth(400);
        gridTabuleiro.setMinHeight(400);
        gridTabuleiro.setMaxWidth(600);
        gridTabuleiro.setMaxHeight(600);

        // Configura colunas com largura proporcional
        for (int i = 0; i < 10; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(10);
            col.setHgrow(javafx.scene.layout.Priority.ALWAYS);
            col.setMinWidth(40);
            col.setPrefWidth(50);
            gridTabuleiro.getColumnConstraints().add(col);
        }
        
        // Configura linhas com altura proporcional
        for (int i = 0; i < 10; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(10);
            row.setVgrow(javafx.scene.layout.Priority.ALWAYS);
            row.setMinHeight(40);
            row.setPrefHeight(50);
            gridTabuleiro.getRowConstraints().add(row);
        }

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int numCasa = 100 - (i * 10 + ((i % 2 == 0) ? j : 9 - j));
                StackPane cell = new StackPane();
                Label casa = new Label(String.valueOf(numCasa));
                casa.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                casa.setAlignment(Pos.CENTER);
                casa.setStyle("-fx-border-color: #333; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #fff;");

                cell.getChildren().add(casa);

                // Desenhar peças
                for (int idx = 0; idx < jogo.getNumeroJogadores(); idx++) {
                    int pos = jogo.getPosicaoJogador(idx);
                    if (pos < 1 || pos > 100) continue;
                    if (pos == numCasa) {
                        String imagemPeao;
                        if (idx == 1) {
                            imagemPeao = "/imagens/peao_azul.png";
                        } else {
                            imagemPeao = "/imagens/peao.png";
                        }
                        java.net.URL peaoUrl = getClass().getResource(imagemPeao);
                        if (peaoUrl != null) {
                            ImageView pecaView = new ImageView(new Image(peaoUrl.toExternalForm()));
                            pecaView.setFitWidth(22);
                            pecaView.setFitHeight(22);
                            cell.getChildren().add(pecaView);
                        }
                    }
                }
                
                // Verificar se é cabeça de cobra (chave do Map)
                if (cobrasRecebidas.containsKey(numCasa)) {
                    casa.setStyle(casa.getStyle() + "-fx-background-color: #e74c3c; -fx-text-fill: white;");
                    casa.setText("🐍 " + numCasa);
                }
                // Verificar se é base de escada (chave do Map, não valor)
                else if (escadasRecebidas.containsKey(numCasa)) {
                    casa.setStyle(casa.getStyle() + "-fx-background-color: #27ae60; -fx-text-fill: white;");
                    casa.setText("🪜 " + numCasa);
                }
                
                gridTabuleiro.add(cell, j, i);
            }
        }
        // Desenhar setas após o layout estar pronto
        javafx.application.Platform.runLater(() -> {
            // Aguarda um pouco mais para garantir que o layout esteja completo
            Timeline esperarLayout = new Timeline(new KeyFrame(Duration.millis(100), e -> {
                desenharSetas();
            }));
            esperarLayout.play();
        });
    }

    /**
     * Desenha as setas visuais das cobras e escadas no overlay.
     * 
     * As cobras são representadas por setas vermelhas que apontam para baixo,
     * e as escadas por setas verdes que apontam para cima.
     */
    private void desenharSetas() {
        overlayPane.getChildren().clear();
        
        // Desenhar cobras (setas vermelhas)
        for (Map.Entry<Integer, Integer> entry : cobrasRecebidas.entrySet()) {
            int cabeca = entry.getKey();
            int cauda = entry.getValue();
            double[] start = getCasaCenter(cabeca);
            double[] end = getCasaCenter(cauda);
            
            // Linha principal da cobra
            javafx.scene.shape.Line corpo = new javafx.scene.shape.Line(start[0], start[1], end[0], end[1]);
            corpo.setStroke(javafx.scene.paint.Color.RED);
            corpo.setStrokeWidth(6);
            overlayPane.getChildren().add(corpo);
            
            // Calcular e desenhar a ponta da seta
            double dx = end[0] - start[0];
            double dy = end[1] - start[1];
            double norm = Math.sqrt(dx*dx + dy*dy);
            if (norm > 0) {
                double arrowLen = 20;
                double arrowAngle = Math.toRadians(30);
                double x1 = end[0] - arrowLen * (dx * Math.cos(arrowAngle) + dy * Math.sin(arrowAngle)) / norm;
                double y1 = end[1] - arrowLen * (dy * Math.cos(arrowAngle) - dx * Math.sin(arrowAngle)) / norm;
                double x2 = end[0] - arrowLen * (dx * Math.cos(-arrowAngle) + dy * Math.sin(-arrowAngle)) / norm;
                double y2 = end[1] - arrowLen * (dy * Math.cos(-arrowAngle) - dx * Math.sin(-arrowAngle)) / norm;
                
                javafx.scene.shape.Line seta1 = new javafx.scene.shape.Line(end[0], end[1], x1, y1);
                javafx.scene.shape.Line seta2 = new javafx.scene.shape.Line(end[0], end[1], x2, y2);
                seta1.setStroke(javafx.scene.paint.Color.RED);
                seta2.setStroke(javafx.scene.paint.Color.RED);
                seta1.setStrokeWidth(6);
                seta2.setStrokeWidth(6);
                overlayPane.getChildren().addAll(seta1, seta2);
            }
        }
        
        // Desenhar escadas (setas verdes)
        for (Map.Entry<Integer, Integer> entry : escadasRecebidas.entrySet()) {
            int base = entry.getKey();
            int topo = entry.getValue();
            double[] start = getCasaCenter(base);
            double[] end = getCasaCenter(topo);
            
            // Linha principal da escada
            javafx.scene.shape.Line corpo = new javafx.scene.shape.Line(start[0], start[1], end[0], end[1]);
            corpo.setStroke(javafx.scene.paint.Color.FORESTGREEN);
            corpo.setStrokeWidth(6);
            overlayPane.getChildren().add(corpo);
            
            // Calcular e desenhar a ponta da seta
            double dx = end[0] - start[0];
            double dy = end[1] - start[1];
            double norm = Math.sqrt(dx*dx + dy*dy);
            if (norm > 0) {
                double arrowLen = 20;
                double arrowAngle = Math.toRadians(30);
                double x1 = end[0] - arrowLen * (dx * Math.cos(arrowAngle) + dy * Math.sin(arrowAngle)) / norm;
                double y1 = end[1] - arrowLen * (dy * Math.cos(arrowAngle) - dx * Math.sin(arrowAngle)) / norm;
                double x2 = end[0] - arrowLen * (dx * Math.cos(-arrowAngle) + dy * Math.sin(-arrowAngle)) / norm;
                double y2 = end[1] - arrowLen * (dy * Math.cos(-arrowAngle) - dx * Math.sin(-arrowAngle)) / norm;
                
                javafx.scene.shape.Line seta1 = new javafx.scene.shape.Line(end[0], end[1], x1, y1);
                javafx.scene.shape.Line seta2 = new javafx.scene.shape.Line(end[0], end[1], x2, y2);
                seta1.setStroke(javafx.scene.paint.Color.FORESTGREEN);
                seta2.setStroke(javafx.scene.paint.Color.FORESTGREEN);
                seta1.setStrokeWidth(6);
                seta2.setStrokeWidth(6);
                overlayPane.getChildren().addAll(seta1, seta2);
            }
        }
    }

    /**
     * Calcula o centro de uma casa específica no gridTabuleiro.
     * 
     * Converte o número da casa (1-100) em coordenadas (x,y) do centro
     * da célula correspondente, considerando o padrão serpentina do tabuleiro
     * e convertendo para o sistema de coordenadas do overlayPane.
     * 
     * @param numCasa Número da casa (1-100)
     * @return Array com coordenadas [x, y] do centro da casa
     */
    private double[] getCasaCenter(int numCasa) {
        // Calcula a linha e coluna corretas para o número da casa
        int linha = 9 - (numCasa - 1) / 10;
        int coluna;
        
        // Corrige o cálculo da coluna baseado no padrão serpentina do tabuleiro
        int linhaOriginal = (numCasa - 1) / 10;
        if (linhaOriginal % 2 == 0) {
            // Linhas pares: 1-10, 21-30, 41-50, 61-70, 81-90 (da esquerda para direita)
            coluna = (numCasa - 1) % 10;
        } else {
            // Linhas ímpares: 11-20, 31-40, 51-60, 71-80, 91-100 (da direita para esquerda)
            coluna = 9 - ((numCasa - 1) % 10);
        }
        
        // Usar dimensões reais do grid diretamente
        double gridWidth = gridTabuleiro.getWidth();
        double gridHeight = gridTabuleiro.getHeight();
        if (gridWidth <= 0) gridWidth = 600; // Valor padrão
        if (gridHeight <= 0) gridHeight = 600; // Valor padrão
        
        double cellWidth = gridWidth / 10.0;
        double cellHeight = gridHeight / 10.0;
        double x = coluna * cellWidth + cellWidth / 2.0;
        double y = linha * cellHeight + cellHeight / 2.0;
        
        // Converter coordenadas do grid para coordenadas do overlayPane
        // Obter bounds do grid em relação ao overlayPane
        javafx.geometry.Bounds gridBoundsInOverlay = overlayPane.sceneToLocal(gridTabuleiro.localToScene(gridTabuleiro.getBoundsInLocal()));
        
        // Adicionar offset do grid em relação ao overlayPane
        x += gridBoundsInOverlay.getMinX();
        y += gridBoundsInOverlay.getMinY();
        
        return new double[]{x, y};
    }

    /**
     * Processa o estado do jogo recebido do servidor.
     * 
     * Este método analisa as mudanças de posição dos jogadores,
     * determina se houve movimento com cobra/escada, e coordena
     * as animações apropriadas.
     * 
     * @param vez String com nomes dos jogadores separados por vírgula
     * @param posicoes String com posições atuais separadas por vírgula
     * @param vitorias String com contadores de vitórias separados por vírgula
     * @param dado Valor do dado que foi rolado
     * @param status Mensagem de status do jogo
     * @param indiceVez Índice do jogador que deve jogar
     * @param jogoAcabou Flag indicando se o jogo terminou
     */
    private void processarEstado(String vez, String posicoes, String vitorias, int dado, String status, int indiceVez, boolean jogoAcabou) {
        String[] nomesJogadores = vez.split(",");
        if (indiceVez < nomesJogadores.length) {
            labelVez.setText("Vez de: " + nomesJogadores[indiceVez]);
        }
        
        if (jogo != null) {
            String[] vitoriasArray = vitorias.split(",");
            for (int i = 0; i < vitoriasArray.length && i < jogo.getNumeroJogadores(); i++) {
                int numVitorias = Integer.parseInt(vitoriasArray[i]);
                jogo.getVitorias().put(i, numVitorias);
            }
        }
        
        String[] pos = posicoes.split(",");
        int jogadorMovendoTmp = -1;
        int origemTmp = -1, destinoTmp = -1;
        Integer casaFinalTmp = null;

        for (int i = 0; i < pos.length; i++) {
            int novaPos = Integer.parseInt(pos[i]);
            int antigaPos = jogo.getPeca(i).getIdCasa();
            
            if (novaPos != antigaPos) {
                jogadorMovendoTmp = i;
                origemTmp = antigaPos;

                int posicaoEsperada = antigaPos + dado;
                
                if (novaPos != posicaoEsperada && posicaoEsperada <= 100) {
                    if (cobrasRecebidas.containsKey(posicaoEsperada)) {
                        destinoTmp = posicaoEsperada;
                        casaFinalTmp = novaPos;
                    }
                    else if (escadasRecebidas.containsKey(posicaoEsperada)) {
                        destinoTmp = posicaoEsperada;
                        casaFinalTmp = novaPos;
                    }
                    else {
                        destinoTmp = novaPos;
                        casaFinalTmp = null;
                    }
                }
                else {
                    destinoTmp = novaPos;
                    casaFinalTmp = null;
                }
            }
            if (i != jogadorMovendoTmp) {
                jogo.getPeca(i).setIdCasa(novaPos);
            }
        }

        final int jogadorMovendo = jogadorMovendoTmp;
        final int origem = origemTmp;
        final int destino = destinoTmp;
        final Integer casaFinal = casaFinalTmp;

        if (jogadorMovendo != -1) {
            animando = true;
            botaoRolar.setDisable(true);
            animarDado(dado, () -> {
                moverComCobraOuEscada(jogadorMovendo, origem, destino, casaFinal, () -> {
                    desenharTabuleiro();
                    atualizarPainelJogadores(indiceVez);
                    atualizarPainelVitorias();
                    areaStatus.setText(status);
                    animando = false;
                    botaoRolar.setDisable(animando || meuIndice != indiceVez || jogoAcabou);
                });
            });
        } else {
            for (int i = 0; i < pos.length; i++) {
                int novaPos = Integer.parseInt(pos[i]);
                jogo.getPeca(i).setIdCasa(novaPos);
            }
            
            desenharTabuleiro();
            atualizarPainelJogadores(indiceVez);
            atualizarPainelVitorias();
            areaStatus.setText(status);
            animando = false;
            botaoRolar.setDisable(animando || meuIndice != indiceVez || jogoAcabou);
        }

        if (jogoAcabou) {
            botaoRolar.setDisable(true);
            mostrarDialogoVitoria(nomesJogadores[indiceVez]);
        }
    }

    /**
     * Exibe o diálogo de vitória quando o jogo termina.
     * 
     * Permite ao jogador escolher se deseja reiniciar a partida
     * ou sair do jogo.
     * 
     * @param vencedor Nome do jogador vencedor
     */
    private void mostrarDialogoVitoria(String vencedor) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Fim de Jogo");
        alert.setHeaderText("🏆 " + vencedor + " venceu o jogo!");
        alert.setContentText("Deseja reiniciar a partida?");
        ButtonType btnSim = new ButtonType("Sim");
        ButtonType btnNao = new ButtonType("Não");
        alert.getButtonTypes().setAll(btnSim, btnNao);

        alert.showAndWait().ifPresent(type -> {
            if (type == btnSim) {
                socketOutput.println("REINICIAR_SIM");
            } else {
                socketOutput.println("REINICIAR_NAO");
            }
        });
    }

    /**
     * Mostra uma mensagem temporária quando um jogador cai em cobra ou escada.
     * 
     * A mensagem aparece no centro da tela por 2 segundos, indicando
     * se o jogador desceu por uma cobra ou subiu por uma escada.
     * 
     * @param casaOrigem Casa onde estava a cobra/escada
     * @param casaDestino Casa para onde o jogador foi movido
     */
    private void mostrarMensagemCobraEscada(int casaOrigem, int casaDestino) {
        String tipo = casaDestino < casaOrigem ? "COBRA" : "ESCADA";
        String emoji = tipo.equals("COBRA") ? "🐍" : "🪜";
        String acao = tipo.equals("COBRA") ? "Desceu pela cobra" : "Subiu pela escada";
        
        Label mensagem = new Label(emoji + " " + acao + " da casa " + casaOrigem + " para a casa " + casaDestino + "!");
        mensagem.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-text-fill: white; " +
                         "-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 15px; " +
                         "-fx-background-radius: 10px; -fx-border-radius: 10px;");
        mensagem.setAlignment(Pos.CENTER);
        mensagem.setWrapText(true);
        mensagem.setMaxWidth(300);
        
        overlayPane.getChildren().add(mensagem);
        
        mensagem.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            mensagem.setLayoutX((overlayPane.getWidth() - newBounds.getWidth()) / 2);
            mensagem.setLayoutY((overlayPane.getHeight() - newBounds.getHeight()) / 2);
        });
        
        Timeline removerMensagem = new Timeline(new KeyFrame(Duration.millis(2000), e -> {
            overlayPane.getChildren().remove(mensagem);
        }));
        removerMensagem.play();
    }

    /**
     * Anima o movimento gradual de uma peça entre duas casas.
     * 
     * Move a peça casa por casa com uma animação temporal,
     * redesenhando o tabuleiro a cada passo.
     * 
     * @param idxJogador Índice do jogador a ser movido
     * @param origem Casa de origem
     * @param destino Casa de destino
     * @param aoTerminar Callback executado ao final da animação
     */
    private void animarMovimentoPeca(int idxJogador, int origem, int destino, Runnable aoTerminar) {
        int passo = origem < destino ? 1 : -1;
        Timeline timeline = new Timeline();
        int totalPassos = Math.abs(destino - origem);

        for (int i = 1; i <= totalPassos; i++) {
            int posAtual = origem + i * passo;
            int frame = i;
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(300 * frame), e -> {
                jogo.getPeca(idxJogador).setIdCasa(posAtual);
                desenharTabuleiro();
            }));
        }

        timeline.setOnFinished(e -> {
            if (aoTerminar != null) aoTerminar.run();
        });
        timeline.play();
    }

    /**
     * Anima o dado mostrando faces aleatórias antes do resultado final.
     * 
     * Simula o efeito de um dado rolando, exibindo faces aleatórias
     * por um curto período antes de mostrar o resultado real.
     * 
     * @param resultado Face final que deve ser exibida no dado
     * @param aoTerminar Callback executado ao final da animação
     */
    private void animarDado(int resultado, Runnable aoTerminar) {
        Timeline timeline = new Timeline();
        for (int i = 0; i < 10; i++) {
            int face = 1 + (int)(Math.random() * 6);
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(60 * i), e -> {
                dadoImage.setImage(new Image(getClass().getResource("/imagens/dado" + face + ".png").toExternalForm()));
            }));
        }
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(600), e -> {
            dadoImage.setImage(new Image(getClass().getResource("/imagens/dado" + resultado + ".png").toExternalForm()));
            if (aoTerminar != null) aoTerminar.run();
        }));
        timeline.play();
    }

    /**
     * Coordena o movimento de uma peça incluindo efeitos de cobra/escada.
     * 
     * Primeiro move a peça normalmente, depois verifica se caiu em
     * cobra/escada e executa o movimento secundário apropriado.
     * 
     * @param idxJogador Índice do jogador
     * @param origem Casa de origem
     * @param destino Casa intermediária (onde está a cobra/escada)
     * @param casaFinal Casa final (null se movimento normal)
     * @param aoTerminar Callback executado ao final de todas as animações
     */
    private void moverComCobraOuEscada(int idxJogador, int origem, int destino, Integer casaFinal, Runnable aoTerminar) {
        animarMovimentoPeca(idxJogador, origem, destino, () -> {
            if (casaFinal != null && !casaFinal.equals(destino)) {
                mostrarMensagemCobraEscada(destino, casaFinal);
                
                Timeline pausaTimeline = new Timeline(new KeyFrame(Duration.millis(2000), e -> {
                    if (casaFinal > destino) {
                        animarMovimentoReto(idxJogador, destino, casaFinal, aoTerminar);
                    } else {
                        animarMovimentoCobra(idxJogador, destino, casaFinal, aoTerminar);
                    }
                }));
                pausaTimeline.play();
            } else {
                if (aoTerminar != null) aoTerminar.run();
            }
        });
    }

    /**
     * Anima movimento em linha reta (usado para escadas).
     * 
     * Move a peça diretamente em linha reta da origem ao destino,
     * usando PathTransition para animação suave.
     * 
     * @param idxJogador Índice do jogador
     * @param origem Casa de origem
     * @param destino Casa de destino
     * @param aoTerminar Callback executado ao final da animação
     */
    private void animarMovimentoReto(int idxJogador, int origem, int destino, Runnable aoTerminar) {
        ImageView pecaReal = encontrarPecaJogador(idxJogador, origem);
        
        if (pecaReal == null) {
            jogo.getPeca(idxJogador).setIdCasa(destino);
            desenharTabuleiro();
            if (aoTerminar != null) aoTerminar.run();
            return;
        }
        
        ((StackPane) pecaReal.getParent()).getChildren().remove(pecaReal);
        
        double[] start = getCasaCenter(origem);
        double[] end = getCasaCenter(destino);
        
        pecaReal.setLayoutX(start[0] - pecaReal.getFitWidth() / 2);
        pecaReal.setLayoutY(start[1] - pecaReal.getFitHeight() / 2);
        overlayPane.getChildren().add(pecaReal);

        Path path = new Path();
        path.getElements().add(new MoveTo(0, 0));
        path.getElements().add(new LineTo(end[0] - start[0], end[1] - start[1]));

        PathTransition pt = new PathTransition(Duration.millis(900), path, pecaReal);
        pt.setOnFinished(e -> {
            overlayPane.getChildren().remove(pecaReal);
            jogo.getPeca(idxJogador).setIdCasa(destino);
            desenharTabuleiro();
            if (aoTerminar != null) aoTerminar.run();
        });
        pt.play();
    }

    /**
     * Anima movimento em curva (usado para cobras).
     * 
     * Move a peça seguindo uma trajetória curva da origem ao destino,
     * usando CubicCurveTo para simular o deslizar pela cobra.
     * 
     * @param idxJogador Índice do jogador
     * @param origem Casa de origem
     * @param destino Casa de destino
     * @param aoTerminar Callback executado ao final da animação
     */
    private void animarMovimentoCobra(int idxJogador, int origem, int destino, Runnable aoTerminar) {
        // Encontrar a peça real do jogador e movê-la para o overlay para animação
        ImageView pecaReal = encontrarPecaJogador(idxJogador, origem);
        
        if (pecaReal == null) {
            // Fallback: teleportar diretamente
            jogo.getPeca(idxJogador).setIdCasa(destino);
            desenharTabuleiro();
            if (aoTerminar != null) aoTerminar.run();
            return;
        }
        
        // Remover a peça do grid
        ((StackPane) pecaReal.getParent()).getChildren().remove(pecaReal);
        
        // Calcular posições inicial e final
        double[] start = getCasaCenter(origem);
        double[] end = getCasaCenter(destino);
        
        // Posicionar a peça no overlay (centrada na posição inicial)
        pecaReal.setLayoutX(start[0] - pecaReal.getFitWidth() / 2);
        pecaReal.setLayoutY(start[1] - pecaReal.getFitHeight() / 2);
        overlayPane.getChildren().add(pecaReal);

        // Criar animação em curva (cobra)
        double dx = end[0] - start[0];
        double dy = end[1] - start[1];
        double cx1 = dx * 0.25 + 30;
        double cy1 = dy * 0.25 - 30;
        double cx2 = dx * 0.75 - 30;
        double cy2 = dy * 0.75 + 30;

        Path path = new Path();
        path.getElements().add(new MoveTo(0, 0)); // Partir da posição atual da peça
        path.getElements().add(new javafx.scene.shape.CubicCurveTo(cx1, cy1, cx2, cy2, dx, dy)); // Movimento relativo

        PathTransition pt = new PathTransition(Duration.millis(1200), path, pecaReal);
        pt.setOnFinished(e -> {
            // Remover do overlay
            overlayPane.getChildren().remove(pecaReal);
            // Atualizar posição da peça no jogo
            jogo.getPeca(idxJogador).setIdCasa(destino);
            // Redesenhar tabuleiro
            desenharTabuleiro();
            if (aoTerminar != null) aoTerminar.run();
        });
        pt.play();
    }

    /**
     * Encontra e retorna a ImageView da peça de um jogador específico.
     * 
     * Busca no gridTabuleiro pela peça do jogador na casa especificada,
     * identificando-a pela imagem correspondente.
     * 
     * @param idxJogador Índice do jogador (0 ou 1)
     * @param casa Número da casa onde buscar a peça
     * @return ImageView da peça encontrada, ou null se não encontrada
     */
    private ImageView encontrarPecaJogador(int idxJogador, int casa) {
        String imagemEsperada = (idxJogador == 1) ? "peao_azul.png" : "peao.png";
        
        for (javafx.scene.Node node : gridTabuleiro.getChildren()) {
            if (node instanceof StackPane) {
                StackPane cell = (StackPane) node;
                
                // Verificar se esta célula corresponde à casa
                Integer row = GridPane.getRowIndex(node);
                Integer col = GridPane.getColumnIndex(node);
                if (row == null) row = 0;
                if (col == null) col = 0;
                
                int numCasaCalculada = 100 - (row * 10 + ((row % 2 == 0) ? col : 9 - col));
                
                if (numCasaCalculada == casa) {
                    // Procurar a peça do jogador nesta célula
                    for (javafx.scene.Node child : cell.getChildren()) {
                        if (child instanceof ImageView) {
                            ImageView img = (ImageView) child;
                            if (img.getImage() != null && img.getImage().getUrl().contains(imagemEsperada)) {
                                return img;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Método de ação do botão sair.
     * 
     * Encerra a aplicação JavaFX completamente.
     */
    @FXML
    private void sair() {
        javafx.application.Platform.exit();
    }

    /**
     * Atualiza o painel lateral com informações dos jogadores.
     * 
     * Exibe para cada jogador:
     * - Nome
     * - Imagem da peça
     * - Posição atual no tabuleiro
     * - Indicador visual se é sua vez de jogar
     * 
     * @param indiceJogadorVez Índice do jogador que deve jogar agora
     */
    private void atualizarPainelJogadores(int indiceJogadorVez) {
        if (jogo == null || painelJogadores == null) return;
        
        painelJogadores.getChildren().clear();
        
        for (int i = 0; i < jogo.getNumeroJogadores(); i++) {
            // Criar um container para cada jogador
            VBox containerJogador = new VBox(5);
            containerJogador.setAlignment(Pos.CENTER);
            containerJogador.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1px; -fx-border-radius: 5px;");
            
            // Nome do jogador
            String nomeJogador = jogo.getNomesJogadores().get(i);
            Label nomeLabel = new Label(nomeJogador);
            nomeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            
            // Imagem da peça
            String imagemPeao = (i == 1) ? "/imagens/peao_azul.png" : "/imagens/peao.png";
            java.net.URL peaoUrl = getClass().getResource(imagemPeao);
            if (peaoUrl != null) {
                ImageView pecaView = new ImageView(new Image(peaoUrl.toExternalForm()));
                pecaView.setFitWidth(32);
                pecaView.setFitHeight(32);
                containerJogador.getChildren().addAll(nomeLabel, pecaView);
            } else {
                // Fallback para texto se a imagem não existir
                String corPeca = (i == 1) ? "Azul" : "Preto";
                Label corLabel = new Label("Peça: " + corPeca);
                corLabel.setStyle("-fx-font-size: 12px;");
                containerJogador.getChildren().addAll(nomeLabel, corLabel);
            }
            
            // Posição atual
            Label posicaoLabel = new Label("Casa: " + jogo.getPosicaoJogador(i));
            posicaoLabel.setStyle("-fx-font-size: 12px;");
            containerJogador.getChildren().add(posicaoLabel);
            
            // Destacar jogador que vai jogar (vez atual)..
            if (i == indiceJogadorVez) {
                containerJogador.setStyle(containerJogador.getStyle() + "-fx-background-color: #e8f5e8; -fx-border-color: #4caf50; -fx-border-width: 2px;");
                // Adicionar indicador de vez....
                Label vezLabel = new Label("SUA VEZ!");
                vezLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #4caf50;");
                containerJogador.getChildren().add(vezLabel);
            }
            
            painelJogadores.getChildren().add(containerJogador);
        }
    }

    /**
     * Atualiza o painel de vitórias com o placar atual.
     * 
     * Exibe o número de vitórias de cada jogador,
     * removendo entradas antigas e adicionando as atualizadas.
     */
    private void atualizarPainelVitorias() {
        if (jogo == null || painelVitorias == null) return;
        
        // Limpar completamente o painel e recriar
        painelVitorias.getChildren().clear();
        
        // Adicionar título
        Label titulo = new Label("🏆 VITÓRIAS");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #d4af37; -fx-padding: 0 0 10 0;");
        painelVitorias.getChildren().add(titulo);
        
        // Adicionar vitórias de cada jogador
        for (int i = 0; i < jogo.getNumeroJogadores(); i++) {
            String nomeJogador = jogo.getNomesJogadores().get(i);
            int vitorias = jogo.getVitoriasJogador(i);
            
            Label vitoriaLabel = new Label(nomeJogador + ": " + vitorias);
            vitoriaLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #8b4513;");
            painelVitorias.getChildren().add(vitoriaLabel);
        }
    }
}
