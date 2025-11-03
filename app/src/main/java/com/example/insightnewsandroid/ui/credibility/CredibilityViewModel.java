// 文件路径: app/java/com/example/insightnewsandroid/ui/credibility/CredibilityViewModel.java

package com.example.insightnewsandroid.ui.credibility;

import android.app.Application; // 导入 Application
import androidx.lifecycle.AndroidViewModel; // 导入 AndroidViewModel
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.insightnewsandroid.db.AppDatabase;
import com.example.insightnewsandroid.db.DetectionRecordEntity;
import com.example.insightnewsandroid.db.SuspiciousSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CredibilityViewModel extends AndroidViewModel { // 修改为继承 AndroidViewModel

    private final MutableLiveData<List<ChatMessage>> chatMessages = new MutableLiveData<>();
    private AppDatabase database; // 添加数据库成员变量
    private ExecutorService executor; // 用于后台线程

    // 构造函数接收 Application
    public CredibilityViewModel(Application application) {
        super(application); // 调用父类构造函数
        // 初始化数据库实例
        this.database = AppDatabase.getDatabase(application);
        // 初始化线程池
        this.executor = Executors.newSingleThreadExecutor();
    }

    public CredibilityViewModel() {
        // 保留无参构造函数以兼容旧代码（如果需要）
        // 注意：这可能会导致数据库操作失败，因为没有 Context
        // 在实际应用中，应优先使用带 Application 参数的构造函数
        super(null);
        this.database = null;
        this.executor = null;
    }

    public void addUserMessage(String text, String imageUrl, String fileName) {
        List<ChatMessage> currentValue = chatMessages.getValue();
        List<ChatMessage> current = new ArrayList<>(currentValue != null ? currentValue : new ArrayList<>());


        ChatMessage userMsg = new ChatMessage(ChatMessage.Type.USER, text);
        if (imageUrl != null) userMsg.setImageUrl(imageUrl);
        if (fileName != null) userMsg.setFileName(fileName);
        current.add(userMsg);

        CredibilityResult result = simulateAnalysis(text, imageUrl, fileName);
        ChatMessage aiMsg = new ChatMessage(ChatMessage.Type.AI, "已分析你的内容：");
        aiMsg.setResult(result);
        current.add(aiMsg);

        chatMessages.setValue(current);

        // --- 新增：保存到历史记录 ---
        saveDetectionRecord(text, result);
        // --- 新增结束 ---
    }

    private void saveDetectionRecord(String fullText, CredibilityResult result) {
        if (database == null || executor == null) {
            // 如果没有初始化数据库，跳过保存
            return;
        }
        // 在后台线程执行数据库操作
        executor.execute(() -> {
            try {
                int score = result.getScore();
                String level = score >= 75 ? "高" : (score >= 50 ? "中" : "低");
                String title = fullText.length() > 50 ? fullText.substring(0, 50) + "..." : fullText;
                String fullReport = result.getReason(); // 使用评估原因作为报告
                List<SuspiciousSpan> suspiciousSpans = new ArrayList<>(); // 示例：空列表
                // 假设 SuspiciousSpan 需要从 result 中构造，但目前 result 没有此信息，暂时留空
                // 你可以在这里添加逻辑来填充 suspiciousSpans

                DetectionRecordEntity record = new DetectionRecordEntity(
                        title,
                        System.currentTimeMillis(),
                        fullText,
                        fullReport,
                        level,
                        suspiciousSpans
                );
                database.detectionDao().insert(record);
                // 可选：打印日志
                // Log.d("CredibilityViewModel", "Record saved to database: " + title);
            } catch (Exception e) {
                // Log.e("CredibilityViewModel", "Error saving record to database", e);
                // 可以选择通知 UI 发生了错误
            }
        });
    }

    private CredibilityResult simulateAnalysis(String text, String img, String file) {
        int score = 80;
        if (text != null) score -= text.length() / 10;
        score = Math.max(30, Math.min(100, score));
        String reason = score >= 75 ? "内容较可靠" : "建议进一步核实";
        return new CredibilityResult(score, reason);
    }

    public LiveData<List<ChatMessage>> getChatMessages() {
        return chatMessages;
    }
}