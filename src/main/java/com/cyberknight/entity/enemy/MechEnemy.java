package com.cyberknight.entity.enemy;

import com.cyberknight.util.CyberColors;
import java.awt.*;

/**
 * CyberMech – slow, tanky, stomps causing shockwave.
 */
public class MechEnemy extends Enemy {

    private static final float WALK_SPEED  = 1.0f;
    private int stompCooldown = 120;
    private boolean stomping  = false;
    private int stompTimer    = 0;
    private int shockwaveRadius = 0;
    private boolean shockwaveActive = false;

    public MechEnemy(float x, float y) {
        super(x, y, 52, 64, 120, 300, 25);
        this.aggroRange = 280f;
    }

    @Override
    protected void updateAI() {
        if (stomping) {
            velX = 0;
            stompTimer--;
            if (shockwaveActive) shockwaveRadius += 8;
            if (stompTimer <= 0) { stomping = false; shockwaveActive = false; shockwaveRadius = 0; }
            return;
        }

        if (aggro) velX = facingRight ? WALK_SPEED : -WALK_SPEED;
        else       velX = 0;

        stompCooldown--;
        if (stompCooldown <= 0 && onGround && aggro) {
            stomping = true;
            stompTimer = 40;
            stompCooldown = 150;
            shockwaveActive = true;
            shockwaveRadius = 0;
            velY = -5; // small jump
        }
    }

    public boolean isShockwaveActive() { return shockwaveActive; }
    public int getShockwaveRadius()     { return shockwaveRadius; }
    public Rectangle getShockwave() {
        if (!shockwaveActive) return null;
        int r = shockwaveRadius;
        return new Rectangle((int)x + width/2 - r, (int)y + height - 16, r * 2, 20);
    }

    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
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

        // Legs
        g2.setColor(CyberColors.MECH_BODY.darker());
        g2.fillRect(dx + 4,        dy + height - 20, 16, 20);
        g2.fillRect(dx + width - 20, dy + height - 20, 16, 20);

        // Body
        g2.setColor(CyberColors.MECH_BODY);
        g2.fillRect(dx, dy + 16, width, height - 16);

        // Head
        g2.setColor(CyberColors.MECH_BODY.brighter());
        g2.fillRect(dx + 8, dy, width - 16, 24);

        // Visor
        g2.setColor(CyberColors.MECH_GLOW);
        g2.fillRect(dx + 12, dy + 6, width - 24, 8);

        // Shoulder armor
        g2.setColor(CyberColors.MECH_BODY.darker());
        g2.fillRect(dx - 6, dy + 20, 12, 20);
        g2.fillRect(dx + width - 6, dy + 20, 12, 20);

        // Glow border
        g2.setColor(CyberColors.MECH_GLOW);
        g2.drawRect(dx, dy + 16, width, height - 16);

        // Shockwave ring
        if (shockwaveActive && shockwaveRadius > 0) {
            int cx = dx + width / 2;
            int cy = dy + height - 8;
            g2.setColor(new Color(0x00, 0xFF, 0x44, Math.max(0, 180 - shockwaveRadius * 2)));
            g2.drawOval(cx - shockwaveRadius, cy - 10, shockwaveRadius * 2, 20);
        }

        if (dying) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
}