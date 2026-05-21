package com.cyberknight.entity.boss;

import com.cyberknight.entity.enemy.DroneEnemy.Bullet;
import com.cyberknight.util.CyberColors;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BOSS 2 – Colossus Prime (Final Boss)
 * A massive cyberpunk war machine with 3 phases.
 *
 * Phase 1 – Ground assault: charge, stomp, cannon volley
 * Phase 2 – Arms split: dual laser sweep, missile salvo
 * Phase 3 – Overload: all attacks combined, faster, enraged mode
 */
public class ColossusPrime extends Boss {

    private enum State { IDLE, CHARGE, STOMP, CANNON, LASER_SWEEP, MISSILES, ENRAGE_SLAM }
    private State  state       = State.IDLE;
    private int    stateTimer  = 90;
    private int    actionCD    = 80;

    // Charge
    private boolean charging   = false;
    private int     chargeTimer= 0;

    // Stomp shockwave
    private boolean stomping   = false;
    private int     stompTimer = 0;
    private int     shockR     = 0;
    private boolean shockOn    = false;

    // Cannon / missiles
    private List<Bullet> bullets   = new ArrayList<>();
    private int cannonCD = 0;

    // Laser sweep
    private boolean lasering   = false;
    private int     laserTimer = 0;
    private float   laserAngle = 0;
    private int     laserDamageCD = 0;

    // Visual
    private float   glowPulse  = 0;
    private int     animFrame  = 0;
    private int     animTimer  = 0;
    private boolean enraged    = false;
    private int     shakeTimer = 0;

    // Phase-2 arm positions
    private float arm1X, arm2X;

    public ColossusPrime(float x, float y) {
        super(x, y, 90, 110, 900, "COLOSSUS PRIME", 3);
        this.arenaLeft  = 60;
        this.arenaRight = 1200;
        this.iFrameMax  = 8;
        arm1X = x - 30; arm2X = x + width + 10;
    }

    @Override
    protected void updateBossAI() {
        glowPulse += 0.07f;
        animTimer++;
        if (animTimer > 8) { animFrame = (animFrame + 1) % 4; animTimer = 0; }
        arm1X = x - 30; arm2X = x + width;
        if (shakeTimer > 0) shakeTimer--;

        bullets.removeIf(b -> !b.active);
        for (Bullet b : bullets) b.update();

        stateTimer--;
        if (stateTimer <= 0) chooseAction();
        executeState();
    }

    private void chooseAction() {
        if (player == null) return;
        int roll;
        if      (phase == 1) roll = (int)(Math.random() * 3);
        else if (phase == 2) roll = (int)(Math.random() * 5);
        else                 roll = (int)(Math.random() * 7);

        state = switch(roll) {
            case 0 -> State.CHARGE;
            case 1 -> State.STOMP;
            case 2 -> State.CANNON;
            case 3 -> State.LASER_SWEEP;
            case 4 -> State.MISSILES;
            case 5 -> State.CANNON;
            case 6 -> State.ENRAGE_SLAM;
            default-> State.IDLE;
        };
        stateTimer = enraged ? 50 : 70;
    }

    private void executeState() {
        switch (state) {
            case CHARGE       -> doCharge();
            case STOMP        -> doStomp();
            case CANNON       -> doCannon();
            case LASER_SWEEP  -> doLaserSweep();
            case MISSILES     -> doMissiles();
            case ENRAGE_SLAM  -> doEnrageSlam();
            case IDLE         -> { velX *= 0.85f; }
        }
    }

    private void doCharge() {
        if (!charging) { charging = true; chargeTimer = enraged ? 35 : 50; velX = facingRight ? 8f : -8f; }
        chargeTimer--;
        if (chargeTimer <= 0) { charging = false; velX = 0; state = State.IDLE; stateTimer = 50; }
    }

    private void doStomp() {
        if (!stomping) {
            stomping = true; stompTimer = 60;
            velY = -13; shockOn = false; shockR = 0;
        }
        stompTimer--;
        if (stompTimer == 30) velY = 20; // crash down
        if (stomping && onGround && stompTimer < 55) {
            shockOn = true; shakeTimer = 20;
            stomping = false; state = State.IDLE; stateTimer = 60;
        }
        if (shockOn) {
            shockR += enraged ? 16 : 11;
            if (shockR > 600) { shockOn = false; shockR = 0; }
        }
    }

