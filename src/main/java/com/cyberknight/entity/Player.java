package com.cyberknight.entity;

import com.cyberknight.core.InputHandler;
import com.cyberknight.level.Level;
import com.cyberknight.util.CyberColors;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Player entity.
 * Movement inspired by Hollow Knight:
 *  - Variable jump height (hold for higher)
 *  - Wall slide / wall jump
 *  - Dash (with cooldown)
 *  - 4-directional attack
 *  - Nail (melee) pogo on enemies
 */
public class Player extends Entity {

    // ── Physics constants (Hollow Knight feel) ────────────────
    private static final float GRAVITY        =  0.55f;
    private static final float MAX_FALL_SPEED =  12f;
    private static final float WALK_SPEED     =  4.2f;
    private static final float JUMP_FORCE     = -13f;
    private static final float WALL_JUMP_X    =  6f;
    private static final float WALL_JUMP_Y    = -11f;
    private static final float DASH_SPEED     =  14f;
    private static final int   DASH_DURATION  =  14;   // frames
    private static final int   DASH_COOLDOWN  =  40;
    private static final float ACCEL          =  0.8f;
    private static final float DECEL          =  0.75f; // friction

    // ── Jump buffering (Hollow Knight quality-of-life) ────────
    private static final int JUMP_BUFFER_FRAMES  = 8;
    private static final int COYOTE_FRAMES       = 6;

    // ── Stats ─────────────────────────────────────────────────
    public static final int MAX_HP      = 3;   // 3 hit = mati
    private static final int MAX_SOULS  = 99;
    private static final int HEAL_SOUL_COST = 33;

    // ── State ─────────────────────────────────────────────────
    private int  souls;
    private int  score;

    // Jump / coyote
    private int  jumpBuffer  = 0;
    private int  coyoteTime  = 0;
    private boolean canDoubleJump = false;
    private boolean hasDoubleJump = false;
    private boolean usedDoubleJump = false;

    // Dash
    private boolean isDashing     = false;
    private int     dashTimer     = 0;
    private int     dashCooldown  = 0;
    private float   dashDir       = 1f;

    // Wall
    private boolean onWallLeft    = false;
    private boolean onWallRight   = false;
    private boolean wallSliding   = false;

    // Attack
    private boolean attacking      = false;
    private int     attackTimer    = 0;
    private static final int ATTACK_DURATION = 18;
    private AttackDir attackDir    = AttackDir.RIGHT;
    private List<Rectangle> attackHitboxHistory = new ArrayList<>();

    // Heal
    private boolean healing       = false;
    private int     healTimer     = 0;
    private static final int HEAL_DURATION = 90;

    // Death / respawn
    private boolean dead          = false;
    private int     deathTimer    = 0;

    // Visual (lama - fallback jika sprite gagal load)
    private int     animFrame     = 0;
    private int     animTimer     = 0;
    private float   squashX       = 1f;
    private float   squashY       = 1f;

    // ── Sprite Animation ──────────────────────────────────────
    private com.cyberknight.util.SpriteSheet sheetIdle;
    private com.cyberknight.util.SpriteSheet sheetDash;
    private com.cyberknight.util.SpriteSheet sheetSlash;
    private com.cyberknight.util.Animation   animIdle;
    private com.cyberknight.util.Animation   animDash;
    private com.cyberknight.util.Animation   animSlash;
    private com.cyberknight.util.Animation   currentAnim;
    private boolean spritesLoaded = false;

    // Ukuran render sprite — ubah nilai ini untuk resize visual karakter
    public static final int SPRITE_SIZE = 96;

    // Hitbox TERPISAH dari sprite — harus < TILE_SIZE (48px) agar bisa masuk celah 1 tile
    public static final int HITBOX_W = 28;  // lebar hitbox
    public static final int HITBOX_H = 44;  // tinggi hitbox (< 48 tile size)

    // Offset sprite relatif terhadap hitbox
    // Kaki sprite harus sejajar kaki hitbox → offset Y negatif ke atas
    public static final int SPRITE_OFFSET_X = -(SPRITE_SIZE / 2 - HITBOX_W / 2); // center horizontal
    public static final int SPRITE_OFFSET_Y = -(SPRITE_SIZE - HITBOX_H);          // kaki sejajar

    private InputHandler input;
    private Level currentLevel;

    public enum AttackDir { LEFT, RIGHT, UP, DOWN }

    // ── Constructor ───────────────────────────────────────────
    public Player(float x, float y, InputHandler input) {
        super(x, y, HITBOX_W, HITBOX_H, MAX_HP);
        this.input  = input;
        this.souls  = 0;
        this.score  = 0;
        this.iFrameMax = 30;
        loadSprites();
    }

