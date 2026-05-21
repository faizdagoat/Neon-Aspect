package com.cyberknight.core;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * InputHandler – semua keyboard input sebagai boolean flags.
 *
 * Fix bug pause:
 *  - pausePressed = single-shot flag untuk MEMBUKA pause (di-set di keyPressed)
 *  - escapePressed = single-shot flag untuk RESUME dari pause (di-set di keyReleased)
 *  - Dengan begitu, satu penekanan ESC tidak bisa membuka DAN menutup pause sekaligus
 */
public class InputHandler extends KeyAdapter {

    // Movement
    public boolean left, right, up, down;

    // Actions
    public boolean jump, jumpHeld;
    public boolean attack, attackUp, attackDown;
    public boolean dash, heal;

    // UI
    public boolean enter;
    public boolean mutePressed;
    public boolean anyKeyPressed;

    // Pause flags — TERPISAH agar tidak saling consume dalam satu key event
    // pausePressed : set di keyPRESSED  → digunakan untuk MEMBUKA pause
    // escapePressed: set di keyRELEASED → digunakan untuk MENUTUP pause (resume)
    public boolean pausePressed;
    public boolean escapePressed;

    // held state ESC (untuk kebutuhan lain jika diperlukan)
    public boolean escape;

    @Override
    public void keyPressed(KeyEvent e) {
        anyKeyPressed = true;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT,  KeyEvent.VK_A -> left   = true;
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> right  = true;
            case KeyEvent.VK_UP,    KeyEvent.VK_W -> up     = true;
            case KeyEvent.VK_DOWN,  KeyEvent.VK_S -> down   = true;
            case KeyEvent.VK_SPACE, KeyEvent.VK_Z -> { jump = true; jumpHeld = true; }
            case KeyEvent.VK_X,     KeyEvent.VK_J -> attack = true;
            case KeyEvent.VK_C,     KeyEvent.VK_K -> dash   = true;
            case KeyEvent.VK_V,     KeyEvent.VK_L -> heal   = true;
            case KeyEvent.VK_ENTER                -> enter  = true;
            case KeyEvent.VK_M                    -> mutePressed = true;
            case KeyEvent.VK_ESCAPE -> {
                // Set pausePressed hanya pada penekanan pertama (bukan key repeat)
                if (!escape) {
                    pausePressed = true;
                }
                escape = true;
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_X && up)   attackUp   = true;
        if (e.getKeyCode() == KeyEvent.VK_X && down)  attackDown = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        anyKeyPressed = false;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT,  KeyEvent.VK_A -> left   = false;
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> right  = false;
            case KeyEvent.VK_UP,    KeyEvent.VK_W -> up     = false;
            case KeyEvent.VK_DOWN,  KeyEvent.VK_S -> down   = false;
            case KeyEvent.VK_SPACE, KeyEvent.VK_Z -> { jump = false; jumpHeld = false; }
            case KeyEvent.VK_X,     KeyEvent.VK_J -> { attack = false; attackUp = false; attackDown = false; }
            case KeyEvent.VK_C,     KeyEvent.VK_K -> dash   = false;
            case KeyEvent.VK_V,     KeyEvent.VK_L -> heal   = false;
            case KeyEvent.VK_ENTER                -> enter  = false;
            case KeyEvent.VK_ESCAPE -> {
                // Set escapePressed saat key DILEPAS — ini untuk resume di PauseScreen
                // Dengan begitu ESC yang membuka pause (keyPressed) tidak bisa
                // sekaligus menutup pause (keyReleased belum terpanggil)
                escapePressed = true;
                escape = false;
            }
        }
    }

    public boolean consumeJump()   { if (jump)         { jump         = false; return true; } return false; }
    public boolean consumeAttack() { if (attack)       { attack       = false; return true; } return false; }
    public boolean consumeDash()   { if (dash)         { dash         = false; return true; } return false; }
    public boolean consumeHeal()   { if (heal)         { heal         = false; return true; } return false; }
    public boolean consumeEnter()  { if (enter)        { enter        = false; return true; } return false; }
    public boolean consumeMute()   { if (mutePressed)  { mutePressed  = false; return true; } return false; }

    /** Consume ESC sebagai aksi resume/back — hanya dari keyReleased */
    public boolean consumeEscape() {
        if (escapePressed) { escapePressed = false; return true; }
        return false;
    }

    /** Consume pause toggle — hanya dipanggil oleh checkPause() di GamePanel */
    public boolean consumePause() {
        if (pausePressed) { pausePressed = false; return true; }
        return false;
    }

    public void resetAll() {
        left = right = up = down = false;
        jump = jumpHeld = false;
        attack = attackUp = attackDown = false;
        dash = heal = false;
        enter = escape = pausePressed = escapePressed = mutePressed = false;
        anyKeyPressed = false;
    }
}
