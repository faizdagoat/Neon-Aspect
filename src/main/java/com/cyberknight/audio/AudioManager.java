package com.cyberknight.audio;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;

/**
 * AudioManager – mengelola background musik game.
 *
 * Fix:
 *  - Resource loading dilakukan di main thread, URL di-pass ke JavaFX thread
 *  - Semua akses MediaPlayer dijaga agar hanya di JavaFX Application Thread
 *  - Fade menggunakan Platform.runLater() yang aman
 */
public class AudioManager {

    private static AudioManager instance;

    private MediaPlayer currentPlayer;
    private String      currentTrack  = "";
    private double      masterVolume  = 0.7;
    private boolean     muted         = false;

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
     * Putar track baru. Resolve URL di calling thread, lalu execute di FX thread.
     * Jika track sama sudah bermain, tidak restart.
     */
    public void play(String trackPath) {
        if (trackPath == null || trackPath.isEmpty()) { stop(); return; }
        if (trackPath.equals(currentTrack) && isPlaying()) return;

        currentTrack = trackPath;

        // Resolve URL di thread ini (bukan FX thread) agar classloader benar
        URL url = resolveURL(trackPath);
        if (url == null) {
            System.err.println("[Audio] File tidak ditemukan: " + trackPath);
            return;
        }

        final String urlStr = url.toExternalForm();
        Platform.runLater(() -> startNewTrack(urlStr));
    }

    private URL resolveURL(String path) {
        // Coba classloader
        URL url = getClass().getClassLoader().getResource(path);
        if (url != null) return url;
        // Coba dengan leading slash
        url = getClass().getResource("/" + path);
        return url;
    }

    private void startNewTrack(String urlString) {
        // Harus di FX thread
        try {
            // Stop dan dispose player lama dulu
            if (currentPlayer != null) {
                currentPlayer.stop();
                currentPlayer.dispose();
                currentPlayer = null;
            }

            Media media = new Media(urlString);
            MediaPlayer player = new MediaPlayer(media);

            player.setCycleCount(MediaPlayer.INDEFINITE); // loop
            player.setVolume(0); // mulai dari 0 untuk fade in
            player.setOnError(() ->
                System.err.println("[Audio] MediaPlayer error: " + player.getError())
            );

            player.setOnReady(() -> {
                player.play();
                // Fade in setelah player siap
                fadeIn(player);
            });

            currentPlayer = player;

        } catch (Exception e) {
            System.err.println("[Audio] startNewTrack error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Stop / Pause / Resume ─────────────────────────────────

    public void stop() {
        currentTrack = "";
        Platform.runLater(() -> {
            if (currentPlayer != null) {
                currentPlayer.stop();
                currentPlayer.dispose();
                currentPlayer = null;
            }
        });
    }

    public void pause() {
        Platform.runLater(() -> {
            if (currentPlayer != null) currentPlayer.pause();
        });
    }

    public void resume() {
        Platform.runLater(() -> {
            if (currentPlayer != null) currentPlayer.play();
        });
    }

    public boolean isPlaying() {
        if (currentPlayer == null) return false;
        try {
            return currentPlayer.getStatus() == MediaPlayer.Status.PLAYING;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Volume ────────────────────────────────────────────────

    public void setVolume(double vol) {
        masterVolume = Math.max(0.0, Math.min(1.0, vol));
        if (!muted) {
            double v = masterVolume;
            Platform.runLater(() -> {
                if (currentPlayer != null) currentPlayer.setVolume(v);
            });
        }
    }

    public void toggleMute() {
        muted = !muted;
        double v = muted ? 0 : masterVolume;
        Platform.runLater(() -> {
            if (currentPlayer != null) currentPlayer.setVolume(v);
        });
    }

    public boolean isMuted()       { return muted; }
    public double getMasterVolume() { return masterVolume; }

    // ── Fade ──────────────────────────────────────────────────

    /**
     * Fade in: naikkan volume dari 0 ke masterVolume secara bertahap.
     * Menggunakan Timeline-style via recursive Platform.runLater.
     */
    private void fadeIn(MediaPlayer player) {
        fadeStep(player, 0, 30);
    }

    private void fadeStep(MediaPlayer player, int step, int totalSteps) {
        if (player != currentPlayer) return; // player sudah diganti, batalkan
        if (step > totalSteps) {
            player.setVolume(muted ? 0 : masterVolume);
            return;
        }
        double vol = (step / (double) totalSteps) * (muted ? 0 : masterVolume);
        player.setVolume(vol);

        // Jadwalkan step berikutnya setelah ~30ms menggunakan Thread + Platform.runLater
        Thread t = new Thread(() -> {
            try { Thread.sleep(30); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> fadeStep(player, step + 1, totalSteps));
        });
        t.setDaemon(true);
        t.start();
    }

    public void dispose() {
        Platform.runLater(() -> {
            if (currentPlayer != null) {
                currentPlayer.stop();
                currentPlayer.dispose();
                currentPlayer = null;
            }
        });
    }
}
