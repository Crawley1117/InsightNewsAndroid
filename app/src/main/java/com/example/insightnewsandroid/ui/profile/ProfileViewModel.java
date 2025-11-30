
package com.example.insightnewsandroid.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.insightnewsandroid.data.model.ApiResponse;
import com.example.insightnewsandroid.data.model.User;
import com.example.insightnewsandroid.data.repository.NewsRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileViewModel extends ViewModel {
    private NewsRepository newsRepository;
    private MutableLiveData<ApiResponse<User>> userProfile = new MutableLiveData<>();

    public ProfileViewModel() {
        this.newsRepository = new NewsRepository();
    }

    public LiveData<ApiResponse<User>> getUserProfile() {
        return userProfile;
    }

    public void fetchUserProfile(String token) {
        newsRepository.getProfile(token).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful()) {
                    userProfile.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                // Handle failure
            }
        });
    }
}
