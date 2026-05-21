package com.cyberknight.entity.boss;

import com.cyberknight.entity.enemy.DroneEnemy.Bullet;
import com.cyberknight.util.CyberColors;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BOSS 1 – NeonWraith
 * A spectral cyberpunk ghost with two phases.
 *
 * Phase 1 (HP > 50%):
 *   – Teleports around arena
 *   – Shoots spread of bullets
 *   – Slams ground (shockwave)
 *
 * Phase 2 (HP ≤ 50%):
 *   – All Phase-1 attacks faster/denser
 *   – Adds homing projectiles
 *   – Rapid teleport barrage
 */
public class NeonWraith extends Boss {

    // ── Attack states ─────────────────────────────────────────
    private enum State { IDLE, TELEPORT, SHOOT_SPREAD, SHOOT_HOMING, GROUND_SLAM, BARRAGE }
    private State   state        = State.IDLE;
    private int     stateTimer   = 60;
    private int     attackCooldown = 80;

    // Teleport
    private boolean teleporting  = false;
    private int     teleportTimer= 0;
    private float   teleportTargetX, teleportTargetY;
    private int     teleportFlash= 0;

    // Ground slam
    private boolean slamming     = false;
    private int     slamTimer    = 0;
    private int     shockwaveR   = 0;
    private boolean shockwaveOn  = false;

    // Projectiles
    private List<Bullet> bullets = new ArrayList<>();
    private List<HomingBullet> homers = new ArrayList<>();

    // Visual
    private float hoverOffset  = 0;
    private int   animTimer    = 0;
    private float glowPulse    = 0;

    public NeonWraith(float x, float y) {
        super(x, y, 60, 80, 500, "NEON WRAITH", 2);
        this.arenaLeft  = 50;
        this.arenaRight = 1200;
        this.iFrameMax  = 12;
    }

    // ────────────────────────────────────────────────────────────
    //  AI
    // ────────────────────────────────────────────────────────────
    @Override
    protected void updateBossAI() {
        glowPulse += 0.08f;
        hoverOffset = (float) Math.sin(glowPulse) * 6f;
        animTimer++;

        // Update projectiles
        bullets.removeIf(b -> !b.active);
        for (Bullet b : bullets) b.update();
        homers.removeIf(h -> !h.active);
        for (HomingBullet h : homers) h.update(player);

        if (teleportFlash > 0) teleportFlash--;

        stateTimer--;
        if (stateTimer <= 0) chooseNextAction();

        executeState();
    }

    private void chooseNextAction() {
        if (player == null) return;
        int roll = (int)(Math.random() * (phase == 2 ? 5 : 3));
        state = switch (roll) {
            case 0 -> State.TELEPORT;
            case 1 -> State.SHOOT_SPREAD;
            case 2 -> State.GROUND_SLAM;
            case 3 -> State.SHOOT_HOMING;
            case 4 -> State.BARRAGE;
            default -> State.IDLE;
        };
        stateTimer = phase == 2 ? 50 : 70;
    }

    private void executeState() {
        switch (state) {
            case TELEPORT      -> doTeleport();
            case SHOOT_SPREAD  -> doShootSpread();
            case SHOOT_HOMING  -> doShootHoming();
            case GROUND_SLAM   -> doGroundSlam();
            case BARRAGE       -> doBarrage();
            case IDLE          -> velX *= 0.9f;
        }
    }

    private void doTeleport() {
        if (!teleporting) {
            teleporting = true;
            teleportTimer = 30;
            teleportFlash = 30;
            // Pick random position near player but not on top
            float offset = (Math.random() < 0.5 ? -1 : 1) * (150 + (float)(Math.random()*100));
            teleportTargetX = Math.max(arenaLeft, Math.min(arenaRight - width, player.getX() + offset));
            teleportTargetY = player.getY() - 100;
        }
        teleportTimer--;
        if (teleportTimer <= 0) {
            x = teleportTargetX; y = teleportTargetY;
            teleporting = false; state = State.IDLE; stateTimer = 40;
            velY = 0;
        }
    }

