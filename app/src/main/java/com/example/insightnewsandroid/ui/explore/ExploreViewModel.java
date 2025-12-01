package com.example.insightnewsandroid.ui.explore;

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

public class ExploreViewModel extends ViewModel {

    private final NewsRepository newsRepository = new NewsRepository();
    private final MutableLiveData<ApiResponse<List<NewsArticle>>> hotTopics = new MutableLiveData<>();

    public LiveData<ApiResponse<List<NewsArticle>>> getHotTopics() {
        return hotTopics;
    }

    public void fetchHotTopics(String token, String category) {
        newsRepository.getHotTopics(token, category).enqueue(new Callback<ApiResponse<List<NewsArticle>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<NewsArticle>>> call, Response<ApiResponse<List<NewsArticle>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    hotTopics.postValue(response.body());
                } else {
                    // Log details for debugging
                    android.util.Log.w("ExploreViewModel", "getHotTopics failed: code=" + response.code() + " body=" + response.errorBody());
                    hotTopics.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<NewsArticle>>> call, Throwable t) {
                android.util.Log.e("ExploreViewModel", "getHotTopics onFailure: " + t.getMessage(), t);
                hotTopics.postValue(null); // Indicate failure
            }
        });
    }
}
