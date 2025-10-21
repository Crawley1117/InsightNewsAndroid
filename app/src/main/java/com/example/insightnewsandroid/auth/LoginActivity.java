// LoginActivity.java
package com.example.insightnewsandroid.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.insightnewsandroid.MainActivity;
import com.example.insightnewsandroid.databinding.ActivityLoginBinding;
import android.view.View;


public class LoginActivity extends AppCompatActivity {
    private AuthRepository authRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
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
            String phone = binding.editPhone.getText().toString();
            if (phone.length() != 11) {
                Toast.makeText(this, "请输入11位手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            // 模拟发送验证码
            Toast.makeText(this, "验证码已发送（模拟）", Toast.LENGTH_SHORT).show();
        });

        binding.btnLogin.setOnClickListener(v -> {
            String phone = binding.editPhone.getText().toString();
            String codeOrPass = binding.editCodeOrPassword.getText().toString();
            if (phone.length() != 11 || codeOrPass.isEmpty()) {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                return;
            }

            // 模拟登录成功
            authRepo.setLoggedIn(phone);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}