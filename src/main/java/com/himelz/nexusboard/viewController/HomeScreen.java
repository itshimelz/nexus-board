package com.himelz.nexusboard.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class HomeScreen {

    private Stage primaryStage;
    private Scene scene;

    public HomeScreen(Stage stage) {
        this.primaryStage = stage;
        createHomeScreen();
    }

    private void createHomeScreen() {
        // Main container
        VBox mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(40));
        mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #3498db);");

        // Title
        Label titleLabel = new Label("♔ NEXUS BOARD ♔");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);");

        // Button container
        VBox buttonContainer = new VBox(15);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setMaxWidth(300);

        // Create styled buttons
        Button newGameBtn = createStyledButton("New Game", "#27ae60");
        Button loadGameBtn = createStyledButton("Load Game", "#f39c12");
        Button settingsBtn = createStyledButton("Settings", "#9b59b6");
        Button exitBtn = createStyledButton("Exit", "#e74c3c");

        // Add button event handlers (placeholder for now)
        newGameBtn.setOnAction(e -> handleNewGame());
        loadGameBtn.setOnAction(e -> handleLoadGame());
        settingsBtn.setOnAction(e -> handleSettings());
        exitBtn.setOnAction(e -> handleExit());

        // Add buttons to container
        buttonContainer.getChildren().addAll(newGameBtn, loadGameBtn, settingsBtn, exitBtn);

        // Add components to main container
        mainContainer.getChildren().addAll(titleLabel, buttonContainer);

        // Create scene
        scene = new Scene(mainContainer, 800, 600);
        scene.setFill(Color.TRANSPARENT);
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        button.setPrefWidth(280);
        button.setPrefHeight(50);
        button.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 25; " +
            "-fx-border-radius: 25; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 1);",
            color
        ));

        // Hover effects
        button.setOnMouseEntered(e -> {
            button.setStyle(button.getStyle() + "-fx-scale-x: 1.05; -fx-scale-y: 1.05;");
        });

        button.setOnMouseExited(e -> {
            button.setStyle(button.getStyle().replace("-fx-scale-x: 1.05; -fx-scale-y: 1.05;", ""));
        });

        return button;
    }

    // Placeholder event handlers
    private void handleNewGame() {
        System.out.println("New Game selected");

    }

    private void handleLoadGame() {
        System.out.println("Load Game selected");

    }

    private void handleSettings() {
        System.out.println("Settings selected");

    }

    private void handleExit() {
        primaryStage.close();
    }

    public Scene getScene() {
        return scene;
    }

    public void show() {
        primaryStage.setTitle("Nexus Board - Chess Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
}
