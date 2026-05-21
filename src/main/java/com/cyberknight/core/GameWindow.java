package com.cyberknight.core;

import javax.swing.*;
import java.awt.*;

/**
 * GameWindow manages the JFrame and holds the GamePanel.
 */
public class GameWindow {

    private JFrame frame;
    private GamePanel gamePanel;

    public static final int TILE_SIZE   = 48;
    public static final int SCREEN_COLS = 26;
    public static final int SCREEN_ROWS = 14;
    public static final int SCREEN_W    = TILE_SIZE * SCREEN_COLS; // 1248
    public static final int SCREEN_H    = TILE_SIZE * SCREEN_ROWS; // 672
    public static final String TITLE    = "CyberKnight";

    public GameWindow() {
        frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        gamePanel = new GamePanel();
        frame.add(gamePanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void startGame() {
        gamePanel.startGameLoop();
    }
}