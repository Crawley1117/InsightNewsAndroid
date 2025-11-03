package com.example.insightnewsandroid.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

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
import java.util.regex.Pattern;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int TAKE_PHOTO_REQUEST = 2;
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final int MAX_BIO_LENGTH = 20;

    private int currentGender = 2;
    private UserProfile currentProfile;
    private ImageView ivAvatar;
    private TextView etUsername, etBio;
    private TextView tvCharCount;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale, rbSecret;

    private String selectedAvatarUri;
    private String currentPhotoPath;

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
        tvCharCount = findViewById(R.id.tv_char_count); // 字符计数TextView

        // 性别选择需要重新实现，因为布局中没有 RadioGroup
        setupGenderSelection();

        // 返回按钮
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        // 保存按钮
        findViewById(R.id.btn_save).setOnClickListener(v -> saveProfile());

        // 设置字符计数监听
        setupCharCount();
    }

    // 验证昵称格式
    private boolean validateUsername(String username) {
        if (username.isEmpty()) {
            Toast.makeText(this, "昵称不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (username.length() > MAX_USERNAME_LENGTH) {
            Toast.makeText(this, "昵称不能超过" + MAX_USERNAME_LENGTH + "个字符", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 检查特殊字符
        if (containsSpecialCharacters(username)) {
            Toast.makeText(this, "昵称不能包含特殊字符", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // 验证个性签名格式
    private boolean validateBio(String bio) {
        if (bio.length() > MAX_BIO_LENGTH) {
            Toast.makeText(this, "个性签名不能超过" + MAX_BIO_LENGTH + "个字符", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // 检查是否包含特殊字符
    private boolean containsSpecialCharacters(String text) {
        // 定义不允许的特殊字符
        String specialChars = "`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？";
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (specialChars.indexOf(c) != -1) {
                return true;
            }
        }
        return false;
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
        etBio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int currentLength = s.length();
                tvCharCount.setText(currentLength + "/" + MAX_BIO_LENGTH);

                // 如果超过20个字，显示红色
                if (currentLength > MAX_BIO_LENGTH) {
                    tvCharCount.setTextColor(ContextCompat.getColor(EditProfileActivity.this, R.color.red));
                } else {
                    // 恢复正常颜色
                    tvCharCount.setTextColor(ContextCompat.getColor(EditProfileActivity.this, R.color.gray));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUserData() {
        currentProfile = UserProfileManager.INSTANCE.getCurrentProfile(this);

        etUsername.setText(currentProfile.getUsername());
        etBio.setText(currentProfile.getBio());

        // 初始化字符计数显示
        int bioLength = currentProfile.getBio() != null ? currentProfile.getBio().length() : 0;
        tvCharCount.setText(bioLength + "/" + MAX_BIO_LENGTH);

        // 如果现有签名超过限制，显示红色
        if (bioLength > MAX_BIO_LENGTH) {
            tvCharCount.setTextColor(ContextCompat.getColor(this, R.color.red));
        }

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
            ivAvatar.setOnClickListener(v -> showImageSelectionDialog());
        }

        // 头像编辑按钮 - 添加空值检查
        View btnEditAvatar = findViewById(R.id.btn_edit_avatar);
        if (btnEditAvatar != null) {
            btnEditAvatar.setOnClickListener(v -> showImageSelectionDialog());
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

    // 显示选择图片方式的对话框
    private void showImageSelectionDialog() {
        String[] options = {"拍照", "从相册选择", "取消"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("选择头像");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // 拍照
                    takePhoto();
                    break;
                case 1: // 从相册选择
                    selectImageFromGallery();
                    break;
                case 2: // 取消
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "选择头像"), PICK_IMAGE_REQUEST);
    }

    // 拍照
    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 确保有相机应用可以处理这个意图
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // 创建图片文件
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "创建图片文件失败", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST);
            }
        } else {
            Toast.makeText(this, "没有找到相机应用", Toast.LENGTH_SHORT).show();
        }
    }

    // 创建图片文件
    private File createImageFile() throws IOException {
        // 创建唯一的文件名
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* 前缀 */
                ".jpg",         /* 后缀 */
                storageDir      /* 目录 */
        );

        // 保存文件路径用于后续使用
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_IMAGE_REQUEST: // 从相册选择
                    if (data != null && data.getData() != null) {
                        Uri imageUri = data.getData();
                        selectedAvatarUri = imageUri.toString();
                        displaySelectedImage(imageUri);
                        Toast.makeText(this, "头像已选择", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case TAKE_PHOTO_REQUEST: // 拍照
                    if (currentPhotoPath != null) {
                        File photoFile = new File(currentPhotoPath);
                        if (photoFile.exists()) {
                            Uri photoUri = Uri.fromFile(photoFile);
                            selectedAvatarUri = photoUri.toString();
                            displaySelectedImage(photoUri);
                            Toast.makeText(this, "照片已拍摄", Toast.LENGTH_SHORT).show();

                            // 通知系统更新相册
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            mediaScanIntent.setData(photoUri);
                            sendBroadcast(mediaScanIntent);
                        }
                    }
                    break;
            }
        }
    }

    // 显示选择的图片
    private void displaySelectedImage(Uri imageUri) {
        Glide.with(this)
                .load(imageUri)
                .apply(RequestOptions.circleCropTransform())
                .into(ivAvatar);
    }

    private void saveProfile() {
        String username = etUsername.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        // 验证昵称格式 - 如果不通过就直接返回，禁止保存
        if (!validateUsername(username)) {
            return;
        }

        // 验证个性签名格式 - 如果不通过就直接返回，禁止保存
        if (!validateBio(bio)) {
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

        // 获取当前用户资料
        UserProfile currentProfile = UserProfileManager.INSTANCE.getCurrentProfile(this);

        // 创建更新后的用户资料 - 需要传递所有参数
        return new UserProfile(
                currentProfile.getUserId(),           // 用户ID
                username,                             // 用户名
                bio,                                  // 个性签名
                gender,                               // 性别
                selectedAvatarUri != null ? selectedAvatarUri : currentProfile.getAvatarUri(), // 头像URI
                currentProfile.getCollectedNews(),    // 收藏的新闻
                currentProfile.getLikedNews(),        // 点赞的新闻
                currentProfile.getDislikedNews(),     // 不喜欢的新闻
                collectedTopics                       // 收藏的话题
        );
    }
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