    private void doCannon() {
        cannonCD--;
        if (cannonCD <= 0 && player != null) {
            float dx = player.getX() + 16 - (x + width / 2f);
            float dy = player.getY() + 24 - (y + height / 2f);
            float len = (float)Math.sqrt(dx*dx+dy*dy);
            float spd = enraged ? 7f : 5.5f;
            int shots = phase >= 2 ? 3 : 1;
            for (int i = 0; i < shots; i++) {
                float scatter = (float)(Math.random()-0.5)*0.8f;
                bullets.add(new Bullet(x + width/2f - 4, y + 30, dx/len*spd+scatter, dy/len*spd+scatter));
            }
            cannonCD = enraged ? 20 : 30;
        }
    }

    private void doLaserSweep() {
        if (phase < 2) { state = State.CANNON; return; }
        if (!lasering) {
            lasering   = true; laserTimer = 80;
            laserAngle = (float)-Math.PI / 3;
            laserDamageCD = 0;
        }
        laserTimer--;
        laserAngle += (float)(Math.PI / 3 / 80f) * 2;
        laserDamageCD--;
        if (laserTimer <= 0) { lasering = false; state = State.IDLE; stateTimer = 60; }
    }

    private void doMissiles() {
        if (phase < 2) { state = State.CANNON; return; }
        if (stateTimer % (enraged ? 10 : 15) == 0 && player != null) {
            float angle = -(float)Math.PI/2 + (float)(Math.random()-0.5)*0.8f;
            float spd = 4f;
            bullets.add(new Bullet(x + width/2f, y, (float)Math.cos(angle)*spd, (float)Math.sin(angle)*spd));
        }
        velX = 0;
    }

    private void doEnrageSlam() {
        if (phase < 3) { state = State.CHARGE; return; }
        // Repeatedly jump and slam
        if (!stomping) {
            stomping = true; stompTimer = 30;
            velY = -16; shockOn = false; shockR = 0;
        }
        stompTimer--;
        if (stompTimer == 15) velY = 22;
        if (stomping && onGround && stompTimer < 28) {
            shockOn = true; shakeTimer = 25;
            stomping = false;
            // Fire cannons in burst
            doCannon(); doCannon();
            state = State.IDLE; stateTimer = 30;
        }
        if (shockOn) { shockR += 18; if (shockR > 700) { shockOn = false; shockR = 0; } }
    }

    @Override
    protected void checkPhaseTransition() {
        if (phase == 1 && currentHealth <= maxHealth * 2 / 3) {
            phase = 2; stateTimer = 120; shakeTimer = 60;
        }
        if (phase == 2 && currentHealth <= maxHealth / 3) {
            phase = 3; enraged = true; stateTimer = 120; shakeTimer = 90;
        }
    }

    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        // Draw bullets
        for (Bullet b : bullets) b.draw(g2, camX, camY);

        // Shockwave
        if (shockOn) {
            int cx = (int)(x - camX + width / 2);
            int cy = (int)(y - camY + height);
            int al = Math.max(0, 200 - shockR / 3);
            g2.setColor(new Color(0xFF, 0x66, 0x00, al));
            g2.drawOval(cx - shockR, cy - 14, shockR * 2, 28);
            g2.setColor(new Color(0xFF, 0x66, 0x00, al / 4));
            g2.fillOval(cx - shockR, cy - 14, shockR * 2, 28);
        }

        int shake = shakeTimer > 0 ? (int)(Math.random() * 5 - 2) : 0;
        int dx = (int)(x - camX) + shake;
        int dy = (int)(y - camY) + shake;

