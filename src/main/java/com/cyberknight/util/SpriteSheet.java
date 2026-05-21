package com.cyberknight.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * SpriteSheet – memuat horizontal strip sprite sheet dan potong per frame.
 *
 * Semua frame diasumsikan lebar = tinggi (square frame).
 * Contoh: idle.png 768x96 → 8 frame masing-masing 96x96
 */
public class SpriteSheet {

    private BufferedImage[] frames;
    private int frameCount;
    private int frameW, frameH;

    public SpriteSheet(String resourcePath) {
        load(resourcePath);
    }

    private void load(String path) {
        try {
            URL url = getClass().getClassLoader().getResource(path);
            if (url == null) {
                System.err.println("[SpriteSheet] Not found: " + path);
                createFallback();
                return;
            }
            BufferedImage sheet = ImageIO.read(url);
            frameH = sheet.getHeight();
            frameW = frameH; // frame selalu square
            frameCount = sheet.getWidth() / frameW;
            frames = new BufferedImage[frameCount];

            for (int i = 0; i < frameCount; i++) {
                frames[i] = sheet.getSubimage(i * frameW, 0, frameW, frameH);
            }
        } catch (IOException e) {
            System.err.println("[SpriteSheet] Error loading: " + path);
            createFallback();
        }
    }

    /** Buat frame placeholder jika asset tidak ditemukan */
    private void createFallback() {
        frameW = 96; frameH = 96; frameCount = 1;
        frames = new BufferedImage[1];
        frames[0] = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = frames[0].createGraphics();
        g.setColor(new Color(0x00, 0xFF, 0xD5, 120));
        g.fillRect(0, 0, 96, 96);
        g.setColor(Color.WHITE);
        g.drawRect(0, 0, 95, 95);
        g.dispose();
    }

    public BufferedImage getFrame(int index) {
        if (frames == null || frames.length == 0) return null;
        return frames[Math.max(0, Math.min(index, frames.length - 1))];
    }

    public int getFrameCount() { return frameCount; }
    public int getFrameW()     { return frameW; }
    public int getFrameH()     { return frameH; }
}