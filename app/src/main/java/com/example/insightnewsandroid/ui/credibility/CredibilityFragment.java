// File: app/java/com/example/insightnewsandroid/ui/credibility/CredibilityFragment.java

package com.example.insightnewsandroid.ui.credibility;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.insightnewsandroid.db.AppDatabase;
import com.example.insightnewsandroid.db.DetectionRecordEntity;
import com.example.insightnewsandroid.db.SuspiciousSpan;
import com.example.insightnewsandroid.EnhancedReportActivity;
import com.example.insightnewsandroid.HistoryActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CredibilityFragment extends Fragment {

    private FragmentCredibilityBinding binding;
    private CredibilityViewModel viewModel; // 使用修改后的 ViewModel
    private Toolbar toolbar; // 添加 Toolbar 成员变量
    private AppDatabase db; // 添加数据库成员变量
    private ExecutorService executor; // 添加线程池成员变量
    private Handler mainHandler; // 添加 Handler 用于切换到主线程

    // --- 新增：用于存储当前显示数据的成员变量 ---
    private String currentFullText = null;
    private String currentSuspiciousSpansJson = null;
    private String currentCredibilityLevel = null;
    // --- 新增结束 ---

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCredibilityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化数据库、线程池和 Handler
        db = AppDatabase.getDatabase(requireContext());
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper()); // 用于切换到主线程

        // 初始化 ViewModel (使用 Factory)
        viewModel = new ViewModelProvider(this, new CredibilityViewModelFactory(requireActivity().getApplication())).get(CredibilityViewModel.class);

        // 初始化 Toolbar
        toolbar = binding.toolbar; // 获取 Toolbar 实例
        if (getActivity() != null) {
            ((androidx.appcompat.app.AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            // 确保 Fragment 能处理菜单
            setHasOptionsMenu(true);
        }

        setupObservers();
        setupClickListeners();

        // --- 新增：为 assessmentCard 设置点击监听器 ---
        binding.assessmentCard.setOnClickListener(v -> {
            // 检查是否有当前数据
            if (currentFullText != null) {
                // 创建 Intent 跳转到 EnhancedReportActivity
                Intent intent = new Intent(requireContext(), EnhancedReportActivity.class);

                // 传递新闻全文 (使用当前存储的文本)
                intent.putExtra("fullText", currentFullText);

                // 传递可疑片段数据 (使用当前存储的 JSON 字符串)
                intent.putExtra("suspiciousSpans", currentSuspiciousSpansJson != null ? currentSuspiciousSpansJson : "[]");

                // 传递可信度等级
                intent.putExtra("credibilityLevel", currentCredibilityLevel != null ? currentCredibilityLevel : "未知");

                // 由于我们没有直接的 "title"，可以使用原文的前缀或 "AI 分析结果" 作为标题
                String title = currentFullText.length() > 50 ? currentFullText.substring(0, 50) + "..." : currentFullText;
                intent.putExtra("title", title);

                startActivity(intent);
            } else {
                // 如果没有当前数据，可以提示用户或跳转到默认页面
                Toast.makeText(requireContext(), "暂无分析结果可查看", Toast.LENGTH_SHORT).show();
            }
        });
        // --- 新增结束 ---

        // --- 新增：加载并显示最新历史记录 ---
        loadAndDisplayLatestHistoryRecord();
        // --- 新增结束 ---
    }

    // --- 新增：加载并显示最新历史记录的方法 ---
    private void loadAndDisplayLatestHistoryRecord() {
        executor.execute(() -> {
            // 在后台线程查询数据库
            List<DetectionRecordEntity> records = db.detectionDao().getAllRecords();

            // 切换到主线程更新UI
            mainHandler.post(() -> {
                if (records != null && !records.isEmpty()) {
                    // 如果有记录，取第一条（按时间倒序排列，所以第一条是最新的）
                    DetectionRecordEntity latestRecord = records.get(0);
                    displayRecord(latestRecord);
                } else {
                    // 如果没有记录，隐藏卡片
                    binding.newsCard.setVisibility(View.GONE);
                    binding.assessmentCard.setVisibility(View.GONE);
                    // 清空当前数据
                    currentFullText = null;
                    currentSuspiciousSpansJson = null;
                    currentCredibilityLevel = null;
                }
            });
        });
    }
    // --- 新增结束 ---

    // --- 修改：显示记录的方法 (仅设置评估卡片背景, 并更新成员变量) ---
    private void displayRecord(DetectionRecordEntity record) {
        // 更新成员变量
        currentFullText = record.fullText;
        currentSuspiciousSpansJson = record.suspiciousSpansJson;
        currentCredibilityLevel = record.credibilityLevel;

        // 1. 显示新闻卡片
        binding.newsCard.setVisibility(View.VISIBLE);
        // 设置新闻标题为记录的标题或全文前部分
        binding.newsTitle.setText(record.title.length() > 50 ? record.title.substring(0, 50) + "..." : record.title);
        // 设置来源为时间戳
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(record.detectionDate));
        binding.newsSource.setText(timestamp);

        // 2. 显示评估结果卡片
        binding.assessmentCard.setVisibility(View.VISIBLE);
        // 设置可信度文本为原文前十个字（或使用记录的其他摘要）
        String displayText = record.fullText.length() > 10 ? record.fullText.substring(0, 10) : record.fullText;
        binding.credibilityText.setText(displayText);
        // 设置可信度百分比为记录的等级（这里假设等级可以转换为百分比，否则需要从其他字段获取）
        // 例如：高->90%, 中->60%, 低->30% (这只是一个示例映射)
        int score = -1; // 初始化为无效值
        switch (record.credibilityLevel) {
            case "高":
                score = 90;
                break;
            case "较高":
                score = 80;
                break;
            case "中":
                score = 60;
                break;
            case "较低":
                score = 40;
                break;
            case "低":
                score = 20;
                break;
            default:
                // 如果没有匹配的等级，保持 score = -1
                break;
        }
        if (score != -1) {
            binding.credibilityPercentage.setText("可信度得分：" + score + "%");
        } else {
            // 如果等级无法映射，直接显示等级
            binding.credibilityPercentage.setText("可信度等级：" + record.credibilityLevel);
        }

        // 设置文字颜色（根据分数或等级）
        int textColor = score >= 75 ? R.color.green : (score >= 50 ? R.color.orange : R.color.red);
        if (score == -1) { // 如果分数无效，则根据等级判断颜色
            textColor = record.credibilityLevel.equals("高") || record.credibilityLevel.equals("较高") ? R.color.green :
                    (record.credibilityLevel.equals("中") ? R.color.orange : R.color.red);
        }
        binding.credibilityText.setTextColor(getResources().getColor(textColor));
        binding.credibilityPercentage.setTextColor(getResources().getColor(textColor));

        // --- 修改：仅设置评估卡片背景 ---
        setCardBackground(binding.assessmentCard, record.credibilityLevel);
        // --- 修改结束 ---
    }
    // --- 修改结束 ---


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
                String originalUserInput = null; // 用于存储原始用户输入
                for (int i = chatMessages.size() - 1; i >= 0; i--) {
                    ChatMessage msg = chatMessages.get(i);
                    if (msg.getType() == ChatMessage.Type.AI && msg.getResult() != null) {
                        lastAiMessage = msg;
                        // 尝试获取对应的用户输入消息
                        // 假设AI消息紧跟在用户消息之后
                        if (i > 0) {
                            ChatMessage previousMsg = chatMessages.get(i - 1);
                            if (previousMsg.getType() == ChatMessage.Type.USER) {
                                originalUserInput = previousMsg.getText(); // 假设 ChatMessage 保存了原始输入
                            }
                        }
                        break;
                    }
                }

                if (lastAiMessage != null) {
                    // 如果找到了带有结果的AI消息，更新UI显示结果
                    CredibilityResult result = lastAiMessage.getResult();
                    // 这里可以显示AI的回复文本作为新闻标题，或者使用用户输入的内容
                    String aiReplyContent = lastAiMessage.getText() != null ? lastAiMessage.getText() : "已分析";
                    // 使用找到的原始输入，如果找不到则尝试从当前输入框获取（可能为空）
                    String originalContent = originalUserInput != null ? originalUserInput : binding.newsInput.getText().toString().trim();
                    showAssessmentResult(result, originalContent, aiReplyContent);

                    // --- 保存到历史记录的逻辑已移至 ViewModel ---
                    // 在 addUserMessage 内部调用的 simulateAnalysis 之后，ViewModel 会自动保存
                    // 因此这里不再需要 Fragment 直接调用数据库保存逻辑
                    // --- 保存逻辑结束 ---
                } else {
                    // 如果没有找到带有结果的AI消息，可能还在分析中，或者只显示初始消息
                    // hideAssessmentResult(); // 如果你想在有消息但没结果时不显示，可以调用
                }
            } else {
                // 如果消息列表为空，不隐藏，因为可能要显示历史记录
                // hideAssessmentResult(); // 如果你想在完全没有消息时隐藏，可以调用
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
            String originalUserInputForReport = null; // 用于报告的原始输入
            if (messages != null) {
                for (int i = messages.size() - 1; i >= 0; i--) {
                    ChatMessage msg = messages.get(i);
                    if (msg.getType() == ChatMessage.Type.AI && msg.getResult() != null) {
                        lastAiMessageWithResult = msg;
                        // 尝试获取对应的用户输入消息
                        if (i > 0) {
                            ChatMessage previousMsg = messages.get(i - 1);
                            if (previousMsg.getType() == ChatMessage.Type.USER) {
                                originalUserInputForReport = previousMsg.getText();
                            }
                        }
                        break;
                    }
                }
            }

            if (lastAiMessageWithResult != null) {
                // 创建 Intent 跳转到 EnhancedReportActivity
                Intent intent = new Intent(requireContext(), EnhancedReportActivity.class);

                // 传递新闻标题 (可以是AI回复的一部分或用户输入)
                String title = lastAiMessageWithResult.getText() != null ? lastAiMessageWithResult.getText() : "未知标题";
                intent.putExtra("title", title);

                // 传递新闻全文 (这里使用找到的原始用户输入)
                String fullText = originalUserInputForReport != null ? originalUserInputForReport : "未找到原文，使用示例文本。";
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
    // 参数 originalNewsContent: 用户输入的原始新闻内容
    // 参数 aiMessageContent: AI 返回的分析内容或摘要
    private void showAssessmentResult(CredibilityResult result, String originalNewsContent, String aiMessageContent) {
        // 更新成员变量
        currentFullText = originalNewsContent; // 使用原始输入作为全文
        currentSuspiciousSpansJson = null; // 这里没有现成的 JSON，可以留空或构造
        int score = result.getScore();
        currentCredibilityLevel = score >= 75 ? "高" : (score >= 50 ? "中" : "低");

        binding.newsCard.setVisibility(View.VISIBLE);
        binding.assessmentCard.setVisibility(View.VISIBLE);

        // --- 更新新闻卡片内容 (显示AI的回复摘要或内容) ---
        String title = aiMessageContent.length() > 50 ? aiMessageContent.substring(0, 50) + "..." : aiMessageContent;
        binding.newsTitle.setText(title);
        binding.newsSource.setText("AI 分析结果"); // 或者显示来源

        // --- 更新评估结果卡片内容 ---
        String reason = result.getReason();
        // int score = result.getScore(); // 已在上面定义
        // String level = score >= 75 ? "高" : (score >= 50 ? "中" : "低"); // 已在上面定义

        // --- 核心修改：显示原文前十个字 ---
        String displayText = originalNewsContent.length() > 10 ? originalNewsContent.substring(0, 10) : originalNewsContent;
        binding.credibilityText.setText(displayText); // 显示原文前10个字
        binding.credibilityPercentage.setText("可信度得分：" + score + "%");

        int textColor = score >= 75 ? R.color.green : (score >= 50 ? R.color.orange : R.color.red);
        binding.credibilityText.setTextColor(getResources().getColor(textColor));
        binding.credibilityPercentage.setTextColor(getResources().getColor(textColor));

        // --- 修改：仅设置评估卡片背景 ---
        setCardBackground(binding.assessmentCard, currentCredibilityLevel);
        // --- 修改结束 ---

        // --- 移除印章 ---
        // binding.stampTextView.setText(level); // 移除此行，因为 layout 中已无此 View
        // binding.stampTextView.setVisibility(View.GONE); // 如果 layout 中保留了但想隐藏，可加此行，但建议直接移除
    }


    // 隐藏评估结果 UI 卡片
    private void hideAssessmentResult() {
        binding.newsCard.setVisibility(View.GONE);
        binding.assessmentCard.setVisibility(View.GONE);
        // 清空当前数据
        currentFullText = null;
        currentSuspiciousSpansJson = null;
        currentCredibilityLevel = null;
    }

    // --- 新增：根据可信度等级设置卡片背景的方法 ---
    private void setCardBackground(com.google.android.material.card.MaterialCardView cardView, String credibilityLevel) {
        int bg;
        switch (credibilityLevel) {
            case "高":
                bg = R.drawable.bg_highscore;
                break;
            case "较高":
                bg = R.drawable.bg_sub_highscore;
                break;
            case "中":
                bg = R.drawable.bg_medium;
                break;
            case "较低":
                bg = R.drawable.bg_sub_lowscore;
                break;
            case "低":
                bg = R.drawable.bg_lowscore;
                break;
            default:
                bg = R.drawable.bg_medium; // 默认为中等
                break;
        }
        // 使用 setContentPadding 来保留 MaterialCardView 的圆角和阴影效果
        // 如果直接设置 cardView.setCardBackgroundColor，可能会覆盖这些效果
        // 更推荐的方式是设置 cardView 的 background，但这会失去 Material Design 的一些特性
        // 所以，如果 bg_drawable 包含了圆角和阴影，则直接设置 background
        // 如果 bg_drawable 只是颜色/渐变，则使用 setCardBackgroundColor
        // 这里假设 bg_drawable 是完整的背景 Drawable (包含圆角、阴影等)
        cardView.setCardBackgroundColor(getResources().getColor(android.R.color.transparent)); // 确保 CardView 本身的背景透明
        cardView.setBackgroundResource(bg); // 设置自定义背景
    }
    // --- 新增结束 ---

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        // 关闭线程池
        if (executor != null) {
            executor.shutdown();
        }
    }

    // --- 新增：CredibilityViewModelFactory ---
    private static class CredibilityViewModelFactory implements ViewModelProvider.Factory {
        private final android.app.Application application;

        public CredibilityViewModelFactory(android.app.Application application) {
            this.application = application;
        }

        @NonNull
        @Override
        public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CredibilityViewModel.class)) {
                return (T) new CredibilityViewModel(application);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass);
        }
    }
    // --- 新增结束 ---
}