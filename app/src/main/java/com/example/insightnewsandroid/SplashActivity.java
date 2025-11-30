// SplashActivity.java (更新版)
package com.example.insightnewsandroid;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import com.example.insightnewsandroid.auth.WelcomeActivity;

import com.example.insightnewsandroid.auth.AuthRepository;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 启动页显示时间，单位毫秒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 在启动页中检查登录状态
        AuthRepository authRepo = new AuthRepository(this);
        boolean isLoggedIn = authRepo.isLoggedIn();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (isLoggedIn) {
                // 已登录，跳转到 MainActivity
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // 未登录，跳转到 WelcomeActivity
                intent = new Intent(SplashActivity.this, WelcomeActivity.class);
            }
            startActivity(intent);
            finish(); // 结束 SplashActivity
        }, SPLASH_DURATION);
    }
}