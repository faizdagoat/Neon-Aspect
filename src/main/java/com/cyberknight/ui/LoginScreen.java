package com.cyberknight.ui;

import com.cyberknight.auth.AuthManager;
import com.cyberknight.auth.AuthManager.LoginResult;
import com.cyberknight.auth.AuthManager.RegisterResult;
import com.cyberknight.auth.DatabaseManager;
import com.cyberknight.core.GamePanel;
import com.cyberknight.core.GameState;
import com.cyberknight.util.CyberColors;

import java.awt.*;
import java.awt.event.*;

/**
 * LoginScreen – layar login / register cyberpunk.
 * Hanya username + password.
 *
 * Kontrol:
 *   TAB / ENTER  → pindah field / submit
 *   F1           → toggle login ↔ register
 *   ESC          → lanjut sebagai Guest
 */
public class LoginScreen extends KeyAdapter {

    public enum Mode { LOGIN, REGISTER }

    private final GamePanel   panel;
    private final AuthManager auth;
    private Mode mode = Mode.LOGIN;

    // Input fields
    private final StringBuilder fUser = new StringBuilder();
    private final StringBuilder fPass = new StringBuilder();
    private int activeField = 0; // 0=username, 1=password

    // State
    private String  message    = "";
    private boolean msgSuccess = false;
    private int     msgTimer   = 0;
    private int     animTimer  = 0;
    private int     shakeTimer = 0;
    private int     cursorTick = 0;
    private boolean cursorOn   = true;

    public LoginScreen(GamePanel panel) {
        this.panel = panel;
        this.auth  = AuthManager.getInstance();
    }

    // ── Input ─────────────────────────────────────────────────
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_TAB -> activeField = activeField == 0 ? 1 : 0;
            case KeyEvent.VK_ENTER -> {
                if (activeField == 0 && fUser.length() > 0) activeField = 1;
                else submit();
            }
            case KeyEvent.VK_BACK_SPACE -> {
                StringBuilder f = activeField == 0 ? fUser : fPass;
                if (f.length() > 0) f.deleteCharAt(f.length() - 1);
            }
            case KeyEvent.VK_F1    -> toggleMode();
            case KeyEvent.VK_ESCAPE -> {
                if (mode == Mode.REGISTER) toggleMode();
                else goToMainMenu(0);
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (c < 32 || c > 126) return;
        StringBuilder f = activeField == 0 ? fUser : fPass;
        if (f.length() < 40) f.append(c);
    }

    private void toggleMode() {
        mode        = (mode == Mode.LOGIN) ? Mode.REGISTER : Mode.LOGIN;
        activeField = 0;
        fPass.setLength(0);
        message     = "";
    }

    // ── Submit ────────────────────────────────────────────────
    private void submit() {
        String user = fUser.toString().trim();
        String pass = fPass.toString();

        if (user.isEmpty()) { error("Masukkan username."); activeField = 0; return; }
        if (pass.isEmpty()) { error("Masukkan password."); activeField = 1; return; }

        if (mode == Mode.LOGIN) {
            LoginResult r = auth.login(user, pass);
            if (r == LoginResult.SUCCESS) {
                success("Selamat datang, " + auth.getUsername() + "!");
                goToMainMenu(900);
            } else {
                error(AuthManager.getLoginMessage(r));
                fPass.setLength(0); activeField = 1;
            }
        } else {
            RegisterResult r = auth.register(user, pass);
            if (r == RegisterResult.SUCCESS) {
                auth.login(user, pass);
                success("Akun dibuat! Selamat datang, " + user + "!");
                goToMainMenu(1000);
            } else {
                error(AuthManager.getRegisterMessage(r));
            }
        }
    }

    private void error(String msg) {
        message = msg; msgSuccess = false; msgTimer = 150; shakeTimer = 18;
    }

    private void success(String msg) {
        message = msg; msgSuccess = true; msgTimer = 120;
    }

    private void goToMainMenu(int delayMs) {
        if (delayMs <= 0) {
            panel.setState(GameState.MAIN_MENU);
            panel.requestFocusInWindow();
            return;
        }
        new Thread(() -> {
            try { Thread.sleep(delayMs); } catch (InterruptedException ignored) {}
            javax.swing.SwingUtilities.invokeLater(() -> {
                panel.setState(GameState.MAIN_MENU);
                panel.requestFocusInWindow();
            });
        }).start();
    }

