package com.cyberknight.ui;

import com.cyberknight.core.GamePanel;
import com.cyberknight.core.GameState;
import com.cyberknight.core.InputHandler;
import com.cyberknight.util.CyberColors;
import java.awt.*;

/**
 * Victory screen – shown after defeating the final boss.
 */
public class VictoryScreen {

    private GamePanel panel;
    private int animTimer = 0;

    // Particle system
    private float[] pX, pY, pVX, pVY;
    private float[] pHue;
    private int     pCount = 120;

    public VictoryScreen(GamePanel panel) {
        this.panel = panel;
        initParticles();
    }

    private void initParticles() {
        pX   = new float[pCount]; pY   = new float[pCount];
        pVX  = new float[pCount]; pVY  = new float[pCount];
        pHue = new float[pCount];
        int W = GamePanel.SCREEN_W, H = GamePanel.SCREEN_H;
        for (int i = 0; i < pCount; i++) {
            pX[i]   = (float)(Math.random() * W);
            pY[i]   = (float)(Math.random() * H);
            pVX[i]  = (float)(Math.random() * 2 - 1);
            pVY[i]  = (float)(-Math.random() * 2 - 0.5f);
            pHue[i] = (float)(Math.random() * 360);
        }
    }

    public void update(InputHandler input) {
        animTimer++;

        // Update particles
        int W = GamePanel.SCREEN_W, H = GamePanel.SCREEN_H;
        for (int i = 0; i < pCount; i++) {
            pX[i] += pVX[i];
            pY[i] += pVY[i];
            pVY[i] += 0.02f; // gentle gravity
            if (pY[i] > H + 10) {
                pY[i] = -10;
                pX[i] = (float)(Math.random() * W);
                pVY[i] = (float)(-Math.random() * 2 - 0.5f);
            }
        }

        if ((input.consumeEnter() || input.consumeJump()) && animTimer > 120) {
            panel.setState(GameState.MAIN_MENU);
        }
    }

    public void draw(Graphics2D g2) {
        int W = GamePanel.SCREEN_W, H = GamePanel.SCREEN_H;

        // Dark gradient background
        GradientPaint bg = new GradientPaint(0, 0,
            new Color(0x02, 0x02, 0x12),
            0, H,
            new Color(0x00, 0x08, 0x22));
        g2.setPaint(bg);
        g2.fillRect(0, 0, W, H);

        // Radial burst from center
        for (int r = 300; r > 0; r -= 20) {
            int alpha = (int)(15 * Math.sin(animTimer * 0.05 + r * 0.01));
            g2.setColor(new Color(0x00, 0xFF, 0xD5, Math.max(0, alpha)));
            g2.drawOval(W / 2 - r, H / 2 - r, r * 2, r * 2);
        }

        // Particles
        for (int i = 0; i < pCount; i++) {
            float hue = (pHue[i] + animTimer * 0.5f) % 360;
            Color c = Color.getHSBColor(hue / 360f, 1f, 1f);
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 180));
            g2.fillOval((int)pX[i] - 2, (int)pY[i] - 2, 5, 5);
        }

        // ── Main title ────────────────────────────────────────
        if (animTimer > 20) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 48));
            String t1 = "MISSION COMPLETE";
            int tw1 = g2.getFontMetrics().stringWidth(t1);

            // Glow layers
            for (int d = 8; d >= 1; d--) {
                g2.setColor(new Color(0x00, 0xFF, 0xD5, 8));
                g2.drawString(t1, W / 2 - tw1 / 2 + d, H / 3 + d);
            }
            g2.setColor(CyberColors.UI_TITLE);
            g2.drawString(t1, W / 2 - tw1 / 2, H / 3);
        }

        if (animTimer > 50) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 28));
            String t2 = "THE GRID IS YOURS";
            int tw2 = g2.getFontMetrics().stringWidth(t2);
            for (int d = 4; d >= 1; d--) {
                g2.setColor(new Color(0xFF, 0x00, 0xFF, 12));
                g2.drawString(t2, W / 2 - tw2 / 2 + d, H / 3 + 55 + d);
            }
            g2.setColor(CyberColors.NEON_PINK);
            g2.drawString(t2, W / 2 - tw2 / 2, H / 3 + 55);
        }

        if (animTimer > 80) {
            g2.setFont(new Font("Monospaced", Font.ITALIC, 16));
            String sub = "Colossus Prime defeated.  The neon city breathes again.";
            int sw = g2.getFontMetrics().stringWidth(sub);
            g2.setColor(new Color(0xAA, 0xAA, 0xFF, 200));
            g2.drawString(sub, W / 2 - sw / 2, H / 3 + 95);
        }

        // ── Press to continue ──────────────────────────────────
        if (animTimer > 120) {
            g2.setFont(new Font("Monospaced", Font.PLAIN, 14));
            String press = "▶  ENTER — Return to Main Menu";
            int pw = g2.getFontMetrics().stringWidth(press);
            int a  = 120 + (int)(100 * Math.sin(animTimer * 0.08));
            g2.setColor(new Color(0x00, 0xFF, 0xD5, a));
            g2.drawString(press, W / 2 - pw / 2, H * 2 / 3 + 20);
        }

        // ── Scan-line overlay ──────────────────────────────────
        g2.setColor(new Color(0, 0, 0, 18));
        for (int y = 0; y < H; y += 3) g2.drawLine(0, y, W, y);
    }
}