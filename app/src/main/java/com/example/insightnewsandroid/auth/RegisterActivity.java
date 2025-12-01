package com.example.insightnewsandroid.auth;

import com.example.insightnewsandroid.data.manager.ApiManager;
import com.example.insightnewsandroid.data.manager.AuthService;
import com.example.insightnewsandroid.data.model.BaseResponse;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.insightnewsandroid.MainActivity;
import com.example.insightnewsandroid.data.manager.ApiManager;
import com.example.insightnewsandroid.data.model.BaseResponse;
import com.example.insightnewsandroid.databinding.ActivityRegisterBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private AuthRepository authRepo;
    private ActivityRegisterBinding binding;
    private CountDownTimer countDownTimer;

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
        String email = binding.editEmail.getText().toString().trim(); // 修改：变量名从 phone 改为 email
        if (!isValidEmail(email)) { // 修改：验证方法
            Toast.makeText(this, "请输入有效的邮箱地址", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<BaseResponse<Void>> call = ApiManager.getAuthService().sendVerificationCode(email); // 修改：使用 email 参数
        call.enqueue(new Callback<BaseResponse<Void>>() {
            @Override
            public void onResponse(Call<BaseResponse<Void>> call, Response<BaseResponse<Void>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<Void> baseResponse = response.body();
                    if (baseResponse != null && baseResponse.isSuccess()) {
                        Toast.makeText(RegisterActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                        startCountDown();
                    } else {
                        String msg = (baseResponse != null) ? baseResponse.getMsg() : "未知错误";
                        Toast.makeText(RegisterActivity.this, "发送失败: " + msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "发送失败: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Void>> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "发送失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        String email = binding.editEmail.getText().toString().trim(); // 修改：变量名从 phone 改为 email
        String code = binding.editCode.getText().toString().trim();
        // 移除：String password = binding.editPassword.getText().toString().trim(); // 不再使用密码

        if (!isValidEmail(email)) { // 修改：验证方法
            Toast.makeText(this, "邮箱格式错误", Toast.LENGTH_SHORT).show();
            return;
        }
        if (code.isEmpty()) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }
        // 移除：密码长度验证
        // if (password.isEmpty() || password.length() < 6) {
        //     Toast.makeText(this, "请输入至少6位密码", Toast.LENGTH_SHORT).show();
        //     return;
        // }

        // 构建请求体，只包含 email 和 code
        AuthService.RegisterRequest request = new AuthService.RegisterRequest(email, code); // 修改：构造函数参数

        Call<BaseResponse<String>> call = ApiManager.getAuthService().register(request); // 修改：调用 register 方法
        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<String> baseResponse = response.body();
                    if (baseResponse != null && baseResponse.isSuccess()) {
                        String token = baseResponse.getData(); // 从data字段获取token
                        if (token != null && !token.isEmpty()) {
                            authRepo.setLoggedIn(email, token); // 存储邮箱和Token
                            Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "注册失败: Token为空", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String msg = (baseResponse != null) ? baseResponse.getMsg() : "未知错误";
                        Toast.makeText(RegisterActivity.this, "注册失败: " + msg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "注册失败: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "注册失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 修改：验证邮箱格式
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}