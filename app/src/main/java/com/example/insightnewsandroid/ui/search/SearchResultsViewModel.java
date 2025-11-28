package com.example.insightnewsandroid.ui.search;

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

public class SearchResultsViewModel extends ViewModel {

    private final NewsRepository newsRepository;
    private final MutableLiveData<ApiResponse<List<NewsArticle>>> searchResults = new MutableLiveData<>();

    public SearchResultsViewModel() {
        this.newsRepository = new NewsRepository();
    }

    public LiveData<ApiResponse<List<NewsArticle>>> getSearchResults() {
        return searchResults;
    }

    // [已修改] 实现真实的搜索API调用
    public void searchTopics(String token, String keyword) {
        newsRepository.searchTopics(token, keyword).enqueue(new Callback<ApiResponse<List<NewsArticle>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<NewsArticle>>> call, Response<ApiResponse<List<NewsArticle>>> response) {
                searchResults.setValue(response.body());
            }

            @Override
            public void onFailure(Call<ApiResponse<List<NewsArticle>>> call, Throwable t) {
                searchResults.setValue(null);
            }
        });
    }
}
