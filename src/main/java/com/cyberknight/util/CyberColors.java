package com.cyberknight.util;

import java.awt.Color;

/**
 * Centralised cyberpunk colour palette.
 * Neon cyan / magenta / amber on deep dark backgrounds.
 */
public final class CyberColors {

    private CyberColors() {}

    // ── Background ────────────────────────────────────────────
    public static final Color BG_DEEP    = new Color(0x05, 0x05, 0x14);
    public static final Color BG_MID     = new Color(0x0A, 0x0A, 0x22);
    public static final Color BG_FAR     = new Color(0x0D, 0x0B, 0x2B);

    // ── Tiles ─────────────────────────────────────────────────
    public static final Color TILE_BASE  = new Color(0x12, 0x12, 0x30);
    public static final Color TILE_EDGE  = new Color(0x00, 0xFF, 0xD5);   // neon cyan
    public static final Color TILE_GLOW  = new Color(0x00, 0xFF, 0xD5, 60);
    public static final Color TILE_DARK  = new Color(0x08, 0x08, 0x1A);
    public static final Color TILE_ALT   = new Color(0x1A, 0x0A, 0x2E);

    // ── Player ────────────────────────────────────────────────
    public static final Color PLAYER_BODY  = new Color(0xCC, 0xCC, 0xFF);
    public static final Color PLAYER_CLOAK = new Color(0x22, 0x22, 0x44);
    public static final Color PLAYER_VISOR = new Color(0x00, 0xFF, 0xFF);
    public static final Color PLAYER_LEG   = new Color(0x33, 0x33, 0x66);
    public static final Color NAIL_COLOR   = new Color(0x00, 0xFF, 0xD5);
    public static final Color DASH_GLOW    = new Color(0xFF, 0x55, 0x00);

    // ── Soul / Heal ───────────────────────────────────────────
    public static final Color SOUL_BLUE   = new Color(0x55, 0xAA, 0xFF);
    public static final Color SOUL_GLOW   = new Color(0x55, 0xAA, 0xFF, 120);

    // ── Enemies ───────────────────────────────────────────────
    public static final Color GRUNT_BODY  = new Color(0x44, 0x11, 0x11);
    public static final Color GRUNT_EYE   = new Color(0xFF, 0x22, 0x22);
    public static final Color DRONE_BODY  = new Color(0x11, 0x22, 0x44);
    public static final Color DRONE_GLOW  = new Color(0x00, 0x88, 0xFF);
    public static final Color TURRET_BODY = new Color(0x33, 0x33, 0x33);
    public static final Color TURRET_GLOW = new Color(0xFF, 0xFF, 0x00);
    public static final Color MECH_BODY   = new Color(0x22, 0x44, 0x22);
    public static final Color MECH_GLOW   = new Color(0x00, 0xFF, 0x44);
    public static final Color ASSASSIN    = new Color(0x44, 0x00, 0x44);
    public static final Color ASSASSIN_GL = new Color(0xFF, 0x00, 0xFF);

    // ── Bosses ────────────────────────────────────────────────
    public static final Color BOSS1_BODY  = new Color(0x55, 0x00, 0x88);
    public static final Color BOSS1_GLOW  = new Color(0xFF, 0x00, 0xFF);
    public static final Color BOSS2_BODY  = new Color(0x88, 0x22, 0x00);
    public static final Color BOSS2_GLOW  = new Color(0xFF, 0x66, 0x00);
    public static final Color BOSS_HP_BG  = new Color(0x33, 0x00, 0x00);
    public static final Color BOSS_HP_FG  = new Color(0xFF, 0x00, 0x44);

    // ── HUD ───────────────────────────────────────────────────
    public static final Color HUD_BG      = new Color(0x00, 0x00, 0x00, 180);
    public static final Color HP_RED      = new Color(0xFF, 0x22, 0x22);
    public static final Color HP_DARK     = new Color(0x44, 0x00, 0x00);
    public static final Color SOUL_COLOR  = new Color(0x44, 0xAA, 0xFF);
    public static final Color NEON_CYAN   = new Color(0x00, 0xFF, 0xD5);
    public static final Color NEON_PINK   = new Color(0xFF, 0x00, 0xFF);
    public static final Color NEON_AMBER  = new Color(0xFF, 0xCC, 0x00);
    public static final Color SCORE_CLR   = new Color(0xFF, 0xCC, 0x00);

    // ── UI ────────────────────────────────────────────────────
    public static final Color UI_TITLE    = new Color(0x00, 0xFF, 0xFF);
    public static final Color UI_SELECT   = new Color(0xFF, 0x00, 0xFF);
    public static final Color UI_NORMAL   = new Color(0xCC, 0xCC, 0xFF);
    public static final Color UI_SHADOW   = new Color(0x00, 0x00, 0x00, 200);
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    // ── Projectiles ───────────────────────────────────────────
    public static final Color BULLET_ENEMY = new Color(0xFF, 0x44, 0x44);
    public static final Color BULLET_BOSS  = new Color(0xFF, 0x00, 0x88);
}