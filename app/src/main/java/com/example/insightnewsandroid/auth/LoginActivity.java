package com.example.insightnewsandroid.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.insightnewsandroid.MainActivity;
import com.example.insightnewsandroid.data.manager.ApiManager;
import com.example.insightnewsandroid.data.manager.AuthService;
import com.example.insightnewsandroid.data.model.BaseResponse;
import com.example.insightnewsandroid.databinding.ActivityLoginBinding;

import android.view.MenuItem;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private AuthRepository authRepo;
    private ActivityLoginBinding binding;
    private CountDownTimer countDownTimer;

    private static final String DEBUG_PASSWORD = "1117";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("登录");
        }

        authRepo = new AuthRepository(this);

        binding.radioCode.setOnCheckedChangeListener((group, checkedId) -> {
            if (binding.radioCode.isChecked()) {
                binding.layoutCodeOrPassword.setHint("验证码");
                binding.btnGetCode.setVisibility(View.VISIBLE);
                binding.editCodeOrPassword.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else {
                binding.layoutCodeOrPassword.setHint("密码");
                binding.btnGetCode.setVisibility(View.GONE);
                binding.editCodeOrPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT);
            }
        });

        binding.btnGetCode.setOnClickListener(v -> {
            String email = binding.editPhone.getText().toString().trim();
            if (!isValidEmail(email)) {
                Toast.makeText(this, "请输入有效的邮箱地址", Toast.LENGTH_SHORT).show();
                return;
            }
            sendVerificationCode(email);
        });

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.editPhone.getText().toString().trim();
            String codeOrPass = binding.editCodeOrPassword.getText().toString().trim();

            if (!isValidEmail(email) || codeOrPass.isEmpty()) {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                return;
            }

            if (binding.radioCode.isChecked()) {
                verifyCodeAndLogin(email, codeOrPass);
            } else {
                if (DEBUG_PASSWORD.equals(codeOrPass)) {
                    Toast.makeText(this, "调试模式密码登录成功", Toast.LENGTH_SHORT).show();
                    authRepo.setLoggedIn(email, "debug_token_placeholder");
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    return;
                }
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
        AuthService.LoginWithCodeRequest request = new AuthService.LoginWithCodeRequest(email, code);
        Call<BaseResponse<String>> call = ApiManager.getAuthService().loginWithCode(request);
        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<String> baseResponse = response.body();
                    if (baseResponse != null && baseResponse.isSuccess()) {
                        String token = baseResponse.getData();
                        if (token != null && !token.isEmpty()) {
                            authRepo.setLoggedIn(email, token);
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

    private void loginWithPassword(String email, String password) {
        AuthService.LoginWithPasswordRequest request = new AuthService.LoginWithPasswordRequest(email, password);
        Call<BaseResponse<String>> call = ApiManager.getAuthService().loginWithPassword(request);
        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<String> baseResponse = response.body();
                    if (baseResponse != null && baseResponse.isSuccess()) {
                        String token = baseResponse.getData();
                        if (token != null && !token.isEmpty()) {
                            authRepo.setLoggedIn(email, token);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}