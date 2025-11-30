package com.example.insightnewsandroid.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.insightnewsandroid.data.model.ApiResponse;
import com.example.insightnewsandroid.data.model.NewsArticle;
import com.example.insightnewsandroid.data.repository.NewsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
        newsRepository.getFavoriteTopics(token).enqueue(new Callback<ApiResponse<List<String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<String>>> call, Response<ApiResponse<List<String>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    List<String> topicIds = response.body().getData();
                    if (topicIds != null && !topicIds.isEmpty()) {
                        fetchDetailsForTopicIds(token, topicIds);
                    } else {
                        favoriteTopicsDetails.setValue(new ArrayList<>());
                    }
                } else {
                    errorMessage.setValue("获取收藏列表失败");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<String>>> call, Throwable t) {
                errorMessage.setValue("网络请求失败");
            }
        });
    }

    private void fetchDetailsForTopicIds(String token, List<String> topicIds) {
        List<NewsArticle> detailsList = new ArrayList<>();
        AtomicInteger successfulCalls = new AtomicInteger(0);

        for (String id : topicIds) {
            try {
                int topicId = Integer.parseInt(id);
                // [已修复] 调用正确的方法名 getTopicDetails
                newsRepository.getTopicDetails(token, topicId).enqueue(new Callback<ApiResponse<NewsArticle>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<NewsArticle>> call, Response<ApiResponse<NewsArticle>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                            NewsArticle article = response.body().getData();
                            if (article != null) {
                                detailsList.add(article);
                            }
                        }
                        if (successfulCalls.incrementAndGet() == topicIds.size()) {
                            favoriteTopicsDetails.setValue(detailsList);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<NewsArticle>> call, Throwable t) {
                        if (successfulCalls.incrementAndGet() == topicIds.size()) {
                            favoriteTopicsDetails.setValue(detailsList);
                        }
                    }
                });
            } catch (NumberFormatException e) {
                if (successfulCalls.incrementAndGet() == topicIds.size()) {
                    favoriteTopicsDetails.setValue(detailsList);
                }
            }
        }
    }
}
