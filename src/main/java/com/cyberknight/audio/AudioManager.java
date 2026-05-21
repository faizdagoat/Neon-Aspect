package com.cyberknight.audio;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;

/**
 * AudioManager – mengelola background musik game.
 *
 * Fitur:
 *  - Play musik dengan loop otomatis
 *  - Fade in / fade out saat ganti musik
 *  - Volume control
 *  - Stop / pause / resume
 *
 * Menggunakan JavaFX MediaPlayer (sudah ada di dependency).
 *
 * File musik taruh di: src/main/resources/audio/
 *   level_music.mp3  → Level 1, 2, 3
 *   boss1_music.mp3  → Boss NeonWraith  (opsional)
 *   boss2_music.mp3  → Boss Colossus Prime (opsional)
 */
public class AudioManager {

    private static AudioManager instance;

    private MediaPlayer currentPlayer;
    private String      currentTrack  = "";
    private float       masterVolume  = 0.7f;
    private boolean     muted         = false;

    // Fade
    private Thread fadeThread;

    // Track keys
    public static final String TRACK_LEVEL = "audio/level_music.mp3";
    public static final String TRACK_BOSS1 = "audio/boss1_music.mp3";
    public static final String TRACK_BOSS2 = "audio/boss2_music.mp3";
    public static final String TRACK_NONE  = "";

    private AudioManager() {}

    public static AudioManager getInstance() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    // ── Play ──────────────────────────────────────────────────

    /**
     * Putar track baru. Jika track sama sudah bermain, tidak restart.
     * Fade out track lama, fade in track baru.
     */
    public void play(String trackPath) {
        if (trackPath == null || trackPath.isEmpty()) { stop(); return; }
        if (trackPath.equals(currentTrack) && isPlaying()) return;

        currentTrack = trackPath;

        // Fade out track lama dulu, lalu play baru
        if (currentPlayer != null) {
            fadeOutThen(() -> startNewTrack(trackPath));
        } else {
            Platform.runLater(() -> startNewTrack(trackPath));
        }
    }

    private void startNewTrack(String path) {
        URL url = getClass().getClassLoader().getResource(path);
        if (url == null) {
            System.err.println("[Audio] File tidak ditemukan: " + path);
            // Coba file fallback
            url = getClass().getClassLoader().getResource(TRACK_LEVEL);
            if (url == null) return;
        }

        try {
            Media media = new Media(url.toExternalForm());
            MediaPlayer player = new MediaPlayer(media);

            player.setVolume(muted ? 0 : masterVolume);
            player.setCycleCount(MediaPlayer.INDEFINITE); // loop selamanya
            player.setOnError(() ->
                System.err.println("[Audio] Error: " + player.getError())
            );

            // Fade in dari volume 0
            player.setVolume(0);
            player.play();

            // Simpan reference
            if (currentPlayer != null) {
                currentPlayer.stop();
                currentPlayer.dispose();
            }
            currentPlayer = player;

            // Fade in
            fadeIn();

        } catch (Exception e) {
            System.err.println("[Audio] startNewTrack error: " + e.getMessage());
        }
    }

    // ── Stop / Pause / Resume ─────────────────────────────────

    public void stop() {
        currentTrack = "";
        if (currentPlayer != null) {
            fadeOutThen(() -> {
                currentPlayer.stop();
                currentPlayer.dispose();
                currentPlayer = null;
            });
        }
    }

    public void pause() {
        if (currentPlayer != null) Platform.runLater(() -> currentPlayer.pause());
    }

    public void resume() {
        if (currentPlayer != null) Platform.runLater(() -> currentPlayer.play());
    }

    public boolean isPlaying() {
        if (currentPlayer == null) return false;
        return currentPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    // ── Volume ────────────────────────────────────────────────

    public void setVolume(float vol) {
        masterVolume = Math.max(0f, Math.min(1f, vol));
        if (currentPlayer != null && !muted) {
            float v = masterVolume;
            Platform.runLater(() -> currentPlayer.setVolume(v));
        }
    }

    public void toggleMute() {
        muted = !muted;
        if (currentPlayer != null) {
            double v = muted ? 0 : masterVolume;
            Platform.runLater(() -> currentPlayer.setVolume(v));
        }
    }

    public boolean isMuted()       { return muted; }
    public float getMasterVolume() { return masterVolume; }

    // ── Fade ──────────────────────────────────────────────────

    private void fadeIn() {
        if (currentPlayer == null) return;
        cancelFade();
        fadeThread = new Thread(() -> {
            try {
                double target = muted ? 0 : masterVolume;
                for (int i = 0; i <= 30; i++) {
                    double vol = (i / 30.0) * target;
                    MediaPlayer p = currentPlayer;
                    if (p == null) break;
                    Platform.runLater(() -> { if (currentPlayer != null) currentPlayer.setVolume(vol); });
                    Thread.sleep(30);
                }
            } catch (InterruptedException ignored) {}
        });
        fadeThread.setDaemon(true);
        fadeThread.start();
    }

    private void fadeOutThen(Runnable after) {
        if (currentPlayer == null) { if (after != null) Platform.runLater(after); return; }
        cancelFade();
        MediaPlayer fadingPlayer = currentPlayer;
        fadeThread = new Thread(() -> {
            try {
                double startVol = fadingPlayer.getVolume();
                for (int i = 30; i >= 0; i--) {
                    double vol = (i / 30.0) * startVol;
                    Platform.runLater(() -> fadingPlayer.setVolume(vol));
                    Thread.sleep(25);
                }
                Platform.runLater(() -> {
                    fadingPlayer.stop();
                    fadingPlayer.dispose();
                    if (after != null) after.run();
                });
            } catch (InterruptedException ignored) {}
        });
        fadeThread.setDaemon(true);
        fadeThread.start();
    }

    private void cancelFade() {
        if (fadeThread != null && fadeThread.isAlive()) fadeThread.interrupt();
    }

    public void dispose() {
        cancelFade();
        if (currentPlayer != null) {
            Platform.runLater(() -> {
                currentPlayer.stop();
                currentPlayer.dispose();
                currentPlayer = null;
            });
        }
    }
}
