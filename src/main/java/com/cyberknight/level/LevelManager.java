package com.cyberknight.level;

import com.cyberknight.audio.AudioManager;
import com.cyberknight.core.Camera;
import com.cyberknight.core.GamePanel;
import com.cyberknight.entity.Player;
import com.cyberknight.entity.boss.Boss;
import com.cyberknight.entity.boss.NeonWraith;
import com.cyberknight.entity.boss.ColossusPrime;
import com.cyberknight.entity.enemy.Enemy;
import com.cyberknight.entity.enemy.DroneEnemy;
import com.cyberknight.entity.enemy.DroneEnemy.Bullet;
import com.cyberknight.entity.enemy.TurretEnemy;
import com.cyberknight.entity.enemy.MechEnemy;
import com.cyberknight.entity.enemy.GruntEnemy;
import com.cyberknight.entity.enemy.AssassinEnemy;
import com.cyberknight.util.CyberColors;

import java.awt.*;
import java.util.List;

/**
 * LevelManager handles:
 *  - Loading / reloading levels
 *  - Updating all entities
 *  - Resolving player ↔ enemy / boss collisions
 *  - Boss fight lifecycle (intro, fight, defeat)
 *  - Goal detection
 */
public class LevelManager {

    private GamePanel panel;
    private Player    player;
    private Camera    camera;

    private Level     currentLevel;
    private int       currentIndex;

    // Boss state
    private Boss      activeBoss      = null;
    private boolean   bossDefeated    = false;
    private boolean   bossIntroShown  = false;
    private int       bossDefeatTimer = 0;

    // Level complete
    private boolean   levelComplete   = false;
    private int       levelCompleteTimer = 0;

    // Death handling - prevent calling playerDied() multiple times
    private boolean   deathHandled    = false;

    // Screen shake
    private int       shakeTimer = 0;
    private int       shakeAmt   = 0;

    public LevelManager(GamePanel panel, Player player, Camera camera) {
        this.panel  = panel;
        this.player = player;
        this.camera = camera;
    }

    public void loadLevel(int index) {
        currentIndex   = index;
        currentLevel   = LevelFactory.build(index, player);
        player.setLevel(currentLevel);
        player.resetPosition(100f, 388f);
        player.grantSpawnIFrames();
        activeBoss     = null;
        bossDefeated   = false;
        bossIntroShown = false;
        levelComplete  = false;
        deathHandled   = false;

        // ── Musik berdasarkan level ────────────────────────────
        AudioManager audio = AudioManager.getInstance();
        switch (index) {
            case 1, 2, 3 -> audio.play(AudioManager.TRACK_LEVEL);  // level biasa
            case 4       -> audio.play(AudioManager.TRACK_BOSS1);   // boss 1
            case 5       -> audio.play(AudioManager.TRACK_BOSS2);   // boss 2 (final)
            default      -> audio.play(AudioManager.TRACK_LEVEL);
        }

        // Spawn boss untuk level 4 & 5
        if (index == 4) {
            NeonWraith boss = new NeonWraith(580, 350);
            boss.setPlayer(player);
            boss.setLevel(currentLevel);
            activeBoss = boss;
        } else if (index == 5) {
            ColossusPrime boss = new ColossusPrime(560, 320);
            boss.setPlayer(player);
            boss.setLevel(currentLevel);
            activeBoss = boss;
        }

        camera.reset();
    }

    public void reloadCurrentLevel() {
        loadLevel(currentIndex);
    }

    // ────────────────────────────────────────────────────────────
    //  UPDATE
    // ────────────────────────────────────────────────────────────
    public void update() {
        if (shakeTimer > 0) shakeTimer--;

        player.update();

        // Check player death - only trigger once
        if (!player.isAlive() && player.getDeathTimer() > 50 && !deathHandled) {
            deathHandled = true;
            panel.playerDied();
            return;
        }

        // Normal level
        if (activeBoss == null) {
            currentLevel.updateEnemies();
            checkEnemyCollisions();
            int earned = currentLevel.collectDeadEnemyScores();
            if (earned > 0) player.addScore(earned);
            checkGoal();
        } else {
            // Boss fight
            activeBoss.update();
            checkBossCollisions();

            if (activeBoss.isDefeated() && !bossDefeated) {
                bossDefeated = true;
                bossDefeatTimer = 120;
                triggerScreenShake(30, 8);
            }
            if (bossDefeated && bossDefeatTimer > 0) {
                bossDefeatTimer--;
                if (bossDefeatTimer <= 0) {
                    bossDefeated = false; // prevent re-trigger
                    panel.nextLevel();
                }
            }
        }

        checkSpikes();
        checkLevelBounds();

        // Level complete timer – only fire once
        if (levelComplete) {
            levelCompleteTimer--;
            if (levelCompleteTimer <= 0) {
                levelComplete = false;   // prevent re-trigger
                panel.nextLevel();
            }
        }
    }

