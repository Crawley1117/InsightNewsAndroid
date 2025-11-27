package com.example.insightnewsandroid.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.View; // 添加 View 类的导入
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.insightnewsandroid.MainActivity;
import com.example.insightnewsandroid.data.manager.ApiManager;
import com.example.insightnewsandroid.data.manager.AuthService; // 导入 AuthService
import com.example.insightnewsandroid.data.model.BaseResponse;
import com.example.insightnewsandroid.databinding.ActivityLoginBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private AuthRepository authRepo;
    private ActivityLoginBinding binding;
    private CountDownTimer countDownTimer;

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
                binding.btnGetCode.setVisibility(View.VISIBLE); // 现在可以正确使用 View.VISIBLE
                binding.editCodeOrPassword.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else {
                binding.layoutCodeOrPassword.setHint("密码");
                binding.btnGetCode.setVisibility(View.GONE); // 现在可以正确使用 View.GONE
                binding.editCodeOrPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
            }
        });

        binding.btnGetCode.setOnClickListener(v -> {
            String email = binding.editPhone.getText().toString().trim(); // 改为 email
            if (!isValidEmail(email)) { // 改为验证邮箱
                Toast.makeText(this, "请输入有效的邮箱地址", Toast.LENGTH_SHORT).show();
                return;
            }
            sendVerificationCode(email);
        });

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.editPhone.getText().toString().trim(); // 改为 email
            String codeOrPass = binding.editCodeOrPassword.getText().toString().trim();

            if (!isValidEmail(email) || codeOrPass.isEmpty()) { // 改为验证邮箱
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                return;
            }

            if (binding.radioCode.isChecked()) {
                // 验证码登录
                verifyCodeAndLogin(email, codeOrPass);
            } else {
                // 密码登录
                if (DEBUG_PASSWORD.equals(codeOrPass)) {
                    // 调试模式密码登录
                    Toast.makeText(this, "调试模式密码登录成功", Toast.LENGTH_SHORT).show();
                    authRepo.setLoggedIn(email, "debug_token_placeholder");
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    return;
                }
                // 正常密码登录流程
                loginWithPassword(email, codeOrPass);
            }
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
                        String msg = (baseResponse != null) ? baseResponse.getMsg() : "未知错误"; // 使用 getMsg()
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
        AuthService.LoginWithCodeRequest request = new AuthService.LoginWithCodeRequest(email, code); // 使用新的请求类，明确指定
        Call<BaseResponse<String>> call = ApiManager.getAuthService().loginWithCode(request); // 注意泛型是 String
        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<String> baseResponse = response.body(); // 注意泛型是 String
                    if (baseResponse != null && baseResponse.isSuccess()) {
                        String token = baseResponse.getData(); // 从data字段获取token
                        if (token != null && !token.isEmpty()) {
                            authRepo.setLoggedIn(email, token);
                            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "登录失败: Token为空", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String msg = (baseResponse != null) ? baseResponse.getMsg() : "未知错误"; // 使用 getMsg()
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

    private void loginWithPassword(String email, String password) {
        AuthService.LoginWithPasswordRequest request = new AuthService.LoginWithPasswordRequest(email, password); // 使用新的请求类，明确指定
        Call<BaseResponse<String>> call = ApiManager.getAuthService().loginWithPassword(request); // 注意泛型是 String
        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<String> baseResponse = response.body(); // 注意泛型是 String
                    if (baseResponse != null && baseResponse.isSuccess()) {
                        String token = baseResponse.getData(); // 从data字段获取token
                        if (token != null && !token.isEmpty()) {
                            authRepo.setLoggedIn(email, token);
                            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "登录失败: Token为空", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String msg = (baseResponse != null) ? baseResponse.getMsg() : "未知错误"; // 使用 getMsg()
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

    // 验证邮箱格式
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
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
}