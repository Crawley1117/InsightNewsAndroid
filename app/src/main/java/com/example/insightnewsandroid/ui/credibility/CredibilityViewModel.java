// 文件路径: app/java/com/example/insightnews/ui/credibility/CredibilityViewModel.java

package com.example.insightnewsandroid.ui.credibility;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class CredibilityViewModel extends ViewModel {

    private final MutableLiveData<List<ChatMessage>> chatMessages = new MutableLiveData<>();

    public CredibilityViewModel() {
        List<ChatMessage> initial = new ArrayList<>();
        initial.add(new ChatMessage(ChatMessage.Type.AI, "你好！我是冰小鉴，可帮你评估信息可信度。"));
        chatMessages.setValue(initial);
    }

    public void addUserMessage(String text, String imageUrl, String fileName) {
        List<ChatMessage> current = new ArrayList<>(chatMessages.getValue());
        ChatMessage userMsg = new ChatMessage(ChatMessage.Type.USER, text);
        if (imageUrl != null) userMsg.setImageUrl(imageUrl);
        if (fileName != null) userMsg.setFileName(fileName);
        current.add(userMsg);

        CredibilityResult result = simulateAnalysis(text, imageUrl, fileName);
        ChatMessage aiMsg = new ChatMessage(ChatMessage.Type.AI, "已分析你的内容：");
        aiMsg.setResult(result);
        current.add(aiMsg);

        chatMessages.setValue(current);
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