    private void loadSprites() {
        try {
            sheetIdle  = new com.cyberknight.util.SpriteSheet("sprites/player/idle.png");
            sheetDash  = new com.cyberknight.util.SpriteSheet("sprites/player/dash.png");
            sheetSlash = new com.cyberknight.util.SpriteSheet("sprites/player/slash.png");

            // Idle: 8 frame, loop, lambat (8 frame delay)
            animIdle  = new com.cyberknight.util.Animation(sheetIdle,  8,  true);
            // Dash: 12 frame, loop, cepat (3 frame delay)
            animDash  = new com.cyberknight.util.Animation(sheetDash,  3,  true);
            // Slash: 21 frame, one-shot, sedang (3 frame delay)
            animSlash = new com.cyberknight.util.Animation(sheetSlash, 3, false);
            animSlash.setOnFinished(() -> attacking = false);

            currentAnim  = animIdle;
            spritesLoaded = true;
        } catch (Exception e) {
            spritesLoaded = false;
            System.err.println("[Player] Sprite load failed: " + e.getMessage());
        }
    }

    public void setLevel(Level level) { this.currentLevel = level; }

    // ────────────────────────────────────────────────────────────
    //  UPDATE
    // ────────────────────────────────────────────────────────────
    @Override
    public void update() {
        if (!alive) {
            handleDeath();
            return;
        }
        updateIFrames();
        handleInput();
        applyPhysics();
        if (currentLevel != null) resolveCollisions();
        updateAnimation();
        updateSquash();
    }

    private void handleInput() {
        if (isDashing) return; // no input override during dash

        // ── Horizontal movement ──────────────────────────────
        if (input.left) {
            velX = Math.max(velX - ACCEL, -WALK_SPEED);
            facingRight = false;
        } else if (input.right) {
            velX = Math.min(velX + ACCEL, WALK_SPEED);
            facingRight = true;
        } else {
            velX *= DECEL;
            if (Math.abs(velX) < 0.1f) velX = 0;
        }

        // ── Jump buffer ──────────────────────────────────────
        if (input.consumeJump()) jumpBuffer = JUMP_BUFFER_FRAMES;
        if (jumpBuffer > 0) jumpBuffer--;

        // ── Wall slide ───────────────────────────────────────
        wallSliding = false;
        if (!onGround && velY > 0) {
            if ((onWallLeft && input.left) || (onWallRight && input.right)) {
                wallSliding = true;
                velY = Math.min(velY, 2.0f); // slow fall
            }
        }

        // ── Jump (coyote + buffer) ───────────────────────────
        if (jumpBuffer > 0 && (onGround || coyoteTime > 0)) {
            doJump(JUMP_FORCE);
            jumpBuffer = 0;
        }
        // Wall jump
        else if (jumpBuffer > 0 && wallSliding) {
            velY = WALL_JUMP_Y;
            velX = onWallLeft ? WALL_JUMP_X : -WALL_JUMP_X;
            facingRight = onWallLeft;
            jumpBuffer = 0;
            squash(1.3f, 0.7f);
        }
        // Double jump
        else if (jumpBuffer > 0 && !onGround && !usedDoubleJump && canDoubleJump) {
            doJump(JUMP_FORCE * 0.9f);
            usedDoubleJump = true;
            jumpBuffer = 0;
        }

        // Variable jump height: release to cut velocity
        if (!input.jumpHeld && velY < -4f) velY += 1.5f;

        // ── Coyote time ──────────────────────────────────────
        if (onGround) coyoteTime = COYOTE_FRAMES;
        else if (coyoteTime > 0) coyoteTime--;

        // ── Dash ─────────────────────────────────────────────
        if (input.consumeDash() && dashCooldown == 0 && !wallSliding) {
            startDash();
        }
        if (dashCooldown > 0) dashCooldown--;

        // ── Attack ───────────────────────────────────────────
        if (input.consumeAttack() && !attacking && !healing) {
            startAttack();
        }
        if (attacking) {
            attackTimer--;
            if (attackTimer <= 0) attacking = false;
        }

        // ── Heal ─────────────────────────────────────────────
        if (input.consumeHeal() && souls >= HEAL_SOUL_COST && !attacking && onGround) {
            startHeal();
        }
        if (healing) {
            healTimer--;
            if (healTimer <= 0) {
                healing = false;
                if (souls >= HEAL_SOUL_COST) {
                    heal(1);  // +1 HP (max 3)
                    souls -= HEAL_SOUL_COST;
                }
            }
        }
    }

    private void doJump(float force) {
        velY = force;
        usedDoubleJump = false;
        onGround = false;
        squash(0.7f, 1.4f);
    }

