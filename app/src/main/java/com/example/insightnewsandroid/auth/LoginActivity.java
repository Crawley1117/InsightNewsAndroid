package com.example.insightnewsandroid.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.insightnewsandroid.MainActivity;
import com.example.insightnewsandroid.databinding.ActivityLoginBinding;
import android.view.View;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    private AuthRepository authRepo;
    private ActivityLoginBinding binding;
    private CountDownTimer countDownTimer;

    // 网络相关常量
    private static final String BASE_URL = "http://116.62.221.163:8087";
    // 用于获取验证码的预认证 Token
    private static final String PRE_AUTH_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiIxODk2MDkzNTUwMCJ9.JV85gnurhGUCeK7D_DnG3NHznpABmSqtse3oNw1RDoc";

    // 调试用密码
    private static final String DEBUG_PASSWORD = "1117";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authRepo = new AuthRepository(this);

        // 切换验证码/密码模式
        binding.radioCode.setOnCheckedChangeListener((group, checkedId) -> {
            if (binding.radioCode.isChecked()) {
                binding.layoutCodeOrPassword.setHint("验证码");
                binding.btnGetCode.setVisibility(View.VISIBLE);
                binding.editCodeOrPassword.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else {
                binding.layoutCodeOrPassword.setHint("密码");
                binding.btnGetCode.setVisibility(View.GONE);
                binding.editCodeOrPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });

        binding.btnGetCode.setOnClickListener(v -> {
            String phone = binding.editPhone.getText().toString().trim();
            if (!isValidPhoneNumber(phone)) {
                Toast.makeText(this, "请输入有效的11位手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            sendVerificationCode(phone);
        });

        binding.btnLogin.setOnClickListener(v -> {
            String phone = binding.editPhone.getText().toString().trim();
            String codeOrPass = binding.editCodeOrPassword.getText().toString().trim();
            if (!isValidPhoneNumber(phone) || codeOrPass.isEmpty()) {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                return;
            }

            if (binding.radioCode.isChecked()) {
                // 验证码登录
                verifyCodeAndLogin(phone, codeOrPass);
            } else {
                // 密码登录
                if (DEBUG_PASSWORD.equals(codeOrPass)) {
                    // 调试模式密码登录
                    Toast.makeText(this, "调试模式密码登录成功", Toast.LENGTH_SHORT).show();
                    authRepo.setLoggedIn(phone, "debug_token_placeholder");
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    return;
                }
                // 正常密码登录流程 (当前为模拟，需后端实现)
                // 模拟登录成功 (临时)
                authRepo.setLoggedIn(phone, "password_login_placeholder");
                Toast.makeText(this, "密码登录（模拟）", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }

    private void sendVerificationCode(String phone) {
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
                    Toast.makeText(LoginActivity.this, "发送失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    resetGetCodeButton();
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
                                Toast.makeText(LoginActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                                startCountDown();
                            } else {
                                Toast.makeText(LoginActivity.this, "发送失败: " + message, Toast.LENGTH_SHORT).show();
                                resetGetCodeButton();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(LoginActivity.this, "发送失败: 服务器响应格式错误", Toast.LENGTH_SHORT).show();
                            resetGetCodeButton();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "发送失败: " + response.code() + " " + responseBody, Toast.LENGTH_SHORT).show();
                        resetGetCodeButton();
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

    private void verifyCodeAndLogin(String phone, String code) {
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

        // 登录接口不需要 Authorization header
        Request request = new Request.Builder()
                .url(BASE_URL + "/user/login") // 请根据你的后端实际接口修改此URL
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "登录失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            String token = json.optString("data", "");

                            if (serverCode == 200 && !token.isEmpty()) {
                                // 登录成功，保存用户ID和Token
                                authRepo.setLoggedIn(phone, token);
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(LoginActivity.this, "登录失败: 服务器响应格式错误", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "登录失败: " + response.code() + " " + responseBody, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("^1[3-9]\\d{9}$");
    }

    private void resetGetCodeButton() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        binding.btnGetCode.setEnabled(true);
        binding.btnGetCode.setText("获取验证码");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private static final OkHttpClient client = new OkHttpClient();
}