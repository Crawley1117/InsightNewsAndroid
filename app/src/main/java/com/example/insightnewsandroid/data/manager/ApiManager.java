package com.example.insightnewsandroid.data.manager;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {
    // 从你提供的示例看，新接口的地址是 http://120.79.169.214:8087
    // 请确认这是否是最终的生产地址，如果是测试地址，请在发布前更换
    private static final String BASE_URL = "http://120.79.169.214:8087/";

    private static AuthService authService;

    public static AuthService getAuthService() {
        if (authService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            authService = retrofit.create(AuthService.class);
        }
        return authService;
    }
}