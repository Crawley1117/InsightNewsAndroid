// ProfileFragment.java
package com.example.insightnewsandroid.ui.profile;

import com.example.insightnewsandroid.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.insightnewsandroid.auth.AuthRepository;
import com.example.insightnewsandroid.auth.WelcomeActivity;

public class ProfileFragment extends Fragment {

    private AuthRepository authRepository;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 在 Fragment 创建时初始化 AuthRepository
        authRepository = new AuthRepository(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 加载 fragment_profile.xml 布局
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 找到登出按钮
        Button btnLogout = view.findViewById(R.id.btn_logout);

        // 为登出按钮设置点击监听器
        btnLogout.setOnClickListener(v -> handleLogout());
    }

    private void handleLogout() {
        // 调用 AuthRepository 的登出方法
        authRepository.logout();

        // 显示一个简单的提示
        Toast.makeText(requireContext(), "已登出", Toast.LENGTH_SHORT).show();

        // 创建 Intent 跳转到 WelcomeActivity
        Intent intent = new Intent(requireContext(), WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // 清除任务栈
        startActivity(intent);

        // 结束 MainActivity，确保用户无法返回到已登出的主界面
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}