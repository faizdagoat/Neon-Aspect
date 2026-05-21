package com.cyberknight.entity.enemy;

import com.cyberknight.util.CyberColors;
import java.awt.*;

/**
 * CyberGrunt – walks left/right, charges player when aggroed.
 */
public class GruntEnemy extends Enemy {

    private static final float WALK_SPEED   = 1.8f;
    private static final float CHARGE_SPEED = 3.8f;
    private int patrolTimer = 80;

    public GruntEnemy(float x, float y) {
        super(x, y, 30, 40, 40, 100, 15);
    }

    @Override
    protected void updateAI() {
        if (aggro) {
            // Charge toward player
            velX = facingRight ? CHARGE_SPEED : -CHARGE_SPEED;
        } else {
            // Patrol back and forth
            velX = facingRight ? WALK_SPEED : -WALK_SPEED;
            patrolTimer--;
            if (patrolTimer <= 0) {
                facingRight = !facingRight;
                patrolTimer = 80;
            }
        }
    }

    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        int dx = (int)(x - camX);
        int dy = (int)(y - camY);

        if (dying) {
            float alpha = 1f - deathTimer / (float) deathFrames;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, alpha)));
        }

        // Iframes flicker
        if (iFrames > 0 && (iFrames / 3) % 2 == 0) {
            if (dying) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            return;
        }

        // Body
        g2.setColor(CyberColors.GRUNT_BODY);
        g2.fillRect(dx, dy + height / 4, width, height * 3 / 4);

        // Head
        g2.setColor(CyberColors.GRUNT_BODY.brighter());
        g2.fillRect(dx + 4, dy, width - 8, height / 3);

        // Eye
        g2.setColor(CyberColors.GRUNT_EYE);
        int eyeX = facingRight ? dx + width - 10 : dx + 4;
        g2.fillRect(eyeX, dy + 6, 6, 5);

        // Neon outline
        g2.setColor(new Color(0xFF, 0x22, 0x22, 100));
        g2.drawRect(dx, dy, width, height);

        if (dying) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
}