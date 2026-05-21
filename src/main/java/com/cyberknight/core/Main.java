package com.cyberknight.core;

/**
 * CyberKnight - Cyberpunk 2D RPG Platformer
 * Main entry point.
 */
public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.startGame();
        });
    }
}