    // ── Update ────────────────────────────────────────────────
    public void update() {
        animTimer++;
        if (msgTimer   > 0) msgTimer--;
        if (shakeTimer > 0) shakeTimer--;
        if (++cursorTick > 28) { cursorOn = !cursorOn; cursorTick = 0; }
    }

    // ── Draw ──────────────────────────────────────────────────
    public void draw(Graphics2D g2) {
        int W = GamePanel.SCREEN_W, H = GamePanel.SCREEN_H;

        // BG
        g2.setColor(new Color(0x02, 0x02, 0x10));
        g2.fillRect(0, 0, W, H);
        g2.setColor(new Color(0x00, 0xFF, 0xD5, 12));
        for (int x = 0; x < W; x += 48) g2.drawLine(x, 0, x, H);
        for (int y = 0; y < H; y += 48) g2.drawLine(0, y, W, y);
        g2.setColor(new Color(0, 0, 0, 20));
        for (int y = 0; y < H; y += 3) g2.drawLine(0, y, W, y);

        int sh = shakeTimer > 0 ? (int)(Math.random() * 6 - 3) : 0;

        // ── Panel ─────────────────────────────────────────────
        int pw = 440, ph = 310;
        int px = W / 2 - pw / 2 + sh, py = H / 2 - ph / 2;

        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(px + 8, py + 8, pw, ph, 18, 18);
        g2.setColor(new Color(0x04, 0x04, 0x1C, 248));
        g2.fillRoundRect(px, py, pw, ph, 18, 18);

        int ga = 60 + (int)(45 * Math.sin(animTimer * 0.06));
        g2.setColor(new Color(0x00, 0xFF, 0xD5, ga));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(px, py, pw, ph, 18, 18);
        g2.setStroke(new BasicStroke(1f));
        drawCorners(g2, px, py, pw, ph);

        // ── Title ─────────────────────────────────────────────
        g2.setFont(new Font("Monospaced", Font.BOLD, 24));
        String title = mode == Mode.LOGIN ? "[ JACK IN ]" : "[ NEW IDENTITY ]";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.setColor(CyberColors.UI_TITLE);
        g2.drawString(title, W / 2 - tw / 2 + sh, py + 44);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        String sub = mode == Mode.LOGIN
            ? "Enter the CyberKnight network"
            : "Register your neural ID";
        int sw = g2.getFontMetrics().stringWidth(sub);
        g2.setColor(new Color(0x44, 0x77, 0x99));
        g2.drawString(sub, W / 2 - sw / 2 + sh, py + 62);

        g2.setColor(new Color(0x00, 0xFF, 0xD5, 40));
        g2.drawLine(px + 28, py + 70, px + pw - 28, py + 70);

        // ── DB warning ────────────────────────────────────────
        if (!DatabaseManager.getInstance().isConnected()) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 10));
            g2.setColor(new Color(0xFF, 0x88, 0x00, 220));
            String warn = "! MySQL tidak terhubung — cek server & DB_PASS di DatabaseManager.java";
            int ww = g2.getFontMetrics().stringWidth(warn);
            g2.drawString(warn, W / 2 - ww / 2 + sh, py + 86);
        }

        // ── Fields ────────────────────────────────────────────
        int fy = py + 100, fw = pw - 60, fx = px + 30, fh = 38;
        drawField(g2, fx, fy, fw, fh, "USERNAME", fUser.toString(), activeField == 0, false, sh);
        fy += fh + 26;
        drawField(g2, fx, fy, fw, fh, "PASSWORD", fPass.toString(), activeField == 1, true, sh);
        fy += fh + 22;

        // ── Button ────────────────────────────────────────────
        String btn = mode == Mode.LOGIN ? ">>  LOGIN  <<" : ">>  REGISTER  <<";
        drawButton(g2, W / 2 + sh, fy + 17, btn);
        fy += 46;

        // ── Message ───────────────────────────────────────────
        if (msgTimer > 0 && !message.isEmpty()) {
            float a = Math.min(1f, msgTimer / 25f);
            Color mc = msgSuccess
                ? new Color(0x00, 0xFF, 0x88, (int)(a * 220))
                : new Color(0xFF, 0x44, 0x44, (int)(a * 220));
            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            int mw = g2.getFontMetrics().stringWidth(message);
            g2.setColor(mc);
            g2.drawString(message, W / 2 - mw / 2 + sh, fy + 6);
        }

        // ── Footer ────────────────────────────────────────────
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        String tog = mode == Mode.LOGIN
            ? "F1 — Belum punya akun?  Register"
            : "F1 — Sudah punya akun?  Login";
        g2.setColor(new Color(0x33, 0x55, 0x66));
        int tow = g2.getFontMetrics().stringWidth(tog);
        g2.drawString(tog, W / 2 - tow / 2, py + ph - 26);

        String esc = mode == Mode.LOGIN ? "ESC — Lanjut sebagai Guest" : "ESC — Kembali ke Login";
        g2.setColor(new Color(0x22, 0x33, 0x44));
        int ew = g2.getFontMetrics().stringWidth(esc);
        g2.drawString(esc, W / 2 - ew / 2, py + ph - 12);
    }

    private void drawField(Graphics2D g2, int x, int y, int w, int h,
                           String label, String val, boolean active, boolean masked, int sh) {
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        g2.setColor(active ? CyberColors.NEON_CYAN : new Color(0x33, 0x66, 0x77));
        g2.drawString(label, x + sh, y - 5);

        g2.setColor(active ? new Color(0x00, 0x16, 0x20) : new Color(0x06, 0x06, 0x14));
        g2.fillRoundRect(x + sh, y, w, h, 8, 8);
        g2.setColor(active ? CyberColors.NEON_CYAN : new Color(0x1A, 0x33, 0x44));
        g2.setStroke(new BasicStroke(active ? 1.5f : 1f));
        g2.drawRoundRect(x + sh, y, w, h, 8, 8);
        g2.setStroke(new BasicStroke(1f));

        String show = masked ? "•".repeat(val.length()) : val;
        if (active && cursorOn) show += "|";
        g2.setFont(new Font("Monospaced", Font.PLAIN, 15));
        FontMetrics fm = g2.getFontMetrics();
        while (fm.stringWidth(show) > w - 18 && show.length() > 1)
            show = show.substring(1);
        g2.setColor(active ? Color.WHITE : new Color(0x88, 0xAA, 0xBB));
        g2.drawString(show, x + 10 + sh, y + h / 2 + 6);
    }

    private void drawButton(Graphics2D g2, int cx, int cy, String text) {
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        int tw = g2.getFontMetrics().stringWidth(text);
        int bw = tw + 44, bh = 34;
        int bx = cx - bw / 2, by = cy - bh / 2;

        g2.setColor(new Color(0x00, 0xFF, 0xD5, 35 + (int)(28 * Math.sin(animTimer * 0.09))));
        g2.fillRoundRect(bx - 4, by - 4, bw + 8, bh + 8, 12, 12);
        g2.setColor(new Color(0x00, 0x2A, 0x38));
        g2.fillRoundRect(bx, by, bw, bh, 8, 8);
        g2.setColor(CyberColors.NEON_CYAN);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(bx, by, bw, bh, 8, 8);
        g2.setStroke(new BasicStroke(1f));
        g2.drawString(text, cx - tw / 2, cy + 5);
    }

    private void drawCorners(Graphics2D g2, int x, int y, int w, int h) {
        int s = 13;
        g2.setColor(CyberColors.NEON_CYAN);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(x,     y+s,   x,     y);     g2.drawLine(x,     y, x+s,   y);
        g2.drawLine(x+w-s, y,     x+w,   y);     g2.drawLine(x+w,   y, x+w,   y+s);
        g2.drawLine(x,     y+h-s, x,     y+h);   g2.drawLine(x,     y+h, x+s, y+h);
        g2.drawLine(x+w-s, y+h,   x+w,   y+h);   g2.drawLine(x+w, y+h-s, x+w, y+h);
        g2.setStroke(new BasicStroke(1f));
    }
}