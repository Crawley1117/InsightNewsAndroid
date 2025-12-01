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
import com.example.insightnewsandroid.data.model.UploadTextResponse;
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

    private final MutableLiveData<List<ChatMessage>> chatMessages = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(); // 新增：用于传递错误信息给UI
    private AppDatabase database;
    private ExecutorService executor;
    private AuthRepository authRepo;

    public CredibilityViewModel(Application application) {
        super(application);
        this.database = AppDatabase.getDatabase(application);
        this.executor = Executors.newSingleThreadExecutor();
        this.authRepo = new AuthRepository(application); // 初始化 AuthRepository
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

        // --- 修改：不再模拟分析，而是调用后端接口 ---
        // CredibilityResult result = simulateAnalysis(text, imageUrl, fileName);
        // ChatMessage aiMsg = new ChatMessage(Chat.Type.AI, "已分析你的内容：");
        // aiMsg.setResult(result);
        // current.add(aiMsg);

        // 添加一个临时的 "正在分析..." 消息
        ChatMessage aiMsg = new ChatMessage(ChatMessage.Type.AI, "正在分析...");
        current.add(aiMsg);
        chatMessages.setValue(current); // 更新UI显示加载状态

        // 调用后端接口
        if (imageUrl != null && text != null) {
            // 如果同时有文本和图片，则调用多模态检测
            callUploadMultimodalDetection(text, imageUrl, current);
        } else if (imageUrl != null) {
            // 如果只有图片，则调用图片检测
            callUploadImageDetection(imageUrl, current);
        } else if (text != null) {
            // 如果只有文本，则调用文本检测
            callUploadTextDetection(text, current);
        } else {
            // 如果都没有，则显示错误或提示
            List<ChatMessage> updatedMessages = new ArrayList<>(current);
            if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                    && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                updatedMessages.remove(updatedMessages.size() - 1);
            }
            ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, "无法分析，缺少文本或图片内容。");
            updatedMessages.add(errorMsg);
            chatMessages.setValue(updatedMessages);
        }
        // --- 修改结束 ---
    }

    // --- 新增：调用后端接口的方法 (文本) ---
    private void callUploadTextDetection(String text, List<ChatMessage> currentMessages) {
        // 1. 获取 Token
        String token = authRepo.getAuthToken(); // 从 AuthRepository 获取 Token，方法名已修正
        if (token == null || token.isEmpty()) {
            Log.e("CredibilityViewModel", "Token is null or empty, cannot upload text.");
            errorMessage.setValue("用户未登录或Token无效");
            // 从消息列表中移除 "正在分析..." 消息
            List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
            if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                    && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                updatedMessages.remove(updatedMessages.size() - 1);
            }
            ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, "无法获取用户信息，请重新登录。");
            updatedMessages.add(errorMsg);
            chatMessages.setValue(updatedMessages);
            return;
        }

        // 2. 构建请求体
        AuthService.UploadTextRequest request = new AuthService.UploadTextRequest(text);

        // 3. 发起网络请求
        Call<BaseResponse<UploadTextResponse>> call = ApiManager.getAuthService().uploadTextDetection("Bearer " + token, request);
        call.enqueue(new Callback<BaseResponse<UploadTextResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<UploadTextResponse>> call, Response<BaseResponse<UploadTextResponse>> response) {
                // 在主线程执行
                if (response.isSuccessful()) {
                    BaseResponse<UploadTextResponse> baseResponse = response.body();
                    if (baseResponse != null && baseResponse.isSuccess()) {
                        // 上传成功，但后端目前返回空对象 {}
                        // TODO: 当后端返回具体检测结果时，处理 UploadTextResponse 对象
                        Log.d("CredibilityViewModel", "Text uploaded successfully. Response: " + baseResponse.getData());

                        // 从消息列表中移除 "正在分析..." 消息
                        List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                        if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                                && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                            updatedMessages.remove(updatedMessages.size() - 1);
                        }

                        // TODO: 这里需要根据后端实际返回的检测结果创建 CredibilityResult
                        // 目前由于后端返回为空，我们只能模拟一个结果或显示一个提示
                        // 暂时使用一个通用的成功消息
                        ChatMessage aiMsg = new ChatMessage(ChatMessage.Type.AI, "文本已提交检测，请稍后查看结果。");
                        // 如果后端返回了结果，可以这样创建:
                        // CredibilityResult result = new CredibilityResult(score, reason);
                        // aiMsg.setResult(result);
                        updatedMessages.add(aiMsg);

                        chatMessages.setValue(updatedMessages);

                        // --- 保存到历史记录 ---
                        // 由于后端未返回具体结果，这里暂时使用模拟结果或空结果保存
                        // 模拟一个结果用于保存
                        CredibilityResult resultToSave = new CredibilityResult(50, "检测已提交，结果待定。"); // 示例：默认50分，原因待定
                        saveDetectionRecord(text, resultToSave); // 保存到本地数据库
                        // --- 保存结束 ---

                    } else {
                        String msg = (baseResponse != null) ? baseResponse.getMsg() : "未知错误";
                        Log.e("CredibilityViewModel", "Upload failed. Server message: " + msg);
                        errorMessage.setValue("检测失败: " + msg);

                        // 从消息列表中移除 "正在分析..." 消息
                        List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                        if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                                && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                            updatedMessages.remove(updatedMessages.size() - 1);
                        }
                        ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, "检测失败: " + msg);
                        updatedMessages.add(errorMsg);
                        chatMessages.setValue(updatedMessages);
                    }
                } else {
                    Log.e("CredibilityViewModel", "Upload failed. HTTP code: " + response.code());
                    errorMessage.setValue("网络请求失败: " + response.code());

                    // 从消息列表中移除 "正在分析..." 消息
                    List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                    if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                            && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                        updatedMessages.remove(updatedMessages.size() - 1);
                    }
                    ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, "网络请求失败: " + response.code());
                    updatedMessages.add(errorMsg);
                    chatMessages.setValue(updatedMessages);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<UploadTextResponse>> call, Throwable t) {
                Log.e("CredibilityViewModel", "Upload failed due to network error.", t);
                errorMessage.setValue("网络错误: " + t.getMessage());

                // 从消息列表中移除 "正在分析..." 消息
                List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                        && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                    updatedMessages.remove(updatedMessages.size() - 1);
                }
                ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, "网络错误: " + t.getMessage());
                updatedMessages.add(errorMsg);
                chatMessages.setValue(updatedMessages);
            }
        });
    }
    // --- 新增结束 ---

    // --- 新增：调用上传图片接口的方法 ---
    private void callUploadImageDetection(String imagePath, List<ChatMessage> currentMessages) {
        // 1. 获取 Token
        String token = authRepo.getAuthToken();
        if (token == null || token.isEmpty()) {
            Log.e("CredibilityViewModel", "Token is null or empty, cannot upload image.");
            errorMessage.setValue("用户未登录或Token无效");
            // 从消息列表中移除 "正在分析..." 消息
            List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
            if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                    && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                updatedMessages.remove(updatedMessages.size() - 1);
            }
            ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, "无法获取用户信息，请重新登录。");
            updatedMessages.add(errorMsg);
            chatMessages.setValue(updatedMessages);
            return;
        }

        // 2. 将图片路径转换为 MultipartBody.Part
        File file = new File(imagePath);
        if (!file.exists() || !file.isFile()) {
            Log.e("CredibilityViewModel", "File does not exist or is not a file: " + imagePath);
            errorMessage.setValue("文件不存在或无效");
            // 从消息列表中移除 "正在分析..." 消息
            List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
            if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                    && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                updatedMessages.remove(updatedMessages.size() - 1);
            }
            ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, "文件不存在或无效。");
            updatedMessages.add(errorMsg);
            chatMessages.setValue(updatedMessages);
            return;
        }

        RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*")); // 根据图片类型调整
        MultipartBody.Part bodyPart = MultipartBody.Part.createFormData("file", file.getName(), requestFile); // "file" 是后端期望的字段名

        // 3. 发起网络请求
        Call<BaseResponse<UploadTextResponse>> call = ApiManager.getAuthService().uploadImageDetection("Bearer " + token, bodyPart);
        call.enqueue(new Callback<BaseResponse<UploadTextResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<UploadTextResponse>> call, Response<BaseResponse<UploadTextResponse>> response) {
                // 在主线程执行
                if (response.isSuccessful()) {
                    BaseResponse<UploadTextResponse> baseResponse = response.body();
                    if (baseResponse != null && baseResponse.isSuccess()) {
                        // 上传成功，但后端目前返回空对象 {}
                        Log.d("CredibilityViewModel", "Image uploaded successfully. Response: " + baseResponse.getData());

                        // 从消息列表中移除 "正在分析..." 消息
                        List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                        if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                                && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                            updatedMessages.remove(updatedMessages.size() - 1);
                        }

                        // TODO: 这里需要根据后端实际返回的检测结果创建 CredibilityResult
                        // 目前由于后端返回为空，我们只能模拟一个结果或显示一个提示
                        ChatMessage aiMsg = new ChatMessage(ChatMessage.Type.AI, "图片已提交检测，请稍后查看结果。");
                        updatedMessages.add(aiMsg);

                        chatMessages.setValue(updatedMessages);

                        // --- 保存到历史记录 (示例：保存图片路径和模拟结果) ---
                        // 模拟一个结果用于保存
                        CredibilityResult resultToSave = new CredibilityResult(50, "图片检测已提交，结果待定。"); // 示例：默认50分，原因待定
                        saveDetectionRecord("图片内容: " + imagePath, resultToSave); // 保存到本地数据库
                        // --- 保存结束 ---

                    } else {
                        String msg = (baseResponse != null) ? baseResponse.getMsg() : "未知错误";
                        Log.e("CredibilityViewModel", "Upload failed. Server message: " + msg);
                        errorMessage.setValue("检测失败: " + msg);

                        // 从消息列表中移除 "正在分析..." 消息
                        List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                        if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                                && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                            updatedMessages.remove(updatedMessages.size() - 1);
                        }
                        ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, "检测失败: " + msg);
                        updatedMessages.add(errorMsg);
                        chatMessages.setValue(updatedMessages);
                    }
                } else {
                    Log.e("CredibilityViewModel", "Upload failed. HTTP code: " + response.code());
                    errorMessage.setValue("网络请求失败: " + response.code());

                    // 从消息列表中移除 "正在分析..." 消息
                    List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                    if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                            && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                        updatedMessages.remove(updatedMessages.size() - 1);
                    }
                    ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, "网络请求失败: " + response.code());
                    updatedMessages.add(errorMsg);
                    chatMessages.setValue(updatedMessages);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<UploadTextResponse>> call, Throwable t) {
                Log.e("CredibilityViewModel", "Upload failed due to network error.", t);
                errorMessage.setValue("网络错误: " + t.getMessage());

                // 从消息列表中移除 "正在分析..." 消息
                List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                        && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                    updatedMessages.remove(updatedMessages.size() - 1);
                }
                ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, "网络错误: " + t.getMessage());
                updatedMessages.add(errorMsg);
                chatMessages.setValue(updatedMessages);
            }
        });
    }
    // --- 新增结束 ---

    // --- 新增：公共方法，供 Fragment 调用 (图片) ---
    public void addUserMessageWithImage(String imagePath) {
        List<ChatMessage> currentValue = chatMessages.getValue();
        List<ChatMessage> current = new ArrayList<>(currentValue != null ? currentValue : new ArrayList<>());

        ChatMessage userMsg = new ChatMessage(ChatMessage.Type.USER, "发送了一张图片");
        userMsg.setImageUrl(imagePath); // 设置图片路径
        current.add(userMsg);

        // 添加一个临时的 "正在分析..." 消息
        ChatMessage aiMsg = new ChatMessage(ChatMessage.Type.AI, "正在分析...");
        current.add(aiMsg);
        chatMessages.setValue(current); // 更新UI显示加载状态

        // 调用私有的网络请求方法
        callUploadImageDetection(imagePath, current); // 传递当前消息列表以更新
    }
    // --- 新增结束 ---

    // --- 新增：调用多模态检测接口的方法 ---
    public void addUserMessageWithTextAndImage(String text, String imageUrl) { // 接收文本和图片URL
        List<ChatMessage> currentValue = chatMessages.getValue();
        List<ChatMessage> current = new ArrayList<>(currentValue != null ? currentValue : new ArrayList<>());

        ChatMessage userMsg = new ChatMessage(ChatMessage.Type.USER, text);
        userMsg.setImageUrl(imageUrl); // 设置图片URL
        current.add(userMsg);

        // 添加一个临时的 "正在分析..." 消息
        ChatMessage aiMsg = new ChatMessage(ChatMessage.Type.AI, "正在分析...");
        current.add(aiMsg);
        chatMessages.setValue(current); // 更新UI显示加载状态

        // 调用后端接口
        callUploadMultimodalDetection(text, imageUrl, current); // 传递当前消息列表以更新
    }

    private void callUploadMultimodalDetection(String text, String imageUrl, List<ChatMessage> currentMessages) {
        // 1. 获取 Token
        String token = authRepo.getAuthToken();
        if (token == null || token.isEmpty()) {
            Log.e("CredibilityViewModel", "Token is null or empty, cannot upload multimodal data.");
            errorMessage.setValue("用户未登录或Token无效");
            // 从消息列表中移除 "正在分析..." 消息
            List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
            if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                    && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                updatedMessages.remove(updatedMessages.size() - 1);
            }
            ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, "无法获取用户信息，请重新登录。");
            updatedMessages.add(errorMsg);
            chatMessages.setValue(updatedMessages);
            return;
        }

        // 2. 构建请求体
        AuthService.MultimodalDetectionRequest request = new AuthService.MultimodalDetectionRequest(text, imageUrl);

        // 3. 发起网络请求
        Call<BaseResponse<UploadTextResponse>> call = ApiManager.getAuthService().uploadMultimodalDetection("Bearer " + token, request);
        call.enqueue(new Callback<BaseResponse<UploadTextResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<UploadTextResponse>> call, Response<BaseResponse<UploadTextResponse>> response) {
                // 在主线程执行
                if (response.isSuccessful()) {
                    BaseResponse<UploadTextResponse> baseResponse = response.body();
                    if (baseResponse != null && baseResponse.isSuccess()) {
                        // 上传成功，但后端目前返回空对象 {}
                        Log.d("CredibilityViewModel", "Multimodal data uploaded successfully. Response: " + baseResponse.getData());

                        // 从消息列表中移除 "正在分析..." 消息
                        List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                        if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                                && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                            updatedMessages.remove(updatedMessages.size() - 1);
                        }

                        // TODO: 这里需要根据后端实际返回的检测结果创建 CredibilityResult
                        // 目前由于后端返回为空，我们只能模拟一个结果或显示一个提示
                        ChatMessage aiMsg = new ChatMessage(ChatMessage.Type.AI, "文本和图片已提交一致性检测，请稍后查看结果。");
                        updatedMessages.add(aiMsg);

                        chatMessages.setValue(updatedMessages);

                        // --- 保存到历史记录 (示例：保存文本和图片路径和模拟结果) ---
                        // 模拟一个结果用于保存
                        CredibilityResult resultToSave = new CredibilityResult(50, "多模态检测已提交，结果待定。"); // 示例：默认50分，原因待定
                        saveDetectionRecord("文本: " + text + " 图片: " + imageUrl, resultToSave); // 保存到本地数据库
                        // --- 保存结束 ---

                    } else {
                        String msg = (baseResponse != null) ? baseResponse.getMsg() : "未知错误";
                        Log.e("CredibilityViewModel", "Upload failed. Server message: " + msg);
                        errorMessage.setValue("检测失败: " + msg);

                        // 从消息列表中移除 "正在分析..." 消息
                        List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                        if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                                && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                            updatedMessages.remove(updatedMessages.size() - 1);
                        }
                        ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, "检测失败: " + msg);
                        updatedMessages.add(errorMsg);
                        chatMessages.setValue(updatedMessages);
                    }
                } else {
                    Log.e("CredibilityViewModel", "Upload failed. HTTP code: " + response.code());
                    errorMessage.setValue("网络请求失败: " + response.code());

                    // 从消息列表中移除 "正在分析..." 消息
                    List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                    if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                            && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                        updatedMessages.remove(updatedMessages.size() - 1);
                    }
                    ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, "网络请求失败: " + response.code());
                    updatedMessages.add(errorMsg);
                    chatMessages.setValue(updatedMessages);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<UploadTextResponse>> call, Throwable t) {
                Log.e("CredibilityViewModel", "Upload failed due to network error.", t);
                errorMessage.setValue("网络错误: " + t.getMessage());

                // 从消息列表中移除 "正在分析..." 消息
                List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
                if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).getType() == ChatMessage.Type.AI
                        && "正在分析...".equals(updatedMessages.get(updatedMessages.size() - 1).getText())) {
                    updatedMessages.remove(updatedMessages.size() - 1);
                }
                ChatMessage errorMsg = new ChatMessage(ChatMessage.Type.AI, "网络错误: " + t.getMessage());
                updatedMessages.add(errorMsg);
                chatMessages.setValue(updatedMessages);
            }
        });
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
                Log.d("CredibilityViewModel", "Record saved to database: " + title);
            } catch (Exception e) {
                Log.e("CredibilityViewModel", "Error saving record to database", e);
            }
        });
    }

    public LiveData<List<ChatMessage>> getChatMessages() {
        return chatMessages;
    }

    // --- 新增：获取错误信息的 LiveData ---
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    // --- 新增结束 ---
}