package com.cyberknight.ui;

import com.cyberknight.core.GamePanel;
import com.cyberknight.core.InputHandler;
import com.cyberknight.util.CyberColors;

import java.awt.*;

/**
 * Main menu with animated cyberpunk background.
 * Options: New Game, Controls, Quit
 */
public class MainMenu {

    private GamePanel panel;
    private int       selected  = 0;
    private int       animTimer = 0;
    private float     glowPulse = 0f;

    private static final String[] OPTIONS = {"NEW GAME", "CONTROLS", "QUIT"};

    // For animated city silhouette
    private int[] buildH;
    private int[] buildX;
    private int   buildCount = 20;

    public MainMenu(GamePanel panel) {
        this.panel = panel;
        buildH = new int[buildCount];
        buildX = new int[buildCount];
        int x = 0;
        for (int i = 0; i < buildCount; i++) {
            int w  = 40 + (int)(Math.random() * 80);
            buildH[i] = 60 + (int)(Math.random() * 280);
            buildX[i] = x;
            x += w + (int)(Math.random() * 30);
        }
    }

    public void update(InputHandler input) {
        animTimer++;
        glowPulse += 0.05f;

        if (input.up) { selected = (selected - 1 + OPTIONS.length) % OPTIONS.length; input.up = false; }
        if (input.down) { selected = (selected + 1) % OPTIONS.length; input.down = false; }

        if (input.consumeEnter() || input.consumeJump()) {
            input.resetAll();
            switch (selected) {
                case 0 -> panel.startNewGame();
                case 1 -> {} // Controls popup handled in draw
                case 2 -> System.exit(0);
            }
        }
    }

    public void draw(Graphics2D g2) {
        int W = GamePanel.SCREEN_W;
        int H = GamePanel.SCREEN_H;

        // ── Background ────────────────────────────────────────
        g2.setColor(new Color(0x03, 0x03, 0x12));
        g2.fillRect(0, 0, W, H);

        // Stars
        for (int i = 0; i < 60; i++) {
            int sx = (i * 211 + animTimer / 2) % W;
            int sy = (i * 137) % (H / 2);
            int size = (i % 3) + 1;
            g2.setColor(new Color(255, 255, 255, 50 + (i % 100)));
            g2.fillOval(sx, sy, size, size);
        }

        // Scrolling city silhouette
        drawCitySilhouette(g2, W, H);

        // Neon grid floor
        g2.setColor(new Color(0x00, 0xFF, 0xD5, 25));
        for (int i = 0; i < W; i += 40) g2.drawLine(i, H / 2, i, H);
        for (int j = H / 2; j < H; j += 30) g2.drawLine(0, j, W, j);

        // ── Title ─────────────────────────────────────────────
        int pulse = (int)(8 * Math.sin(glowPulse));

        // Glow behind title
        g2.setColor(new Color(0x00, 0xFF, 0xFF, 25 + pulse * 2));
        g2.setFont(new Font("Monospaced", Font.BOLD, 80));
        String title = "NEON";
        String sub   = "ASPECT";
        int tw1 = g2.getFontMetrics().stringWidth(title);
        int tw2 = g2.getFontMetrics().stringWidth(sub);
        for (int d = 1; d <= 6; d++) {
            g2.setColor(new Color(0x00, 0xFF, 0xFF, 10));
            g2.drawString(title, W / 2 - tw1 / 2 + d, H / 4 + d);
            g2.drawString(sub,   W / 2 - tw2 / 2 + d, H / 4 + 80 + d);
        }
        // Main title
        g2.setColor(CyberColors.UI_TITLE);
        g2.drawString(title, W / 2 - tw1 / 2, H / 4);
        g2.setColor(CyberColors.NEON_PINK);
        g2.drawString(sub,   W / 2 - tw2 / 2, H / 4 + 80);

        // Tagline
        g2.setFont(new Font("Monospaced", Font.ITALIC, 14));
        String tag = "Jack into the neon abyss. Survive the grid.";
        int tagW = g2.getFontMetrics().stringWidth(tag);
        g2.setColor(new Color(0xAA, 0xAA, 0xFF, 180));
        g2.drawString(tag, W / 2 - tagW / 2, H / 4 + 110);

        // ── Menu options ──────────────────────────────────────
        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        int menuStartY = H / 2 + 20;
        for (int i = 0; i < OPTIONS.length; i++) {
            int oy = menuStartY + i * 50;
            boolean sel = (i == selected);

            if (sel) {
                // Selection glow
                int ow = g2.getFontMetrics().stringWidth(OPTIONS[i]);
                g2.setColor(new Color(0x00, 0xFF, 0xFF, 30 + pulse * 3));
                g2.fillRoundRect(W / 2 - ow / 2 - 20, oy - 20, ow + 40, 32, 8, 8);

                // Arrow
                g2.setColor(CyberColors.NEON_PINK);
                g2.drawString("▶", W / 2 - ow / 2 - 40, oy);
                g2.setColor(CyberColors.UI_SELECT);
            } else {
                g2.setColor(new Color(0xAA, 0xAA, 0xFF, 180));
            }

            int tw = g2.getFontMetrics().stringWidth(OPTIONS[i]);
            g2.drawString(OPTIONS[i], W / 2 - tw / 2, oy);
        }

        // ── Controls hint ─────────────────────────────────────
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(new Color(0x66, 0x66, 0x99));
        String ctrl = "↑↓ Navigate  |  ENTER / Z — Select";
        int cw = g2.getFontMetrics().stringWidth(ctrl);
        g2.drawString(ctrl, W / 2 - cw / 2, H - 20);

        // ── Controls popup ────────────────────────────────────
        if (selected == 1 && animTimer > 0) {
            drawControlsPopup(g2, W, H);
        }
    }

