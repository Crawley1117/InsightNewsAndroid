package com.example.insightnewsandroid;

import android.app.Application;
import android.content.Context;
import android.util.Log; // 添加日志导入

public class InsightNewsApplication extends Application {
    private static final String TAG = "InsightNewsApp"; // 定义日志标签
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        Log.d(TAG, "InsightNewsApplication onCreate called, context set."); // 添加日志
    }

    public static Context getContext() {
        return context;
    }
}