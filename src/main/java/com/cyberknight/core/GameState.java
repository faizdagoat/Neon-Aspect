package com.cyberknight.core;

/**
 * All possible states the game can be in.
 */
public enum GameState {
    CUTSCENE,       // video intro sebelum main menu
    LOGIN,          // layar login / register
    MAIN_MENU,
    PLAYING,
    PAUSED,
    GAME_OVER,
    LEVEL_COMPLETE,
    BOSS_INTRO,
    VICTORY
}