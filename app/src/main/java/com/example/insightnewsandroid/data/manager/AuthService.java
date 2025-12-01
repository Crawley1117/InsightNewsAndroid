package com.example.insightnewsandroid.data.manager;

import com.example.insightnewsandroid.data.model.AnalysisReport; // 导入模型
import com.example.insightnewsandroid.data.model.BaseResponse;
import com.example.insightnewsandroid.data.model.DetectionHistoryItem; // 导入模型
import com.example.insightnewsandroid.data.model.UploadTextResponse; // 导入模型

import java.util.List; // 导入 List
import okhttp3.ResponseBody; // 导入 ResponseBody 用于下载文件
import okhttp3.MultipartBody; // 导入 MultipartBody
import okhttp3.RequestBody;   // 导入 RequestBody
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET; // 添加 GET 注解
import retrofit2.http.Header; // 添加 Header 注解
import retrofit2.http.Headers; // 添加 Headers 注解
import retrofit2.http.Multipart; // 添加 Multipart 注解
import retrofit2.http.POST;
import retrofit2.http.Part;    // 添加 Part 注解
import retrofit2.http.Path;    // 添加 Path 注解 (用于 URL 路径参数)
import retrofit2.http.Query;   // 添加 Query 注解
import retrofit2.http.Streaming; // 添加 Streaming 注解 (用于大文件下载)

public interface AuthService {

    /**
     * 发送邮箱验证码
     */
    @GET("common/code")
    Call<BaseResponse<Void>> sendVerificationCode(@Query("email") String email);

    /**
     * 邮箱验证码登录
     */
    @POST("user/login")
    Call<BaseResponse<String>> loginWithCode(@Body AuthService.LoginWithCodeRequest request);

    /**
     * 邮箱密码登录
     */
    @POST("user/login")
    Call<BaseResponse<String>> loginWithPassword(@Body AuthService.LoginWithPasswordRequest request);

    /**
     * 邮箱验证码注册
     */
    @POST("user/register")
    Call<BaseResponse<String>> register(@Body AuthService.RegisterRequest request);

    // --- 新增：新闻检测相关接口 ---

    /**
     * 上传文本进行检测
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com)",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @POST("detection/upload/text")
    Call<BaseResponse<UploadTextResponse>> uploadTextDetection(
            @Header("Authorization") String token, // 传递 Authorization Header
            @Body AuthService.UploadTextRequest request // 传递请求体
    );

    /**
     * 上传图片进行检测
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com)",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @Multipart
    @POST("detection/upload/file") // 假设后端不需要 filePath 查询参数，直接接收文件
    Call<BaseResponse<UploadTextResponse>> uploadImageDetection( // 假设返回格式与文本相同
                                                                 @Header("Authorization") String token, // 传递 Authorization Header
                                                                 @Part MultipartBody.Part file // 传递文件
                                                                 // @Query("filePath") String filePath // 如果后端需要 filePath 查询参数，请取消此行注释
    );

    /**
     * 上传文本和图片进行一致性检测
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com)",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @POST("detection/upload/multimodal")
    Call<BaseResponse<UploadTextResponse>> uploadMultimodalDetection( // 假设返回格式与文本/图片上传相同
                                                                      @Header("Authorization") String token, // 传递 Authorization Header
                                                                      @Body AuthService.MultimodalDetectionRequest request // 传递请求体
    );

    /**
     * 查看检测历史
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com)",
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
            "User-Agent: Apifox/1.0.0 (https://apifox.com)",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @GET("detection/report/{id}") // 使用 Path 注解获取 id
    Call<BaseResponse<AnalysisReport>> getAnalysisReport(
            @Header("Authorization") String token, // 传递 Authorization Header
            @Path("id") String reportId // 传递报告 ID
    );
    // --- 新增结束 ---

    /**
     * 下载分析报告 (后端未准备好，暂时不调用)
     */
    @Streaming // 添加 Streaming 注解以处理大文件下载
    @GET("detection/report/download/{id}") // 使用 Path 注解获取 id
    Call<ResponseBody> downloadAnalysisReport(
            @Header("Authorization") String token, // 传递 Authorization Header (如果需要)
            @Path("id") String reportId, // 传递报告 ID
            @Query("format") String format // 传递下载格式，例如 "pdf", "docx", "txt"
    );
    // --- 新增结束 ---

    // --- 新增：获取话题列表接口 ---
    /**
     * 获取话题列表
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com)",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @GET("topic") // 注意：URL 中包含 'category' 查询参数
    Call<BaseResponse<List<TopicItem>>> getTopics( // 假设 TopicItem 是话题列表项的模型
                                                   @Header("Authorization") String token, // 传递 Authorization Header
                                                   @Query("category") String category // 传递 category 查询参数
    );
    // --- 新增结束 ---

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
        private String content; // 文本内容
        private String imageUrl; // 图片URL

        public MultimodalDetectionRequest(String content, String imageUrl) {
            this.content = content;
            this.imageUrl = imageUrl;
        }

        // Getters and Setters
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }
    // --- 检测请求体模型 (多模态) ---

    // --- 认证相关请求体模型 (保持不变) ---

    // 验证码登录请求体模型
    class LoginWithCodeRequest {
        private String email;
        private String code;

        public LoginWithCodeRequest(String email, String code) {
            this.email = email;
            this.code = code;
        }

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

    // 密码登录请求体模型
    class LoginWithPasswordRequest {
        private String email;
        private String password;

        public LoginWithPasswordRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // 注册请求体模型
    class RegisterRequest {
        private String email;
        private String code;
        private String password;

        public RegisterRequest(String email, String code, String password) {
            this.email = email;
            this.code = code;
            this.password = password;
        }

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // --- 话题列表项模型 (需要根据后端实际返回结构定义) ---
    class TopicItem {
        // 例如:
        // @SerializedName("id")
        // private String id;
        // @SerializedName("title")
        // private String title;
        // @SerializedName("category")
        // private String category;
        // @SerializedName("created_at")
        // private String createdAt;
        // // Getters and Setters...
    }
    // --- 话题列表项模型 ---
}