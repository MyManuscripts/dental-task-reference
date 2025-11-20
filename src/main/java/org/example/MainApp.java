package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.ui.TaxReferenceView;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            TaxReferenceView view = new TaxReferenceView();

            primaryStage.setScene(view.getScene());
            primaryStage.setTitle("Dental Tax Reference (JavaFX)");
            primaryStage.setWidth(800);
            primaryStage.setHeight(600);
            primaryStage.show();
        }catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}