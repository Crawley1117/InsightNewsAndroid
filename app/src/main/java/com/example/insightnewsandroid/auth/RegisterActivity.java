package com.example.insightnewsandroid.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.insightnewsandroid.MainActivity;
import com.example.insightnewsandroid.databinding.ActivityRegisterBinding;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {

    private AuthRepository authRepo;
    private ActivityRegisterBinding binding;
    private CountDownTimer countDownTimer;

    // 网络相关常量
    private static final String BASE_URL = "http://116.62.221.163:8087";
    // 用于获取验证码的预认证 Token
    private static final String PRE_AUTH_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiIxODk2MDkzNTUwMCJ9.JV85gnurhGUCeK7D_DnG3NHznpABmSqtse3oNw1RDoc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authRepo = new AuthRepository(this);

        // 返回按钮
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // 获取验证码按钮
        binding.btnGetCode.setOnClickListener(v -> sendVerificationCode());

        // 注册按钮
        binding.btnRegister.setOnClickListener(v -> register());
    }

    private void sendVerificationCode() {
        String phone = binding.editPhone.getText().toString().trim();
        if (!isValidPhoneNumber(phone)) {
            Toast.makeText(this, "请输入有效的11位手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/common/code").newBuilder();
        urlBuilder.addQueryParameter("phone", phone);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create("", MediaType.get("application/json")))
                .addHeader("Authorization", PRE_AUTH_TOKEN) // 发送验证码时需要预认证 Token
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "发送失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.btnGetCode.setEnabled(true); // 请求失败，重新启用按钮
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                boolean success = response.isSuccessful();

                runOnUiThread(() -> {
                    if (success) {
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            int code = json.getInt("code");
                            String message = json.getString("msg");
                            if (code == 200) {
                                Toast.makeText(RegisterActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                                startCountDown();
                            } else {
                                Toast.makeText(RegisterActivity.this, "发送失败: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(RegisterActivity.this, "发送失败: 服务器响应格式错误", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "发送失败: " + response.code() + " " + responseBody, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void startCountDown() {
        binding.btnGetCode.setEnabled(false);
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.btnGetCode.setText((millisUntilFinished / 1000) + "秒后重试");
            }

            @Override
            public void onFinish() {
                binding.btnGetCode.setEnabled(true);
                binding.btnGetCode.setText("获取验证码");
            }
        }.start();
    }

    private void register() {
        String phone = binding.editPhone.getText().toString().trim();
        String code = binding.editCode.getText().toString().trim();

        if (!isValidPhoneNumber(phone)) {
            Toast.makeText(this, "手机号格式错误", Toast.LENGTH_SHORT).show();
            return;
        }
        if (code.isEmpty()) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }

        verifyCodeAndRegister(phone, code);
    }

    private void verifyCodeAndRegister(String phone, String code) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("phone", phone);
            jsonBody.put("code", code);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "内部错误", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        // 注册接口不需要 Authorization header
        Request request = new Request.Builder()
                .url(BASE_URL + "/user/register")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "注册失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                boolean success = response.isSuccessful();

                runOnUiThread(() -> {
                    if (success) {
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            int serverCode = json.getInt("code");
                            String message = json.getString("msg");
                            String token = json.optString("data", ""); // 使用 optString 避免 JSONException

                            if (serverCode == 200 && !token.isEmpty()) {
                                // 注册成功，保存用户ID和Token
                                authRepo.setLoggedIn(phone, token); // 关键改进：存储Token
                                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(RegisterActivity.this, "注册失败: 服务器响应格式错误", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "注册失败: " + response.code() + " " + responseBody, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("^1[3-9]\\d{9}$");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        // binding 不是成员变量，不需要在这里设为 null
    }

    private static final OkHttpClient client = new OkHttpClient();
}