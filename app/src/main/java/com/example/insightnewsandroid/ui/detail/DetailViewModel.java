package com.example.insightnewsandroid.ui.detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.insightnewsandroid.data.model.ApiResponse;
import com.example.insightnewsandroid.data.model.Comment;
import com.example.insightnewsandroid.data.model.NewsArticle;
import com.example.insightnewsandroid.data.repository.NewsRepository;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailViewModel extends ViewModel {

    private final NewsRepository newsRepository;
    private final MutableLiveData<ApiResponse<NewsArticle>> topicDetails = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse<List<Comment>>> comments = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse<Void>> commentPostResult = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse<Void>> toggleLikeResult = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse<List<Comment>>> commentReplies = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse<Void>> deleteCommentResult = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse<Void>> toggleFavoriteResult = new MutableLiveData<>();

    public DetailViewModel() {
        this.newsRepository = new NewsRepository();
    }

    public LiveData<ApiResponse<NewsArticle>> getTopicDetails() { return topicDetails; }
    public LiveData<ApiResponse<List<Comment>>> getComments() { return comments; }
    public LiveData<ApiResponse<Void>> getCommentPostResult() { return commentPostResult; }
    public LiveData<ApiResponse<Void>> getToggleLikeResult() { return toggleLikeResult; }
    public LiveData<ApiResponse<List<Comment>>> getCommentReplies() { return commentReplies; }
    public LiveData<ApiResponse<Void>> getDeleteCommentResult() { return deleteCommentResult; }
    public LiveData<ApiResponse<Void>> getToggleFavoriteResult() { return toggleFavoriteResult; }

    public void fetchTopicDetails(String token, int topicId) {
        newsRepository.getTopicDetails(token, topicId).enqueue(new Callback<ApiResponse<NewsArticle>>() {
            @Override
            public void onResponse(Call<ApiResponse<NewsArticle>> call, Response<ApiResponse<NewsArticle>> response) {
                topicDetails.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ApiResponse<NewsArticle>> call, Throwable t) {
                topicDetails.setValue(null);
            }
        });
    }

    public void toggleTopicFavorite(String token, int topicId) {
        newsRepository.toggleTopicFavorite(token, topicId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                toggleFavoriteResult.setValue(response.body());
                if(response.isSuccessful()){
                    // 成功后，重新获取详情以更新UI状态
                    fetchTopicDetails(token, topicId);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                toggleFavoriteResult.setValue(null);
            }
        });
    }

    public void fetchComments(String token, String topicId, int page, int pagesize) {
        newsRepository.getComments(token, topicId, page, pagesize).enqueue(new Callback<ApiResponse<List<Comment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Comment>>> call, Response<ApiResponse<List<Comment>>> response) {
                comments.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ApiResponse<List<Comment>>> call, Throwable t) {
                comments.setValue(null);
            }
        });
    }

    public void fetchCommentReplies(String token, int commentId) {
        newsRepository.getCommentReplies(token, commentId).enqueue(new Callback<ApiResponse<List<Comment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Comment>>> call, Response<ApiResponse<List<Comment>>> response) {
                commentReplies.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ApiResponse<List<Comment>>> call, Throwable t) {
                commentReplies.setValue(null);
            }
        });
    }

    public void postComment(String token, String topicId, Comment comment) {
        newsRepository.postComment(token, topicId, comment).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                commentPostResult.setValue(response.body());
                if (response.isSuccessful()) {
                    fetchComments(token, topicId, 1, 10);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                commentPostResult.setValue(null);
            }
        });
    }

    public void toggleCommentLike(String token, int commentId) {
        newsRepository.toggleCommentLike(token, commentId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                toggleLikeResult.setValue(response.body());
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                toggleLikeResult.setValue(null);
            }
        });
    }

    public void deleteComment(String token, String topicId, int commentId) {
        newsRepository.deleteComment(token, commentId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                deleteCommentResult.setValue(response.body());
                if (response.isSuccessful()) {
                    fetchComments(token, topicId, 1, 10);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                deleteCommentResult.setValue(null);
            }
        });
    }

    public void resetCommentPostResult() {
        commentPostResult.setValue(null);
    }
}
