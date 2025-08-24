open module com.himelz.nexusboard {
    requires java.base;
    requires java.desktop;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Export app package
//    exports com.himelz.nexusboard.app;
    
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
}
