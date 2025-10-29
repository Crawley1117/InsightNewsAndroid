package com.example.insightnewsandroid.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.auth.AuthRepository;
import com.example.insightnewsandroid.auth.WelcomeActivity;
import com.example.insightnewsandroid.data.UserProfileManager;
import com.example.insightnewsandroid.data.model.UserProfile;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private AuthRepository authRepository;
    private UserProfile currentProfile;

    // Views
    private ImageView ivAvatar, ivGender;
    private TextView tvUsername, tvBio;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authRepository = new AuthRepository(requireContext());

        // 初始化用户资料
        currentProfile = UserProfileManager.INSTANCE.getCurrentProfile(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 加载 fragment_profile.xml 布局
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadUserData();
        setupClickListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 当从编辑页面返回时，重新加载数据以显示更新后的信息
        loadUserData();
    }

    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.iv_avatar);
        ivGender = view.findViewById(R.id.iv_gender);
        tvUsername = view.findViewById(R.id.tv_username);
        tvBio = view.findViewById(R.id.tv_bio);
    }

    private void loadUserData() {
        // 使用统一的数据源
        currentProfile = UserProfileManager.INSTANCE.getCurrentProfile(requireContext());

        tvUsername.setText(currentProfile.getUsername());
        tvBio.setText(currentProfile.getBio());

        // 设置性别图标
        if (currentProfile.getGender() == 0) {
            ivGender.setImageResource(R.drawable.ic_male); // 男性图标
        } else if (currentProfile.getGender() == 1) {
            ivGender.setImageResource(R.drawable.ic_female); // 女性图标
        } else {
            ivGender.setImageResource(R.drawable.ic_unknown_gender); // 未知性别图标
        }

        // 加载头像 - 使用与编辑资料页面相同的默认头像
        if (currentProfile.getAvatarUri() != null && !currentProfile.getAvatarUri().isEmpty()) {
            // 加载保存的头像
            Glide.with(this)
                    .load(Uri.parse(currentProfile.getAvatarUri()))
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivAvatar);
        } else {
            // 使用与编辑资料页面相同的默认头像
            Glide.with(this)
                    .load(R.drawable.avatar_placeholder)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivAvatar);
        }
    }

    // ProfileFragment.java - 修改 setupClickListeners 方法
// ProfileFragment.java - 修改 setupClickListeners 方法
    private void setupClickListeners() {
        // 编辑资料 - 这个已经工作
        requireView().findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            Log.d("ProfileFragment", "点击编辑资料按钮");
            try {
                Log.d("ProfileFragment", "准备启动 EditProfileActivity");
                Intent intent = new Intent(requireContext(), EditProfileActivity.class);
                Log.d("ProfileFragment", "Intent 创建成功");
                startActivity(intent);
                Log.d("ProfileFragment", "成功启动 EditProfileActivity");
            } catch (Exception e) {
                Log.e("ProfileFragment", "启动 EditProfileActivity 失败", e);
                Toast.makeText(requireContext(), "无法打开编辑资料: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });

        // 新闻收藏 - 添加详细日志
        requireView().findViewById(R.id.btn_collection).setOnClickListener(v -> {
            Log.d("ProfileFragment", "点击新闻收藏按钮");
            try {
                Log.d("ProfileFragment", "准备启动 NewsCollectionActivity");
                Intent intent = new Intent(requireContext(), NewsCollectionActivity.class);
                Log.d("ProfileFragment", "Intent 创建成功");
                startActivity(intent);
                Log.d("ProfileFragment", "成功启动 NewsCollectionActivity");
            } catch (Exception e) {
                Log.e("ProfileFragment", "启动 NewsCollectionActivity 失败", e);
                Toast.makeText(requireContext(), "无法打开新闻收藏: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });

        // 话题收藏 - 这个已经工作
        requireView().findViewById(R.id.btn_topic_collection).setOnClickListener(v -> {
            Log.d("ProfileFragment", "点击话题收藏按钮");
            try {
                Log.d("ProfileFragment", "准备启动 TopicCollectionActivity");
                Intent intent = new Intent(requireContext(), TopicCollectionActivity.class);
                Log.d("ProfileFragment", "Intent 创建成功");
                startActivity(intent);
                Log.d("ProfileFragment", "成功启动 TopicCollectionActivity");
            } catch (Exception e) {
                Log.e("ProfileFragment", "启动 TopicCollectionActivity 失败", e);
                Toast.makeText(requireContext(), "无法打开话题收藏: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "选择头像"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            // 更新用户资料 - 使用当前用户资料的值
            UserProfile updatedProfile = new UserProfile(
                    currentProfile.getUsername(),  // 使用现有的用户名
                    currentProfile.getBio(),       // 使用现有的简介
                    currentProfile.getGender(),    // 使用现有的性别
                    imageUri.toString(),           // 使用新选择的图片URI
                    currentProfile.getCollectedNews(),
                    currentProfile.getLikedNews(),
                    currentProfile.getDislikedNews(),
                    currentProfile.getCollectedTopics() // 添加收藏的话题列表
            );

            // 保存到统一数据源
            UserProfileManager.INSTANCE.updateProfile(requireContext(), updatedProfile);

            // 更新当前资料引用
            currentProfile = updatedProfile;

            // 重新加载数据
            loadUserData();

            Toast.makeText(requireContext(), "头像更新成功", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFeedbackDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("问题反馈")
                .setMessage("请将问题反馈发送至：support@insightnews.com")
                .setPositiveButton("确定", null)
                .show();
    }

    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("确认退出")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("确定", (dialog, which) -> handleLogout())
                .setNegativeButton("Cancel", null)
                .show();
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

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
