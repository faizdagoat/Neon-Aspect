package com.cyberknight.core;

import com.cyberknight.entity.Player;
import com.cyberknight.level.Level;

/**
 * Smooth-following camera with level boundary clamping.
 */
public class Camera {

    private float x, y;
    private int screenW, screenH;
    private static final float SMOOTH = 0.1f;

    public Camera(int screenW, int screenH) {
        this.screenW = screenW;
        this.screenH = screenH;
    }

    public void update(Player player, Level level) {
        if (player == null || level == null) return;

        // Target: center the player
        float targetX = player.getX() - screenW / 2f + player.getWidth()  / 2f;
        float targetY = player.getY() - screenH / 2f + player.getHeight() / 2f;

        // Smooth follow
        x += (targetX - x) * SMOOTH * 4f;
        y += (targetY - y) * SMOOTH * 4f;

        // Clamp to level bounds
        int levelW = level.getWidthPx();
        int levelH = level.getHeightPx();

        x = Math.max(0, Math.min(x, levelW  - screenW));
        y = Math.max(0, Math.min(y, levelH  - screenH));
    }

    public int getOffsetX() { return (int) x; }
    public int getOffsetY() { return (int) y; }

    public void reset() { x = 0; y = 0; }
}