    private void checkGoal() {
        if (currentLevel.getGoalRect() == null) return;
        if (!levelComplete && player.getHitbox().intersects(currentLevel.getGoalRect())) {
            levelComplete = true;
            levelCompleteTimer = 90;
        }
    }

    private void checkSpikes() {
        for (Rectangle spike : currentLevel.getSpikes()) {
            if (player.getHitbox().intersects(spike)) {
                player.takeDamage(999); // instant kill
                return;
            }
        }
    }

    private void checkLevelBounds() {
        if (player.getY() > currentLevel.getHeightPx() + 100) {
            player.takeDamage(999);
        }
    }

    // ─── Enemy collision ────────────────────────────────────────
    private void checkEnemyCollisions() {
        Rectangle attackBox = player.getAttackHitbox();

        for (Enemy enemy : currentLevel.getEnemies()) {
            if (enemy.isDying() || !enemy.isAlive()) continue;

            // Player attack hits enemy
            if (attackBox != null && attackBox.intersects(enemy.getHitbox())) {
                enemy.takeDamage(25);
                givePlayerSouls(11);
                // Pogo: if attacking DOWN while above enemy, bounce up
                if (player.getAttackDir() == Player.AttackDir.DOWN && player.getY() < enemy.getY()) {
                    player.setVelY(-11f);
                }
            }

            // Enemy body touches player
            if (!player.isInvincible() && enemy.getBounds().intersects(player.getHitbox())) {
                player.takeDamage(1);
                triggerScreenShake(10, 4);
            }

            // Drone / turret bullets hit player
            checkBulletHits(enemy);

            // Mech shockwave
            if (enemy instanceof MechEnemy mech) {
                if (mech.isShockwaveActive() && mech.getShockwave() != null &&
                    !player.isInvincible() && mech.getShockwave().intersects(player.getHitbox())) {
                    player.takeDamage(1);
                }
            }
        }
    }

    private void checkBulletHits(Enemy enemy) {
        List<Bullet> bullets = null;
        if (enemy instanceof DroneEnemy d)   bullets = d.getBullets();
        if (enemy instanceof TurretEnemy t)  bullets = t.getBullets();
        if (bullets == null) return;

        Rectangle ph = player.getHitbox();
        for (Bullet b : bullets) {
            if (!b.active) continue;
            if (b.getBounds().intersects(ph)) {
                if (!player.isInvincible()) {
                    player.takeDamage(1);
                    triggerScreenShake(8, 3);
                }
                b.active = false;
            }
        }
    }

    // ─── Boss collision ─────────────────────────────────────────
    private void checkBossCollisions() {
        if (activeBoss == null || activeBoss.isDying()) return;

        Rectangle attackBox = player.getAttackHitbox();

        // Player attacks boss
        if (attackBox != null && attackBox.intersects(activeBoss.getHitbox())) {
            activeBoss.takeDamage(20);
            givePlayerSouls(11);
            if (player.getAttackDir() == Player.AttackDir.DOWN &&
                player.getY() < activeBoss.getY()) {
                player.setVelY(-11f);
            }
        }

        // Boss body hits player
        if (!player.isInvincible() && activeBoss.getBounds().intersects(player.getHitbox())) {
            player.takeDamage(1);
            triggerScreenShake(10, 4);
        }

        // Boss projectiles
        if (activeBoss instanceof NeonWraith nw) {
            checkBossNeonWraithHits(nw);
        } else if (activeBoss instanceof ColossusPrime cp) {
            checkBossColossusPrimeHits(cp);
        }
    }

    private void checkBossNeonWraithHits(NeonWraith nw) {
        Rectangle ph = player.getHitbox();
        for (Bullet b : nw.getBullets()) {
            if (b.active && b.getBounds().intersects(ph) && !player.isInvincible()) {
                player.takeDamage(1); triggerScreenShake(8,3); b.active = false;
            }
        }
        for (NeonWraith.HomingBullet h : nw.getHomers()) {
            if (h.active && h.getBounds().intersects(ph) && !player.isInvincible()) {
                player.takeDamage(1); triggerScreenShake(8,3); h.active = false;
            }
        }
        if (nw.isShockwaveActive() && nw.getShockwave() != null &&
            !player.isInvincible() && nw.getShockwave().intersects(ph)) {
            player.takeDamage(1); triggerScreenShake(12, 5);
        }
    }

