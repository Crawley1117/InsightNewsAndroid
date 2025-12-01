package com.example.insightnewsandroid.data.repository;

import com.example.insightnewsandroid.data.model.ApiResponse;
import com.example.insightnewsandroid.data.model.Comment;
import com.example.insightnewsandroid.data.model.NewsArticle;
import com.example.insightnewsandroid.data.model.User;
import com.example.insightnewsandroid.data.model.QuizQuestion;
import com.example.insightnewsandroid.data.remote.ApiClient;
import com.example.insightnewsandroid.data.remote.NewsApiService;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;

public class NewsRepository {

    private final NewsApiService newsApiService = ApiClient.getApiService();

    // 修改所有方法，移除 "Bearer " 前缀
    public Call<ApiResponse<List<NewsArticle>>> getHotTopics(String token, String category) {
        String finalCategory = (category == null || category.isEmpty() || "全部".equals(category)) ? null : category;
        // [已修改] 直接使用 token，不添加 "Bearer " 前缀
        String authHeader = (token == null || token.isEmpty()) ? null : token;
        return newsApiService.getHotTopics(authHeader, finalCategory);
    }

    public Call<ApiResponse<NewsArticle>> getTopicDetails(String token, int topicId) {
        // [已修改] 直接使用 token，不添加 "Bearer " 前缀
        String authHeader = (token == null || token.isEmpty()) ? null : token;
        return newsApiService.getTopicDetails(authHeader, topicId);
    }

    public Call<ApiResponse<Void>> toggleTopicFavorite(String token, int topicId) {
        // [已修改] 直接使用 token，不添加 "Bearer " 前缀
        String authHeader = (token == null || token.isEmpty()) ? null : token;
        return newsApiService.toggleTopicFavorite(authHeader, topicId);
    }

    public Call<ApiResponse<List<Comment>>> getComments(String token, String topicId, int page, int pagesize) {
        // [已修改] 直接使用 token，不添加 "Bearer " 前缀
        String authHeader = (token == null || token.isEmpty()) ? null : token;
        return newsApiService.getComments(authHeader, topicId, page, pagesize);
    }

    public Call<ApiResponse<Void>> postComment(String token, String topicId, Comment comment) {
        // [已修改] 直接使用 token，不添加 "Bearer " 前缀
        String authHeader = (token == null || token.isEmpty()) ? null : token;
        return newsApiService.postComment(authHeader, topicId, comment);
    }

    public Call<ApiResponse<Void>> toggleCommentLike(String token, int commentId) {
        // [已修改] 直接使用 token，不添加 "Bearer " 前缀
        String authHeader = (token == null || token.isEmpty()) ? null : token;
        return newsApiService.toggleCommentLike(authHeader, commentId);
    }

    public Call<ApiResponse<List<Comment>>> getCommentReplies(String token, int commentId) {
        // [已修改] 直接使用 token，不添加 "Bearer " 前缀
        String authHeader = (token == null || token.isEmpty()) ? null : token;
        return newsApiService.getCommentReplies(authHeader, commentId);
    }

    public Call<ApiResponse<Void>> deleteComment(String token, int commentId) {
        // [已修改] 直接使用 token，不添加 "Bearer " 前缀
        String authHeader = (token == null || token.isEmpty()) ? null : token;
        return newsApiService.deleteComment(authHeader, commentId);
    }

    public Call<ApiResponse<List<QuizQuestion>>> getBingjianQuiz() {
        return newsApiService.getBingjianQuiz();
    }

    public Call<ApiResponse<List<QuizQuestion>>> getLingxinQuiz() {
        return newsApiService.getLingxinQuiz();
    }

    public Call<ApiResponse<List<QuizQuestion>>> getNuanyangQuiz() {
        return newsApiService.getNuanyangQuiz();
    }

    public Call<ApiResponse<User>> getProfile(String token) {
        // [已修改] 直接使用 token，不添加 "Bearer " 前缀
        String authHeader = (token == null || token.isEmpty()) ? null : token;
        return newsApiService.getProfile(authHeader);
    }

    public Call<ApiResponse<String>> uploadFile(String token, MultipartBody.Part file) {
        // [已修改] 直接使用 token，不添加 "Bearer " 前缀
        String authHeader = (token == null || token.isEmpty()) ? null : token;
        return newsApiService.uploadFile(authHeader, file);
    }

    public Call<ApiResponse<List<NewsArticle>>> getFavoriteTopics(String token) {
        // [已修改] 直接使用 token，不添加 "Bearer " 前缀
        String authHeader = (token == null || token.isEmpty()) ? null : token;
        return newsApiService.getFavoriteTopics(authHeader);
    }

    // [已修改] 直接使用 token，不添加 "Bearer " 前缀
    public Call<ApiResponse<List<String>>> getHotSearchTerms(String token) {
        String authHeader = (token == null || token.isEmpty()) ? null : token;
        return newsApiService.getHotSearchTerms(authHeader);
    }

    public Call<ApiResponse<List<String>>> getSearchHistory(String token) {
        // [已修改] 直接使用 token，不添加 "Bearer " 前缀
        String authHeader = (token == null || token.isEmpty()) ? null : token;
        return newsApiService.getSearchHistory(authHeader);
    }

    public Call<ApiResponse<Void>> clearSearchHistory(String token) {
        // [已修改] 直接使用 token，不添加 "Bearer " 前缀
        String authHeader = (token == null || token.isEmpty()) ? null : token;
        return newsApiService.clearSearchHistory(authHeader);
    }

    public Call<ApiResponse<List<NewsArticle>>> searchTopics(String token, String keyword) {
        // [已修改] 直接使用 token，不添加 "Bearer " 前缀
        String authHeader = (token == null || token.isEmpty()) ? null : token;
        return newsApiService.searchTopics(authHeader, keyword);
    }
}