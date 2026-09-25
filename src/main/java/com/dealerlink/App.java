package com.dealerlink;

import com.dealerlink.db.DatabaseManager;
import com.dealerlink.util.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * DealerLink - Smart B2B Shop-Dealer Supply Platform.
 * Entry point: initializes the SQLite database, then shows the Login screen.
 */
public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        DatabaseManager.initialize();

        switchScene("/com/dealerlink/fxml/Login.fxml", "DealerLink - Login");

        stage.setOnCloseRequest(e -> SessionManager.shutdown());
        stage.show();
    }

    /**
     * Utility used by controllers to switch the root of the primary stage.
     */
    public static void switchScene(String fxmlPath, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1000, 650);
        scene.getStylesheets().add(App.class.getResource("/com/dealerlink/css/style.css").toExternalForm());
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}