package com.cyberknight.ui;

import com.cyberknight.core.GamePanel;
import com.cyberknight.entity.Player;
import com.cyberknight.level.LevelManager;
import com.cyberknight.util.CyberColors;

import java.awt.*;

/**
 * HUD - HP sebagai 3 mask ikon (Hollow Knight style).
 * Setiap hit mengurangi 1 mask. 3 hit = mati.
 * Tidak ada sistem lives — langsung Game Over.
 */
public class HUD {

    private Player       player;
    private LevelManager levelManager;
    private int          animTimer    = 0;
    private int          hpShakeTimer = 0;

    public HUD(Player player, LevelManager lm) {
        this.player       = player;
        this.levelManager = lm;
    }

    public void update() {
        animTimer++;
        if (hpShakeTimer > 0) hpShakeTimer--;
    }

    public void triggerHpShake() { hpShakeTimer = 12; }

    public void draw(Graphics2D g2) {
        drawHPMasks(g2);
        drawSoulVessel(g2);
        drawScore(g2);
        drawLevelName(g2);
    }

    // ── HP Masks (3 ikon) ─────────────────────────────────────
    private void drawHPMasks(Graphics2D g2) {
        int hp      = player.getCurrentHealth();
        int maxHp   = Player.MAX_HP;
        int size    = 30;
        int gap     = 8;
        int startX  = 20;
        int startY  = 16;

        int shakeX = hpShakeTimer > 0 ? (int)(Math.random() * 4 - 2) : 0;
        int shakeY = hpShakeTimer > 0 ? (int)(Math.random() * 4 - 2) : 0;

        for (int i = 0; i < maxHp; i++) {
            int mx = startX + i * (size + gap) + shakeX;
            int my = startY + shakeY;
            drawMask(g2, mx, my, size, i < hp);
        }
    }

    private void drawMask(Graphics2D g2, int x, int y, int s, boolean filled) {
        if (filled) {
            // Glow aura
            int ga = 55 + (int)(35 * Math.sin(animTimer * 0.12));
            g2.setColor(new Color(0x00, 0xFF, 0xD5, ga));
            g2.fillOval(x - 5, y - 5, s + 10, s + 10);
            // Shell
            g2.setColor(new Color(0x00, 0xBB, 0x99));
            g2.fillOval(x, y, s, s);
            // Inner core
            g2.setColor(new Color(0x88, 0xFF, 0xEE));
            g2.fillOval(x + s/4, y + s/4, s/2, s/2);
            // Shine
            g2.setColor(new Color(255, 255, 255, 110));
            g2.fillOval(x + s/4, y + s/5, s/4, s/5);
            // Border
            g2.setColor(CyberColors.NEON_CYAN);
            g2.drawOval(x, y, s, s);
        } else {
            // Empty — dark cracked shell
            g2.setColor(new Color(0x06, 0x18, 0x18));
            g2.fillOval(x, y, s, s);
            g2.setColor(new Color(0x00, 0x44, 0x44));
            g2.drawLine(x + s/3, y + s/4, x + s*2/3, y + s*3/4);
            g2.drawLine(x + s/2, y + s/4, x + s/3, y + s*2/3);
            g2.setColor(new Color(0x00, 0x55, 0x44));
            g2.drawOval(x, y, s, s);
        }
    }

    // ── Soul vessel ───────────────────────────────────────────
    private void drawSoulVessel(Graphics2D g2) {
        int souls = player.getSouls();
        int bx = 20, by = 58, h = 8;

        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        g2.setColor(CyberColors.SOUL_COLOR);
        g2.drawString("SOUL", bx, by - 2);

        for (int i = 0; i < 3; i++) {
            int segX   = bx + i * 34;
            int filled = Math.min(33, Math.max(0, souls - i * 33));
            g2.setColor(new Color(0x00, 0x11, 0x22));
            g2.fillRect(segX, by, 32, h);
            if (filled > 0) {
                float p = filled / 33f;
                g2.setColor(CyberColors.SOUL_COLOR);
                g2.fillRect(segX, by, (int)(32 * p), h);
                if (filled == 33) {
                    g2.setColor(new Color(0x44, 0xAA, 0xFF,
                        50 + (int)(30 * Math.sin(animTimer * 0.15f))));
                    g2.fillRect(segX, by, 32, h);
                }
            }
            g2.setColor(new Color(0x00, 0x88, 0xCC));
            g2.drawRect(segX, by, 32, h);
        }
        if (souls >= 33) {
            g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
            int a = 100 + (int)(60 * Math.sin(animTimer * 0.1));
            g2.setColor(new Color(0x44, 0xAA, 0xFF, a));
            g2.drawString("[V]", bx + 108, by + 7);
        }
    }

    // ── Score + Username ──────────────────────────────────────
    private void drawScore(Graphics2D g2) {
        // Username
        String user = com.cyberknight.auth.AuthManager.getInstance().getUsername();
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(new Color(0x00, 0xFF, 0xD5, 140));
        g2.drawString("[ " + user.toUpperCase() + " ]", GamePanel.SCREEN_W - 160, 36);

        // Score
        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        String text = "SCORE: " + String.format("%07d", player.getScore());
        int tw = g2.getFontMetrics().stringWidth(text);
        g2.setColor(Color.BLACK);
        g2.drawString(text, GamePanel.SCREEN_W - tw - 19, 21);
        g2.setColor(CyberColors.SCORE_CLR);
        g2.drawString(text, GamePanel.SCREEN_W - tw - 20, 20);
    }

    // ── Level name ────────────────────────────────────────────
    private void drawLevelName(Graphics2D g2) {
        if (levelManager.getCurrentLevel() == null) return;
        String name = levelManager.getCurrentLevel().getName();
        g2.setFont(new Font("Monospaced", Font.ITALIC, 11));
        int tw = g2.getFontMetrics().stringWidth(name);
        g2.setColor(new Color(0x00, 0xFF, 0xD5, 100));
        g2.drawString(name, GamePanel.SCREEN_W / 2 - tw / 2, GamePanel.SCREEN_H - 14);
    }
}