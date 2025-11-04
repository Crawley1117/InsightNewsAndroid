package com.example.insightnewsandroid.ui.detail;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.data.manager.TopicCollectionManager;
import com.example.insightnewsandroid.data.manager.TopicManager;
import com.example.insightnewsandroid.data.model.NewsItem;
import com.example.insightnewsandroid.data.model.Topic;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DetailTopicActivity extends AppCompatActivity
        implements TopicCommentsFragment.OnCommentActionListener {

    private static final String TAG = "DetailTopicActivity";

    private DetailTopicViewModel viewModel;
    private AlertDialog commentDialog;
    private EditText etCommentInput;
    private int topicId;
    private String token;
    private int currentReplyCommentId = -1;

    // 收藏相关变量
    private ImageButton ivCollect;
    private boolean isCollected = false;
    private TopicCollectionManager collectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_topic);

        // 获取传递的参数
        if (getIntent() != null) {
            topicId = getIntent().getIntExtra("TOPIC_ID", -1);
            token = getIntent().getStringExtra("TOKEN");
        }

        Log.d(TAG, "初始化详情页，topicId: " + topicId);

        // 初始化收藏管理器
        collectionManager = TopicCollectionManager.getInstance(this);

        initViews();
        initViewModel();
        setupCommentInput();
        setupViewPager();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        // 设置返回按钮
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        // 初始化收藏按钮
        ivCollect = findViewById(R.id.ivCollect);
        if (ivCollect != null) {
            // 检查当前话题是否已收藏
            checkCollectionStatus();

            ivCollect.setOnClickListener(v -> {
                toggleTopicCollection();
            });
        }
    }

    /**
     * 检查收藏状态
     */
    private void checkCollectionStatus() {
        if (topicId != -1 && collectionManager != null) {
            isCollected = collectionManager.isTopicCollected(topicId);
            updateCollectButton();
            Log.d(TAG, "收藏状态检查: topicId=" + topicId + ", isCollected=" + isCollected);
        }
    }

    /**
     * 切换收藏状态
     */
    private void toggleTopicCollection() {
        if (topicId == -1) {
            Toast.makeText(this, "话题信息不完整", Toast.LENGTH_SHORT).show();
            return;
        }

        if (collectionManager == null) {
            collectionManager = TopicCollectionManager.getInstance(this);
        }

        // 获取当前话题的完整信息
        TopicManager topicManager = TopicManager.getInstance(this);
        Topic currentTopic = topicManager.getTopicById(topicId);

        if (currentTopic == null) {
            Toast.makeText(this, "话题不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success;
        if (isCollected) {
            // 取消收藏
            success = collectionManager.removeFromCollection(topicId);
            if (success) {
                isCollected = false;
                Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "取消收藏成功: topicId=" + topicId);
            }
        } else {
            // 添加收藏
            success = collectionManager.addToCollection(currentTopic);
            if (success) {
                isCollected = true;
                Toast.makeText(this, "收藏成功", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "收藏成功: topicId=" + topicId + ", title=" + currentTopic.getTitle());
            }
        }

        if (success) {
            updateCollectButton();
        } else {
            Toast.makeText(this, "操作失败，请重试", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "收藏操作失败: topicId=" + topicId);
        }
    }

    /**
     * 更新收藏按钮状态
     */
    private void updateCollectButton() {
        if (ivCollect != null) {
            if (isCollected) {
                ivCollect.setImageResource(R.drawable.ic_topic_selected);
                ivCollect.setContentDescription("已收藏");
                Log.d(TAG, "更新收藏按钮状态: 已收藏");
            } else {
                ivCollect.setImageResource(R.drawable.ic_topic_unselected);
                ivCollect.setContentDescription("收藏");
                Log.d(TAG, "更新收藏按钮状态: 未收藏");
            }
        }
    }

    /**
     * 初始化 ViewModel
     */
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(DetailTopicViewModel.class);

        // 设置上下文
        viewModel.setAppContext(this);

        // 观察话题详情变化
        viewModel.getTopicDetail().observe(this, topicDetail -> {
            if (topicDetail != null) {
                updateTopicUI(topicDetail);
            }
        });

        // 观察评论提交结果
        viewModel.getCommentSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "评论发表成功", Toast.LENGTH_SHORT).show();
                // 清空输入框
                if (etCommentInput != null) {
                    etCommentInput.setText("");
                }
            }
        });

        // 观察错误信息
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // 加载话题详情
        if (topicId != -1) {
            viewModel.loadTopicDetail(topicId);
        }
    }

    /**
     * 更新话题UI
     */
    private void updateTopicUI(NewsItem topicDetail) {
        // 更新标题
        TextView tvTitle = findViewById(R.id.tv_title);
        if (tvTitle != null && topicDetail.getTitle() != null) {
            tvTitle.setText(topicDetail.getTitle());
        }

        // 更新描述
        TextView tvDescription = findViewById(R.id.tv_description);
        if (tvDescription != null && topicDetail.getContent() != null) {
            tvDescription.setText(topicDetail.getContent());
        }

        // 更新分类
        TextView tvCategory = findViewById(R.id.tv_category);
        if (tvCategory != null && topicDetail.getTopicCategory() != null) {
            tvCategory.setText(topicDetail.getTopicCategory());
        }

        // 更新关注数
        TextView tvFollowCount = findViewById(R.id.tv_follow_count);
        if (tvFollowCount != null) {
            String followText = topicDetail.getViewCount() + "人关注";
            tvFollowCount.setText(followText);
        }

        // 更新头部图片
        ImageView ivHeader = findViewById(R.id.iv_header);
        if (ivHeader != null && topicDetail.getImageUrl() != null && !topicDetail.getImageUrl().isEmpty()) {
            // 这里可以使用Glide或Picasso加载图片
            // Glide.with(this).load(topicDetail.getImageUrl()).into(ivHeader);
            Log.d(TAG, "话题图片URL: " + topicDetail.getImageUrl());
        }

        // 重新检查收藏状态（确保数据加载完成后更新）
        checkCollectionStatus();
    }

    // 以下保持原有代码不变...
    private void setupCommentInput() {
        LinearLayout llCommentInput = findViewById(R.id.ll_comment_input);
        TextView tvCommentHint = findViewById(R.id.tv_comment_hint);

        if (llCommentInput != null) {
            llCommentInput.setOnClickListener(v -> {
                showCommentInputDialog("发表评论", -1);
            });
        }

        if (tvCommentHint != null) {
            tvCommentHint.setOnClickListener(v -> {
                showCommentInputDialog("发表评论", -1);
            });
        }
    }

    private void setupViewPager() {
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        if (viewPager != null && tabLayout != null) {
            DetailTopicPagerAdapter pagerAdapter = new DetailTopicPagerAdapter(this, topicId, token);
            viewPager.setAdapter(pagerAdapter);

            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                if (position == 0) {
                    tab.setText("资讯");
                } else {
                    tab.setText("评论");
                }
            }).attach();
        }
    }

    public void showCommentInputDialog(String hint, int commentId) {
        this.currentReplyCommentId = commentId;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_comment_input, null);
        builder.setView(dialogView);

        commentDialog = builder.create();

        Window window = commentDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
        }

        initDialogViews(dialogView, hint);
        commentDialog.show();
        showKeyboard();
    }

    private void initDialogViews(View dialogView, String hint) {
        etCommentInput = dialogView.findViewById(R.id.et_comment_input);
        ImageButton btnSend = dialogView.findViewById(R.id.btn_send);

        btnSend.setOnClickListener(v -> {
            String commentText = etCommentInput.getText().toString().trim();
            if (!commentText.isEmpty()) {
                submitComment(commentText);
                hideKeyboard();
                commentDialog.dismiss();
                currentReplyCommentId = -1;
            } else {
                Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show();
            }
        });

        etCommentInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void submitComment(String commentText) {
        if (viewModel != null) {
            viewModel.addComment(topicId, commentText);
            Log.d(TAG, "发表评论: " + commentText);
        } else {
            Toast.makeText(this, "无法提交评论，请检查网络连接", Toast.LENGTH_SHORT).show();
        }
    }

    private void showKeyboard() {
        if (etCommentInput != null) {
            etCommentInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                new Handler().postDelayed(() -> {
                    imm.showSoftInput(etCommentInput, InputMethodManager.SHOW_IMPLICIT);
                }, 100);
            }
        }
    }

    private void hideKeyboard() {
        if (etCommentInput != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etCommentInput.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onShowCommentInput(String hint, int commentId) {
        showCommentInputDialog(hint, commentId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 当从其他页面返回时，重新检查收藏状态
        checkCollectionStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commentDialog != null && commentDialog.isShowing()) {
            commentDialog.dismiss();
        }
    }
}
