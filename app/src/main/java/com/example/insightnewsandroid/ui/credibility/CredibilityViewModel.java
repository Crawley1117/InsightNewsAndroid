package com.example.insightnewsandroid.ui.credibility;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.insightnewsandroid.auth.AuthRepository;
import com.example.insightnewsandroid.data.manager.ApiManager;
import com.example.insightnewsandroid.data.manager.AuthService;
import com.example.insightnewsandroid.data.model.BaseResponse;
import com.example.insightnewsandroid.data.model.DetectionResult;
import com.example.insightnewsandroid.db.AppDatabase;
import com.example.insightnewsandroid.db.DetectionRecordEntity;
import com.example.insightnewsandroid.db.SuspiciousSpan;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CredibilityViewModel extends AndroidViewModel {

    private static final String TAG = "CredibilityViewModel";
    private final MutableLiveData<List<ChatMessage>> chatMessages = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private AppDatabase database;
    private ExecutorService executor;
    private AuthRepository authRepo;

    public CredibilityViewModel(Application application) {
        super(application);
        this.database = AppDatabase.getDatabase(application);
        this.executor = Executors.newSingleThreadExecutor();
        this.authRepo = new AuthRepository(application);
    }

    // 保留无参构造函数以兼容旧代码（如果需要）
    public CredibilityViewModel() {
        super(null);
        this.database = null;
        this.executor = null;
        this.authRepo = null;
    }

    public void addUserMessage(String text, String imageUrl, String fileName) {
        List<ChatMessage> currentValue = chatMessages.getValue();
        List<ChatMessage> current = new ArrayList<>(currentValue != null ? currentValue : new ArrayList<>());

        ChatMessage userMsg = new ChatMessage(ChatMessage.Type.USER, text);
        if (imageUrl != null) userMsg.setImageUrl(imageUrl);
        if (fileName != null) userMsg.setFileName(fileName);
        current.add(userMsg);

        // 添加一个临时的 "正在分析..." 消息
        ChatMessage aiMsg = new ChatMessage(ChatMessage.Type.AI, "正在分析...");
        current.add(aiMsg);
        chatMessages.setValue(current); // 更新UI显示加载状态

        // 调用后端接口
        if (imageUrl != null && text != null) {
            callUploadMultimodalDetection(text, imageUrl, current);
        } else if (imageUrl != null) {
            callUploadImageDetection(imageUrl, current);
        } else if (text != null) {
            callUploadTextDetection(text, current);
        } else {
            List<ChatMessage> updatedMessages = new ArrayList<>(current);
            if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                    && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                updatedMessages.remove(updatedMessages.size() - 1);
            }
            ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, "无法分析，缺少文本或图片内容。");
            updatedMessages.add(errorMsg);
            chatMessages.setValue(updatedMessages);
        }
    }

    // --- 修改：调用后端接口的方法 (文本) ---
    private void callUploadTextDetection(String text, List<ChatMessage> currentMessages) {
        String token = authRepo.getAuthToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token is null or empty, cannot upload text.");
            errorMessage.setValue("用户未登录或Token无效");
            updateMessagesForError(currentMessages, "无法获取用户信息，请重新登录。");
            return;
        }

        AuthService.UploadTextRequest request = new AuthService.UploadTextRequest(text);

        Call<BaseResponse<DetectionResult>> call = ApiManager.getAuthService().uploadTextDetection("Bearer " + token, request);
        call.enqueue(new Callback<BaseResponse<DetectionResult>>() {
            @Override
            public void onResponse(Call<BaseResponse<DetectionResult>> call, Response<BaseResponse<DetectionResult>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<DetectionResult> baseResponse = response.body();
                    if (baseResponse != null && baseResponse.isSuccess()) {
                        DetectionResult detectionResult = baseResponse.getData(); // 获取 DetectionResult

                        if (detectionResult != null) { // 检查 DetectionResult 是否为 null
                            Log.d(TAG, "Text uploaded successfully. Detection Result: " + detectionResult.getTitle());

                            List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                            if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                                    && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                                updatedMessages.remove(updatedMessages.size() - 1);
                            }

                            CredibilityResult credibilityResult = convertDetectionToCredibilityResult(detectionResult);
                            ChatMessage aiMsg = new ChatMessage(ChatMessage.Type.AI, "文本检测完成。");
                            aiMsg.setResult(credibilityResult);
                            updatedMessages.add(aiMsg);

                            chatMessages.setValue(updatedMessages);

                            saveDetectionRecord(text, credibilityResult);
                        } else {
                            // --- 关键修改：处理 data 为 null 的情况 ---
                            Log.w(TAG, "Upload succeeded (200) but DetectionResult data is null. Server msg: " + (baseResponse.getMsg() != null ? baseResponse.getMsg() : "null"));
                            String userMessage;
                            if (baseResponse.getMsg() != null && !baseResponse.getMsg().isEmpty()) {
                                userMessage = "服务器提示: " + baseResponse.getMsg();
                            } else {
                                userMessage = "服务器暂时无法返回检测结果，请稍后重试或查看历史记录。"; // 友好提示
                            }
                            updateMessagesForError(currentMessages, userMessage);
                            // --- 修改结束 ---
                        }
                    } else {
                        // 服务器返回了非 200 的 code 或非空的错误 msg
                        String msg = (baseResponse != null) ? baseResponse.getMsg() : "未知错误";
                        Log.e(TAG, "Upload failed. Server message: " + msg);

                        // 从消息列表中移除 "正在分析..." 消息
                        List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                        if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                                && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                            updatedMessages.remove(updatedMessages.size() - 1);
                        }
                        // --- 修改：处理 msg 为 null 的情况 ---
                        String userFriendlyErrorMsg = "检测失败: " + (msg != null ? msg : "服务器返回了错误，但未提供具体原因。");
                        ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, userFriendlyErrorMsg);
                        // --- 修改结束 ---
                        updatedMessages.add(errorMsg);
                        chatMessages.setValue(updatedMessages);

                        // 通过 errorMessage LiveData 通知 UI
                        errorMessage.setValue(userFriendlyErrorMsg);
                    }
                } else {
                    Log.e(TAG, "Upload failed. HTTP code: " + response.code());

                    // 从消息列表中移除 "正在分析..." 消息
                    List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                    if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                            && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                        updatedMessages.remove(updatedMessages.size() - 1);
                    }
                    // --- 修改：处理 HTTP 错误码 ---
                    ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, "网络请求失败: " + response.code());
                    // --- 修改结束 ---
                    updatedMessages.add(errorMsg);
                    chatMessages.setValue(updatedMessages);

                    // 通过 errorMessage LiveData 通知 UI
                    errorMessage.setValue("网络请求失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<DetectionResult>> call, Throwable t) {
                Log.e(TAG, "Upload failed due to network error.", t);

                // 从消息列表中移除 "正在分析..." 消息
                List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                        && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                    updatedMessages.remove(updatedMessages.size() - 1);
                }
                // --- 修改：处理网络异常 ---
                String errorMessageText = "网络错误: " + (t.getMessage() != null ? t.getMessage() : "连接失败");
                ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, errorMessageText);
                // --- 修改结束 ---
                updatedMessages.add(errorMsg);
                chatMessages.setValue(updatedMessages);

                // 通过 errorMessage LiveData 通知 UI
                errorMessage.setValue(errorMessageText);
            }
        });
    }
    // --- 修改结束 ---

    // --- 修改：调用上传图片接口的方法 ---
    private void callUploadImageDetection(String imagePath, List<ChatMessage> currentMessages) {
        String token = authRepo.getAuthToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token is null or empty, cannot upload image.");
            errorMessage.setValue("用户未登录或Token无效");
            updateMessagesForError(currentMessages, "无法获取用户信息，请重新登录。");
            return;
        }

        File file = new File(imagePath);
        if (!file.exists() || !file.isFile()) {
            Log.e(TAG, "File does not exist or is not a file: " + imagePath);
            errorMessage.setValue("文件不存在或无效");
            updateMessagesForError(currentMessages, "文件不存在或无效。");
            return;
        }

        RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
        MultipartBody.Part bodyPart = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        Call<BaseResponse<DetectionResult>> call = ApiManager.getAuthService().uploadImageDetection("Bearer " + token, bodyPart);
        call.enqueue(new Callback<BaseResponse<DetectionResult>>() {
            @Override
            public void onResponse(Call<BaseResponse<DetectionResult>> call, Response<BaseResponse<DetectionResult>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<DetectionResult> baseResponse = response.body();
                    if (baseResponse != null && baseResponse.isSuccess()) {
                        DetectionResult detectionResult = baseResponse.getData(); // 获取 DetectionResult

                        if (detectionResult != null) { // 检查 DetectionResult 是否为 null
                            Log.d(TAG, "Image uploaded successfully. Detection Result: " + detectionResult.getTitle());

                            List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                            if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                                    && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                                updatedMessages.remove(updatedMessages.size() - 1);
                            }

                            CredibilityResult credibilityResult = convertDetectionToCredibilityResult(detectionResult);
                            ChatMessage aiMsg = new ChatMessage(ChatMessage.Type.AI, "图片检测完成。");
                            aiMsg.setResult(credibilityResult);
                            updatedMessages.add(aiMsg);

                            chatMessages.setValue(updatedMessages);

                            saveDetectionRecord("图片内容: " + imagePath, credibilityResult);
                        } else {
                            // --- 关键修改：处理 data 为 null 的情况 ---
                            Log.w(TAG, "Image upload succeeded (200) but DetectionResult data is null. Server msg: " + (baseResponse.getMsg() != null ? baseResponse.getMsg() : "null"));
                            String userMessage;
                            if (baseResponse.getMsg() != null && !baseResponse.getMsg().isEmpty()) {
                                userMessage = "服务器提示: " + baseResponse.getMsg();
                            } else {
                                userMessage = "服务器暂时无法返回检测结果，请稍后重试或查看历史记录。"; // 友好提示
                            }
                            updateMessagesForError(currentMessages, userMessage);
                            // --- 修改结束 ---
                        }
                    } else {
                        // 服务器返回了非 200 的 code 或非空的错误 msg
                        String msg = (baseResponse != null) ? baseResponse.getMsg() : "未知错误";
                        Log.e(TAG, "Upload failed. Server message: " + msg);

                        List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                        if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                                && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                            updatedMessages.remove(updatedMessages.size() - 1);
                        }
                        // --- 修改：处理 msg 为 null 的情况 ---
                        String userFriendlyErrorMsg = "检测失败: " + (msg != null ? msg : "服务器返回了错误，但未提供具体原因。");
                        ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, userFriendlyErrorMsg);
                        // --- 修改结束 ---
                        updatedMessages.add(errorMsg);
                        chatMessages.setValue(updatedMessages);

                        // 通过 errorMessage LiveData 通知 UI
                        errorMessage.setValue(userFriendlyErrorMsg);
                    }
                } else {
                    Log.e(TAG, "Upload failed. HTTP code: " + response.code());

                    List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                    if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                            && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                        updatedMessages.remove(updatedMessages.size() - 1);
                    }
                    // --- 修改：处理 HTTP 错误码 ---
                    ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, "网络请求失败: " + response.code());
                    // --- 修改结束 ---
                    updatedMessages.add(errorMsg);
                    chatMessages.setValue(updatedMessages);

                    // 通过 errorMessage LiveData 通知 UI
                    errorMessage.setValue("网络请求失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<DetectionResult>> call, Throwable t) {
                Log.e(TAG, "Upload failed due to network error.", t);

                List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                        && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                    updatedMessages.remove(updatedMessages.size() - 1);
                }
                // --- 修改：处理网络异常 ---
                String errorMessageText = "网络错误: " + (t.getMessage() != null ? t.getMessage() : "连接失败");
                ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, errorMessageText);
                // --- 修改结束 ---
                updatedMessages.add(errorMsg);
                chatMessages.setValue(updatedMessages);

                // 通过 errorMessage LiveData 通知 UI
                errorMessage.setValue(errorMessageText);
            }
        });
    }
    // --- 修改结束 ---

    // --- 新增：公共方法，供 Fragment 调用 (图片) ---
    public void addUserMessageWithImage(String imagePath) {
        List<ChatMessage> currentValue = chatMessages.getValue();
        List<ChatMessage> current = new ArrayList<>(currentValue != null ? currentValue : new ArrayList<>());

        ChatMessage userMsg = new ChatMessage(ChatMessage.Type.USER, "发送了一张图片");
        userMsg.setImageUrl(imagePath);
        current.add(userMsg);

        ChatMessage aiMsg = new ChatMessage(ChatMessage.Type.AI, "正在分析...");
        current.add(aiMsg);
        chatMessages.setValue(current);

        callUploadImageDetection(imagePath, current);
    }
    // --- 新增结束 ---

    // --- 修改：调用多模态检测接口的方法 ---
    public void addUserMessageWithTextAndImage(String text, String imageUrl) {
        List<ChatMessage> currentValue = chatMessages.getValue();
        List<ChatMessage> current = new ArrayList<>(currentValue != null ? currentValue : new ArrayList<>());

        ChatMessage userMsg = new ChatMessage(ChatMessage.Type.USER, text);
        userMsg.setImageUrl(imageUrl);
        current.add(userMsg);

        ChatMessage aiMsg = new ChatMessage(ChatMessage.Type.AI, "正在分析...");
        current.add(aiMsg);
        chatMessages.setValue(current);

        callUploadMultimodalDetection(text, imageUrl, current);
    }

    private void callUploadMultimodalDetection(String text, String imageUrl, List<ChatMessage> currentMessages) {
        String token = authRepo.getAuthToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token is null or empty, cannot upload multimodal data.");
            errorMessage.setValue("用户未登录或Token无效");
            updateMessagesForError(currentMessages, "无法获取用户信息，请重新登录。");
            return;
        }

        AuthService.MultimodalDetectionRequest request = new AuthService.MultimodalDetectionRequest(text, imageUrl);

        Call<BaseResponse<DetectionResult>> call = ApiManager.getAuthService().uploadMultimodalDetection("Bearer " + token, request);
        call.enqueue(new Callback<BaseResponse<DetectionResult>>() {
            @Override
            public void onResponse(Call<BaseResponse<DetectionResult>> call, Response<BaseResponse<DetectionResult>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<DetectionResult> baseResponse = response.body();
                    if (baseResponse != null && baseResponse.isSuccess()) {
                        DetectionResult detectionResult = baseResponse.getData(); // 获取 DetectionResult

                        if (detectionResult != null) { // 检查 DetectionResult 是否为 null
                            Log.d(TAG, "Multimodal data uploaded successfully. Detection Result: " + detectionResult.getTitle());

                            List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                            if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                                    && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                                updatedMessages.remove(updatedMessages.size() - 1);
                            }

                            CredibilityResult credibilityResult = convertDetectionToCredibilityResult(detectionResult);
                            ChatMessage aiMsg = new ChatMessage(ChatMessage.Type.AI, "文本和图片一致性检测完成。");
                            aiMsg.setResult(credibilityResult);
                            updatedMessages.add(aiMsg);

                            chatMessages.setValue(updatedMessages);

                            saveDetectionRecord("文本: " + text + " 图片: " + imageUrl, credibilityResult);
                        } else {
                            // --- 关键修改：处理 data 为 null 的情况 ---
                            Log.w(TAG, "Multimodal upload succeeded (200) but DetectionResult data is null. Server msg: " + (baseResponse.getMsg() != null ? baseResponse.getMsg() : "null"));
                            String userMessage;
                            if (baseResponse.getMsg() != null && !baseResponse.getMsg().isEmpty()) {
                                userMessage = "服务器提示: " + baseResponse.getMsg();
                            } else {
                                userMessage = "服务器暂时无法返回检测结果，请稍后重试或查看历史记录。"; // 友好提示
                            }
                            updateMessagesForError(currentMessages, userMessage);
                            // --- 修改结束 ---
                        }
                    } else {
                        // 服务器返回了非 200 的 code 或非空的错误 msg
                        String msg = (baseResponse != null) ? baseResponse.getMsg() : "未知错误";
                        Log.e(TAG, "Upload failed. Server message: " + msg);

                        List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                        if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                                && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                            updatedMessages.remove(updatedMessages.size() - 1);
                        }
                        // --- 修改：处理 msg 为 null 的情况 ---
                        String userFriendlyErrorMsg = "检测失败: " + (msg != null ? msg : "服务器返回了错误，但未提供具体原因。");
                        ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, userFriendlyErrorMsg);
                        // --- 修改结束 ---
                        updatedMessages.add(errorMsg);
                        chatMessages.setValue(updatedMessages);

                        // 通过 errorMessage LiveData 通知 UI
                        errorMessage.setValue(userFriendlyErrorMsg);
                    }
                } else {
                    Log.e(TAG, "Upload failed. HTTP code: " + response.code());

                    List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                    if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                            && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                        updatedMessages.remove(updatedMessages.size() - 1);
                    }
                    // --- 修改：处理 HTTP 错误码 ---
                    ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, "网络请求失败: " + response.code());
                    // --- 修改结束 ---
                    updatedMessages.add(errorMsg);
                    chatMessages.setValue(updatedMessages);

                    // 通过 errorMessage LiveData 通知 UI
                    errorMessage.setValue("网络请求失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<DetectionResult>> call, Throwable t) {
                Log.e(TAG, "Upload failed due to network error.", t);

                List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                        && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                    updatedMessages.remove(updatedMessages.size() - 1);
                }
                // --- 修改：处理网络异常 ---
                String errorMessageText = "网络错误: " + (t.getMessage() != null ? t.getMessage() : "连接失败");
                ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, errorMessageText);
                // --- 修改结束 ---
                updatedMessages.add(errorMsg);
                chatMessages.setValue(updatedMessages);

                // 通过 errorMessage LiveData 通知 UI
                errorMessage.setValue(errorMessageText);
            }
        });
    }
    // --- 修改结束 ---

    // --- 新增：统一的错误消息更新方法 ---
    private void updateMessagesForError(List<ChatMessage> currentMessages, String errorMessageText) {
        List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
        if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
            updatedMessages.remove(updatedMessages.size() - 1);
        }
        ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, errorMessageText);
        updatedMessages.add(errorMsg);
        chatMessages.setValue(updatedMessages);
    }
    // --- 新增结束 ---

    // --- 移除：simulateAnalysis 方法 ---
    // private CredibilityResult simulateAnalysis(String text, String img, String file) { ... }
    // --- 移除结束 ---

    private void saveDetectionRecord(String fullText, CredibilityResult result) {
        if (database == null || executor == null) {
            return;
        }
        executor.execute(() -> {
            try {
                int score = result.getScore();
                String level = score >= 75 ? "高" : (score >= 50 ? "中" : "低");
                String title = fullText.length() > 50 ? fullText.substring(0, 50) + "..." : fullText;
                String fullReport = result.getReason();
                List<SuspiciousSpan> suspiciousSpans = new ArrayList<>(); // 示例：空列表

                DetectionRecordEntity record = new DetectionRecordEntity(
                        title,
                        System.currentTimeMillis(),
                        fullText,
                        fullReport,
                        level,
                        suspiciousSpans
                );
                database.detectionDao().insert(record);
                Log.d(TAG, "Record saved to database: " + title);
            } catch (Exception e) {
                Log.e(TAG, "Error saving record to database", e);
            }
        });
    }

    // --- 修改：将 DetectionResult 转换为 CredibilityResult ---
    private CredibilityResult convertDetectionToCredibilityResult(DetectionResult detectionResult) {
        // 使用服务器返回的可信度分数和证据链等信息创建 CredibilityResult
        int score = detectionResult.getCredibility() != null ? detectionResult.getCredibility() : 0;
        String reason = buildReasonFromEvidence(detectionResult.getEvidenceChain());
        return new CredibilityResult(score, reason);
    }

    private String buildReasonFromEvidence(List<DetectionResult.EvidenceChainItem> evidenceChain) {
        if (evidenceChain == null || evidenceChain.isEmpty()) {
            return "未提供具体分析理由。";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < evidenceChain.size(); i++) {
            DetectionResult.EvidenceChainItem item = evidenceChain.get(i);
            sb.append("证据 ").append(i + 1).append(": ");
            if (item.getQuote() != null) sb.append(item.getQuote()).append(" ");
            if (item.getReason() != null) sb.append(item.getReason());
            sb.append("\n");
        }
        return sb.toString().trim();
    }
    // --- 修改结束 ---

    public LiveData<List<ChatMessage>> getChatMessages() {
        return chatMessages;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}