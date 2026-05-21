package com.cyberknight.core;

import com.cyberknight.audio.AudioManager;
import com.cyberknight.auth.AuthManager;
import com.cyberknight.entity.Player;
import com.cyberknight.level.LevelManager;
import com.cyberknight.ui.CutscenePlayer;
import com.cyberknight.ui.HUD;
import com.cyberknight.ui.LoginScreen;
import com.cyberknight.ui.MainMenu;
import com.cyberknight.ui.GameOverScreen;
import com.cyberknight.ui.VictoryScreen;
import com.cyberknight.ui.PauseScreen;

import javafx.application.Platform;
import javax.swing.*;
import java.awt.*;

/**
 * GamePanel is the main JPanel. It runs the game loop (60 FPS).
 * Flow: CUTSCENE → LOGIN → MAIN_MENU → PLAYING → ...
 */
public class GamePanel extends JPanel implements Runnable {

    public static final int FPS       = 60;
    public static final int TILE_SIZE = GameWindow.TILE_SIZE;
    public static final int SCREEN_W  = GameWindow.SCREEN_W;
    public static final int SCREEN_H  = GameWindow.SCREEN_H;

    private Thread          gameThread;
    private GameState       state;
    private InputHandler    input;
    private Player          player;
    private LevelManager    levelManager;
    private HUD             hud;
    private LoginScreen     loginScreen;
    private MainMenu        mainMenu;
    private GameOverScreen  gameOverScreen;
    private VictoryScreen   victoryScreen;
    private PauseScreen     pauseScreen;
    private Camera          camera;
    private CutscenePlayer  cutscene;

