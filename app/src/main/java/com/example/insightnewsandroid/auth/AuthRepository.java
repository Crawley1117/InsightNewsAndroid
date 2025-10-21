// AuthRepository.java
package com.example.insightnewsandroid.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthRepository {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_PHONE = "phone";

    private final SharedPreferences prefs;

    public AuthRepository(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setLoggedIn(String phone) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_PHONE, phone)
                .apply();
    }

    public void logout() {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_PHONE)
                .apply();
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, "");
    }
}