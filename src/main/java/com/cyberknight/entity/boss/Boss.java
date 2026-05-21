package com.cyberknight.entity.boss;

import com.cyberknight.entity.Entity;
import com.cyberknight.entity.Player;
import com.cyberknight.level.Level;
import java.awt.*;

/**
 * Abstract Boss base.
 * Bosses have phases, a name, and a dedicated HP bar drawn on screen.
 * Combat pattern inspired by Hollow Knight (phase transitions, telegraphed attacks).
 */
public abstract class Boss extends Entity {

    protected String name;
    protected int    phase         = 1;
    protected int    maxPhases;
    protected Player player;
    protected Level  level;

    // Intro
    protected boolean introPlaying = true;
    protected int     introTimer   = 120;

    // Death
    protected boolean dying        = false;
    protected int     deathTimer   = 0;
    protected int     deathFrames  = 180; // 3 seconds

    // Arena bounds (boss stays within these)
    protected int arenaLeft, arenaRight;

    protected static final float GRAVITY    = 0.55f;
    protected static final float MAX_FALL   = 16f;
    protected static final int   IFRAME_MAX = 10;

    public Boss(float x, float y, int w, int h, int hp, String name, int phases) {
        super(x, y, w, h, hp);
        this.name      = name;
        this.maxPhases = phases;
        this.iFrameMax = IFRAME_MAX;
    }

    public void setPlayer(Player p) { this.player = p; }
    public void setLevel(Level l)   { this.level  = l; }

    @Override
    public void update() {
        if (dying) { deathTimer++; return; }
        if (!alive) { dying = true; return; }
        updateIFrames();

        if (introPlaying) {
            introTimer--;
            if (introTimer <= 0) introPlaying = false;
            return;
        }

        checkPhaseTransition();
        updateBossAI();
        applyPhysics();
        if (level != null) resolveCollisions();
    }

    protected abstract void updateBossAI();
    protected abstract void checkPhaseTransition();

    protected void applyPhysics() {
        velY = Math.min(velY + GRAVITY, MAX_FALL);
        x += velX;
        y += velY;
        // Clamp to arena
        if (x < arenaLeft)  { x = arenaLeft;  velX = 0; }
        if (x + width > arenaRight) { x = arenaRight - width; velX = 0; }
    }

    protected void resolveCollisions() {
        onGround = false;
        for (Rectangle tile : level.getSolidTiles()) {
            Rectangle b = getBounds();
            if (!b.intersects(tile)) continue;
            float ot = (b.y + b.height) - tile.y;
            float ob = (tile.y + tile.height) - b.y;
            float ol = (b.x + b.width)  - tile.x;
            float or2= (tile.x + tile.width)  - b.x;
            float mx = Math.min(ol, or2);
            float my = Math.min(ot, ob);
            if (my < mx) {
                if (ot < ob) { y = tile.y - height; velY = 0; onGround = true; }
                else         { y = tile.y + tile.height; velY = 0; }
            }
        }
    }

    public boolean isDefeated()   { return dying && deathTimer >= deathFrames; }
    public boolean isDying()      { return dying; }
    public boolean isIntro()      { return introPlaying; }
    public int     getPhase()     { return phase; }
    public String  getName()      { return name; }
    public int     getIntroTimer(){ return introTimer; }

    /** Draw the boss HP bar at the bottom of the screen. */
    public void drawHPBar(Graphics2D g2, int screenW, int screenH) {
        int barW = screenW - 100;
        int barH = 18;
        int barX = 50;
        int barY = screenH - 50;

        // Background
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(barX - 4, barY - 24, barW + 8, barH + 28);

        // Name
        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2.setColor(getBossColor());
        g2.drawString(name + (maxPhases > 1 ? "  [Phase " + phase + "/" + maxPhases + "]" : ""), barX, barY - 6);

        // HP bar bg
        g2.setColor(new Color(0x33, 0x00, 0x00));
        g2.fillRect(barX, barY, barW, barH);

        // HP fill
        float pct = (float) currentHealth / maxHealth;
        g2.setColor(getBossColor());
        g2.fillRect(barX, barY, (int)(barW * pct), barH);

        // Shine
        g2.setColor(new Color(255, 255, 255, 40));
        g2.fillRect(barX, barY, (int)(barW * pct), barH / 2);

        // Border
        g2.setColor(getBossColor().darker());
        g2.drawRect(barX, barY, barW, barH);
    }

    protected abstract Color getBossColor();
}