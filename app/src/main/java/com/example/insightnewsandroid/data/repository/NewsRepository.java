package com.example.insightnewsandroid.data.repository;

import com.example.insightnewsandroid.data.model.ApiResponse;
import com.example.insightnewsandroid.data.model.Comment;
import com.example.insightnewsandroid.data.model.LoginResponse;
import com.example.insightnewsandroid.data.model.NewsArticle;
import com.example.insightnewsandroid.data.model.QuizQuestion;
import com.example.insightnewsandroid.data.model.User;
import com.example.insightnewsandroid.data.remote.ApiService;
import com.example.insightnewsandroid.data.remote.RetrofitClient;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;

public class NewsRepository {
    private final ApiService apiService;

    public NewsRepository() {
        this.apiService = RetrofitClient.getApiService();
    }

    // User Authentication
    public Call<ApiResponse<LoginResponse>> login(User user) {
        return apiService.login(user);
    }

    public Call<ApiResponse<Void>> register(User user) {
        return apiService.register(user);
    }

    // User Profile
    public Call<ApiResponse<User>> getProfile(String token) {
        return apiService.getProfile(token);
    }

    public Call<ApiResponse<Void>> updateUser(String token, User user) {
        return apiService.updateUser(token, user);
    }

    public Call<ApiResponse<List<String>>> getFavoriteTopics(String token) {
        return apiService.getFavoriteTopics(token);
    }

    // News & Topics Articles
    public Call<ApiResponse<List<NewsArticle>>> getHotTopics(String token, String category) {
        return apiService.getHotTopics(token, category);
    }

    public Call<ApiResponse<NewsArticle>> getTopicDetails(String token, int topicId) {
        return apiService.getTopicDetails(token, topicId);
    }

    public Call<ApiResponse<Void>> toggleTopicFavorite(String token, int topicId) {
        return apiService.toggleTopicFavorite(token, topicId);
    }

    public Call<ApiResponse<List<String>>> getHotSearchTerms(String token) {
        return apiService.getHotSearchTerms(token);
    }

    public Call<ApiResponse<List<NewsArticle>>> searchTopics(String token, String keyword) {
        return apiService.searchTopics(token, keyword);
    }

    // Search History
    public Call<ApiResponse<List<String>>> getSearchHistory(String token) {
        return apiService.getSearchHistory(token);
    }

    public Call<ApiResponse<Void>> clearSearchHistory(String token) {
        return apiService.clearSearchHistory(token);
    }

    // Comments
    public Call<ApiResponse<List<Comment>>> getComments(String token, String topicId, int page, int pagesize) {
        return apiService.getComments(token, topicId, page, pagesize);
    }

    public Call<ApiResponse<List<Comment>>> getCommentReplies(String token, int commentId) {
        return apiService.getCommentReplies(token, commentId);
    }

    public Call<ApiResponse<Void>> postComment(String token, String topicId, Comment comment) {
        return apiService.postComment(token, topicId, comment);
    }

    public Call<ApiResponse<Void>> toggleCommentLike(String token, int commentId) {
        return apiService.toggleCommentLike(token, commentId);
    }

    public Call<ApiResponse<Void>> deleteComment(String token, int commentId) {
        return apiService.deleteComment(token, commentId);
    }

    // Quiz
    public Call<ApiResponse<List<QuizQuestion>>> getBingjianQuiz() {
        return apiService.getBingjianQuiz();
    }
    
    public Call<ApiResponse<List<QuizQuestion>>> getLingxinQuiz() {
        return apiService.getLingxinQuiz();
    }

    public Call<ApiResponse<List<QuizQuestion>>> getNuanyangQuiz() {
        return apiService.getNuanyangQuiz();
    }

    // 文件上传
    public Call<ApiResponse<String>> uploadFile(String token, MultipartBody.Part file) {
        return apiService.uploadFile(token, file);
    }
}