    private float    fadeAlpha = 1f;
    private boolean  fadingIn  = true;
    private boolean  fadingOut = false;
    private Runnable afterFade = null;

    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_W, SCREEN_H));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);
        setLayout(null);

        try { Platform.startup(() -> {}); } catch (Exception ignored) {}

        input = new InputHandler();
        addKeyListener(input);

        // Init login screen dan tambahkan key listener-nya
        loginScreen    = new LoginScreen(this);
        addKeyListener(loginScreen);

        mainMenu       = new MainMenu(this);
        gameOverScreen = new GameOverScreen(this);
        victoryScreen  = new VictoryScreen(this);
        pauseScreen    = new PauseScreen(this);

        startCutscene();
    }

    // ────────────────────────────────────────────────────────────
    //  PUBLIC API
    // ────────────────────────────────────────────────────────────

    public void startGameLoop() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    /** Mulai cutscene intro — dipanggil saat konstruktor */
    private void startCutscene() {
        state    = GameState.CUTSCENE;
        cutscene = new CutscenePlayer(SCREEN_W, SCREEN_H, () -> {
            SwingUtilities.invokeLater(() -> {
                remove(cutscene);
                cutscene = null;
                revalidate();
                repaint();
                // Setelah cutscene → layar LOGIN
                state = GameState.LOGIN;
                fadeIn();
                requestFocusInWindow();
            });
        });
        cutscene.setBounds(0, 0, SCREEN_W, SCREEN_H);
        add(cutscene);
        revalidate();
        SwingUtilities.invokeLater(() -> cutscene.requestFocusInWindow());
    }

    /** Called from MainMenu / GameOver to start / restart a fresh game. */
    public void startNewGame() {
        input.resetAll();
        player       = new Player(100, 384, input);
        camera       = new Camera(SCREEN_W, SCREEN_H);
        levelManager = new LevelManager(this, player, camera);
        hud          = new HUD(player, levelManager);
        levelManager.loadLevel(1);
        state = GameState.PLAYING;
        fadeIn();
    }

    public void nextLevel() {
        int next = levelManager.getCurrentLevelIndex() + 1;
        if (next > 5) {
            fadeOutThen(() -> {
                input.resetAll();
                AudioManager.getInstance().stop(); // stop musik saat victory
                setState(GameState.VICTORY);
                fadeIn();
            });
        } else {
            fadeOutThen(() -> transitionToLevel(next));
        }
    }

    /** Helper dipanggil dari lambda — menghindari capture non-final field. */
    private void transitionToLevel(int index) {
        input.resetAll();
        levelManager.loadLevel(index);   // loadLevel sudah reset posisi player di dalamnya
        state = GameState.PLAYING;
        fadeIn();
    }

    public void playerDied() {
        AudioManager.getInstance().stop(); // stop musik saat mati
        fadeOutThen(() -> {
            input.resetAll();
            setState(GameState.GAME_OVER);
            fadeIn();
        });
    }

    public void setState(GameState s) { this.state = s; }
    public GameState getState()       { return state; }
    public InputHandler getInput()    { return input; }
    public Player getPlayer()         { return player; }
    public LevelManager getLevelManager() { return levelManager; }

    // ────────────────────────────────────────────────────────────
    //  GAME LOOP
    // ────────────────────────────────────────────────────────────

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double delta = 0;
        long   last  = System.nanoTime();

        while (gameThread != null) {
            long now = System.nanoTime();
            delta += (now - last) / drawInterval;
            last   = now;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update() {
        updateFade();

        switch (state) {
            case CUTSCENE      -> { if (cutscene != null) cutscene.update(); }
            case LOGIN         -> loginScreen.update();
            case MAIN_MENU     -> mainMenu.update(input);
            case PLAYING       -> {
                levelManager.update();
                camera.update(player, levelManager.getCurrentLevel());
                hud.update();
                checkPause();
            }
            case PAUSED        -> pauseScreen.update(input);
            case GAME_OVER     -> gameOverScreen.update(input);
            case VICTORY       -> victoryScreen.update(input);
            case LEVEL_COMPLETE-> {}
            case BOSS_INTRO    -> {}
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (state == GameState.CUTSCENE) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (state) {
            case LOGIN      -> loginScreen.draw(g2);
            case MAIN_MENU  -> mainMenu.draw(g2);
            case PLAYING    -> {
                levelManager.draw(g2);
                hud.draw(g2);
            }
            case PAUSED     -> {
                levelManager.draw(g2);
                hud.draw(g2);
                pauseScreen.draw(g2);
            }
            case GAME_OVER  -> gameOverScreen.draw(g2);
            case VICTORY    -> victoryScreen.draw(g2);
            default         -> {}
        }

        drawFadeOverlay(g2);
        g2.dispose();
    }

    // ────────────────────────────────────────────────────────────
    //  HELPERS
    // ────────────────────────────────────────────────────────────

    private void checkPause() {
        if (input.consumePause()) {
            // JANGAN reset escape atau flag lain di sini
            // PauseScreen akan handle consumeEscape() sendiri saat state sudah PAUSED
            state = GameState.PAUSED;
        }
    }

    private void fadeIn() {
        fadeAlpha = 1f; fadingIn = true; fadingOut = false;
    }

    private void fadeOutThen(Runnable after) {
        // Guard: ignore if already fading out
        if (fadingOut) return;
        fadingIn  = false;
        fadingOut = true;
        // Start fade from current alpha (not 0!) so it always reaches 1f
        // fadeAlpha keeps its current value and counts UP to 1f
        afterFade = after;
    }

    private void updateFade() {
        if (fadingIn) {
            fadeAlpha = Math.max(0f, fadeAlpha - 0.04f);
            if (fadeAlpha <= 0f) { fadeAlpha = 0f; fadingIn = false; }
        }
        if (fadingOut) {
            fadeAlpha = Math.min(1f, fadeAlpha + 0.04f);
            if (fadeAlpha >= 1f) {
                fadeAlpha = 1f;
                fadingOut = false;
                if (afterFade != null) {
                    Runnable cb = afterFade;
                    afterFade = null;
                    cb.run();
                }
            }
        }
    }

    private void drawFadeOverlay(Graphics2D g2) {
        if (fadeAlpha > 0f) {
            g2.setColor(new Color(0f, 0f, 0f, fadeAlpha));
            g2.fillRect(0, 0, SCREEN_W, SCREEN_H);
        }
    }
}