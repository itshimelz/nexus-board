package com.himelz.nexusboard.app;

import com.himelz.nexusboard.viewController.LandingPage;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main application entry point for Nexus Board Chess Game.
 * Follows the user flow: Landing Page → Homepage → Game Window
 */
public class ChessApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Start with Landing Page following proper user flow
        LandingPage landingPage = new LandingPage(stage);
        landingPage.show();
    }
}