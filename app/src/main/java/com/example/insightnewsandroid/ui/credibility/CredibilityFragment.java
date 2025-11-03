// File: app/java/com/example/insightnewsandroid/ui/credibility/CredibilityFragment.java

package com.example.insightnewsandroid.ui.credibility;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.databinding.FragmentCredibilityBinding;
import com.example.insightnewsandroid.db.SuspiciousSpan;
import com.example.insightnewsandroid.HistoryActivity;
import com.google.android.material.textfield.TextInputEditText;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class CredibilityFragment extends Fragment {

    private FragmentCredibilityBinding binding;
    private CredibilityViewModel viewModel;
    private Toolbar toolbar; // 添加 Toolbar 成员变量

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCredibilityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(CredibilityViewModel.class);

        // 初始化 Toolbar
        toolbar = binding.toolbar; // 获取 Toolbar 实例
        if (getActivity() != null) {
            ((androidx.appcompat.app.AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            // 确保 Fragment 能处理菜单
            setHasOptionsMenu(true);
        }

        setupObservers();
        setupClickListeners();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.credibility_menu, menu); // 加载菜单资源
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_history) { // 确保 ID 与 menu 文件中一致
            // 创建 Intent 跳转到 HistoryActivity
            Intent intent = new Intent(requireContext(), HistoryActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupObservers() {
        // 观察聊天消息列表 (根据你的 ViewModel)
        viewModel.getChatMessages().observe(getViewLifecycleOwner(), chatMessages -> {
            if (chatMessages != null && !chatMessages.isEmpty()) {
                // 获取最新的 AI 消息（包含评估结果）
                ChatMessage lastAiMessage = null;
                for (int i = chatMessages.size() - 1; i >= 0; i--) {
                    ChatMessage msg = chatMessages.get(i);
                    if (msg.getType() == ChatMessage.Type.AI && msg.getResult() != null) {
                        lastAiMessage = msg;
                        break;
                    }
                }

                if (lastAiMessage != null) {
                    // 如果找到了带有结果的AI消息，更新UI显示结果
                    CredibilityResult result = lastAiMessage.getResult();
                    // 这里可以显示AI的回复文本作为新闻标题，或者使用用户输入的内容
                    String aiReplyContent = lastAiMessage.getText() != null ? lastAiMessage.getText() : "已分析";
                    showAssessmentResult(result, aiReplyContent);
                } else {
                    // 如果没有找到带有结果的AI消息，可能还在分析中，或者只显示初始消息
                    // 例如，如果最后一条是用户消息，可能需要等待AI回复
                    // 可以选择隐藏结果卡片，或者显示一个“正在分析...”的提示
                    hideAssessmentResult();
                }
            } else {
                // 如果消息列表为空，隐藏结果
                hideAssessmentResult();
            }
        });
    }

    private void setupClickListeners() {
        // --- 移除对 clockButton 的引用 ---
        // binding.clockButton.setOnClickListener(v -> { ... });
        // --- 移除结束 ---

        // 输入框右侧的“+”按钮
        binding.addButton.setOnClickListener(v -> {
            String inputText = binding.newsInput.getText().toString().trim();
            if (!inputText.isEmpty()) {
                // 调用 ViewModel 添加用户消息并触发分析
                // 目前只传递文本，图片和文件名传 null，你可以扩展这里的逻辑
                viewModel.addUserMessage(inputText, null, null);
                // 清空输入框
                binding.newsInput.setText("");
            } else {
                Toast.makeText(requireContext(), "请输入新闻内容", Toast.LENGTH_SHORT).show();
            }
        });

        // “查看详细分析报告”按钮
        binding.reportButton.setOnClickListener(v -> {
            // 获取当前评估结果
            List<ChatMessage> messages = viewModel.getChatMessages().getValue();
            ChatMessage lastAiMessageWithResult = null;
            if (messages != null) {
                for (int i = messages.size() - 1; i >= 0; i--) {
                    ChatMessage msg = messages.get(i);
                    if (msg.getType() == ChatMessage.Type.AI && msg.getResult() != null) {
                        lastAiMessageWithResult = msg;
                        break;
                    }
                }
            }

            if (lastAiMessageWithResult != null) {
                // 创建 Intent 跳转到 EnhancedReportActivity
                Intent intent = new Intent(requireContext(), com.example.insightnewsandroid.EnhancedReportActivity.class);

                // 传递新闻标题 (可以是AI回复的一部分或用户输入)
                String title = lastAiMessageWithResult.getText() != null ? lastAiMessageWithResult.getText() : "未知标题";
                intent.putExtra("title", title);

                // 传递新闻全文 (这里可以传递用户输入的原文或其他相关内容)
                // 为了演示，我们仍然使用示例文本，但在实际应用中应传递用户输入
                String fullText = binding.newsInput.getText().toString().trim(); // 或者从 ChatMessage 中获取
                if (fullText.isEmpty()) {
                    fullText = "未找到原文，使用示例文本。"; // 如果输入框已清空，使用默认值或从消息列表获取
                }
                intent.putExtra("fullText", fullText);

                // 传递可疑片段数据 (JSON 字符串)
                // 这里构造一个示例的 SuspiciousSpan 列表，实际应用中应从 CredibilityResult 或 ChatMessage 获取
                CredibilityResult result = lastAiMessageWithResult.getResult();
                List<SuspiciousSpan> suspiciousSpans = new ArrayList<>();
                if (result != null) {
                    // 假设 CredibilityResult 包含或可以生成 SuspiciousSpan 列表
                    // 由于当前 CredibilityResult 只有 score 和 reason，我们构造示例
                    SuspiciousSpan span1 = new SuspiciousSpan();
                    span1.text = "示例可疑词";
                    span1.start = 0;
                    span1.end = 5;
                    span1.credibilityScore = result.getScore(); // 使用评估分数
                    span1.evidence = "示例证据";
                    span1.analysis = result.getReason(); // 使用评估原因
                    suspiciousSpans.add(span1);
                }

                Gson gson = new Gson();
                String spansJson = gson.toJson(suspiciousSpans);
                intent.putExtra("suspiciousSpans", spansJson);

                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "暂无分析报告可查看", Toast.LENGTH_SHORT).show();
            }
        });

        // 四个功能按钮 (可以扩展为选择图片、文件等)
        binding.photoButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "选择图片功能待实现", Toast.LENGTH_SHORT).show();
            // 这里可以启动图片选择器
            // 选择图片后，调用 viewModel.addUserMessage("", imageUrl, null);
        });

        binding.cameraButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "启动相机功能待实现", Toast.LENGTH_SHORT).show();
            // 这里可以启动相机
        });

        binding.microphoneButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "开始录音功能待实现", Toast.LENGTH_SHORT).show();
            // 这里可以启动录音
        });

        binding.folderButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "选择文件功能待实现", Toast.LENGTH_SHORT).show();
            // 这里可以启动文件选择器
            // 选择文件后，调用 viewModel.addUserMessage("", null, fileName);
        });

        // 评价按钮
        binding.likeButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "已点赞", Toast.LENGTH_SHORT).show();
        });

        binding.dislikeButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "已点踩", Toast.LENGTH_SHORT).show();
        });

        binding.starButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "已收藏", Toast.LENGTH_SHORT).show();
        });
    }

    // 显示评估结果到 UI 卡片
    private void showAssessmentResult(CredibilityResult result, String aiMessageContent) {
        binding.newsCard.setVisibility(View.VISIBLE);
        binding.assessmentCard.setVisibility(View.VISIBLE);

        // --- 更新新闻卡片内容 (可以显示AI的回复摘要或用户输入) ---
        String title = aiMessageContent.length() > 50 ? aiMessageContent.substring(0, 50) + "..." : aiMessageContent;
        binding.newsTitle.setText(title);
        binding.newsSource.setText("AI 分析结果"); // 或者显示来源

        // --- 更新评估结果卡片内容 ---
        int score = result.getScore();
        String reason = result.getReason();
        String level = score >= 75 ? "高" : (score >= 50 ? "中" : "低");

        binding.credibilityText.setText("可信度等级: " + level + " (" + reason + ")");
        binding.credibilityPercentage.setText("可信度得分：" + score + "%");

        int textColor = score >= 75 ? R.color.green : (score >= 50 ? R.color.orange : R.color.red);
        binding.credibilityText.setTextColor(getResources().getColor(textColor));
        binding.credibilityPercentage.setTextColor(getResources().getColor(textColor));

        // --- 更新印章文字 ---
        binding.stampTextView.setText(level);
    }

    // 隐藏评估结果 UI 卡片
    private void hideAssessmentResult() {
        binding.newsCard.setVisibility(View.GONE);
        binding.assessmentCard.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}