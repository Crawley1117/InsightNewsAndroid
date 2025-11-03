package com.example.insightnewsandroid.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.auth.AuthRepository;
import com.example.insightnewsandroid.auth.WelcomeActivity;
import com.example.insightnewsandroid.data.UserProfileManager;
import com.example.insightnewsandroid.data.model.UserProfile;
import com.example.insightnewsandroid.HistoryActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int TAKE_PHOTO_REQUEST = 2;

    private AuthRepository authRepository;
    private UserProfile currentProfile;

    // Views
    private ImageView ivAvatar, ivGender;
    private TextView tvUsername, tvBio;

    private String currentPhotoPath;

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

    private void setupClickListeners() {
        // 编辑资料
        requireView().findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            Log.d("ProfileFragment", "点击编辑资料按钮");
            try {
                Intent intent = new Intent(requireContext(), EditProfileActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("ProfileFragment", "启动 EditProfileActivity 失败", e);
                Toast.makeText(requireContext(), "无法打开编辑资料: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // 新闻收藏
        requireView().findViewById(R.id.btn_collection).setOnClickListener(v -> {
            Log.d("ProfileFragment", "点击新闻收藏按钮");
            try {
                Intent intent = new Intent(requireContext(), NewsCollectionActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("ProfileFragment", "启动 NewsCollectionActivity 失败", e);
                Toast.makeText(requireContext(), "无法打开新闻收藏: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // 话题收藏
        requireView().findViewById(R.id.btn_topic_collection).setOnClickListener(v -> {
            Log.d("ProfileFragment", "点击话题收藏按钮");
            try {
                Intent intent = new Intent(requireContext(), TopicCollectionActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("ProfileFragment", "启动 TopicCollectionActivity 失败", e);
                Toast.makeText(requireContext(), "无法打开话题收藏: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // 历史记录 - 新增的点击事件
        requireView().findViewById(R.id.btn_history).setOnClickListener(v -> {
            Log.d("ProfileFragment", "点击历史记录按钮");
            try {
                Log.d("ProfileFragment", "准备启动 HistoryActivity");
                Intent intent = new Intent(requireContext(), HistoryActivity.class);
                Log.d("ProfileFragment", "Intent 创建成功");
                startActivity(intent);
                Log.d("ProfileFragment", "成功启动 HistoryActivity");
            } catch (Exception e) {
                Log.e("ProfileFragment", "启动 HistoryActivity 失败", e);
                Toast.makeText(requireContext(), "无法打开历史记录: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });

        // 设置
        requireView().findViewById(R.id.btn_setting).setOnClickListener(v -> {
            Log.d("ProfileFragment", "点击设置按钮");
            Toast.makeText(requireContext(), "设置功能开发中", Toast.LENGTH_SHORT).show();
        });

        // 问题反馈
        requireView().findViewById(R.id.btn_feedback).setOnClickListener(v -> {
            Log.d("ProfileFragment", "点击问题反馈按钮");
            showFeedbackDialog();
        });

        // 退出登录 - 关键修改：添加退出登录按钮监听
        requireView().findViewById(R.id.btn_logout).setOnClickListener(v -> {
            Log.d("ProfileFragment", "点击退出登录按钮");
            showLogoutDialog();
        });

        // 头像编辑按钮 - 修改为显示选择对话框
        requireView().findViewById(R.id.btn_edit_avatar).setOnClickListener(v -> {
            Log.d("ProfileFragment", "点击头像编辑按钮");
            showImageSelectionDialog();
        });

        // 头像本身也可以点击
        ivAvatar.setOnClickListener(v -> {
            Log.d("ProfileFragment", "点击头像");
            showImageSelectionDialog();
        });
    }

    // 显示选择图片方式的对话框
    private void showImageSelectionDialog() {
        String[] options = {"拍照", "从相册选择", "取消"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
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
        if (takePictureIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            // 创建图片文件
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(requireContext(), "创建图片文件失败", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST);
            }
        } else {
            Toast.makeText(requireContext(), "没有找到相机应用", Toast.LENGTH_SHORT).show();
        }
    }

    // 创建图片文件
    private File createImageFile() throws IOException {
        // 创建唯一的文件名
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK) {
            Uri imageUri = null;

            switch (requestCode) {
                case PICK_IMAGE_REQUEST: // 从相册选择
                    if (data != null && data.getData() != null) {
                        imageUri = data.getData();
                        Toast.makeText(requireContext(), "头像已选择", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case TAKE_PHOTO_REQUEST: // 拍照
                    if (currentPhotoPath != null) {
                        File photoFile = new File(currentPhotoPath);
                        if (photoFile.exists()) {
                            imageUri = Uri.fromFile(photoFile);
                            Toast.makeText(requireContext(), "照片已拍摄", Toast.LENGTH_SHORT).show();

                            // 通知系统更新相册
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            mediaScanIntent.setData(imageUri);
                            requireContext().sendBroadcast(mediaScanIntent);
                        }
                    }
                    break;
            }

            // 如果有图片URI，更新头像
            if (imageUri != null) {
                updateAvatar(imageUri.toString());
            }
        }
    }

    // 更新头像
    private void updateAvatar(String imageUriString) {
        // 获取当前用户资料
        UserProfile currentProfile = UserProfileManager.INSTANCE.getCurrentProfile(requireContext());

        // 更新用户资料 - 传递所有参数
        UserProfile updatedProfile = new UserProfile(
                currentProfile.getUserId(),           // 用户ID
                currentProfile.getUsername(),         // 使用现有的用户名
                currentProfile.getBio(),              // 使用现有的简介
                currentProfile.getGender(),           // 使用现有的性别
                imageUriString,                       // 使用新选择的图片URI
                currentProfile.getCollectedNews(),    // 收藏的新闻
                currentProfile.getLikedNews(),        // 点赞的新闻
                currentProfile.getDislikedNews(),     // 不喜欢的新闻
                currentProfile.getCollectedTopics()   // 收藏的话题
        );

        // 保存到统一数据源
        UserProfileManager.INSTANCE.updateProfile(requireContext(), updatedProfile);

        // 更新当前资料引用
        currentProfile = updatedProfile;

        // 重新加载数据
        loadUserData();

        Toast.makeText(requireContext(), "头像更新成功", Toast.LENGTH_SHORT).show();
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
                .setNegativeButton("取消", null)
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
