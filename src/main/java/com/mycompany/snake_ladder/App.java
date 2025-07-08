package com.mycompany.snake_ladder;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
//Concluido 1 de 4
/**
 * Classe principal da aplicação Cobras e Escadas.
 * Responsável por inicializar a aplicação JavaFX e determinar se será executada
 * como servidor ou cliente.
 * 
 * @author Snake_Ladder Team
 * @version 1.0
 */
public class App extends Application {
    /**
     * Flag para determinar se a aplicação será executada como servidor.
     */
    private static boolean isServidor = true;

    /**
     * Inicia a aplicação no modo servidor.
     * 
     * @param args Argumentos da linha de comando
     */
    public static void launchServidor(String[] args) {
        isServidor = true;
        launch(args);
    }

    /**
     * Inicia a aplicação no modo cliente.
     * 
     * @param args Argumentos da linha de comando
     */
    public static void launchCliente(String[] args) {
        isServidor = false;
        launch(args);
    }

    /**
     * Método principal do JavaFX que configura e exibe a janela principal.
     * Carrega o FXML apropriado baseado no modo (servidor ou cliente).
     * 
     * @param stage Palco principal da aplicação JavaFX
     * @throws Exception Se ocorrer erro ao carregar o FXML
     */
    @Override
    public void start(Stage stage) throws Exception {
        String fxml = isServidor
            ? "/com/mycompany/snake_ladder/lobby_servidor.fxml"
            : "/com/mycompany/snake_ladder/lobby_cliente.fxml";
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Cobras e Escadas - " + (isServidor ? "Servidor" : "Cliente"));
        stage.show();
    }

    /**
     * Método principal da aplicação.
     * Por padrão, inicia no modo servidor.
     * 
     * @param args Argumentos da linha de comando
     */
    public static void main(String[] args) {
        launch(args);
    }
}