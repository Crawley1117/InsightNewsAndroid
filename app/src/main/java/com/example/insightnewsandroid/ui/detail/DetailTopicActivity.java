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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.insightnewsandroid.R;
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
    private int currentReplyCommentId = -1; // 添加回复评论ID

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

        // 设置收藏按钮 - 暂时移除收藏功能，因为ViewModel中没有对应方法
        ImageButton ivCollect = findViewById(R.id.ivCollect);
        if (ivCollect != null) {
            ivCollect.setOnClickListener(v -> {
                // 暂时显示提示，因为ViewModel中没有收藏方法
                Toast.makeText(this, "收藏功能暂未实现", Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * 初始化 ViewModel
     */
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(DetailTopicViewModel.class);

        // 设置上下文
        viewModel.setAppContext(this);

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
     * 设置评论输入框点击监听
     */
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

    /**
     * 设置 ViewPager
     */
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

    /**
     * 显示评论输入对话框 - 添加 hint 和 commentId 参数
     */
    public void showCommentInputDialog(String hint, int commentId) {
        this.currentReplyCommentId = commentId;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_comment_input, null);
        builder.setView(dialogView);

        commentDialog = builder.create();

        // 设置对话框样式 - 底部显示
        Window window = commentDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
        }

        initDialogViews(dialogView, hint);
        commentDialog.show();

        // 自动弹出键盘
        showKeyboard();
    }

    private void initDialogViews(View dialogView, String hint) {
        etCommentInput = dialogView.findViewById(R.id.et_comment_input);
        ImageButton btnSend = dialogView.findViewById(R.id.btn_send);

        // 发送按钮
        btnSend.setOnClickListener(v -> {
            String commentText = etCommentInput.getText().toString().trim();
            if (!commentText.isEmpty()) {
                submitComment(commentText);
                hideKeyboard();
                commentDialog.dismiss();
                // 重置回复状态
                currentReplyCommentId = -1;
            } else {
                Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show();
            }
        });

        // 监听输入变化
        etCommentInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // 可以在这里添加字符计数等功能
            }
        });
    }

    /**
     * 提交评论 - 使用ViewModel中的正确方法签名
     */
    private void submitComment(String commentText) {
        if (viewModel != null) {
            viewModel.addComment(topicId, commentText);
            Log.d(TAG, "发表评论: " + commentText);
        } else {
            Toast.makeText(this, "无法提交评论，请检查网络连接", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 显示键盘
     */
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

    /**
     * 隐藏键盘
     */
    private void hideKeyboard() {
        if (etCommentInput != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etCommentInput.getWindowToken(), 0);
            }
        }
    }

    /**
     * 实现 TopicCommentsFragment 的回调接口
     * 当在评论列表中点击回复时调用
     */
    @Override
    public void onShowCommentInput(String hint, int commentId) {
        // 当点击回复时，显示对话框
        showCommentInputDialog(hint, commentId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commentDialog != null && commentDialog.isShowing()) {
            commentDialog.dismiss();
        }
    }
}