    private void drawCitySilhouette(Graphics2D g2, int W, int H) {
        int scroll = (animTimer / 3) % W;
        for (int pass = 0; pass < 2; pass++) {
            int alpha = pass == 0 ? 80 : 50;
            int yBase = H - (pass == 0 ? 100 : 150);
            for (int i = 0; i < buildCount; i++) {
                int bx = (buildX[i] - scroll + W * 2) % (W + 200) - 100;
                int bh = buildH[i] * (pass == 0 ? 1 : 0) + (pass == 1 ? buildH[(i + 3) % buildCount] / 2 : 0);
                int bw = 40 + (i % 5) * 12;
                g2.setColor(new Color(0x08, 0x08, 0x22, alpha));
                g2.fillRect(bx, yBase - bh, bw, bh);
                // Window dots
                g2.setColor(new Color(0xFF, 0xFF, 0x88, 40 + (i % 30)));
                for (int wr = 0; wr < bh / 18; wr++)
                    for (int wc = 0; wc < bw / 12; wc++)
                        if ((i + wr + wc + animTimer / 120) % 3 != 0)
                            g2.fillRect(bx + 4 + wc * 12, yBase - bh + 6 + wr * 18, 4, 6);
            }
        }
    }

    private void drawControlsPopup(Graphics2D g2, int W, int H) {
        int pw = 420, ph = 280;
        int px = W / 2 - pw / 2, py = H / 2 - ph / 2;
        g2.setColor(new Color(0x00, 0x00, 0x22, 220));
        g2.fillRoundRect(px, py, pw, ph, 16, 16);
        g2.setColor(CyberColors.NEON_CYAN);
        g2.drawRoundRect(px, py, pw, ph, 16, 16);

        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2.setColor(CyberColors.UI_TITLE);
        g2.drawString("CONTROLS", px + pw / 2 - 50, py + 28);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(CyberColors.UI_NORMAL);
        String[] lines = {
            "A / ← D / →   Move",
            "Z / SPACE      Jump (hold for higher)",
            "X / J          Attack (+ ↑↓ for direction)",
            "C / K          Dash",
            "V / L          Heal (cost: 33 SOUL)",
            "ESC            Pause",
            "",
            "Wall Slide: press toward wall mid-air",
            "Pogo: attack DOWN on enemies to bounce"
        };
        for (int i = 0; i < lines.length; i++) {
            g2.drawString(lines[i], px + 20, py + 56 + i * 20);
        }
    }
}