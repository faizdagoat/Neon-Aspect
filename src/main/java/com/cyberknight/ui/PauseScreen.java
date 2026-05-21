package com.cyberknight.ui;

import com.cyberknight.audio.AudioManager;
import com.cyberknight.core.GamePanel;
import com.cyberknight.core.GameState;
import com.cyberknight.core.InputHandler;
import com.cyberknight.util.CyberColors;
import java.awt.*;

/**
 * PauseScreen – overlay saat game di-pause.
 * ESC = resume, M = toggle mute musik.
 */
public class PauseScreen {

    private GamePanel panel;
    private int  selected  = 0;
    private int  animTimer = 0;

    private static final String[] OPTIONS = {"RESUME", "MAIN MENU", "QUIT"};

    public PauseScreen(GamePanel panel) {
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
            switch (selected) {
                case 0 -> { input.resetAll(); panel.setState(GameState.PLAYING); }
                case 1 -> { input.resetAll(); AudioManager.getInstance().stop(); panel.setState(GameState.MAIN_MENU); }
                case 2 -> System.exit(0);
            }
        }

        if (input.consumeEscape()) {
            input.resetAll();
            panel.setState(GameState.PLAYING);
        }

        if (input.consumeMute()) {
            AudioManager.getInstance().toggleMute();
        }
    }

    public void draw(Graphics2D g2) {
        int W = GamePanel.SCREEN_W, H = GamePanel.SCREEN_H;

        // Overlay
        g2.setColor(new Color(0x00, 0x00, 0x0A, 170));
        g2.fillRect(0, 0, W, H);
        g2.setColor(new Color(0, 0, 0, 30));
        for (int y = 0; y < H; y += 3) g2.drawLine(0, y, W, y);

        // Panel
        int pw = 370, ph = 280;
        int px = W / 2 - pw / 2;
        int py = H / 2 - ph / 2;

        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(px + 6, py + 6, pw, ph, 18, 18);

        g2.setColor(new Color(0x04, 0x04, 0x1A, 240));
        g2.fillRoundRect(px, py, pw, ph, 18, 18);

        int ga = 60 + (int)(50 * Math.sin(animTimer * 0.07));
        g2.setColor(new Color(0x00, 0xFF, 0xD5, ga));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(px, py, pw, ph, 18, 18);
        g2.setStroke(new BasicStroke(1f));

        // Title
        g2.setFont(new Font("Monospaced", Font.BOLD, 26));
        String title = "// PAUSED //";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.setColor(CyberColors.UI_TITLE);
        g2.drawString(title, W / 2 - tw / 2, py + 42);

        g2.setColor(new Color(0x00, 0xFF, 0xD5, 50));
        g2.fillRect(px + 24, py + 52, pw - 48, 1);

        // Options
        g2.setFont(new Font("Monospaced", Font.BOLD, 19));
        for (int i = 0; i < OPTIONS.length; i++) {
            int oy  = py + 80 + i * 44;
            boolean sel = (i == selected);
            int ow  = g2.getFontMetrics().stringWidth(OPTIONS[i]);

            if (sel) {
                g2.setColor(new Color(0x00, 0xFF, 0xD5, 28));
                g2.fillRoundRect(W / 2 - ow / 2 - 18, oy - 17, ow + 36, 26, 8, 8);
                g2.setColor(CyberColors.NEON_PINK);
                g2.drawString("▶", W / 2 - ow / 2 - 34, oy);
                g2.setColor(CyberColors.UI_SELECT);
            } else {
                g2.setColor(new Color(0x88, 0x88, 0xCC));
            }
            g2.drawString(OPTIONS[i], W / 2 - ow / 2, oy);
        }

        // Musik status
        AudioManager audio = AudioManager.getInstance();
        String muteStr = audio.isMuted() ? "♪ MUSIK: OFF" : "♪ MUSIK: ON";
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        g2.setColor(audio.isMuted()
            ? new Color(0xFF, 0x44, 0x44, 180)
            : new Color(0x00, 0xFF, 0xD5, 180));
        int mw = g2.getFontMetrics().stringWidth(muteStr);
        g2.drawString(muteStr, W / 2 - mw / 2, py + ph - 42);

        // Hints
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(new Color(0x44, 0x55, 0x66));
        String h1 = "↑↓ Navigate   |   ENTER — Select   |   ESC — Resume";
        String h2 = "M — Toggle Musik";
        int h1w = g2.getFontMetrics().stringWidth(h1);
        int h2w = g2.getFontMetrics().stringWidth(h2);
        g2.drawString(h1, W / 2 - h1w / 2, py + ph - 26);
        g2.drawString(h2, W / 2 - h2w / 2, py + ph - 12);
    }
}
