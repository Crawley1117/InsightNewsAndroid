package com.example.insightnewsandroid.data.remote;

import com.example.insightnewsandroid.data.model.ApiResponse;
import com.example.insightnewsandroid.data.model.Comment;
import com.example.insightnewsandroid.data.model.NewsArticle;
import com.example.insightnewsandroid.data.model.User;
import com.example.insightnewsandroid.data.model.QuizQuestion;
import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NewsApiService {

    @POST("user/login")
    Call<ApiResponse<String>> login(@Body User user);

    @POST("user/register")
    Call<ApiResponse<Void>> register(@Body User user);

    @GET("user/profile")
    Call<ApiResponse<User>> getProfile(@Header("Authorization") String token);

    @PUT("user/profile")
    Call<ApiResponse<Void>> updateUserProfile(@Header("Authorization") String token, @Body User user);

    @Multipart
    @POST("user/upload")
    Call<ApiResponse<String>> uploadFile(@Header("Authorization") String token, @Part MultipartBody.Part file);

    @GET("topic")
    Call<ApiResponse<List<NewsArticle>>> getHotTopics(@Header("Authorization") String token, @Query("category") String category);

    @GET("topic/{id}")
    Call<ApiResponse<NewsArticle>> getTopicDetails(@Header("Authorization") String token, @Path("id") int topicId);

    @POST("topic/favorite/{id}")
    Call<ApiResponse<Void>> toggleTopicFavorite(@Header("Authorization") String token, @Path("id") int topicId);

    @GET("topic/favorites")
    Call<ApiResponse<List<NewsArticle>>> getFavoriteTopics(@Header("Authorization") String token);

    @GET("comment/{topicId}")
    Call<ApiResponse<List<Comment>>> getComments(@Header("Authorization") String token, @Path("topicId") String topicId, @Query("page") int page, @Query("pagesize") int pagesize);

    @POST("comment/{topicId}")
    Call<ApiResponse<Void>> postComment(@Header("Authorization") String token, @Path("topicId") String topicId, @Body Comment comment);

    @POST("comment/like/{commentId}")
    Call<ApiResponse<Void>> toggleCommentLike(@Header("Authorization") String token, @Path("commentId") int commentId);

    @GET("comment/replies/{commentId}")
    Call<ApiResponse<List<Comment>>> getCommentReplies(@Header("Authorization") String token, @Path("commentId") int commentId);

    @DELETE("comment/{commentId}")
    Call<ApiResponse<Void>> deleteComment(@Header("Authorization") String token, @Path("commentId") int commentId);

    @GET("quiz/bingjian")
    Call<ApiResponse<List<QuizQuestion>>> getBingjianQuiz();

    @GET("quiz/lingxin")
    Call<ApiResponse<List<QuizQuestion>>> getLingxinQuiz();

    @GET("quiz/nuanyang")
    Call<ApiResponse<List<QuizQuestion>>> getNuanyangQuiz();

    // [已修正] 根据实际日志，热搜词接口需要认证
    @GET("topic/hot/search")
    Call<ApiResponse<List<String>>> getHotSearchTerms(@Header("Authorization") String token);

    // [已修正] 获取搜索历史
    @GET("topic/search/history")
    Call<ApiResponse<List<String>>> getSearchHistory(@Header("Authorization") String token);

    // [已修正] 清空搜索历史
    @DELETE("topic/delete/search")
    Call<ApiResponse<Void>> clearSearchHistory(@Header("Authorization") String token);

    // [已修正] 搜索话题
    @GET("topic/search")
    Call<ApiResponse<List<NewsArticle>>> searchTopics(@Header("Authorization") String token, @Query("keyword") String keyword);
}