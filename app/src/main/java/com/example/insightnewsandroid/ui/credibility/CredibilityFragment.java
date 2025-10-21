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
import com.example.insightnewsandroid.databinding.FragmentCredibilityBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CredibilityFragment extends Fragment {

    private FragmentCredibilityBinding binding;
    private CredibilityViewModel viewModel;
    private ChatAdapter adapter;

    // Room 相关
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

                // 检查最后一条是否是 AI 回复（表示一次检测完成）
                ChatMessage lastMessage = messages.get(messages.size() - 1);
                if (lastMessage.getType() == ChatMessage.Type.AI) {
                    String title = getNewsTitle(messages);
                    String report = buildFullReport(lastMessage);
                    String credibility = extractCredibilityFromResult(lastMessage);
                    saveDetectionRecord(title, report, credibility);
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
            return "较高"; // 默认
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

    private void saveDetectionRecord(String title, String report, String credibility) {
        executor.execute(() -> {
            DetectionRecordEntity record = new DetectionRecordEntity(
                    title,
                    System.currentTimeMillis(),
                    credibility,
                    report
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