    private void doShootSpread() {
        if (stateTimer % (phase == 2 ? 15 : 20) == 0) {
            int count = phase == 2 ? 7 : 5;
            for (int i = 0; i < count; i++) {
                float angle = (float)(Math.PI * 2 / count * i);
                float spd   = phase == 2 ? 5.5f : 4f;
                bullets.add(new Bullet(
                    x + width / 2f - 4, y + height / 2f - 4,
                    (float)Math.cos(angle) * spd, (float)Math.sin(angle) * spd
                ));
            }
        }
        // Float in place
        velX = 0;
    }

    private void doShootHoming() {
        if (stateTimer % 25 == 0 && player != null) {
            homers.add(new HomingBullet(x + width / 2f, y + height / 2f));
        }
    }

    private void doGroundSlam() {
        if (!slamming) {
            slamming   = true;
            slamTimer  = 50;
            velY       = -14; // jump up
        }
        slamTimer--;
        if (slamTimer == 20 && !onGround) {
            velY = 18; // slam down
        }
        if (slamTimer <= 0 || (slamming && onGround && slamTimer < 45)) {
            if (onGround) {
                shockwaveOn = true; shockwaveR = 0;
            }
            slamming = false; state = State.IDLE; stateTimer = 60;
        }
        if (shockwaveOn) {
            shockwaveR += 12;
            if (shockwaveR > 400) { shockwaveOn = false; shockwaveR = 0; }
        }
    }

    private void doBarrage() {
        if (phase < 2) { state = State.SHOOT_SPREAD; return; }
        if (stateTimer % 8 == 0 && player != null) {
            float dx = player.getX() - x;
            float dy = player.getY() - y;
            float len = (float)Math.sqrt(dx*dx + dy*dy);
            if (len > 0) {
                float scatter = (float)(Math.random() - 0.5) * 0.5f;
                bullets.add(new Bullet(
                    x + width / 2f, y + height / 2f,
                    dx / len * 6f + scatter, dy / len * 6f + scatter
                ));
            }
        }
    }

    @Override
    protected void checkPhaseTransition() {
        if (phase == 1 && currentHealth <= maxHealth / 2) {
            phase = 2;
            teleportFlash = 60;
            // Heal slightly on phase change (classic boss behaviour)
            // currentHealth stays the same – just speed up
            state = State.IDLE;
            stateTimer = 90;
        }
    }

    // ────────────────────────────────────────────────────────────
    //  DRAW
    // ────────────────────────────────────────────────────────────
    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        // Draw projectiles
        for (Bullet b : bullets) b.draw(g2, camX, camY);
        for (HomingBullet h : homers) h.draw(g2, camX, camY);

        // Shockwave
        if (shockwaveOn) {
            int cx = (int)(x - camX + width / 2);
            int cy = (int)(y - camY + height);
            int alpha = Math.max(0, 180 - shockwaveR / 2);
            g2.setColor(new Color(0xFF, 0x00, 0xFF, alpha));
            g2.drawOval(cx - shockwaveR, cy - 12, shockwaveR * 2, 24);
            g2.setColor(new Color(0xFF, 0x00, 0xFF, alpha / 3));
            g2.fillOval(cx - shockwaveR, cy - 12, shockwaveR * 2, 24);
        }

        if (teleporting && teleportFlash > 20) return; // invisible during teleport

        int dx = (int)(x - camX);
        int dy = (int)(y - camY + hoverOffset);

