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
    private final NewsRepository newsRepository;
    private final MutableLiveData<ApiResponse<List<NewsArticle>>> hotTopics = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse<List<String>>> hotSearchTerms = new MutableLiveData<>();

    public ExploreViewModel() {
        this.newsRepository = new NewsRepository();
    }

    public LiveData<ApiResponse<List<NewsArticle>>> getHotTopics() {
        return hotTopics;
    }

    public LiveData<ApiResponse<List<String>>> getHotSearchTerms() {
        return hotSearchTerms;
    }

    public void fetchHotTopics(String token, String category) {
        newsRepository.getHotTopics(token, category).enqueue(new Callback<ApiResponse<List<NewsArticle>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<NewsArticle>>> call, Response<ApiResponse<List<NewsArticle>>> response) {
                hotTopics.setValue(response.body());
            }

            @Override
            public void onFailure(Call<ApiResponse<List<NewsArticle>>> call, Throwable t) {
                hotTopics.setValue(null);
            }
        });
    }

    public void fetchHotSearchTerms(String token) {
        newsRepository.getHotSearchTerms(token).enqueue(new Callback<ApiResponse<List<String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<String>>> call, Response<ApiResponse<List<String>>> response) {
                hotSearchTerms.setValue(response.body());
            }

            @Override
            public void onFailure(Call<ApiResponse<List<String>>> call, Throwable t) {
                hotSearchTerms.setValue(null);
            }
        });
    }
}
