package com.example.insightnewsandroid.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.auth.AuthRepository;
import com.example.insightnewsandroid.data.model.User;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private static final int MAX_USERNAME_LENGTH = 20;
    private static final int MAX_BIO_LENGTH = 20;

    private EditProfileViewModel viewModel;
    private AuthRepository authRepository;

    private ImageView ivAvatar;
    // [已修改] 添加 etRegion 和 etEmail
    private EditText etUsername, etBio, etRegion, etEmail;
    private TextView tvCharCount;

    private User currentUserData;
    private String originalAvatarUrl;
    private String uploadedAvatarUrl; 
    private String token;
    private int currentGender = 2;

    private Uri tempPhotoUri;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Uri> takePhotoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_edit_profile);

        viewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);
        authRepository = new AuthRepository(this);
        token = authRepository.getAuthToken();

        initViews();
        registerActivityLaunchers();
        setupClickListeners();
        setupCharCount();
        setupGenderSelection();
        setupObservers();
        loadInitialUserData();
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.iv_avatar);
        etUsername = findViewById(R.id.et_name);
        etBio = findViewById(R.id.et_profile);
        tvCharCount = findViewById(R.id.tv_char_count);
        // [已新增] 初始化新增的视图
        etRegion = findViewById(R.id.et_region);
        etEmail = findViewById(R.id.et_email);
    }

    private void registerActivityLaunchers() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            handleAvatarUpdate(imageUri);
                        }
                    }
                });

        takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && tempPhotoUri != null) {
                        handleAvatarUpdate(tempPhotoUri);
                    }
                });
    }

    private void setupObservers() {
        viewModel.getUserProfile().observe(this, apiResponse -> {
            if (apiResponse != null && apiResponse.getCode() == 200) {
                currentUserData = apiResponse.getData();
                if (currentUserData != null) {
                    displayUserData(currentUserData);
                }
            } else {
                Toast.makeText(this, "加载用户信息失败", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getUpdateSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "资料更新成功", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "资料更新失败", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getAvatarUploadResponse().observe(this, apiResponse -> {
            if (apiResponse != null && apiResponse.getCode() == 200) {
                uploadedAvatarUrl = apiResponse.getData();
                Toast.makeText(this, "头像上传成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "头像上传失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save).setOnClickListener(v -> saveProfile());
        ivAvatar.setOnClickListener(v -> showImageSelectionDialog());
        View btnEditAvatar = findViewById(R.id.btn_edit_avatar);
        if (btnEditAvatar != null) { 
            btnEditAvatar.setOnClickListener(v -> showImageSelectionDialog());
        }
    }

    private void loadInitialUserData() {
        if (token != null && !token.isEmpty()) {
            viewModel.fetchUserProfile(token);
        } else {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayUserData(User user) {
        etUsername.setText(user.getUsername());
        etBio.setText(user.getProfile());
        originalAvatarUrl = user.getAvatarUrl();
        // [已新增] 显示新增的字段信息
        etRegion.setText(user.getRegion());
        etEmail.setText(user.getEmail());

        if ("男".equals(user.getGender())) setGenderSelection(0);
        else if ("女".equals(user.getGender())) setGenderSelection(1);
        else setGenderSelection(2);

        Glide.with(this).load(originalAvatarUrl).placeholder(R.drawable.avatar_placeholder).error(R.drawable.avatar_placeholder).apply(RequestOptions.circleCropTransform()).into(ivAvatar);
    }

    private void saveProfile() {
        if (token == null || token.isEmpty()) return;

        // [已修改] 收集所有字段的信息
        String newUsername = etUsername.getText().toString().trim();
        String newBio = etBio.getText().toString().trim();
        String newRegion = etRegion.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();

        User userToUpdate = new User();
        userToUpdate.setUsername(newUsername);
        userToUpdate.setProfile(newBio);
        userToUpdate.setRegion(newRegion);
        userToUpdate.setEmail(newEmail);
        
        userToUpdate.setAvatarUrl(uploadedAvatarUrl != null ? uploadedAvatarUrl : originalAvatarUrl);
        
        String genderString;
        switch (currentGender) {
            case 0: genderString = "男"; break;
            case 1: genderString = "女"; break;
            default: genderString = "保密"; break;
        }
        userToUpdate.setGender(genderString);

        viewModel.updateUserProfile(token, userToUpdate);
    }

    private void handleAvatarUpdate(Uri imageUri) {
        displaySelectedImage(imageUri);
        if (token != null && !token.isEmpty()) {
            Toast.makeText(this, "正在上传头像...", Toast.LENGTH_SHORT).show();
            viewModel.uploadAvatar(token, imageUri, this);
        } else {
            Toast.makeText(this, "登录状态失效，无法上传", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupGenderSelection() {
        findViewById(R.id.ll_female).setOnClickListener(v -> setGenderSelection(1));
        findViewById(R.id.ll_male).setOnClickListener(v -> setGenderSelection(0));
        findViewById(R.id.ll_unknown).setOnClickListener(v -> setGenderSelection(2));
    }

    private void setGenderSelection(int gender) {
        resetGenderSelection();
        switch (gender) {
            case 0: findViewById(R.id.ll_male).setBackgroundResource(R.drawable.gender_selected_background); break;
            case 1: findViewById(R.id.ll_female).setBackgroundResource(R.drawable.gender_selected_background); break;
            case 2: findViewById(R.id.ll_unknown).setBackgroundResource(R.drawable.gender_selected_background); break;
        }
        currentGender = gender;
    }

    private void resetGenderSelection() {
        findViewById(R.id.ll_male).setBackgroundResource(android.R.color.transparent);
        findViewById(R.id.ll_female).setBackgroundResource(android.R.color.transparent);
        findViewById(R.id.ll_unknown).setBackgroundResource(android.R.color.transparent);
    }

    private void setupCharCount() {
        etBio.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int currentLength = s.length();
                tvCharCount.setText(String.format(Locale.getDefault(), "%d/%d", currentLength, MAX_BIO_LENGTH));
                tvCharCount.setTextColor(ContextCompat.getColor(EditProfileActivity.this, 
                    currentLength > MAX_BIO_LENGTH ? R.color.red : R.color.gray));
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void showImageSelectionDialog() {
        String[] options = {"拍照", "从相册选择", "取消"};
        new androidx.appcompat.app.AlertDialog.Builder(this).setTitle("选择头像").setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: takePhoto(); break;
                case 1: selectImageFromGallery(); break;
                case 2: dialog.dismiss(); break;
            }
        }).show();
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void takePhoto() {
        try {
            File photoFile = createImageFile();
            tempPhotoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", photoFile);
            takePhotoLauncher.launch(tempPhotoUri);
        } catch (IOException ex) {
            Toast.makeText(this, "创建图片文件失败", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void displaySelectedImage(Uri imageUri) {
        Glide.with(this).load(imageUri).apply(RequestOptions.circleCropTransform()).into(ivAvatar);
    }
}