    private void startDash() {
        isDashing   = true;
        dashTimer   = DASH_DURATION;
        dashDir     = facingRight ? 1f : -1f;
        dashCooldown= DASH_COOLDOWN;
        iFrames     = DASH_DURATION + 5; // brief invincibility
        velY        = 0;
        squash(1.5f, 0.6f);
    }

    private void startAttack() {
        attacking    = true;
        attackTimer  = ATTACK_DURATION;
        if (input.up)        attackDir = AttackDir.UP;
        else if (input.down && !onGround) attackDir = AttackDir.DOWN;
        else                 attackDir = facingRight ? AttackDir.RIGHT : AttackDir.LEFT;
        // Reset animasi slash agar mulai dari frame 0
        if (spritesLoaded && animSlash != null) animSlash.reset();
    }

    private void startHeal() {
        healing   = true;
        healTimer = HEAL_DURATION;
        velX      = 0;
    }

    private void applyPhysics() {
        if (isDashing) {
            velX = DASH_SPEED * dashDir;
            velY = 0;
            dashTimer--;
            if (dashTimer <= 0) isDashing = false;
        } else {
            // Gravity
            velY = Math.min(velY + GRAVITY, MAX_FALL_SPEED);
        }

        x += velX;
        y += velY;
    }

    private void resolveCollisions() {
        onGround     = false;
        onWallLeft   = false;
        onWallRight  = false;

        for (Rectangle tile : currentLevel.getSolidTiles()) {
            Rectangle bounds = getBounds();

            if (!bounds.intersects(tile)) continue;

            float overlapLeft  = (bounds.x + bounds.width)  - tile.x;
            float overlapRight = (tile.x  + tile.width)     - bounds.x;
            float overlapTop   = (bounds.y + bounds.height) - tile.y;
            float overlapBot   = (tile.y  + tile.height)    - bounds.y;

            float minX = Math.min(overlapLeft, overlapRight);
            float minY = Math.min(overlapTop,  overlapBot);

            if (minY < minX) {
                // Vertical
                if (overlapTop < overlapBot) {
                    y = tile.y - height;
                    velY = 0;
                    onGround = true;
                    usedDoubleJump = false;
                } else {
                    y = tile.y + tile.height;
                    velY = 0;
                }
            } else {
                // Horizontal
                if (overlapLeft < overlapRight) {
                    x = tile.x - width;
                    onWallRight = true;
                } else {
                    x = tile.x + tile.width;
                    onWallLeft = true;
                }
                velX = 0;
            }
        }

        // Fall through bottom → die
        if (y > currentLevel.getHeightPx() + 200) {
            alive = false;
            onDeath();
        }
    }

    private void updateAnimation() {
        if (!spritesLoaded) {
            // Fallback animasi lama
            animTimer++;
            int speed = isDashing ? 2 : (Math.abs(velX) > 0.5f ? 6 : 10);
            if (animTimer >= speed) { animTimer = 0; animFrame = (animFrame + 1) % 4; }
            return;
        }

        // Pilih animasi berdasarkan state
        com.cyberknight.util.Animation next;
        if (attacking) {
            next = animSlash;
        } else if (isDashing) {
            next = animDash;
        } else {
            next = animIdle;
        }

        // Ganti animasi jika berbeda — reset agar mulai dari frame 0
        if (next != currentAnim) {
            currentAnim = next;
            currentAnim.reset();
        }

        currentAnim.update();
    }

    private void updateSquash() {
        squashX += (1f - squashX) * 0.2f;
        squashY += (1f - squashY) * 0.2f;
    }

    private void squash(float sx, float sy) {
        squashX = sx; squashY = sy;
    }

    private void handleDeath() {
        deathTimer++;
        if (deathTimer > 60) {
            // Notify panel (done via level manager poll)
        }
    }

    @Override
    protected void onDeath() {
        if (dead) return; // prevent double trigger
        dead = true;
        deathTimer = 0;
        iFrames = 0;
    }

    // ────────────────────────────────────────────────────────────
    //  DRAW
    // ────────────────────────────────────────────────────────────
    @Override
    public void draw(Graphics2D g2, int camX, int camY) {
        // Invincibility flicker
        if (iFrames > 0 && (iFrames / 5) % 2 == 0) return;

        int drawX = (int)(x - camX);
        int drawY = (int)(y - camY);

        if (spritesLoaded && currentAnim != null) {
            drawSprite(g2, drawX, drawY);
        } else {
            drawFallback(g2, drawX, drawY);
        }
    }

