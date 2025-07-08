module com.mycompany.snake_ladder {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.mycompany.snake_ladder to javafx.fxml;
    opens com.mycompany.snake_ladder.controller to javafx.fxml;
    exports com.mycompany.snake_ladder;
    exports com.mycompany.snake_ladder.controller;
}