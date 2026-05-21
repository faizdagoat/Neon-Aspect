package com.cyberknight.entity;

import java.awt.*;

/**
 * Base class for every game entity (Player, Enemy, Boss, Projectile).
 * Demonstrates OOP: encapsulation, abstraction, inheritance.
 */
public abstract class Entity {

    // Position & size
    protected float x, y;
    protected int width, height;

    // Physics
    protected float velX, velY;
    protected boolean onGround;
    protected boolean facingRight = true;

    // Stats
    protected int maxHealth;
    protected int currentHealth;
    protected boolean alive = true;

    // Invincibility frames
    protected int iFrames    = 0;
    protected int iFrameMax  = 60; // 1 second at 60fps

    public Entity(float x, float y, int width, int height, int maxHealth) {
        this.x = x; this.y = y;
        this.width = width; this.height = height;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    // ── Abstract interface ────────────────────────────────────
    public abstract void update();
    public abstract void draw(Graphics2D g2, int camX, int camY);

    // ── Physics ───────────────────────────────────────────────
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, width, height);
    }

    public Rectangle getHitbox() {
        // Slightly inset hitbox for fairness
        return new Rectangle((int)x + 4, (int)y + 4, width - 8, height - 4);
    }

    // ── Damage ────────────────────────────────────────────────
    public void takeDamage(int amount) {
        if (iFrames > 0 || !alive) return;
        currentHealth -= amount;
        iFrames = iFrameMax;
        if (currentHealth <= 0) {
            currentHealth = 0;
            alive = false;
            onDeath();
        } else {
            onHurt();
        }
    }

    protected void onDeath() {}
    protected void onHurt()  {}

    protected void updateIFrames() {
        if (iFrames > 0) iFrames--;
    }

    // ── Getters / Setters ────────────────────────────────────
    public float getX()          { return x; }
    public float getY()          { return y; }
    public int   getWidth()      { return width; }
    public int   getHeight()     { return height; }
    public float getVelX()       { return velX; }
    public float getVelY()       { return velY; }
    public boolean isOnGround()  { return onGround; }
    public boolean isFacingRight(){ return facingRight; }
    public int  getMaxHealth()   { return maxHealth; }
    public int  getCurrentHealth(){ return currentHealth; }
    public boolean isAlive()     { return alive; }
    public boolean isInvincible(){ return iFrames > 0; }

    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setVelX(float v) { this.velX = v; }
    public void setVelY(float v) { this.velY = v; }
    public void setOnGround(boolean b) { this.onGround = b; }

    public void heal(int amount) {
        currentHealth = Math.min(currentHealth + amount, maxHealth);
    }
}