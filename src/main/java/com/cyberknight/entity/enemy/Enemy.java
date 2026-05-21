package com.cyberknight.entity.enemy;

import com.cyberknight.entity.Entity;
import com.cyberknight.entity.Player;
import com.cyberknight.level.Level;

import java.awt.*;

/**
 * Abstract base for all grunt enemies.
 * Subclasses implement behaviour via updateAI().
 */
public abstract class Enemy extends Entity {

    protected Player  player;
    protected Level   level;
    protected int     scoreValue;
    protected int     attackDamage;
    protected boolean aggro       = false;
    protected float   aggroRange  = 300f;
    protected boolean knockedBack = false;
    protected int     knockTimer  = 0;
    protected static final float GRAVITY = 0.55f;
    protected static final float MAX_FALL = 12f;

    // death animation
    protected boolean dying       = false;
    protected int     deathTimer  = 0;
    protected int     deathFrames = 40;

    public Enemy(float x, float y, int w, int h, int hp, int score, int dmg) {
        super(x, y, w, h, hp);
        this.scoreValue  = score;
        this.attackDamage= dmg;
        this.iFrameMax   = 20;
    }

    public void setPlayerRef(Player p)  { this.player = p; }
    public void setLevelRef(Level l)    { this.level  = l; }

    @Override
    public void update() {
        updateIFrames();
        if (dying) {
            deathTimer++;
            return;
        }
        if (!alive) { dying = true; return; }

        checkAggro();
        if (knockedBack) {
            updateKnockback();
        } else {
            updateAI();
        }
        applyGravity();
        if (level != null) resolveCollisions();
    }

    protected abstract void updateAI();

    protected void checkAggro() {
        if (player == null) return;
        float dx = player.getX() - x;
        float dy = player.getY() - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        aggro = dist < aggroRange;
        if (aggro) facingRight = dx > 0;
    }

    protected void applyGravity() {
        velY = Math.min(velY + GRAVITY, MAX_FALL);
        y += velY;
        x += velX;
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
            } else {
                if (ol < or2) { x = tile.x - width; velX = 0; }
                else          { x = tile.x + tile.width; velX = 0; }
                reverseDirection();
            }
        }
    }

    protected void reverseDirection() { facingRight = !facingRight; }

    protected void updateKnockback() {
        knockTimer--;
        velX *= 0.8f;
        if (knockTimer <= 0) { knockedBack = false; velX = 0; }
    }

    @Override
    public void takeDamage(int amount) {
        super.takeDamage(amount);
        if (alive) {
            knockedBack = true;
            knockTimer  = 15;
            velX = (player != null && player.getX() < x) ? 4f : -4f;
            velY = -3f;
        }
    }

    public boolean isFullyDead()  { return dying && deathTimer >= deathFrames; }
    public int  getScoreValue()   { return scoreValue; }
    public int  getAttackDamage() { return attackDamage; }
    public boolean isDying()      { return dying; }
}