package com.example.insightnewsandroid.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthRepository {

    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_AUTH_TOKEN = "auth_token"; // 新增：存储认证 Token

    private final SharedPreferences prefs;

    public AuthRepository(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * 设置用户登录状态，包括用户ID和Token。
     * @param userId 用户ID（例如手机号）
     * @param token 服务器返回的认证Token
     */
    public void setLoggedIn(String userId, String token) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_PHONE, userId) // 依然使用手机号作为用户标识
                .putString(KEY_AUTH_TOKEN, token) // 存储Token
                .apply();
    }

    /**
     * 设置用户登录状态（旧方法，用于模拟登录等场景，Token设为null或空字符串）。
     * @param userId 用户ID（例如手机号）
     */
    public void setLoggedIn(String userId) {
        setLoggedIn(userId, ""); // 调用新方法，传入空Token
    }

    public void logout() {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_PHONE)
                .remove(KEY_AUTH_TOKEN) // 登出时也移除Token
                .apply();
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, "");
    }

    /**
     * 获取存储的认证Token。
     * @return 认证Token，如果未登录或Token不存在则返回空字符串。
     */
    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, "");
    }
}