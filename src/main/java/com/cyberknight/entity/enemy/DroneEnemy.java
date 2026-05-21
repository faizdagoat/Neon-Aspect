package com.cyberknight.entity.enemy;

import com.cyberknight.util.CyberColors;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CyberDrone – hovers, shoots bullets at player.
 */
public class DroneEnemy extends Enemy {

    private float  baseY;
    private float  hoverTimer  = 0;
    private int    shootCooldown = 0;
    private static final int SHOOT_RATE  = 90;
    private static final float HOVER_AMP = 12f;

    // Bullets
    private List<Bullet> bullets = new ArrayList<>();

    public DroneEnemy(float x, float y) {
        super(x, y, 36, 24, 30, 150, 10);
        this.baseY    = y;
        this.aggroRange = 400f;
    }

    @Override
    protected void applyGravity() {
        // Drone floats – no gravity
        hoverTimer += 0.06f;
        y = baseY + (float) Math.sin(hoverTimer) * HOVER_AMP;
        x += velX;
    }

    @Override
    protected void resolveCollisions() { /* drone ignores tiles */ }

    @Override
    protected void updateAI() {
        if (!aggro) { velX *= 0.9f; return; }

        // Slowly move toward player horizontally
        float dx = player.getX() - x;
        velX = dx > 0 ? 1.2f : -1.2f;

        // Shoot
        if (shootCooldown > 0) shootCooldown--;
        else {
            spawnBullet();
            shootCooldown = SHOOT_RATE;
        }

        // Update bullets
        bullets.removeIf(b -> !b.active);
        for (Bullet b : bullets) b.update();
    }

    private void spawnBullet() {
        if (player == null) return;
        float dx = player.getX() + 16 - (x + width / 2f);
        float dy = player.getY() + 24 - (y + height / 2f);
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        bullets.add(new Bullet(x + width / 2f - 4, y + height / 2f - 4, dx / len * 5f, dy / len * 5f));
    }

    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        // Draw bullets first
        for (Bullet b : bullets) b.draw(g2, camX, camY);

        int dx = (int)(x - camX);
        int dy = (int)(y - camY);

        if (dying) {
            float alpha = 1f - deathTimer / (float) deathFrames;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, alpha)));
        }
        if (iFrames > 0 && (iFrames / 3) % 2 == 0) {
            if (dying) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            return;
        }

        // Body hexagon (drawn as rounded rect)
        g2.setColor(CyberColors.DRONE_BODY);
        g2.fillRoundRect(dx, dy, width, height, 10, 10);

        // Glow ring
        g2.setColor(CyberColors.DRONE_GLOW);
        g2.drawRoundRect(dx, dy, width, height, 10, 10);
        g2.setColor(new Color(0x00, 0x88, 0xFF, 60));
        g2.fillRoundRect(dx - 4, dy - 4, width + 8, height + 8, 14, 14);

        // Eye / sensor
        g2.setColor(CyberColors.DRONE_GLOW);
        g2.fillOval(dx + width / 2 - 4, dy + height / 2 - 4, 8, 8);

        // Rotors
        g2.setColor(new Color(0x88, 0xCC, 0xFF));
        g2.fillRect(dx - 6, dy, 6, 4);
        g2.fillRect(dx + width, dy, 6, 4);

        if (dying) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    public List<Bullet> getBullets() { return bullets; }

    // ── Inner Bullet class ───────────────────────────────────
    public static class Bullet {
        public float x, y, vx, vy;
        public boolean active = true;
        private int life = 180;

        public Bullet(float x, float y, float vx, float vy) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
        }

        public void update() {
            x += vx; y += vy;
            life--;
            if (life <= 0) active = false;
        }

        public Rectangle getBounds() { return new Rectangle((int)x, (int)y, 8, 8); }

        public void draw(Graphics2D g2, int camX, int camY) {
            if (!active) return;
            int bx = (int)(x - camX);
            int by = (int)(y - camY);
            g2.setColor(CyberColors.BULLET_ENEMY);
            g2.fillOval(bx, by, 8, 8);
            g2.setColor(new Color(0xFF, 0x88, 0x88, 100));
            g2.fillOval(bx - 3, by - 3, 14, 14);
        }
    }
}