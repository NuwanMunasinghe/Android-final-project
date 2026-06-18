package com.example.s23010340.authentication;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    public static final String ROLE_CLIENT = "client";
    public static final String ROLE_LABOUR = "labour";

    private static final String PREFS_NAME = "build_buddy_session";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_ROLE = "role";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_LAST_EMAIL = "last_email";
    private static final String KEY_LAST_ROLE = "last_role";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveClientSession(String email) {
        preferences.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_ROLE, ROLE_CLIENT)
            .putString(KEY_EMAIL, email)
            .putString(KEY_LAST_EMAIL, email)
            .putString(KEY_LAST_ROLE, ROLE_CLIENT)
            .apply();
    }

    public void saveLabourSession(String email) {
        preferences.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_ROLE, ROLE_LABOUR)
            .putString(KEY_EMAIL, email)
            .putString(KEY_LAST_EMAIL, email)
            .putString(KEY_LAST_ROLE, ROLE_LABOUR)
            .apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_LOGGED_IN, false);
    }

    public String getRole() {
        return preferences.getString(KEY_ROLE, "");
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, "");
    }

    public String getLastEmail() {
        return preferences.getString(KEY_LAST_EMAIL, "");
    }

    public String getLastRole() {
        return preferences.getString(KEY_LAST_ROLE, "");
    }

    public void clearSession() {
        preferences.edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .putString(KEY_ROLE, "")
            .putString(KEY_EMAIL, "")
            .apply();
    }
}
