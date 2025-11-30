// src/main/java/com/example/insightnewsandroid/data/model/AuthResponse.java
package com.example.insightnewsandroid.data.model;

public class AuthResponse {
    // 通常登录/注册成功后，后端会返回一个token或用户信息
    // 根据你的后端实际返回调整，这里先假设返回一个token字符串
    // 如果后端直接返回token字符串，则data字段就是String
    // 如果后端返回一个包含token的对象，比如 {"token": "xxx", "userId": "yyy"}，则需要定义一个子类
    // 从你之前的代码看，后端直接返回token字符串的可能性较大
    // 我们先按String处理，如果不对，你再告诉我实际结构
    private String token; // 或者其他字段名，如 access_token

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}