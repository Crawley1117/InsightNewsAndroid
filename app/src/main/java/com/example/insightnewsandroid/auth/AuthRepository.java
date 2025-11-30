package com.example.insightnewsandroid.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthRepository {

    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_EMAIL = "email"; // 修改：KEY_PHONE 改为 KEY_EMAIL
    private static final String KEY_AUTH_TOKEN = "auth_token";

    private final SharedPreferences prefs;

    public AuthRepository(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * 设置用户登录状态，包括用户ID（邮箱）和Token。
     * @param email 用户邮箱
     * @param token 服务器返回的认证Token
     */
    public void setLoggedIn(String email, String token) { // 修改：参数名 userId 改为 email
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_EMAIL, email) // 修改：KEY_PHONE 改为 KEY_EMAIL
                .putString(KEY_AUTH_TOKEN, token)
                .apply();
    }

    /**
     * 设置用户登录状态（旧方法，用于模拟登录等场景，Token设为null或空字符串）。
     * @param email 用户邮箱
     */
    public void setLoggedIn(String email) { // 修改：参数名 userId 改为 email
        setLoggedIn(email, ""); // 调用新方法，传入空Token
    }

    public void logout() {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_EMAIL) // 修改：KEY_PHONE 改为 KEY_EMAIL
                .remove(KEY_AUTH_TOKEN)
                .apply();
    }

    public String getEmail() { // 修改：方法名 getPhone 改为 getEmail
        return prefs.getString(KEY_EMAIL, "");
    }

    /**
     * 获取存储的认证Token。
     * @return 认证Token，如果未登录或Token不存在则返回空字符串。
     */
    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, "");
    }
}