    private void checkBossColossusPrimeHits(ColossusPrime cp) {
        Rectangle ph = player.getHitbox();
        for (Bullet b : cp.getBullets()) {
            if (b.active && b.getBounds().intersects(ph) && !player.isInvincible()) {
                player.takeDamage(1); triggerScreenShake(8,3); b.active = false;
            }
        }
        if (cp.isShockwaveActive() && cp.getShockwave() != null &&
            !player.isInvincible() && cp.getShockwave().intersects(ph)) {
            player.takeDamage(1); triggerScreenShake(15, 6);
        }
        // Laser
        if (cp.isLasering() && cp.getLaserRect(0,0) != null) {
            Rectangle laser = cp.getLaserRect(0, 0);
            if (cp.getLaserDamageCD() <= 0 && laser.intersects(ph) && !player.isInvincible()) {
                player.takeDamage(1);
                cp.resetLaserDamageCD(30); // laser tick lebih jarang
            }
        }
    }

    // ────────────────────────────────────────────────────────────
    //  DRAW
    // ────────────────────────────────────────────────────────────
    public void draw(Graphics2D g2) {
        int shakeX = shakeTimer > 0 ? (int)(Math.random() * shakeAmt * 2 - shakeAmt) : 0;
        int shakeY = shakeTimer > 0 ? (int)(Math.random() * shakeAmt * 2 - shakeAmt) : 0;

        int camX = camera.getOffsetX() + shakeX;
        int camY = camera.getOffsetY() + shakeY;

        currentLevel.draw(g2, camX, camY, GamePanel.SCREEN_W, GamePanel.SCREEN_H);
        currentLevel.drawEnemies(g2, camX, camY);
        player.draw(g2, camX, camY);

        if (activeBoss != null) {
            activeBoss.draw(g2, camX, camY);
            if (!activeBoss.isIntro() && !activeBoss.isDefeated()) {
                activeBoss.drawHPBar(g2, GamePanel.SCREEN_W, GamePanel.SCREEN_H);
            }
        }

        // Boss intro text
        if (activeBoss != null && activeBoss.isIntro()) {
            drawBossIntro(g2);
        }

        // Level complete flash
        if (levelComplete) {
            float a = 0.4f * (1f - levelCompleteTimer / 90f);
            g2.setColor(new Color(0f, 1f, 0.8f, a));
            g2.fillRect(0, 0, GamePanel.SCREEN_W, GamePanel.SCREEN_H);
        }
    }

    private void drawBossIntro(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, GamePanel.SCREEN_H / 2 - 50, GamePanel.SCREEN_W, 100);

        g2.setFont(new Font("Monospaced", Font.BOLD, 28));
        String name = activeBoss.getName();
        int tw = g2.getFontMetrics().stringWidth(name);
        g2.setColor(Color.BLACK);
        g2.drawString(name, GamePanel.SCREEN_W / 2 - tw / 2 + 2, GamePanel.SCREEN_H / 2 + 2);
        g2.setColor(activeBoss instanceof NeonWraith ? CyberColors.BOSS1_GLOW : CyberColors.BOSS2_GLOW);
        g2.drawString(name, GamePanel.SCREEN_W / 2 - tw / 2, GamePanel.SCREEN_H / 2);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 14));
        String sub = activeBoss instanceof NeonWraith ? "— Phantom Protocol Activated —" : "— Omega Directive: Annihilate —";
        int sw = g2.getFontMetrics().stringWidth(sub);
        g2.setColor(CyberColors.UI_NORMAL);
        g2.drawString(sub, GamePanel.SCREEN_W / 2 - sw / 2, GamePanel.SCREEN_H / 2 + 28);
    }

    // ── Helpers ───────────────────────────────────────────────
    private void triggerScreenShake(int frames, int amount) {
        shakeTimer = frames; shakeAmt = amount;
    }

    private void givePlayerSouls(int n) {
        player.addSouls(n);
    }

    public Level getCurrentLevel()     { return currentLevel; }
    public int getCurrentLevelIndex()  { return currentIndex; }
    public Boss getActiveBoss()        { return activeBoss; }
    public boolean isBossLevel()       { return activeBoss != null; }
}