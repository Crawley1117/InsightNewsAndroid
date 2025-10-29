// LoginActivity.java
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
    private CountDownTimer countDownTimer; // 添加倒计时器成员变量

    // --- 添加网络相关常量 ---
    private static final String BASE_URL = "http://116.62.221.163:8087"; // 替换为你的后端基础URL
    private static final String AUTH_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiIxODk2MDkzNTUwMCJ9.JV85gnurhGUCeK7D_DnG3NHznpABmSqtse3oNw1RDoc"; // 注意：硬编码令牌不安全！
    private static final OkHttpClient client = new OkHttpClient();
    // --- 添加网络相关常量 ---

    // --- 定义调试用的验证码 ---
    private static final String DEBUG_CODE = "041122";
    // --- 定义调试用的验证码 ---


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
            String phone = binding.editPhone.getText().toString().trim(); // 添加 trim()
            if (!isValidPhoneNumber(phone)) { // 使用更健壮的验证
                Toast.makeText(this, "请输入有效的11位手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            sendVerificationCode(phone); // 调用发送验证码的方法
        });

        binding.btnLogin.setOnClickListener(v -> {
            String phone = binding.editPhone.getText().toString().trim(); // 添加 trim()
            String codeOrPass = binding.editCodeOrPassword.getText().toString().trim(); // 添加 trim()
            if (!isValidPhoneNumber(phone) || codeOrPass.isEmpty()) {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                return;
            }

            if (binding.radioCode.isChecked()) {
                // --- 使用验证码登录 ---
                // --- 检查是否为调试验证码 ---
                if (DEBUG_CODE.equals(codeOrPass)) {
                    // 是调试验证码，直接登录成功
                    Toast.makeText(this, "调试模式登录成功", Toast.LENGTH_SHORT).show();
                    authRepo.setLoggedIn(phone); // 使用输入的手机号设置登录状态
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    return; // 直接返回，不执行下面的网络请求
                }
                // --- 检查是否为调试验证码 ---
                verifyCodeAndLogin(phone, codeOrPass); // 调用验证码登录方法 (非调试验证码)
            } else {
                // --- 使用密码登录 (模拟，你需要实现后端密码登录接口) ---
                // 模拟登录成功 (临时)
                authRepo.setLoggedIn(phone);
                Toast.makeText(this, "密码登录（模拟）", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }

    // --- 新增：发送验证码方法 (更新解析逻辑) ---
    private void sendVerificationCode(String phone) {
        // --- 发送网络请求 ---
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/common/code").newBuilder();
        urlBuilder.addQueryParameter("phone", phone);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create("", MediaType.get("application/json"))) // 发送空的 POST 请求体
                .addHeader("Authorization", "Bearer " + AUTH_TOKEN) // **重要**：根据后端要求调整认证方式
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "发送失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    if (countDownTimer != null) {
                        countDownTimer.cancel(); // 如果请求失败，确保倒计时停止
                        binding.btnGetCode.setEnabled(true);
                        binding.btnGetCode.setText("获取验证码");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                boolean success = response.isSuccessful();

                runOnUiThread(() -> {
                    if (success) {
                        // --- 解析服务器响应 (根据统一格式: code, msg, data) ---
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            int code = json.getInt("code"); // 例如: 200
                            String message = json.getString("msg"); // <--- 修改：使用 "msg" 字段
                            // String data = json.getString("data"); // 如果需要，也可以获取 data 字段
                            if (code == 200) { // 假设 200 表示成功
                                Toast.makeText(LoginActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                                startCountDown(); // 启动倒计时
                            } else {
                                Toast.makeText(LoginActivity.this, "发送失败: " + message, Toast.LENGTH_SHORT).show();
                                // 发送失败，可能也需要重置按钮状态
                                if (countDownTimer != null) {
                                    countDownTimer.cancel();
                                }
                                binding.btnGetCode.setEnabled(true);
                                binding.btnGetCode.setText("获取验证码");
                            }
                        } catch (JSONException e) {
                            // 如果响应不是 JSON 或格式不正确
                            Toast.makeText(LoginActivity.this, "发送失败: 服务器响应格式错误", Toast.LENGTH_SHORT).show();
                            // 解析失败，可能也需要重置按钮状态
                            if (countDownTimer != null) {
                                countDownTimer.cancel();
                            }
                            binding.btnGetCode.setEnabled(true);
                            binding.btnGetCode.setText("获取验证码");
                        }
                        // --- 解析服务器响应 (根据统一格式: code, msg, data) ---
                    } else {
                        Toast.makeText(LoginActivity.this, "发送失败: " + response.code() + " " + responseBody, Toast.LENGTH_SHORT).show();
                        // 请求失败，可能也需要重置按钮状态
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        binding.btnGetCode.setEnabled(true);
                        binding.btnGetCode.setText("获取验证码");
                    }
                });
            }
        });
        // --- 发送网络请求 ---
    }
    // --- 新增：发送验证码方法 (更新解析逻辑) ---

    // --- 新增：启动倒计时方法 ---
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
    // --- 新增：启动倒计时方法 ---

    // --- 新增：验证码登录方法 (更新解析逻辑) ---
    private void verifyCodeAndLogin(String phone, String code) {
        // 后端接口: POST http://116.62.221.163:8087/user/register
        // Headers:
        //   Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiIxODk2MDkzNTUwMCJ9.JV85gnurhGUCeK7D_DnG3NHznpABmSqtse3oNw1RDoc
        //   Content-Type: application/json
        // Body (JSON):
        //   {"phone": "实际手机号", "code": "实际验证码"}

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

        Request request = new Request.Builder()
                .url(BASE_URL + "/user/register") // **关键修改**: 使用正确的注册/登录接口URL (注意：这个接口同时用于注册和登录)
                .post(body)
                .addHeader("Content-Type", "application/json") // **关键修改**: 添加 Content-Type 头
                .addHeader("Authorization", "Bearer " + AUTH_TOKEN) // **关键修改**: 添加 Authorization 头
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
                        // --- 解析登录结果 (根据新格式) ---
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            int serverCode = json.getInt("code"); // 例如: 200
                            String message = json.getString("msg"); // 例如: "操作成功"
                            String token = json.getString("data"); // 例如: "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."

                            if (serverCode == 200) { // 假设 200 表示登录成功
                                // 登录成功，保存登录状态
                                // 注意：这里假设 token 本身不包含用户手机号信息，AuthRepository 仍然使用手机号作为标识
                                // 如果 token 包含用户ID，你可能需要解析 token 或在登录成功后获取用户信息
                                authRepo.setLoggedIn(phone); // 依然使用手机号设置登录状态
                                // (可选) 你可能需要在 AuthRepository 或其他地方存储这个 token，用于后续需要认证的 API 调用
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show(); // 显示 "操作成功"

                                // 跳转到主界面
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                // 登录失败
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show(); // 显示具体失败原因
                            }
                        } catch (JSONException e) {
                            // 如果响应不是 JSON 或格式不正确
                            Toast.makeText(LoginActivity.this, "登录失败: 服务器响应格式错误", Toast.LENGTH_SHORT).show();
                        }
                        // --- 解析登录结果 (根据新格式) ---
                    } else {
                        Toast.makeText(LoginActivity.this, "登录失败: " + response.code() + " " + responseBody, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    // --- 新增：验证码登录方法 (更新解析逻辑) ---


    // --- 使用更健壮的手机号验证 ---
    private boolean isValidPhoneNumber(String phone) {
        // 简单的11位数字验证，实际应用中可能需要更复杂的正则
        return phone.matches("^1[3-9]\\d{9}$");
    }
    // --- 使用更健壮的手机号验证 ---


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        // binding 不是成员变量，不需要在这里设为 null
    }
}