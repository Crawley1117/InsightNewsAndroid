package com.example.insightnewsandroid.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextWatcher;
import android.text.Editable;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.data.UserProfileManager;
import com.example.insightnewsandroid.data.model.UserProfile;
import com.example.insightnewsandroid.data.model.Topic;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.ArrayList;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private static final int PICK_IMAGE_REQUEST = 1;

    private int currentGender = 2;
    private UserProfile currentProfile;
    private ImageView ivAvatar;
    private TextView etUsername, etBio;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale, rbSecret;

    private String selectedAvatarUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_edit_profile);

        initViews();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.iv_avatar);

        // 修改这里：使用正确的 ID
        etUsername = findViewById(R.id.et_name);        // 原来是 et_username
        etBio = findViewById(R.id.et_profile);          // 原来是 et_bio

        // 性别选择需要重新实现，因为布局中没有 RadioGroup
        setupGenderSelection();

        // 返回按钮
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        // 保存按钮
        findViewById(R.id.btn_save).setOnClickListener(v -> saveProfile());

        // 设置字符计数监听
        setupCharCount();
    }

    // 添加性别选择设置方法
    private void setupGenderSelection() {
        View llFemale = findViewById(R.id.ll_female);
        View llMale = findViewById(R.id.ll_male);
        View llUnknown = findViewById(R.id.ll_unknown);

        // 设置点击监听器
        llFemale.setOnClickListener(v -> setGenderSelection(1));
        llMale.setOnClickListener(v -> setGenderSelection(0));
        llUnknown.setOnClickListener(v -> setGenderSelection(2));
    }

    // 设置性别选择状态
    private void setGenderSelection(int gender) {
        // 重置所有选择状态
        resetGenderSelection();

        // 设置选中状态
        switch (gender) {
            case 0: // 男
                findViewById(R.id.ll_male).setBackgroundResource(R.drawable.gender_selected_background);
                break;
            case 1: // 女
                findViewById(R.id.ll_female).setBackgroundResource(R.drawable.gender_selected_background);
                break;
            case 2: // 保密
                findViewById(R.id.ll_unknown).setBackgroundResource(R.drawable.gender_selected_background);
                break;
        }
        currentGender = gender;
    }

    // 重置性别选择状态
    private void resetGenderSelection() {
        findViewById(R.id.ll_male).setBackgroundResource(android.R.color.transparent);
        findViewById(R.id.ll_female).setBackgroundResource(android.R.color.transparent);
        findViewById(R.id.ll_unknown).setBackgroundResource(android.R.color.transparent);
    }

    // 设置字符计数
    private void setupCharCount() {
        TextView tvCharCount = findViewById(R.id.tv_char_count);
        etBio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCharCount.setText(s.length() + "/20");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUserData() {
        currentProfile = UserProfileManager.INSTANCE.getCurrentProfile(this);

        etUsername.setText(currentProfile.getUsername());
        etBio.setText(currentProfile.getBio());

        // 设置性别选择
        setGenderSelection(currentProfile.getGender());

        // 加载头像
        if (currentProfile.getAvatarUri() != null && !currentProfile.getAvatarUri().isEmpty()) {
            selectedAvatarUri = currentProfile.getAvatarUri();
            Glide.with(this)
                    .load(Uri.parse(selectedAvatarUri))
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivAvatar);
        } else {
            // 设置默认头像
            Glide.with(this)
                    .load(R.drawable.avatar_placeholder)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivAvatar);
        }

        Log.d(TAG, "用户资料加载完成");
    }

    private void setupClickListeners() {
        // 头像选择 - 添加空值检查
        if (ivAvatar != null) {
            ivAvatar.setOnClickListener(v -> selectImage());
        }

        // 头像编辑按钮 - 添加空值检查
        View btnEditAvatar = findViewById(R.id.btn_edit_avatar);
        if (btnEditAvatar != null) {
            btnEditAvatar.setOnClickListener(v -> selectImage());
        }

        // 保存按钮 - 添加空值检查
        View btnSave = findViewById(R.id.btn_save);
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveProfile());
        }

        // 返回按钮 - 添加空值检查
        View ivBack = findViewById(R.id.iv_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "选择头像"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            selectedAvatarUri = imageUri.toString();

            // 显示选择的图片
            Glide.with(this)
                    .load(imageUri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivAvatar);

            Toast.makeText(this, "头像已选择", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfile() {
        String username = etUsername.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 直接使用 currentGender 变量，不需要再调用 getSelectedGender()
        int gender = currentGender;

        // 创建更新后的用户资料
        UserProfile updatedProfile = createUpdatedProfile(username, bio, gender);

        // 保存到统一数据源
        UserProfileManager.INSTANCE.updateProfile(this, updatedProfile);

        Toast.makeText(this, "资料更新成功", Toast.LENGTH_SHORT).show();
        finish();
    }

    private int getSelectedGender() {
        // 根据当前选中的性别返回对应的值
        return currentGender;
    }

    private UserProfile createUpdatedProfile(String username, String bio, int gender) {
        // 获取收藏的话题列表
        List<Topic> collectedTopics = UserProfileManager.INSTANCE.getCollectedTopics(this);
        if (collectedTopics == null) {
            collectedTopics = new ArrayList<>();
        }

        // 创建更新后的用户资料
        return new UserProfile(
                username,
                bio,
                gender,
                selectedAvatarUri != null ? selectedAvatarUri : currentProfile.getAvatarUri(),
                currentProfile.getCollectedNews(),
                currentProfile.getLikedNews(),
                currentProfile.getDislikedNews(),
                collectedTopics  // 直接传递List<Topic>
        );
    }

// 可以删除 getCollectedTopicsAsJson() 方法，因为不再需要

    /**
     * 将收藏的话题列表转换为JSON字符串
     */
    private String getCollectedTopicsAsJson() {
        try {
            // 从当前用户资料获取收藏的话题
            List<com.example.insightnewsandroid.data.model.Topic> collectedTopics =
                    UserProfileManager.INSTANCE.getCollectedTopics(this);

            if (collectedTopics != null && !collectedTopics.isEmpty()) {
                Gson gson = new Gson();
                return gson.toJson(collectedTopics);
            }
        } catch (Exception e) {
            Log.e(TAG, "转换收藏话题为JSON失败: " + e.getMessage());
        }
        return "[]"; // 默认返回空数组
    }

    /**
     * 处理返回键
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}