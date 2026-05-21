package com.cyberknight.ui;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;

/**
 * CutscenePlayer – memutar video MP4 sebagai cutscene.
 *
 * Cara pakai:
 *   Taruh file video di: src/main/resources/cutscene/intro.mp4
 *
 * Flow:
 *   Video diputar fullscreen di atas JFrame.
 *   Setelah video selesai (atau SPACE/ENTER ditekan) → onFinished dipanggil.
 *
 * Integrasi JavaFX + Swing menggunakan JFXPanel.
 */
public class CutscenePlayer extends JPanel {

    private JFXPanel    fxPanel;
    private MediaPlayer mediaPlayer;
    private boolean     finished   = false;
    private Runnable    onFinished;

    // Overlay teks "tekan SPACE untuk skip"
    private int         animTimer  = 0;

    public CutscenePlayer(int width, int height, Runnable onFinished) {
        this.onFinished = onFinished;
        setPreferredSize(new Dimension(width, height));
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // JFXPanel adalah bridge Swing ↔ JavaFX
        fxPanel = new JFXPanel();
        fxPanel.setPreferredSize(new Dimension(width, height));
        add(fxPanel, BorderLayout.CENTER);

        // Setup harus dilakukan di JavaFX Application Thread
        Platform.runLater(() -> setupVideo(width, height));

        // Key listener untuk skip
        setFocusable(true);
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int k = e.getKeyCode();
                if (k == java.awt.event.KeyEvent.VK_SPACE ||
                    k == java.awt.event.KeyEvent.VK_ENTER ||
                    k == java.awt.event.KeyEvent.VK_ESCAPE) {
                    skip();
                }
            }
        });
    }

    private void setupVideo(int width, int height) {
        // Cari file video
        String videoPath = getVideoPath();

        if (videoPath == null) {
            // Tidak ada video – langsung skip ke game
            SwingUtilities.invokeLater(this::finish);
            return;
        }

        try {
            Media       media  = new Media(videoPath);
            mediaPlayer        = new MediaPlayer(media);
            MediaView   view   = new MediaView(mediaPlayer);

            // Fit ke ukuran panel
            view.setFitWidth(width);
            view.setFitHeight(height);
            view.setPreserveRatio(true);

            StackPane root = new StackPane(view);
            root.setStyle("-fx-background-color: black;");

            Scene scene = new Scene(root, width, height);
            fxPanel.setScene(scene);

            // Event saat video selesai
            mediaPlayer.setOnEndOfMedia(() ->
                SwingUtilities.invokeLater(this::finish)
            );

            // Event error – jika video tidak bisa diload, skip saja
            mediaPlayer.setOnError(() ->
                SwingUtilities.invokeLater(this::finish)
            );

            mediaPlayer.play();

        } catch (Exception e) {
            SwingUtilities.invokeLater(this::finish);
        }
    }

    /**
     * Cari file video di beberapa lokasi:
     * 1. src/main/resources/cutscene/intro.mp4
     * 2. resources/cutscene/intro.mp4
     * 3. intro.mp4 di direktori jar
     */
    private String getVideoPath() {
        // Coba dari resources (JAR)
        URL url = getClass().getClassLoader().getResource("cutscene/intro.mp4");
        if (url != null) return url.toExternalForm();

        // Coba dari filesystem relatif
        String[] paths = {
            "src/main/resources/cutscene/intro.mp4",
            "resources/cutscene/intro.mp4",
            "intro.mp4"
        };
        for (String path : paths) {
            File f = new File(path);
            if (f.exists()) return f.toURI().toString();
        }
        return null;
    }

    /** Skip video dan lanjut ke game */
    public void skip() {
        if (finished) return;
        if (mediaPlayer != null) {
            Platform.runLater(() -> mediaPlayer.stop());
        }
        finish();
    }

    private void finish() {
        if (finished) return;
        finished = true;
        if (mediaPlayer != null) {
            Platform.runLater(() -> {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            });
        }
        if (onFinished != null) onFinished.run();
    }

    /** Update animTimer untuk skip hint */
    public void update() { animTimer++; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Gambar hint "SKIP" di pojok kanan bawah
        if (animTimer > 60) {
            Graphics2D g2 = (Graphics2D) g;
            int alpha = 120 + (int)(80 * Math.sin(animTimer * 0.06));
            g2.setFont(new Font("Monospaced", Font.PLAIN, 13));
            g2.setColor(new Color(0x00, 0xFF, 0xD5, Math.min(255, alpha)));
            String hint = "SPACE / ENTER — Skip";
            int tw = g2.getFontMetrics().stringWidth(hint);
            g2.drawString(hint, getWidth() - tw - 20, getHeight() - 20);
        }
    }

    public boolean isFinished() { return finished; }
}