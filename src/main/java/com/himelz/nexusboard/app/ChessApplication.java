package com.himelz.nexusboard.app;

import com.himelz.nexusboard.view.HomeScreen;
import javafx.application.Application;
import javafx.stage.Stage;

public class ChessApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        HomeScreen homeScreen = new HomeScreen(stage);
        homeScreen.show();
    }
}