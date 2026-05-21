package com.cyberknight.ui;

import com.cyberknight.core.GamePanel;
import com.cyberknight.core.GameState;
import com.cyberknight.core.InputHandler;
import com.cyberknight.util.CyberColors;
import java.awt.*;

/**
 * Game Over screen – shown when player runs out of lives.
 * Options: Retry, Main Menu, Quit
 */
public class GameOverScreen {

    private GamePanel panel;
    private int selected  = 0;
    private int animTimer = 0;

    private static final String[] OPTIONS = {"RETRY", "MAIN MENU", "QUIT"};

    public GameOverScreen(GamePanel panel) {
        this.panel = panel;
    }

    public void update(InputHandler input) {
        animTimer++;

        if (input.up) {
            selected = (selected - 1 + OPTIONS.length) % OPTIONS.length;
            input.up = false;
        }
        if (input.down) {
            selected = (selected + 1) % OPTIONS.length;
            input.down = false;
        }
        if (input.consumeEnter() || input.consumeJump()) {
            input.resetAll();
            switch (selected) {
                case 0 -> panel.startNewGame();
                case 1 -> panel.setState(GameState.MAIN_MENU);
                case 2 -> System.exit(0);
            }
        }
    }

    public void draw(Graphics2D g2) {
        int W = GamePanel.SCREEN_W;
        int H = GamePanel.SCREEN_H;

        // Dark overlay
        g2.setColor(new Color(0, 0, 0, 210));
        g2.fillRect(0, 0, W, H);

        // CRT static noise effect
        for (int i = 0; i < 400; i++) {
            int nx = (int)(Math.random() * W);
            int ny = (int)(Math.random() * H);
            int a  = (int)(Math.random() * 70);
            g2.setColor(new Color(200, 0, 0, a));
            g2.fillRect(nx, ny, (int)(Math.random() * 4) + 1, 1);
        }

        // Horizontal scan lines
        g2.setColor(new Color(255, 0, 0, 10));
        for (int y = 0; y < H; y += 4) g2.drawLine(0, y, W, y);

        // ── Title ─────────────────────────────────────────────
        g2.setFont(new Font("Monospaced", Font.BOLD, 62));
        String title = "JACK OUT";
        int tw = g2.getFontMetrics().stringWidth(title);

        // Glitch effect – offset copies
        g2.setColor(new Color(0xFF, 0x00, 0x44, 80));
        g2.drawString(title, W / 2 - tw / 2 + 4, H / 3 + 4);
        g2.setColor(new Color(0x00, 0xFF, 0xFF, 60));
        g2.drawString(title, W / 2 - tw / 2 - 3, H / 3 - 3);
        // Main
        g2.setColor(new Color(0xFF, 0x22, 0x22));
        g2.drawString(title, W / 2 - tw / 2, H / 3);

        // Subtitle
        g2.setFont(new Font("Monospaced", Font.ITALIC, 17));
        String sub = "Neural link severed.  You flatlined in the grid.";
        int sw = g2.getFontMetrics().stringWidth(sub);
        g2.setColor(new Color(0xFF, 0x88, 0x88, 200));
        g2.drawString(sub, W / 2 - sw / 2, H / 3 + 52);

        // ── Menu options ──────────────────────────────────────
        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        int startY = H / 2 + 30;
        for (int i = 0; i < OPTIONS.length; i++) {
            int oy  = startY + i * 50;
            boolean sel = (i == selected);
            int ow  = g2.getFontMetrics().stringWidth(OPTIONS[i]);

            if (sel) {
                // Highlight box
                g2.setColor(new Color(0xFF, 0x22, 0x22, 45));
                g2.fillRoundRect(W / 2 - ow / 2 - 20, oy - 20, ow + 40, 30, 8, 8);
                // Arrow
                g2.setColor(new Color(0xFF, 0x44, 0x44));
                g2.drawString("▶", W / 2 - ow / 2 - 38, oy);
                g2.setColor(new Color(0xFF, 0x44, 0x44));
            } else {
                g2.setColor(new Color(0x99, 0x44, 0x44));
            }
            g2.drawString(OPTIONS[i], W / 2 - ow / 2, oy);
        }

        // ── Hint ──────────────────────────────────────────────
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(new Color(0x55, 0x33, 0x33));
        String hint = "↑↓ Navigate   |   ENTER / Z — Select";
        int hw = g2.getFontMetrics().stringWidth(hint);
        g2.drawString(hint, W / 2 - hw / 2, H - 22);
    }
}