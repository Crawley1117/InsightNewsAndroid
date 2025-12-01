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

public class SearchViewModel extends ViewModel {

    private final NewsRepository newsRepository;
    private final MutableLiveData<ApiResponse<List<String>>> hotSearchTerms = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse<List<String>>> searchHistory = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse<Void>> clearHistoryResult = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse<List<NewsArticle>>> searchResults = new MutableLiveData<>();

    public SearchViewModel() {
        this.newsRepository = new NewsRepository();
    }

    public LiveData<ApiResponse<List<String>>> getHotSearchTerms() {
        return hotSearchTerms;
    }

    public LiveData<ApiResponse<List<String>>> getSearchHistory() {
        return searchHistory;
    }

    public LiveData<ApiResponse<Void>> getClearHistoryResult() {
        return clearHistoryResult;
    }

    public LiveData<ApiResponse<List<NewsArticle>>> getSearchResults() {
        return searchResults;
    }

    // [已修正] 添加token参数
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

    public void loadSearchHistory(String token) {
        newsRepository.getSearchHistory(token).enqueue(new Callback<ApiResponse<List<String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<String>>> call, Response<ApiResponse<List<String>>> response) {
                searchHistory.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ApiResponse<List<String>>> call, Throwable t) {
                searchHistory.setValue(null);
            }
        });
    }

    public void clearSearchHistory(String token) {
        newsRepository.clearSearchHistory(token).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                clearHistoryResult.setValue(response.body());
                if(response.isSuccessful()){
                    loadSearchHistory(token);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                clearHistoryResult.setValue(null);
            }
        });
    }

    public void performSearch(String token, String keyword) {
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