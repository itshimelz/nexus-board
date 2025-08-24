module com.himelz.nexusboard {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.dlsc.formsfx;

    // Export main package
//    exports com.himelz.nexusboard;
    exports com.himelz.nexusboard.app;
    
    // Export model packages
    exports com.himelz.nexusboard.model;
    exports com.himelz.nexusboard.model.board;
    exports com.himelz.nexusboard.model.pieces;
    
    // Export view packages
    exports com.himelz.nexusboard.viewController;

    // Export network package
    exports com.himelz.nexusboard.network;
    
    // Export utils package
    exports com.himelz.nexusboard.utils;
    
    // Open packages to JavaFX for FXML injection
    opens com.himelz.nexusboard.app to javafx.fxml;
    opens com.himelz.nexusboard.viewController to javafx.fxml;
}
