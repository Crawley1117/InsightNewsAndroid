package com.example.insightnewsandroid.data.remote;

import com.example.insightnewsandroid.data.model.ApiResponse;
import com.example.insightnewsandroid.data.model.Comment;
import com.example.insightnewsandroid.data.model.LoginResponse;
import com.example.insightnewsandroid.data.model.NewsArticle;
import com.example.insightnewsandroid.data.model.User;

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

public interface ApiService {

    // User Authentication
    @POST("user/login")
    Call<ApiResponse<LoginResponse>> login(@Body User user);

    @POST("user/register")
    Call<ApiResponse<Void>> register(@Body User user);

    // User Profile
    @GET("user/info")
    Call<ApiResponse<User>> getProfile(@Header("Authorization") String token);

    @PUT("user/update")
    Call<ApiResponse<Void>> updateUser(@Header("Authorization") String token, @Body User user);

    @GET("user/favorite/topics")
    Call<ApiResponse<List<String>>> getFavoriteTopics(@Header("Authorization") String token);

    // News & Topics Articles
    @GET("topic")
    Call<ApiResponse<List<NewsArticle>>> getHotTopics(@Header("Authorization") String token, @Query("category") String category);

    @GET("topic/{topicId}")
    Call<ApiResponse<NewsArticle>> getTopicDetails(@Header("Authorization") String token, @Path("topicId") int topicId);

    // [已新增] 收藏/取消收藏话题
    @POST("topic/favorite/{topicId}")
    Call<ApiResponse<Void>> toggleTopicFavorite(@Header("Authorization") String token, @Path("topicId") int topicId);

    @GET("topic/hot/search")
    Call<ApiResponse<List<String>>> getHotSearchTerms(@Header("Authorization") String token);

    @GET("topic/search")
    Call<ApiResponse<List<NewsArticle>>> searchTopics(@Header("Authorization") String token, @Query("keyword") String keyword);

    // Search History
    @GET("topic/search/history")
    Call<ApiResponse<List<String>>> getSearchHistory(@Header("Authorization") String token);

    @DELETE("topic/delete/search")
    Call<ApiResponse<Void>> clearSearchHistory(@Header("Authorization") String token);

    // Comments
    @GET("topic/comment/page/{topicId}")
    Call<ApiResponse<List<Comment>>> getComments(@Header("Authorization") String token, 
                                                 @Path("topicId") String topicId, 
                                                 @Query("page") int page, 
                                                 @Query("pagesize") int pagesize);

    @GET("topic/comment/replies/{commentId}")
    Call<ApiResponse<List<Comment>>> getCommentReplies(@Header("Authorization") String token, @Path("commentId") int commentId);

    @POST("topic/comment/add/{topicId}")
    Call<ApiResponse<Void>> postComment(@Header("Authorization") String token, 
                                       @Path("topicId") String topicId, 
                                       @Body Comment comment);

    @POST("topic/comment/toggle-like/{commentId}")
    Call<ApiResponse<Void>> toggleCommentLike(@Header("Authorization") String token, @Path("commentId") int commentId);
    
    @DELETE("topic/delete/{commentId}")
    Call<ApiResponse<Void>> deleteComment(@Header("Authorization") String token, @Path("commentId") int commentId);

    // Common
    @POST("common/code")
    Call<ApiResponse<Object>> sendVerificationCode(@Query("phone") String phone, @Header("Authorization") String token);

    @Multipart
    @POST("common/upload")
    Call<ApiResponse<String>> uploadFile(@Header("Authorization") String token, @Part MultipartBody.Part file);
}