    private void drawSprite(Graphics2D g2, int drawX, int drawY) {
        java.awt.image.BufferedImage frame = currentAnim.getCurrentFrame();
        if (frame == null) return;

        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int sW = (int)(SPRITE_SIZE * squashX);
        int sH = (int)(SPRITE_SIZE * squashY);

        // Posisi sprite: kaki sprite (sy+sH) = kaki hitbox (drawY+height)
        int sx = drawX + SPRITE_OFFSET_X;
        int sy = drawY + SPRITE_OFFSET_Y;

        if (!facingRight) {
            g.translate(sx + sW, sy);
            g.scale(-1, 1);
            g.drawImage(frame, 0, 0, sW, sH, null);
        } else {
            g.drawImage(frame, sx, sy, sW, sH, null);
        }

        // Dash trail
        if (isDashing) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            int trailOffset = facingRight ? -sW / 2 : sW / 2;
            if (!facingRight) {
                g.drawImage(frame, trailOffset, 0, sW, sH, null);
            } else {
                g.drawImage(frame, sx + trailOffset, sy, sW, sH, null);
            }
        }

        // Heal glow
        if (healing) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            g.setColor(CyberColors.SOUL_BLUE);
            g.fillOval(sx - 6, sy, sW + 12, sH);
        }

        g.dispose();
    }

    /** Fallback drawing (jika sprite tidak bisa diload) */
    private void drawFallback(Graphics2D g2, int drawX, int drawY) {
        int sW = (int)(width  * squashX);
        int sH = (int)(height * squashY);
        int offX = (width  - sW) / 2;
        int offY = height - sH;

        Graphics2D g = (Graphics2D) g2.create();
        g.translate(drawX + offX, drawY + offY);

        g.setColor(healing ? CyberColors.SOUL_BLUE : CyberColors.PLAYER_BODY);
        g.fillRect(0, sH / 4, sW, sH * 3 / 4);
        g.setColor(CyberColors.PLAYER_CLOAK);
        g.fillRect(0, sH / 2, sW, sH / 2);
        g.setColor(CyberColors.PLAYER_BODY);
        g.fillOval(sW / 6, 0, sW * 2 / 3, sH / 3);
        g.setColor(isDashing ? CyberColors.DASH_GLOW : CyberColors.PLAYER_VISOR);
        g.fillRect(sW / 4, sH / 12, sW / 2, sH / 10);

        if (attacking) {
            g.setColor(CyberColors.NAIL_COLOR);
            float progress = 1f - (attackTimer / (float) ATTACK_DURATION);
            int len = (int)(40 * Math.sin(progress * Math.PI));
            switch (attackDir) {
                case RIGHT -> g.fillRect(sW, sH / 2 - 3, len, 6);
                case LEFT  -> g.fillRect(-len, sH / 2 - 3, len, 6);
                case UP    -> g.fillRect(sW / 2 - 3, -len, 6, len);
                case DOWN  -> g.fillRect(sW / 2 - 3, sH, 6, len);
            }
        }
        g.dispose();
    }

    // ────────────────────────────────────────────────────────────
    //  PUBLIC API
    // ────────────────────────────────────────────────────────────
    public Rectangle getAttackHitbox() {
        if (!attacking) return null;
        int ax = (int)x, ay = (int)y;
        return switch (attackDir) {
            case RIGHT -> new Rectangle(ax + width, ay + height / 4, 48, 24);
            case LEFT  -> new Rectangle(ax - 48,    ay + height / 4, 48, 24);
            case UP    -> new Rectangle(ax + 4,     ay - 48,         24, 48);
            case DOWN  -> new Rectangle(ax + 4,     ay + height,     24, 48);
        };
    }

    public boolean isAttacking()       { return attacking; }
    public AttackDir getAttackDir()    { return attackDir; }
    public int getSouls()              { return souls; }
    public int getScore()              { return score; }
    public boolean isDashing()         { return isDashing; }
    public boolean isHealing()         { return healing; }
    public boolean isDead()            { return dead; }
    public int getDeathTimer()         { return deathTimer; }

    public void addSouls(int amount)   { souls = Math.min(souls + amount, MAX_SOULS); }
    public void addScore(int amount)   { score += amount; }
    public void unlockDoubleJump()     { canDoubleJump = true; }

    public void fullHeal() {
        currentHealth = MAX_HP;
        souls = 0;
    }

    public void resetPosition(float nx, float ny) {
        x = nx; y = ny;
        velX = 0; velY = 0;
        alive = true; dead = false;
        deathTimer = 0;
        attacking = false; healing = false;
        isDashing = false;
        dashCooldown = 0;
        iFrames = 30;
        width  = HITBOX_W;
        height = HITBOX_H;
        currentHealth = MAX_HP;
        souls = 0;
    }

    /** Beri invincibility 3 detik saat pertama spawn — cegah langsung kena enemy */
    public void grantSpawnIFrames() {
        iFrames = 180; // 3 detik
    }

    public static int getMaxSouls() { return MAX_SOULS; }
}