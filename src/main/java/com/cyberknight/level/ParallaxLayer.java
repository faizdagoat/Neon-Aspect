package com.cyberknight.level;

import java.awt.*;
import java.util.Random;

/**
 * Procedurally drawn parallax background layer.
 * Each layer has a scroll factor and draws cyberpunk city elements.
 */
public class ParallaxLayer {

    private float scrollFactor;
    private Color color;
    private int   layerType; // 0=stars, 1=buildings, 2=grid
    private int[] data; // random seed data

    public ParallaxLayer(float scrollFactor, Color color, int layerType, long seed) {
        this.scrollFactor = scrollFactor;
        this.color        = color;
        this.layerType    = layerType;
        Random rng = new Random(seed);
        data = new int[80];
        for (int i = 0; i < data.length; i++) data[i] = rng.nextInt(1000);
    }

    public void draw(Graphics2D g2, int camX, int camY, int screenW, int screenH) {
        int offsetX = (int)(camX * scrollFactor);
        switch (layerType) {
            case 0 -> drawStars(g2, offsetX, screenW, screenH);
            case 1 -> drawBuildings(g2, offsetX, screenW, screenH);
            case 2 -> drawGrid(g2, offsetX, camY, screenW, screenH);
        }
    }

    private void drawStars(Graphics2D g2, int offsetX, int w, int h) {
        for (int i = 0; i < 40; i++) {
            // Safe index access - data has 80 elements, i max=39 so i*2+1 max=79 - OK
            int raw = data[i * 2] * 13 + i * 97; // spread positions more evenly
            int sx  = ((raw - offsetX / 2) % w + w) % w; // always positive
            int sy  = ((data[i * 2 + 1] * 7 + i * 53) % h + h) % h;
            int size  = (data[i] % 3) + 1;
            int alpha = 80 + (data[i] % 120);
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            g2.fillOval(sx, sy, size, size);
        }
    }

    private void drawBuildings(Graphics2D g2, int offsetX, int w, int h) {
        int count = 12;
        int segW  = w / count + 60;
        for (int i = 0; i < count + 2; i++) {
            int bh   = 80 + data[i % data.length] % 220;
            int bw   = 40 + data[(i + 1) % data.length] % 80;
            int bx   = (i * segW - offsetX % (w + segW * 2) + w * 2) % (w + segW * 2) - segW;
            int by   = h - bh;
            int alpha= 60 + data[(i + 2) % data.length] % 80;

            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            g2.fillRect(bx, by, bw, bh);

            // Windows
            g2.setColor(new Color(0xFF, 0xFF, 0x88, 40 + (data[i] % 40)));
            int wRows = bh / 16;
            int wCols = bw / 10;
            for (int wr = 0; wr < wRows; wr++)
                for (int wc = 0; wc < wCols; wc++)
                    if ((data[(i + wr + wc) % data.length] % 3) != 0)
                        g2.fillRect(bx + 3 + wc * 10, by + 4 + wr * 16, 5, 7);

            // Antenna
            if (data[i % data.length] % 2 == 0) {
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 120));
                g2.fillRect(bx + bw / 2, by - 20, 2, 20);
                // blinking light
                long t = System.currentTimeMillis();
                if ((t / 400 + i) % 2 == 0) {
                    g2.setColor(new Color(0xFF, 0x22, 0x22, 200));
                    g2.fillOval(bx + bw / 2 - 2, by - 24, 6, 6);
                }
            }
        }
    }

    private void drawGrid(Graphics2D g2, int offsetX, int camY, int w, int h) {
        int gridSize = 64;
        int alpha    = 18;
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        // Vertical lines
        int startX = -(offsetX % gridSize);
        for (int lx = startX; lx < w; lx += gridSize)
            g2.drawLine(lx, 0, lx, h);
        // Horizontal lines
        int startY = -(camY % gridSize);
        for (int ly = startY; ly < h; ly += gridSize)
            g2.drawLine(0, ly, w, ly);
    }
}