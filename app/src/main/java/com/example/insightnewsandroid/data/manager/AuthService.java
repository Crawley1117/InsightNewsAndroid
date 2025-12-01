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
     * 根据 Apifox: POST
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com)",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @POST("common/code") // 修正：方法为 POST
    Call<BaseResponse<Void>> sendVerificationCode(@Query("email") String email); // 参数使用 @Query

    /**
     * 邮箱验证码登录
     * 根据 Apifox: (无直接信息，但通常为 POST)
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com)",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @POST("user/login") // 保持 POST
    Call<BaseResponse<String>> loginWithCode(@Body AuthService.LoginWithCodeRequest request);

    /**
     * 邮箱密码登录
     * 根据 Apifox: (无直接信息，但通常为 POST)
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com)",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @POST("user/login") // 保持 POST
    Call<BaseResponse<String>> loginWithPassword(@Body AuthService.LoginWithPasswordRequest request);

    /**
     * 邮箱验证码注册
     * 根据 Apifox: POST, Body: {email, code}
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com)",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @POST("user/register") // 保持 POST
    Call<BaseResponse<String>> register(@Body AuthService.RegisterRequest request); // 注意：参数为 RegisterRequest

    // --- 新增：新闻检测相关接口 (需要特殊 header) ---

    /**
     * 上传文本进行检测
     * 根据 Apifox: POST
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com)",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @POST("detection/upload/text") // 保持 POST
    Call<BaseResponse<UploadTextResponse>> uploadTextDetection(
            @Header("Authorization") String token, // 传递 Authorization Header
            @Body AuthService.UploadTextRequest request // 传递请求体
    );

    /**
     * 上传图片进行检测
     * 根据 Apifox: POST
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com)",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @Multipart
    @POST("detection/upload/file") // 保持 POST
    Call<BaseResponse<UploadTextResponse>> uploadImageDetection( // 假设返回格式与文本相同
                                                                 @Header("Authorization") String token, // 传递 Authorization Header
                                                                 @Part MultipartBody.Part file // 传递文件
                                                                 // @Query("filePath") String filePath // 如果后端需要 filePath 查询参数，请取消此行注释
    );

    /**
     * 上传文本和图片做一致性检测
     * 根据 Apifox: POST
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com)",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @POST("detection/upload/multimodal") // 保持 POST
    Call<BaseResponse<UploadTextResponse>> uploadMultimodalDetection( // 假设返回格式与文本/图片上传相同
                                                                      @Header("Authorization") String token, // 传递 Authorization Header
                                                                      @Body AuthService.MultimodalDetectionRequest request // 传递请求体
    );

    /**
     * 查看检测历史
     * 根据 Apifox: GET
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com)",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @GET("detection/history") // 修正：方法为 GET
    Call<BaseResponse<List<DetectionHistoryItem>>> getDetectionHistory(
            @Header("Authorization") String token
    );

    /**
     * 查看分析报告
     * 根据 Apifox: GET
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com)",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @GET("detection/report/{id}") // 修正：方法为 GET
    Call<BaseResponse<AnalysisReport>> getAnalysisReport(
            @Header("Authorization") String token, // 传递 Authorization Header
            @Path("id") String reportId // 传递报告 ID
    );

    /**
     * 下载分析报告
     * 根据 Apifox: GET
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com)",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @Streaming // 添加 Streaming 注解以处理大文件下载
    @GET("detection/report/download/{id}") // 修正：方法为 GET
    Call<ResponseBody> downloadAnalysisReport(
            @Header("Authorization") String token, // 传递 Authorization Header (如果需要)
            @Path("id") String reportId, // 传递报告 ID
            @Query("format") String format // 传递下载格式，例如 "pdf", "docx", "txt"
    );

    /**
     * 收藏新闻检测结果
     * 根据 Apifox: POST
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com)",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @POST("detection/favorite") // 假设路径是 /detection/favorite，你需要根据实际后端路径调整
    Call<BaseResponse<Void>> collectDetectionResult(
            @Header("Authorization") String token, // 传递 Authorization Header
            @Body AuthService.CollectRequest request // 假设有一个 CollectRequest 模型
    );

    /**
     * 点踩新闻检测结果
     * 根据 Apifox: POST
     */
    @Headers({
            "User-Agent: Apifox/1.0.0 (https://apifox.com)",
            "Accept: */*",
            "Connection: keep-alive"
    })
    @POST("detection/dislike") // 假设路径是 /detection/dislike，你需要根据实际后端路径调整
    Call<BaseResponse<Void>> dislikeDetectionResult(
            @Header("Authorization") String token, // 传递 Authorization Header
            @Body AuthService.DislikeRequest request // 假设有一个 DislikeRequest 模型
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

    // 注册请求体模型 (修改：移除 password 字段)
    class RegisterRequest {
        private String email;
        private String code;
        // private String password; // 移除 password 字段

        public RegisterRequest(String email, String code) { // 修改：构造函数参数
            this.email = email;
            this.code = code;
            // this.password = password; // 移除 password 赋值
        }

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        // public String getPassword() { return password; } // 移除 getter
        // public void setPassword(String password) { this.password = password; } // 移除 setter
    }
    // --- 注册请求体模型 (修改) ---

    // --- 新增：收藏和点踩请求体模型 ---
    class CollectRequest {
        private String id; // 检测结果ID

        public CollectRequest(String id) {
            this.id = id;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }

    class DislikeRequest {
        private String id; // 检测结果ID

        public DislikeRequest(String id) {
            this.id = id;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }
    // --- 新增结束 ---

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