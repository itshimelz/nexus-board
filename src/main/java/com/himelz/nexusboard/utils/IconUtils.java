package com.himelz.nexusboard.utils;

import javafx.scene.control.Label;

/**
 * Utility class for creating and managing icons using Unicode symbols
 */
public class IconUtils {
    
    // Unicode symbol constants for icons
    public static final String REFRESH = "↻";           // Refresh/New Game
    public static final String UNDO = "↶";             // Undo
    public static final String FLAG = "⚑";             // Flag/Resign  
    public static final String HANDSHAKE = "🤝";       // Handshake/Draw (backup: ⚯)
    public static final String STOP = "⏹";            // Stop
    public static final String ARROW_LEFT = "←";       // Back arrow
    public static final String DESKTOP = "⌨";          // Desktop/Host
    public static final String TIMER = "⏱";           // Timer/Clock
    public static final String CIRCLE = "●";           // Circle indicator
    public static final String COPY = "⧉";             // Copy
    public static final String QUESTION = "?";         // Help/Question
    public static final String PLAY = "▶";             // Play/Start
    public static final String HOME = "⌂";             // Home
    public static final String NETWORK = "⚡";          // Network/Multiplayer
    public static final String GAMEPAD = "🎮";         // Gaming (backup: ⚡)
    public static final String COG = "⚙";              // Settings
    public static final String CHART_BAR = "📊";       // Statistics/Analysis (backup: ▣)
    public static final String CONNECTION = "●";        // Connection status
    
    /**
     * Create an icon label
     * @param iconCode The Unicode symbol
     * @param size The font size
     * @return Label with the icon
     */
    public static Label createIcon(String iconCode, double size) {
        Label icon = new Label(iconCode);
        icon.setStyle("-fx-font-size: " + size + "px;");
        return icon;
    }
    
    /**
     * Create an icon label with default size
     * @param iconCode The Unicode symbol
     * @return Label with the icon
     */
    public static Label createIcon(String iconCode) {
        return createIcon(iconCode, 14);
    }
    
    /**
     * Create an icon with text
     * @param iconCode The Unicode symbol
     * @param text The text to display
     * @param size The icon size
     * @return Label with icon and text
     */
    public static Label createIconWithText(String iconCode, String text, double size) {
        Label iconLabel = createIcon(iconCode, size);
        iconLabel.setText(iconCode + " " + text);
        return iconLabel;
    }
    
    /**
     * Get icon text for buttons (without creating Label)
     * @param iconCode The Unicode symbol
     * @param text The button text
     * @return Combined icon and text string
     */
    public static String getIconText(String iconCode, String text) {
        return iconCode + " " + text;
    }
}
