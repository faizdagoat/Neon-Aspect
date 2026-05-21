package com.cyberknight.auth;

import org.mindrot.jbcrypt.BCrypt;

/**
 * AuthManager – login, register, dan session.
 * Password di-hash BCrypt — tidak pernah simpan plaintext.
 */
public class AuthManager {

    private static AuthManager instance;
    private final DatabaseManager db;
    private UserData currentUser = null;

    private AuthManager() {
        db = DatabaseManager.getInstance();
    }

    public static AuthManager getInstance() {
        if (instance == null) instance = new AuthManager();
        return instance;
    }

    // ── Register ──────────────────────────────────────────────
    public enum RegisterResult {
        SUCCESS, USERNAME_TAKEN,
        USERNAME_TOO_SHORT, USERNAME_TOO_LONG, USERNAME_INVALID,
        PASSWORD_TOO_SHORT, DB_ERROR
    }

    public RegisterResult register(String username, String password) {
        if (username == null || username.trim().length() < 3)
            return RegisterResult.USERNAME_TOO_SHORT;
        if (username.trim().length() > 20)
            return RegisterResult.USERNAME_TOO_LONG;
        if (!username.trim().matches("[a-zA-Z0-9_]+"))
            return RegisterResult.USERNAME_INVALID;
        if (password == null || password.length() < 6)
            return RegisterResult.PASSWORD_TOO_SHORT;
        if (!db.isConnected())
            return RegisterResult.DB_ERROR;

        String hashed = BCrypt.hashpw(password, BCrypt.gensalt(10));
        boolean ok = db.registerUser(username.trim().toLowerCase(), hashed);
        return ok ? RegisterResult.SUCCESS : RegisterResult.USERNAME_TAKEN;
    }

    // ── Login ─────────────────────────────────────────────────
    public enum LoginResult {
        SUCCESS, USER_NOT_FOUND, WRONG_PASSWORD, DB_ERROR
    }

    public LoginResult login(String username, String password) {
        if (username == null || password == null) return LoginResult.USER_NOT_FOUND;
        if (!db.isConnected()) return LoginResult.DB_ERROR;

        UserData user = db.getUserByUsername(username.trim().toLowerCase());
        if (user == null) return LoginResult.USER_NOT_FOUND;

        try {
            if (!BCrypt.checkpw(password, user.hashedPassword))
                return LoginResult.WRONG_PASSWORD;
        } catch (Exception e) {
            return LoginResult.WRONG_PASSWORD;
        }

        currentUser = user;
        System.out.println("[Auth] Login: " + user.username);
        return LoginResult.SUCCESS;
    }

    // ── Session ───────────────────────────────────────────────
    public void logout()             { currentUser = null; }
    public boolean isLoggedIn()      { return currentUser != null; }
    public UserData getCurrentUser() { return currentUser; }
    public String getUsername()      { return currentUser != null ? currentUser.username : "Guest"; }

    // ── Pesan ─────────────────────────────────────────────────
    public static String getRegisterMessage(RegisterResult r) {
        return switch (r) {
            case SUCCESS            -> "Akun berhasil dibuat!";
            case USERNAME_TAKEN     -> "Username sudah dipakai.";
            case USERNAME_TOO_SHORT -> "Username minimal 3 karakter.";
            case USERNAME_TOO_LONG  -> "Username maksimal 20 karakter.";
            case USERNAME_INVALID   -> "Hanya huruf, angka, underscore.";
            case PASSWORD_TOO_SHORT -> "Password minimal 6 karakter.";
            case DB_ERROR           -> "Database tidak terhubung. Cek MySQL.";
        };
    }

    public static String getLoginMessage(LoginResult r) {
        return switch (r) {
            case SUCCESS        -> "Login berhasil!";
            case USER_NOT_FOUND -> "Username tidak ditemukan.";
            case WRONG_PASSWORD -> "Password salah.";
            case DB_ERROR       -> "Database tidak terhubung. Cek MySQL.";
        };
    }
}