package com.example.insightnewsandroid.data.manager;

import com.example.insightnewsandroid.data.model.AnalysisReport;
import com.example.insightnewsandroid.data.model.BaseResponse;
import com.example.insightnewsandroid.data.model.DetectionHistoryItem;
import com.example.insightnewsandroid.data.model.DetectionResult; // 导入新模型
import com.example.insightnewsandroid.data.model.UploadTextResponse;

import java.util.List;
import okhttp3.ResponseBody;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface AuthService {

    /**
     * 发送邮箱验证码
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com  )",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @POST("common/code")
    Call<BaseResponse<Void>> sendVerificationCode(@Query("email") String email);

    /**
     * 邮箱验证码登录
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com  )",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @POST("user/login")
    Call<BaseResponse<String>> loginWithCode(@Body AuthService.LoginWithCodeRequest request);

    /**
     * 邮箱密码登录
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com  )",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @POST("user/login")
    Call<BaseResponse<String>> loginWithPassword(@Body AuthService.LoginWithPasswordRequest request);

    /**
     * 邮箱验证码注册
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com  )",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @POST("user/register")
    Call<BaseResponse<String>> register(@Body AuthService.RegisterRequest request);

    // --- 新增：新闻检测相关接口 ---

    /**
     * 上传文本进行检测
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com  )",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @POST("detection/upload/text")
    Call<BaseResponse<DetectionResult>> uploadTextDetection( // 修改返回类型
                                                             @Header("Authorization") String token,
                                                             @Body AuthService.UploadTextRequest request
    );

    /**
     * 上传图片进行检测
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com  )",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @Multipart
    @POST("detection/upload/file")
    Call<BaseResponse<DetectionResult>> uploadImageDetection( // 修改返回类型
                                                              @Header("Authorization") String token,
                                                              @Part MultipartBody.Part file
    );

    /**
     * 上传文本和图片做一致性检测
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com  )",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @POST("detection/upload/multimodal")
    Call<BaseResponse<DetectionResult>> uploadMultimodalDetection( // 修改返回类型
                                                                   @Header("Authorization") String token,
                                                                   @Body AuthService.MultimodalDetectionRequest request
    );

    /**
     * 查看检测历史
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com  )",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @GET("detection/history")
    Call<BaseResponse<List<DetectionHistoryItem>>> getDetectionHistory(
            @Header("Authorization") String token
    );

    /**
     * 查看分析报告
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com  )",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @GET("detection/report/{id}")
    Call<BaseResponse<AnalysisReport>> getAnalysisReport(
            @Header("Authorization") String token,
            @Path("id") String reportId
    );

    /**
     * 下载分析报告
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com  )",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @Streaming
    @GET("detection/report/download/{id}")
    Call<ResponseBody> downloadAnalysisReport(
            @Header("Authorization") String token,
            @Path("id") String reportId,
            @Query("format") String format
    );

    /**
     * 收藏新闻检测结果
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com  )",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @POST("detection/favorite")
    Call<BaseResponse<Void>> collectDetectionResult(
            @Header("Authorization") String token,
            @Body AuthService.CollectRequest request
    );

    /**
     * 点踩新闻检测结果
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com  )",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @POST("detection/dislike")
    Call<BaseResponse<Void>> dislikeDetectionResult(
            @Header("Authorization") String token,
            @Body AuthService.DislikeRequest request
    );

    // --- 检测请求体模型 (文本) ---
    class UploadTextRequest {
        private String content;

        public UploadTextRequest(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    // --- 检测请求体模型 (多模态) ---
    class MultimodalDetectionRequest {
        private String content;
        private String imageUrl;

        public MultimodalDetectionRequest(String content, String imageUrl) {
            this.content = content;
            this.imageUrl = imageUrl;
        }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    // --- 认证相关请求体模型 ---

    class LoginWithCodeRequest {
        private String email;
        private String code;

        public LoginWithCodeRequest(String email, String code) {
            this.email = email;
            this.code = code;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

    class LoginWithPasswordRequest {
        private String email;
        private String password;

        public LoginWithPasswordRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    class RegisterRequest {
        private String email;
        private String code;

        public RegisterRequest(String email, String code) {
            this.email = email;
            this.code = code;
        }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

    // --- 新增：收藏和点踩请求体模型 ---
    class CollectRequest {
        private String id;

        public CollectRequest(String id) {
            this.id = id;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }

    class DislikeRequest {
        private String id;

        public DislikeRequest(String id) {
            this.id = id;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }
}