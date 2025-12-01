package com.example.insightnewsandroid.data.manager;

import com.example.insightnewsandroid.InsightNewsApplication;
import com.example.insightnewsandroid.auth.AuthRepository;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit; // 导入 TimeUnit

public class ApiManager {
    private static final String BASE_URL = "http://120.79.169.214:8087/";

    private static AuthService authService;
    private static final AuthRepository authRepo = new AuthRepository(InsightNewsApplication.getContext());

    private static OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        String token = authRepo.getAuthToken();

                        Request.Builder requestBuilder = original.newBuilder();
                        if (token != null && !token.isEmpty()) {
                            requestBuilder.header("Authorization", "Bearer " + token);
                        }
                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    }
                })
                // --- 添加超时配置 ---
                .connectTimeout(30, TimeUnit.SECONDS) // 连接超时时间
                .readTimeout(60, TimeUnit.SECONDS)    // 读取超时时间 (等待服务器响应的时间)
                .writeTimeout(60, TimeUnit.SECONDS)   // 写入超时时间 (发送请求体的时间)
                // --- 添加结束 ---
                .build();
    }

    public static AuthService getAuthService() {
        if (authService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(createOkHttpClient()) // 使用配置了超时的 OkHttpClient
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            authService = retrofit.create(AuthService.class);
        }
        return authService;
    }
}