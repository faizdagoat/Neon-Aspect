package com.cyberknight.level;

import com.cyberknight.entity.enemy.Enemy;
import com.cyberknight.entity.enemy.DroneEnemy;
import com.cyberknight.entity.enemy.TurretEnemy;
import com.cyberknight.entity.enemy.MechEnemy;
import com.cyberknight.entity.enemy.GruntEnemy;
import com.cyberknight.entity.enemy.AssassinEnemy;
import com.cyberknight.entity.Player;
import com.cyberknight.util.CyberColors;
import java.awt.*;

/**
 * Static factory that builds each Level by hand.
 * Levels 1-3 are normal; Level 4 = Boss 1; Level 5 = final Boss 2.
 * (Normal levels 1,2,3 lead to boss levels 4 and 5.)
 * Tile codes: 0=air, 1=solid, 3=spike, 4=goal, 5=deco
 */
public class LevelFactory {

    private LevelFactory() {}

    public static Level build(int index, Player player) {
        return switch (index) {
            case 1  -> buildLevel1(player);
            case 2  -> buildLevel2(player);
            case 3  -> buildLevel3(player);
            case 4  -> buildBossLevel1(player);
            case 5  -> buildBossLevel2(player);
            default -> buildLevel1(player);
        };
    }

    // ─────────────────────────────────────────────────────────
    //  LEVEL 1 – Neon Slums (tutorial-ish, grunts + drones)
    // ─────────────────────────────────────────────────────────
    private static Level buildLevel1(Player player) {
        int[][] map = {
//           0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25
/* 0 */  {  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
/* 1 */  {  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
/* 2 */  {  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
/* 3 */  {  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
/* 4 */  {  0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
/* 5 */  {  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
/* 6 */  {  0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0 },
/* 7 */  {  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
/* 8 */  {  0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4 },
/* 9 */  {  0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
/* 10 */ {  1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
/* 11 */ {  1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
/* 12 */ {  1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
/* 13 */ {  1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
        };

        Level lvl = new Level("Neon Slums", 1, map,
            CyberColors.BG_DEEP, CyberColors.TILE_EDGE);

        lvl.addParallaxLayer(new ParallaxLayer(0.1f, new Color(0x00, 0x44, 0x66, 80), 0, 1L));
        lvl.addParallaxLayer(new ParallaxLayer(0.3f, new Color(0x00, 0x22, 0x44, 60), 1, 2L));
        lvl.addParallaxLayer(new ParallaxLayer(0.15f, new Color(0x00, 0xFF, 0xD5, 20), 2, 3L));

        // Enemies – grunt patrols + one drone
        // Spawn mulai dari x=400 agar tidak langsung kontak dengan player (x=100)
        GruntEnemy g1 = new GruntEnemy(400,  432); setEnemyRefs(g1, player, lvl);
        GruntEnemy g2 = new GruntEnemy(580,  432); setEnemyRefs(g2, player, lvl);
        GruntEnemy g3 = new GruntEnemy(780,  432); setEnemyRefs(g3, player, lvl);
        DroneEnemy d1 = new DroneEnemy(700,  260); setEnemyRefs(d1, player, lvl);
        GruntEnemy g4 = new GruntEnemy(950,  432); setEnemyRefs(g4, player, lvl);
        GruntEnemy g5 = new GruntEnemy(1150, 432); setEnemyRefs(g5, player, lvl);

        lvl.addEnemy(g1); lvl.addEnemy(g2); lvl.addEnemy(g3);
        lvl.addEnemy(d1); lvl.addEnemy(g4); lvl.addEnemy(g5);
        return lvl;
    }

    // ─────────────────────────────────────────────────────────
    //  LEVEL 2 – Digital Ruins (platforms, turrets, assassins)
    // ─────────────────────────────────────────────────────────
    private static Level buildLevel2(Player player) {
        int[][] map = {
/* 0 */  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 1 */  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 2 */  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 3 */  {0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 4 */  {0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,1,1,0,0,0,0},
/* 5 */  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 6 */  {0,0,1,1,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0},
/* 7 */  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 8 */  {0,0,0,0,0,0,0,3,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4},
/* 9 */  {1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1},
/*10 */  {1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1},
/*11 */  {1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1},
/*12 */  {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
/*13 */  {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        };

        Level lvl = new Level("Digital Ruins", 2, map,
            new Color(0x08, 0x02, 0x18), CyberColors.NEON_PINK);

        lvl.addParallaxLayer(new ParallaxLayer(0.1f, new Color(0x44, 0x00, 0x44, 80), 0, 10L));
        lvl.addParallaxLayer(new ParallaxLayer(0.25f, new Color(0x22, 0x00, 0x33, 70), 1, 11L));
        lvl.addParallaxLayer(new ParallaxLayer(0.12f, new Color(0xFF, 0x00, 0xFF, 15), 2, 12L));

        TurretEnemy t1 = new TurretEnemy(380, 384); setEnemyRefs(t1, player, lvl);
        TurretEnemy t2 = new TurretEnemy(750, 384); setEnemyRefs(t2, player, lvl);
        AssassinEnemy a1 = new AssassinEnemy(520, 384); setEnemyRefs(a1, player, lvl);
        AssassinEnemy a2 = new AssassinEnemy(950, 384); setEnemyRefs(a2, player, lvl);
        GruntEnemy g1 = new GruntEnemy(300, 384); setEnemyRefs(g1, player, lvl);
        DroneEnemy d1 = new DroneEnemy(620, 200);  setEnemyRefs(d1, player, lvl);

        lvl.addEnemy(t1); lvl.addEnemy(t2);
        lvl.addEnemy(a1); lvl.addEnemy(a2);
        lvl.addEnemy(g1); lvl.addEnemy(d1);
        return lvl;
    }

    // ─────────────────────────────────────────────────────────
    //  LEVEL 3 – Mech Factory (mechs + all enemies combined)
    // ─────────────────────────────────────────────────────────
    private static Level buildLevel3(Player player) {
        int[][] map = {
/* 0 */  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 1 */  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 2 */  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 3 */  {0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0},
/* 4 */  {0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0},
/* 5 */  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 6 */  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0},
/* 7 */  {0,0,1,1,0,0,0,0,0,0,3,3,3,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 8 */  {0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,4},
/* 9 */  {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
/*10 */  {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
/*11 */  {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
/*12 */  {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
/*13 */  {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        };

        Level lvl = new Level("Mech Factory", 3, map,
            new Color(0x05, 0x10, 0x05), CyberColors.MECH_GLOW);

        lvl.addParallaxLayer(new ParallaxLayer(0.1f, new Color(0x00, 0x33, 0x00, 80), 0, 20L));
        lvl.addParallaxLayer(new ParallaxLayer(0.3f, new Color(0x00, 0x22, 0x00, 70), 1, 21L));
        lvl.addParallaxLayer(new ParallaxLayer(0.12f, new Color(0x00, 0xFF, 0x44, 12), 2, 22L));

        MechEnemy m1 = new MechEnemy(380,  384);  setEnemyRefs(m1, player, lvl);
        MechEnemy m2 = new MechEnemy(900,  384);  setEnemyRefs(m2, player, lvl);
        GruntEnemy g1 = new GruntEnemy(280, 384); setEnemyRefs(g1, player, lvl);
        GruntEnemy g2 = new GruntEnemy(650, 384); setEnemyRefs(g2, player, lvl);
        TurretEnemy t1 = new TurretEnemy(560, 384); setEnemyRefs(t1, player, lvl);
        AssassinEnemy a1 = new AssassinEnemy(1050, 384); setEnemyRefs(a1, player, lvl);
        DroneEnemy d1 = new DroneEnemy(750, 180);  setEnemyRefs(d1, player, lvl);

        lvl.addEnemy(m1); lvl.addEnemy(m2); lvl.addEnemy(g1); lvl.addEnemy(g2);
        lvl.addEnemy(t1); lvl.addEnemy(a1); lvl.addEnemy(d1);
        return lvl;
    }

    // ─────────────────────────────────────────────────────────
    //  LEVEL 4 – Boss Arena: NeonWraith
    // ─────────────────────────────────────────────────────────
    private static Level buildBossLevel1(Player player) {
        int[][] map = {
/* 0 */  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 1 */  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 2 */  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 3 */  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 4 */  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 5 */  {1,0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,1},
/* 6 */  {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
/* 7 */  {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
/* 8 */  {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
/* 9 */  {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
/*10 */  {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
/*11 */  {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
/*12 */  {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
/*13 */  {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        };

        Level lvl = new Level("Phantom Circuit — Boss Lair", 4, map,
            new Color(0x08, 0x00, 0x18), CyberColors.BOSS1_GLOW);

        lvl.addParallaxLayer(new ParallaxLayer(0.05f, new Color(0x88, 0x00, 0xAA, 60), 0, 30L));
        lvl.addParallaxLayer(new ParallaxLayer(0.2f,  new Color(0x44, 0x00, 0x66, 50), 1, 31L));
        lvl.addParallaxLayer(new ParallaxLayer(0.1f,  new Color(0xFF, 0x00, 0xFF, 10), 2, 32L));
        // No enemies – boss is handled by LevelManager
        return lvl;
    }

    // ─────────────────────────────────────────────────────────
    //  LEVEL 5 – Final Boss Arena: Colossus Prime
    // ─────────────────────────────────────────────────────────
    private static Level buildBossLevel2(Player player) {
        int[][] map = {
/* 0 */  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 1 */  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 2 */  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 3 */  {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
/* 4 */  {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
/* 5 */  {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
/* 6 */  {1,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,0,1},
/* 7 */  {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
/* 8 */  {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
/* 9 */  {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
/*10 */  {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
/*11 */  {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
/*12 */  {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
/*13 */  {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        };

        Level lvl = new Level("Omega Core — Final Confrontation", 5, map,
            new Color(0x10, 0x06, 0x00), CyberColors.BOSS2_GLOW);

        lvl.addParallaxLayer(new ParallaxLayer(0.05f, new Color(0xAA, 0x44, 0x00, 60), 0, 40L));
        lvl.addParallaxLayer(new ParallaxLayer(0.2f,  new Color(0x66, 0x22, 0x00, 50), 1, 41L));
        lvl.addParallaxLayer(new ParallaxLayer(0.1f,  new Color(0xFF, 0x66, 0x00, 10), 2, 42L));
        return lvl;
    }

    private static void setEnemyRefs(Enemy e, Player player, Level lvl) {
        e.setPlayerRef(player);
        e.setLevelRef(lvl);
    }
}