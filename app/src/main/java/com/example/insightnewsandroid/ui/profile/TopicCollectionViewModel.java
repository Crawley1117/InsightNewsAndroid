package com.example.insightnewsandroid.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.insightnewsandroid.data.model.ApiResponse;
import com.example.insightnewsandroid.data.model.NewsArticle;
import com.example.insightnewsandroid.data.repository.NewsRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TopicCollectionViewModel extends ViewModel {
    private final NewsRepository newsRepository;
    private final MutableLiveData<List<NewsArticle>> favoriteTopicsDetails = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public TopicCollectionViewModel() {
        this.newsRepository = new NewsRepository();
    }

    public LiveData<List<NewsArticle>> getFavoriteTopicsDetails() {
        return favoriteTopicsDetails;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchFavoriteTopics(String token) {
        // [已修复] 将Callback的类型从List<String>修正为List<NewsArticle>
        newsRepository.getFavoriteTopics(token).enqueue(new Callback<ApiResponse<List<NewsArticle>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<NewsArticle>>> call, Response<ApiResponse<List<NewsArticle>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    favoriteTopicsDetails.setValue(response.body().getData());
                } else {
                    errorMessage.setValue("获取收藏列表失败");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<NewsArticle>>> call, Throwable t) {
                errorMessage.setValue("网络请求失败");
            }
        });
    }
}
