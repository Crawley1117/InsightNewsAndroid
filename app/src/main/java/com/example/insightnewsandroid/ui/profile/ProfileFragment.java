package com.example.insightnewsandroid.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.auth.AuthRepository;
import com.example.insightnewsandroid.auth.WelcomeActivity;
import com.example.insightnewsandroid.data.model.User;
import com.example.insightnewsandroid.HistoryActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale; // [已修复] 添加缺失的导包

public class ProfileFragment extends Fragment {

    private AuthRepository authRepository;
    private ProfileViewModel profileViewModel;

    private ImageView ivAvatar, ivGender;
    private TextView tvUsername, tvBio;

    private String currentPhotoPath;
    private Uri tempPhotoUri;

    // [已优化] 使用新的 Activity Result API
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Uri> takePhotoLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authRepository = new AuthRepository(requireContext());
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // [已优化] 注册 Activity Result 回调
        registerActivityLaunchers();
    }

    private void registerActivityLaunchers() {
        // 注册从相册选择图片的回调
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

        // 注册拍照的回调
        takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success) {
                        if (tempPhotoUri != null) {
                            handleAvatarUpdate(tempPhotoUri);
                        }
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupObservers();
        setupClickListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserDataFromServer();
    }

    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.iv_avatar);
        ivGender = view.findViewById(R.id.iv_gender);
        tvUsername = view.findViewById(R.id.tv_username);
        tvBio = view.findViewById(R.id.tv_bio);
    }

    private void setupObservers() {
        profileViewModel.getUserProfile().observe(getViewLifecycleOwner(), apiResponse -> {
            if (apiResponse != null && apiResponse.getCode() == 200) {
                User user = apiResponse.getData();
                if (user != null) {
                    updateUiWithUserData(user);
                }
            } else {
                String errorMsg = (apiResponse != null) ? apiResponse.getMessage() : "网络请求失败";
                Toast.makeText(getContext(), "加载用户信息失败: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserDataFromServer() {
        String token = authRepository.getAuthToken();
        if (token != null && !token.isEmpty()) {
            profileViewModel.fetchUserProfile(token);
        } else {
            Toast.makeText(getContext(), "请先登录以查看个人信息", Toast.LENGTH_SHORT).show();
            clearUserData();
        }
    }

    private void updateUiWithUserData(User user) {
        tvUsername.setText(user.getUsername());
        tvBio.setVisibility(View.GONE);
        ivGender.setVisibility(View.GONE);

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(this).load(user.getAvatarUrl()).placeholder(R.drawable.avatar_placeholder).error(R.drawable.avatar_placeholder).apply(RequestOptions.circleCropTransform()).into(ivAvatar);
        } else {
            Glide.with(this).load(R.drawable.avatar_placeholder).apply(RequestOptions.circleCropTransform()).into(ivAvatar);
        }
    }

    private void clearUserData() {
        tvUsername.setText("未登录");
        tvBio.setText("登录后可查看更多信息");
        ivAvatar.setImageResource(R.drawable.avatar_placeholder);
        ivGender.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        requireView().findViewById(R.id.btn_edit_profile).setOnClickListener(v -> startActivity(new Intent(requireContext(), EditProfileActivity.class)));
        requireView().findViewById(R.id.btn_collection).setOnClickListener(v -> startActivity(new Intent(requireContext(), NewsCollectionActivity.class)));
        requireView().findViewById(R.id.btn_topic_collection).setOnClickListener(v -> startActivity(new Intent(requireContext(), TopicCollectionActivity.class)));
        requireView().findViewById(R.id.btn_history).setOnClickListener(v -> startActivity(new Intent(requireContext(), HistoryActivity.class)));
        requireView().findViewById(R.id.btn_setting).setOnClickListener(v -> Toast.makeText(requireContext(), "设置功能开发中", Toast.LENGTH_SHORT).show());
        requireView().findViewById(R.id.btn_feedback).setOnClickListener(v -> showFeedbackDialog());
        requireView().findViewById(R.id.btn_logout).setOnClickListener(v -> showLogoutDialog());
        requireView().findViewById(R.id.btn_edit_avatar).setOnClickListener(v -> showImageSelectionDialog());
        ivAvatar.setOnClickListener(v -> showImageSelectionDialog());
    }

    private void showImageSelectionDialog() {
        String[] options = {"拍照", "从相册选择", "取消"};
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("选择头像")
                .setItems(options, (dialog, which) -> {
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
            tempPhotoUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", photoFile);
            takePhotoLauncher.launch(tempPhotoUri);
        } catch (IOException ex) {
            Toast.makeText(requireContext(), "创建图片文件失败", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // [已优化] 统一处理头像更新
    private void handleAvatarUpdate(Uri imageUri) {
        // TODO: 在这里调用 ViewModel 上传图片文件到服务器
        // 1. 调用 ViewModel 的方法，将 imageUri 传递过去
        // 2. ViewModel 负责将 Uri 转换为文件，并通过 Repository 上传
        // 3. 上传成功后，服务器会返回一个新的图片URL
        // 4. 调用 ViewModel 的 updateUserProfile 方法，将包含新URL的User对象保存到服务器
        // 5. 成功后，ViewModel 会更新 LiveData，个人主页会自动刷新显示新头像

        // 临时在UI上显示，以提供即时反馈
        Glide.with(this).load(imageUri).apply(RequestOptions.circleCropTransform()).into(ivAvatar);
        Toast.makeText(requireContext(), "头像已更新（本地预览），上传功能待实现", Toast.LENGTH_SHORT).show();
    }

    private void showFeedbackDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext()).setTitle("问题反馈").setMessage("请将问题反馈发送至：support@insightnews.com").setPositiveButton("确定", null).show();
    }

    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext()).setTitle("确认退出").setMessage("确定要退出登录吗？").setPositiveButton("确定", (dialog, which) -> handleLogout()).setNegativeButton("取消", null).show();
    }

    private void handleLogout() {
        authRepository.logout();
        Toast.makeText(requireContext(), "已登出", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(requireContext(), WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
