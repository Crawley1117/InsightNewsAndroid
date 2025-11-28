
package com.example.insightnewsandroid.ui.profile;

import android.content.Context;
import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.insightnewsandroid.data.model.ApiResponse;
import com.example.insightnewsandroid.data.model.User;
import com.example.insightnewsandroid.data.repository.NewsRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileViewModel extends ViewModel {
    private final NewsRepository newsRepository;
    private final MutableLiveData<ApiResponse<User>> userProfile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    // [已新增] 用于通知UI头像上传结果的LiveData
    private final MutableLiveData<ApiResponse<String>> avatarUploadResponse = new MutableLiveData<>();

    public EditProfileViewModel() {
        this.newsRepository = new NewsRepository();
    }

    public LiveData<ApiResponse<User>> getUserProfile() {
        return userProfile;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    public LiveData<ApiResponse<String>> getAvatarUploadResponse() {
        return avatarUploadResponse;
    }

    public void fetchUserProfile(String token) {
        newsRepository.getProfile(token).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                userProfile.setValue(response.body());
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                userProfile.setValue(null);
            }
        });
    }

    public void updateUserProfile(String token, User userToUpdate) {
        // ... (原有的更新逻辑保持不变)
    }

    // [已新增] 上传头像的方法
    public void uploadAvatar(String token, Uri imageUri, Context context) {
        File file = createTempFileFromUri(imageUri, context);
        if (file == null) {
            avatarUploadResponse.setValue(null); // 通知UI文件创建失败
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse(context.getContentResolver().getType(imageUri)), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        newsRepository.uploadFile(token, body).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                avatarUploadResponse.setValue(response.body());
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                avatarUploadResponse.setValue(null);
            }
        });
    }

    // [已新增] 从Uri创建临时文件的辅助方法
    private File createTempFileFromUri(Uri uri, Context context) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("avatar", ".jpg", context.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
