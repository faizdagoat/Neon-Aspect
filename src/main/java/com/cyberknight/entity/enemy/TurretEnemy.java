package com.cyberknight.entity.enemy;

import com.cyberknight.util.CyberColors;
import com.cyberknight.entity.enemy.DroneEnemy.Bullet;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CyberTurret – stationary, shoots 3-shot burst at player.
 */
public class TurretEnemy extends Enemy {

    private int  shootCooldown = 90;
    private int  burstCount    = 0;
    private int  burstDelay    = 0;
    private List<Bullet> bullets = new ArrayList<>();
    private float aimAngle = 0;

    public TurretEnemy(float x, float y) {
        super(x, y, 32, 36, 50, 200, 12);
        this.aggroRange = 500f;
    }

    @Override
    protected void applyGravity() {
        // Turret is fixed
        velX = 0; velY = 0;
        y += velY; x += velX;
    }

    @Override
    protected void resolveCollisions() {}

    @Override
    protected void updateAI() {
        bullets.removeIf(b -> !b.active);
        for (Bullet b : bullets) b.update();

        if (!aggro || player == null) return;

        // Aim toward player
        float dx = player.getX() + 16 - (x + width / 2f);
        float dy = player.getY() + 24 - (y + height / 2f);
        aimAngle = (float) Math.atan2(dy, dx);

        if (burstCount > 0) {
            burstDelay--;
            if (burstDelay <= 0) {
                fireBullet();
                burstCount--;
                burstDelay = 10;
            }
            return;
        }

        shootCooldown--;
        if (shootCooldown <= 0) {
            burstCount = 3;
            burstDelay = 0;
            shootCooldown = 100;
        }
    }

    private void fireBullet() {
        float bx = x + width / 2f - 4;
        float by = y + height / 2f - 4;
        float speed = 6f;
        bullets.add(new Bullet(bx, by, (float)Math.cos(aimAngle)*speed, (float)Math.sin(aimAngle)*speed));
    }

    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        for (Bullet b : bullets) b.draw(g2, camX, camY);

        int dx = (int)(x - camX);
        int dy = (int)(y - camY);

        if (dying) {
            float a = 1f - deathTimer / (float) deathFrames;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, a)));
        }
        if (iFrames > 0 && (iFrames / 3) % 2 == 0) {
            if (dying) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            return;
        }

        // Base
        g2.setColor(CyberColors.TURRET_BODY);
        g2.fillRect(dx, dy + height / 2, width, height / 2);
        g2.fillRect(dx + 4, dy + height / 4, width - 8, height / 4 + 4);

        // Barrel (rotates toward player)
        Graphics2D g = (Graphics2D) g2.create();
        g.translate(dx + width / 2, dy + height / 3);
        g.rotate(aimAngle);
        g.setColor(CyberColors.TURRET_BODY.brighter());
        g.fillRect(0, -4, 22, 8);
        g.setColor(CyberColors.TURRET_GLOW);
        g.drawRect(0, -4, 22, 8);
        g.dispose();

        // Body glow
        g2.setColor(CyberColors.TURRET_GLOW);
        g2.drawRect(dx, dy + height / 2, width, height / 2);

        if (dying) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    public List<Bullet> getBullets() { return bullets; }
}