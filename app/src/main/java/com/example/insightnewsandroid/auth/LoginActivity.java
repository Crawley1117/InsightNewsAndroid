package com.example.insightnewsandroid.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.insightnewsandroid.MainActivity;
import com.example.insightnewsandroid.databinding.ActivityLoginBinding;


import com.example.insightnewsandroid.data.manager.ApiManager;
import com.example.insightnewsandroid.data.manager.AuthService;
import com.example.insightnewsandroid.data.model.BaseResponse;
// 移除：okhttp3 相关导入 (如果只使用 Retrofit)
// import okhttp3.*;
// import org.json.JSONException;
// import org.json.JSONObject;
// import java.io.IOException;

import com.example.insightnewsandroid.data.manager.ApiManager;
import com.example.insightnewsandroid.data.model.BaseResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private AuthRepository authRepo;
    private ActivityLoginBinding binding;
    private CountDownTimer countDownTimer;

    // 移除：BASE_URL 和 PRE_AUTH_TOKEN (现在使用 ApiManager)
    // private static final String BASE_URL = "http://116.62.221.163:8087";
    // private static final String PRE_AUTH_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiIxODk2MDkzNTUwMCJ9.JV85gnurhGUCeK7D_DnG3NHznpABmSqtse3oNw1RDoc";

    // 移除：DEBUG_PASSWORD (不再需要)
    // private static final String DEBUG_PASSWORD = "1117";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authRepo = new AuthRepository(this);

        // 移除：RadioGroup 相关逻辑
        // binding.radioCode.setOnCheckedChangeListener((group, checkedId) -> { ... });

        binding.btnGetCode.setOnClickListener(v -> {
            String email = binding.editPhone.getText().toString().trim(); // 修改：变量名从 phone 改为 email
            if (!isValidEmail(email)) { // 修改：验证方法
                Toast.makeText(this, "请输入有效的邮箱地址", Toast.LENGTH_SHORT).show();
                return;
            }
            sendVerificationCode(email);
        });

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.editPhone.getText().toString().trim(); // 修改：变量名从 phone 改为 email
            String code = binding.editCodeOrPassword.getText().toString().trim(); // 修改：变量名从 codeOrPass 改为 code

            if (!isValidEmail(email) || code.isEmpty()) { // 修改：验证方法
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                return;
            }

            // 移除：if(binding.radioCode.isChecked()) 判断和密码登录分支
            // 移除：调试模式密码登录逻辑
            // if (DEBUG_PASSWORD.equals(codeOrPass)) { ... }

            // 直接进行验证码登录
            verifyCodeAndLogin(email, code);
        });
    }

    private void sendVerificationCode(String email) {
        Call<BaseResponse<Void>> call = ApiManager.getAuthService().sendVerificationCode(email);
        call.enqueue(new Callback<BaseResponse<Void>>() {
            @Override
            public void onResponse(Call<BaseResponse<Void>> call, Response<BaseResponse<Void>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<Void> baseResponse = response.body();
                    if (baseResponse != null && baseResponse.isSuccess()) {
                        Toast.makeText(LoginActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                        startCountDown();
                    } else {
                        String msg = (baseResponse != null) ? baseResponse.getMsg() : "未知错误";
                        Toast.makeText(LoginActivity.this, "发送失败: " + msg, Toast.LENGTH_SHORT).show();
                        resetGetCodeButton();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "发送失败: " + response.code(), Toast.LENGTH_SHORT).show();
                    resetGetCodeButton();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Void>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "发送失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                resetGetCodeButton();
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

    private void verifyCodeAndLogin(String email, String code) {
        // 构建请求体
        AuthService.LoginWithCodeRequest request = new AuthService.LoginWithCodeRequest(email, code);

        // 获取 Token (如果需要)
        String token = authRepo.getAuthToken(); // 可能用于 Authorization header，取决于后端设计
        // 假设后端要求在 header 中携带 token (例如用于预验证)，否则可能不需要
        // String authHeader = token != null ? "Bearer " + token : "";

        Call<BaseResponse<String>> call = ApiManager.getAuthService().loginWithCode(request); // 注意：调用 loginWithCode
        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<String> baseResponse = response.body();
                    if (baseResponse != null && baseResponse.isSuccess()) {
                        String token = baseResponse.getData(); // 从data字段获取token
                        if (token != null && !token.isEmpty()) {
                            authRepo.setLoggedIn(email, token); // 存储邮箱和Token
                            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "登录失败: Token为空", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String msg = (baseResponse != null) ? baseResponse.getMsg() : "未知错误";
                        Toast.makeText(LoginActivity.this, "登录失败: " + msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "登录失败: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "登录失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 修改：验证邮箱格式
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
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

    // 移除：private static final OkHttpClient client = new OkHttpClient(); (不再需要)
}