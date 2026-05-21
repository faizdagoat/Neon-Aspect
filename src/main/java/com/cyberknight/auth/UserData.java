package com.cyberknight.auth;

/**
 * UserData – record user dari MySQL.
 */
public class UserData {
    public final int    id;
    public final String username;
    public final String hashedPassword;

    public UserData(int id, String username, String hashedPassword) {
        this.id             = id;
        this.username       = username;
        this.hashedPassword = hashedPassword;
    }
}