        if (dying) {
            float a = 1f - Math.min(1f, deathTimer / (float)(deathFrames/2));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, a)));
        }
        if (iFrames > 0 && (iFrames / 2) % 2 == 0) {
            if (dying) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            return;
        }

        // Phase 3 enrage aura
        if (enraged) {
            int auraA = (int)(50 + 40 * Math.abs(Math.sin(glowPulse)));
            g2.setColor(new Color(0xFF, 0x44, 0x00, auraA));
            g2.fillOval(dx - 30, dy - 30, width + 60, height + 60);
        }

        // Arms (phase 2+)
        if (phase >= 2) {
            g2.setColor(new Color(0x33, 0x55, 0x22));
            g2.fillRect(dx - 28, dy + 20, 28, 18);
            g2.fillRect(dx + width, dy + 20, 28, 18);
            g2.setColor(CyberColors.BOSS2_GLOW);
            g2.drawRect(dx - 28, dy + 20, 28, 18);
            g2.drawRect(dx + width, dy + 20, 28, 18);
        }

        // Legs
        g2.setColor(new Color(0x22, 0x33, 0x11));
        g2.fillRect(dx + 8,          dy + height - 24, 22, 24);
        g2.fillRect(dx + width - 30, dy + height - 24, 22, 24);

        // Main body
        Color bodyC = enraged ? new Color(0xAA, 0x33, 0x00) : CyberColors.BOSS2_BODY;
        g2.setColor(bodyC);
        g2.fillRect(dx, dy + 20, width, height - 20);

        // Chest plate
        g2.setColor(bodyC.darker());
        g2.fillRect(dx + 10, dy + 30, width - 20, height / 2);

        // Head
        g2.setColor(bodyC.brighter());
        g2.fillRect(dx + 8, dy, width - 16, 28);

        // Eyes – glow based on phase
        Color eyeC = enraged ? new Color(0xFF, 0x44, 0x00) : CyberColors.BOSS2_GLOW;
        int ep = (int)(6 + 4 * Math.abs(Math.sin(glowPulse * 2)));
        g2.setColor(eyeC);
        g2.fillOval(dx + 14, dy + 6, ep + 2, ep);
        g2.fillOval(dx + width - 20, dy + 6, ep + 2, ep);

        // Cannon barrel (visible during cannon state)
        g2.setColor(new Color(0x55, 0x55, 0x55));
        g2.fillRect(dx + width / 2 - 6, dy + 25, 12, 30);
        g2.setColor(eyeC);
        g2.drawRect(dx + width / 2 - 6, dy + 25, 12, 30);

        // Laser beam
        if (lasering) {
            float bx = x - camX + width / 2f;
            float by = y - camY + 30;
            float laserLen = 800f;
            int laserAlpha = (int)(150 + 100 * Math.sin(laserTimer * 0.3));
            g2.setColor(new Color(0xFF, 0x88, 0x00, laserAlpha));
            g2.setStroke(new BasicStroke(8));
            g2.drawLine((int)bx, (int)by,
                (int)(bx + Math.cos(laserAngle) * laserLen),
                (int)(by + Math.sin(laserAngle) * laserLen));
            g2.setStroke(new BasicStroke(1));
        }

        // Glow border
        g2.setColor(new Color(0xFF, 0x66, 0x00, 100));
        g2.drawRect(dx, dy + 20, width, height - 20);
        g2.drawRect(dx + 8, dy, width - 16, 28);

        if (dying) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    @Override protected Color getBossColor() { return CyberColors.BOSS2_GLOW; }

    public List<Bullet> getBullets()     { return bullets; }
    public boolean isShockwaveActive()   { return shockOn; }
    public int getShockwaveRadius()      { return shockR; }
    public boolean isLasering()          { return lasering; }
    public float getLaserAngle()         { return laserAngle; }
    public int getLaserDamageCD()        { return laserDamageCD; }
    public void resetLaserDamageCD(int v){ laserDamageCD = v; }

    public Rectangle getShockwave() {
        if (!shockOn) return null;
        int cx = (int)x + width/2;
        int cy = (int)y + height;
        return new Rectangle(cx - shockR, cy - 16, shockR * 2, 32);
    }

    /**
     * Returns laser collision rect in world space.
     * camX/camY params kept for API compatibility but ignored in collision checks
     * (collision is always world-space).
     */
    public Rectangle getLaserRect(int camX, int camY) {
        if (!lasering) return null;
        float bx = x + width / 2f;
        float by = y + 30;
        float ex = bx + (float)Math.cos(laserAngle) * 800;
        float ey = by + (float)Math.sin(laserAngle) * 800;
        int x1 = (int)Math.min(bx, ex);
        int x2 = (int)Math.max(bx, ex);
        int y1 = (int)Math.min(by, ey);
        int y2 = (int)Math.max(by, ey);
        return new Rectangle(x1, y1 - 8, Math.max(x2 - x1, 10), Math.max(y2 - y1 + 16, 16));
    }
}