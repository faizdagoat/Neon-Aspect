package com.cyberknight.level;

import com.cyberknight.entity.enemy.Enemy;
import com.cyberknight.util.CyberColors;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A Level stores:
 *  - A tile map (int[][])
 *  - Solid collision rectangles
 *  - Enemy spawn list
 *  - Goal / exit position
 *  - Parallax background layers
 *
 * Tile codes:
 *   0 = air
 *   1 = solid ground
 *   2 = solid platform (one-way – not yet used)
 *   3 = spike (kills player)
 *   4 = goal (exit portal)
 *   5 = decoration (neon sign)
 */
public class Level {

    public static final int T = 48; // tile size

    private int[][]       tileMap;
    private int           cols, rows;
    private List<Rectangle> solidTiles = new ArrayList<>();
    private List<Rectangle> spikes     = new ArrayList<>();
    private List<Enemy>     enemies    = new ArrayList<>();
    private Rectangle       goalRect;
    private String          name;
    private int             index;
    private Color           bgColor;
    private Color           accentColor;

    // Parallax
    private List<ParallaxLayer> parallaxLayers = new ArrayList<>();

    public Level(String name, int index, int[][] map, Color bg, Color accent) {
        this.name       = name;
        this.index      = index;
        this.tileMap    = map;
        this.rows       = map.length;
        this.cols       = map[0].length;
        this.bgColor    = bg;
        this.accentColor= accent;
        buildCollisions();
    }

    private void buildCollisions() {
        solidTiles.clear(); spikes.clear();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int tile = tileMap[r][c];
                Rectangle rect = new Rectangle(c * T, r * T, T, T);
                if (tile == 1) solidTiles.add(rect);
                if (tile == 3) spikes.add(rect);
                if (tile == 4) goalRect = rect;
            }
        }
    }

    public void addEnemy(Enemy e) { enemies.add(e); }

    /** Draw background + tiles */
    public void draw(Graphics2D g2, int camX, int camY, int screenW, int screenH) {
        // Solid background
        g2.setColor(bgColor);
        g2.fillRect(0, 0, screenW, screenH);

        // Parallax layers
        for (ParallaxLayer pl : parallaxLayers) pl.draw(g2, camX, camY, screenW, screenH);

        // Grid scan – only draw visible tiles
        int startC = Math.max(0, camX / T - 1);
        int endC   = Math.min(cols - 1, (camX + screenW) / T + 1);
        int startR = Math.max(0, camY / T - 1);
        int endR   = Math.min(rows - 1, (camY + screenH) / T + 1);

        for (int r = startR; r <= endR; r++) {
            for (int c = startC; c <= endC; c++) {
                int tile = tileMap[r][c];
                if (tile == 0) continue;
                int tx = c * T - camX;
                int ty = r * T - camY;
                drawTile(g2, tile, tx, ty, c, r);
            }
        }
    }

    private void drawTile(Graphics2D g2, int type, int tx, int ty, int c, int r) {
        switch (type) {
            case 1 -> {
                // Solid tile body
                g2.setColor(CyberColors.TILE_BASE);
                g2.fillRect(tx, ty, T, T);
                // Inner detail (slightly darker inset)
                g2.setColor(CyberColors.TILE_DARK);
                g2.fillRect(tx + 4, ty + 8, T - 8, T - 12);
                // Top edge glow - only on surface tiles (air above)
                boolean hasAirAbove = (r == 0 || tileMap[r-1][c] == 0 || tileMap[r-1][c] == 3 || tileMap[r-1][c] == 4);
                if (hasAirAbove) {
                    // Thin 2px neon line on top surface only
                    g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 180));
                    g2.fillRect(tx, ty, T, 2);
                    // Very subtle glow below the line
                    g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 18));
                    g2.fillRect(tx, ty + 2, T, 4);
                }
                // Subtle tile border
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 12));
                g2.drawRect(tx, ty, T - 1, T - 1);
            }
            case 3 -> {
                // Spike
                g2.setColor(new Color(0xFF, 0x22, 0x22));
                int[] px = {tx + T/2, tx, tx + T};
                int[] py = {ty, ty + T, ty + T};
                g2.fillPolygon(px, py, 3);
                g2.setColor(new Color(0xFF, 0x88, 0x88, 120));
                g2.drawPolygon(px, py, 3);
            }
            case 4 -> {
                // Goal portal
                long t = System.currentTimeMillis();
                int pulse = (int)(128 + 100 * Math.sin(t / 300.0));
                g2.setColor(new Color(0, pulse, pulse, 180));
                g2.fillRoundRect(tx + 4, ty + 4, T - 8, T - 8, 10, 10);
                g2.setColor(Color.CYAN);
                g2.drawRoundRect(tx + 4, ty + 4, T - 8, T - 8, 10, 10);
                g2.setFont(new Font("Monospaced", Font.BOLD, 9));
                g2.setColor(Color.WHITE);
                g2.drawString("EXIT", tx + 8, ty + T / 2 + 4);
            }
            case 5 -> {
                // Decoration – neon sign
                long t2 = System.currentTimeMillis();
                boolean blink = (t2 / 500) % 2 == 0;
                g2.setColor(blink ? CyberColors.NEON_PINK : CyberColors.NEON_CYAN);
                g2.fillRect(tx + 4, ty + T/3, T - 8, T/3);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Monospaced", Font.BOLD, 7));
                g2.drawString("CYBER", tx + 6, ty + T/2 + 2);
            }
        }
    }

    public void drawEnemies(Graphics2D g2, int camX, int camY) {
        for (Enemy e : enemies) e.draw(g2, camX, camY);
    }

    public void updateEnemies() {
        for (Enemy e : enemies) e.update();
    }

    /** Remove fully-dead enemies; return their score values. */
    public int collectDeadEnemyScores() {
        int total = 0;
        List<Enemy> toRemove = new ArrayList<>();
        for (Enemy e : enemies) {
            if (e.isFullyDead()) { total += e.getScoreValue(); toRemove.add(e); }
        }
        enemies.removeAll(toRemove);
        return total;
    }

    public void addParallaxLayer(ParallaxLayer pl) { parallaxLayers.add(pl); }

    // ── Getters ───────────────────────────────────────────────
    public List<Rectangle> getSolidTiles() { return solidTiles; }
    public List<Rectangle> getSpikes()     { return spikes; }
    public List<Enemy>     getEnemies()    { return enemies; }
    public Rectangle       getGoalRect()   { return goalRect; }
    public int  getWidthPx()  { return cols * T; }
    public int  getHeightPx() { return rows * T; }
    public String getName()   { return name; }
    public int  getIndex()    { return index; }
    public int[][] getTileMap(){ return tileMap; }
    public int getCols()      { return cols; }
    public int getRows()      { return rows; }
}