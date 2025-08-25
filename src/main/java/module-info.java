open module com.himelz.nexusboard {
    requires java.base;
    requires java.desktop;
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;

    // Export app package
    exports com.himelz.nexusboard.app;
    
    // Export model packages
    exports com.himelz.nexusboard.model;
    exports com.himelz.nexusboard.model.board;
    exports com.himelz.nexusboard.model.pieces;
    
    // Export view packages
    exports com.himelz.nexusboard.viewController;
    exports com.himelz.nexusboard.viewmodel;

    // Export network package
    exports com.himelz.nexusboard.network;
    
    // Export utils package
    exports com.himelz.nexusboard.utils;
}
