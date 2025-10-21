package com.example.insightnewsandroid.ui.credibility;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.db.AppDatabase;
import com.example.insightnewsandroid.db.DetectionRecordEntity;
import com.example.insightnewsandroid.db.SuspiciousSpan;
import com.example.insightnewsandroid.databinding.FragmentCredibilityBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CredibilityFragment extends Fragment {

    private FragmentCredibilityBinding binding;
    private CredibilityViewModel viewModel;
    private ChatAdapter adapter;

    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = AppDatabase.getDatabase(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(CredibilityViewModel.class);
        binding = FragmentCredibilityBinding.inflate(inflater, container, false);

        setupRecyclerView();
        setupInputActions();
        observeChatMessages();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter(new ArrayList<>());
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.chatRecyclerView.setAdapter(adapter);
    }

    private void observeChatMessages() {
        viewModel.getChatMessages().observe(getViewLifecycleOwner(), messages -> {
            if (messages == null) messages = new ArrayList<>();
            adapter = new ChatAdapter(messages);
            binding.chatRecyclerView.setAdapter(adapter);
            if (!messages.isEmpty()) {
                binding.chatRecyclerView.scrollToPosition(messages.size() - 1);

                ChatMessage lastMessage = messages.get(messages.size() - 1);
                if (lastMessage.getType() == ChatMessage.Type.AI) {
                    String title = getNewsTitle(messages);
                    String fullText = getFullText(messages); // ✅ 提取全文
                    String report = buildFullReport(lastMessage);
                    String credibility = extractCredibilityFromResult(lastMessage);
                    List<SuspiciousSpan> spans = extractSuspiciousSpans(lastMessage, fullText); // ✅ 提取可疑片段

                    // ✅ 传入所有必要参数
                    saveDetectionRecord(title, fullText, report, credibility, spans);
                }
            }
        });
    }

    private String getNewsTitle(List<ChatMessage> messages) {
        for (ChatMessage msg : messages) {
            if (msg.getType() == ChatMessage.Type.USER) {
                String text = msg.getText();
                if (text != null && !text.isEmpty()) {
                    return text.length() > 20 ? text.substring(0, 20) + "..." : text;
                }
            }
        }
        return "未命名新闻";
    }

    private String getFullText(List<ChatMessage> messages) {
        for (ChatMessage msg : messages) {
            if (msg.getType() == ChatMessage.Type.USER) {
                return msg.getText() != null ? msg.getText() : "";
            }
        }
        return "";
    }

    private List<SuspiciousSpan> extractSuspiciousSpans(ChatMessage aiMessage, String fullText) {
        // TODO: 从 AI 分析结果中提取可疑片段
        // 示例：假设 AI 返回了关键词或位置信息
        List<SuspiciousSpan> spans = new ArrayList<>();

        // 临时模拟：将全文中包含“震惊”、“速看”的片段标记为可疑
        String[] keywords = {"震惊", "速看", "刚刚", "重磅"};
        for (String keyword : keywords) {
            int index = fullText.indexOf(keyword);
            if (index != -1) {
                SuspiciousSpan span = new SuspiciousSpan();
                span.text = keyword;
                span.start = index;
                span.end = index + keyword.length();
                span.credibilityScore = 40; // 低可信度
                span.analysis = "包含夸张用语";
                span.evidence = "新闻标题常用夸张词汇吸引点击";
                spans.add(span);
            }
        }

        return spans;
    }

    private String buildFullReport(ChatMessage aiMessage) {
        StringBuilder report = new StringBuilder();
        if (aiMessage.getText() != null) {
            report.append(aiMessage.getText()).append("\n\n");
        }
        CredibilityResult result = aiMessage.getResult();
        if (result != null) {
            report.append("可信度分数：")
                    .append(result.getScore())
                    .append("%\n")
                    .append("分析理由：")
                    .append(result.getReason());
        }
        return report.toString().trim();
    }

    private String extractCredibilityFromResult(ChatMessage aiMessage) {
        CredibilityResult result = aiMessage.getResult();
        if (result == null) {
            return "较高";
        }
        int score = result.getScore();
        if (score >= 80) {
            return "高";
        } else if (score >= 60) {
            return "较高";
        } else {
            return "低";
        }
    }

    // ✅ 修正：接收 fullText 和 spans
    private void saveDetectionRecord(String title, String fullText, String report,
                                     String credibility, List<SuspiciousSpan> spans) {
        executor.execute(() -> {
            DetectionRecordEntity record = new DetectionRecordEntity(
                    title,
                    System.currentTimeMillis(),
                    fullText,           // ✅ 新闻全文
                    report,             // AI 报告
                    credibility,
                    spans               // ✅ 可疑片段
            );
            db.detectionDao().insert(record);
            mainHandler.post(() -> {
                Toast.makeText(requireContext(), R.string.record_saved, Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void setupInputActions() {
        binding.sendButton.setOnClickListener(v -> sendMessage());
        binding.editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
        binding.plusButton.setOnClickListener(v -> {
            // TODO: 未来扩展上传功能
        });
    }

    private void sendMessage() {
        String inputText = binding.editText.getText().toString().trim();
        if (inputText.isEmpty()) return;
        viewModel.addUserMessage(inputText, null, null);
        binding.editText.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}