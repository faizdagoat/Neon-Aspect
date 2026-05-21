package com.cyberknight.entity.enemy;

import com.cyberknight.util.CyberColors;
import java.awt.*;

/**
 * CyberAssassin – dashes at player, teleports when hit.
 */
public class AssassinEnemy extends Enemy {

    private static final float DASH_SPEED  = 7f;
    private static final float IDLE_SPEED  = 0f;
    private int  dashCooldown = 60;
    private boolean dashing   = false;
    private int  dashTimer    = 0;
    private int  teleportFlash= 0;

    public AssassinEnemy(float x, float y) {
        super(x, y, 28, 44, 60, 250, 20);
        this.aggroRange = 350f;
        this.iFrameMax  = 30;
    }

    @Override
    protected void updateAI() {
        if (teleportFlash > 0) { teleportFlash--; return; }

        if (!aggro) { velX = 0; return; }

        if (dashing) {
            dashTimer--;
            if (dashTimer <= 0) { dashing = false; velX = 0; dashCooldown = 90; }
        } else {
            dashCooldown--;
            velX = 0;
            // Face player
            if (player != null) facingRight = player.getX() > x;

            if (dashCooldown <= 0) {
                dashing   = true;
                dashTimer = 20;
                velX = facingRight ? DASH_SPEED : -DASH_SPEED;
            }
        }
    }

    @Override
    public void takeDamage(int amount) {
        super.takeDamage(amount);
        if (alive && player != null) {
            // Teleport away
            float offsetX = player.getX() > x ? -200f : 200f;
            x += offsetX;
            teleportFlash = 15;
            velX = 0;
        }
    }

    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        int dx = (int)(x - camX);
        int dy = (int)(y - camY);

        // Teleport flash effect
        if (teleportFlash > 0) {
            g2.setColor(new Color(0xFF, 0x00, 0xFF, (teleportFlash * 15)));
            g2.fillRect(dx - 10, dy - 10, width + 20, height + 20);
            return;
        }

        if (dying) {
            float a = 1f - deathTimer / (float) deathFrames;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, a)));
        }
        if (iFrames > 0 && (iFrames / 3) % 2 == 0) {
            if (dying) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            return;
        }

        // Body
        g2.setColor(CyberColors.ASSASSIN);
        g2.fillRect(dx, dy, width, height);

        // Cloak edge
        g2.setColor(CyberColors.ASSASSIN_GL);
        g2.drawRect(dx, dy, width, height);

        // Eyes
        g2.setColor(CyberColors.ASSASSIN_GL);
        g2.fillOval(facingRight ? dx + width - 10 : dx + 4, dy + 8, 6, 6);

        // Dash trail
        if (dashing) {
            g2.setColor(new Color(0xFF, 0x00, 0xFF, 60));
            int tx = facingRight ? dx - width : dx + width;
            g2.fillRect(tx, dy, width, height);
        }

        if (dying) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
}