        if (dying) {
            float a = 1f - Math.min(1f, deathTimer / (float)(deathFrames / 2));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, a)));
        }
        if (iFrames > 0 && (iFrames / 2) % 2 == 0) {
            if (dying) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            return;
        }

        // Phase 2: extra glow aura
        if (phase == 2) {
            int auraA = (int)(60 + 40 * Math.abs(Math.sin(glowPulse)));
            g2.setColor(new Color(0xFF, 0x00, 0xFF, auraA));
            g2.fillOval(dx - 20, dy - 20, width + 40, height + 40);
        }

        // Robe/body
        Color bodyC = phase == 2 ? new Color(0x77, 0x00, 0xAA) : CyberColors.BOSS1_BODY;
        g2.setColor(bodyC);
        int[] xp = {dx + width/2, dx, dx + 10, dx + width - 10, dx + width};
        int[] yp = {dy, dy + height/3, dy + height, dy + height, dy + height/3};
        g2.fillPolygon(xp, yp, 5);

        // Head
        g2.setColor(new Color(0xBB, 0x88, 0xFF));
        g2.fillOval(dx + 8, dy - 10, width - 16, 40);

        // Eyes – 2 glowing orbs
        int pulse = (int)(8 + 6 * Math.abs(Math.sin(glowPulse * 2)));
        g2.setColor(CyberColors.BOSS1_GLOW);
        g2.fillOval(dx + 14, dy + 2, pulse, pulse);
        g2.fillOval(dx + width - 14 - pulse, dy + 2, pulse, pulse);

        // Inner white glow
        g2.setColor(Color.WHITE);
        g2.fillOval(dx + 16, dy + 4, pulse / 2, pulse / 2);
        g2.fillOval(dx + width - 13 - pulse / 2, dy + 4, pulse / 2, pulse / 2);

        // Glow border
        g2.setColor(new Color(0xFF, 0x00, 0xFF, 120));
        g2.drawPolygon(xp, yp, 5);

        if (introPlaying) {
            // Intro flash
            g2.setColor(new Color(0xFF, 0xFF, 0xFF, Math.max(0, introTimer * 2)));
            g2.fillPolygon(xp, yp, 5);
        }

        if (dying) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    @Override protected Color getBossColor() { return CyberColors.BOSS1_GLOW; }

    // Getters for collision checks in LevelManager
    public List<Bullet> getBullets()          { return bullets; }
    public List<HomingBullet> getHomers()     { return homers; }
    public boolean isShockwaveActive()        { return shockwaveOn; }
    public int getShockwaveRadius()           { return shockwaveR; }
    public Rectangle getShockwave() {
        if (!shockwaveOn) return null;
        int cx = (int)x + width / 2;
        int cy = (int)y + height;
        return new Rectangle(cx - shockwaveR, cy - 14, shockwaveR * 2, 28);
    }

    // ── Homing Bullet inner class ─────────────────────────────
    public static class HomingBullet {
        public float x, y, vx, vy;
        public boolean active = true;
        private int life = 300;
        private static final float TURN_SPEED = 0.06f;
        private static final float SPEED = 3.5f;

        public HomingBullet(float x, float y) {
            this.x = x; this.y = y;
            double angle = Math.random() * Math.PI * 2;
            vx = (float)Math.cos(angle) * SPEED;
            vy = (float)Math.sin(angle) * SPEED;
        }

        public void update(com.cyberknight.entity.Player player) {
            if (!active || player == null) return;
            float dx = player.getX() + 16 - x;
            float dy = player.getY() + 24 - y;
            float len = (float)Math.sqrt(dx*dx+dy*dy);
            if (len > 0) {
                vx += (dx/len * SPEED - vx) * TURN_SPEED;
                vy += (dy/len * SPEED - vy) * TURN_SPEED;
                float spd = (float)Math.sqrt(vx*vx+vy*vy);
                if (spd > SPEED * 1.5f) { vx = vx/spd*SPEED*1.5f; vy = vy/spd*SPEED*1.5f; }
            }
            x += vx; y += vy;
            life--;
            if (life <= 0) active = false;
        }

        public Rectangle getBounds() { return new Rectangle((int)x-5,(int)y-5,10,10); }

        public void draw(Graphics2D g2, int camX, int camY) {
            if (!active) return;
            int bx = (int)(x - camX); int by = (int)(y - camY);
            g2.setColor(CyberColors.BOSS1_GLOW);
            g2.fillOval(bx - 5, by - 5, 10, 10);
            g2.setColor(new Color(0xFF, 0x88, 0xFF, 80));
            g2.fillOval(bx - 9, by - 9, 18, 18);
        }
    }
}