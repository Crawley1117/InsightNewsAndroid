// RegisterActivity.java
package com.example.insightnewsandroid.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.insightnewsandroid.MainActivity;
import com.example.insightnewsandroid.databinding.ActivityRegisterBinding;

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
        String phone = binding.editPhone.getText().toString().trim();
        if (phone.length() != 11) {
            Toast.makeText(this, "请输入11位手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        // 模拟发送验证码（实际项目中调用短信 API）
        Toast.makeText(this, "验证码已发送至 " + phone + "（模拟）", Toast.LENGTH_SHORT).show();

        // 启动60秒倒计时
        startCountDown();
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

        if (phone.length() != 11) {
            Toast.makeText(this, "手机号格式错误", Toast.LENGTH_SHORT).show();
            return;
        }
        if (code.isEmpty()) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 模拟验证验证码（实际项目中需校验服务端）
        if (!"123456".equals(code)) { // 临时：固定验证码为 123456
            Toast.makeText(this, "验证码错误", Toast.LENGTH_SHORT).show();
            return;
        }

        // 注册成功，保存登录状态
        authRepo.setLoggedIn(phone);
        Toast.makeText(this, "注册成功！", Toast.LENGTH_SHORT).show();

        // 跳转到主界面
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        binding = null;
    }
}