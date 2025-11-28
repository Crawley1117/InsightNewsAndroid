package com.example.insightnewsandroid.ui.detail;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
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
import com.example.insightnewsandroid.auth.AuthRepository;
import com.example.insightnewsandroid.data.model.Comment;
import com.example.insightnewsandroid.data.model.NewsArticle;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DetailTopicActivity extends AppCompatActivity
        implements TopicCommentsFragment.OnCommentActionListener {

    private DetailViewModel viewModel;
    private AuthRepository authRepository;
    private AlertDialog commentDialog;
    private EditText etCommentInput;
    private String topicId;
    private String token;
    private int currentReplyParentId = 0;
    private ViewPager2 viewPager;
    private ImageButton ivCollect; // [已修复] 修正类型为ImageButton

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_topic);

        authRepository = new AuthRepository(this);
        token = authRepository.getAuthToken();

        int topicIdInt = getIntent().getIntExtra("TOPIC_ID", -1);
        if (topicIdInt == -1) {
            Toast.makeText(this, "无效的话题ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        this.topicId = String.valueOf(topicIdInt);

        initViews();
        initViewModel();
        setupCommentInput();
        setupViewPager();
    }

    private void initViews() {
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
        // [已修复] 使用正确的ID: ivCollect
        ivCollect = findViewById(R.id.ivCollect);

        ivCollect.setOnClickListener(v -> {
            if (token != null && !token.isEmpty()) {
                viewModel.toggleTopicFavorite(token, Integer.parseInt(topicId));
            } else {
                Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);

        viewModel.getTopicDetails().observe(this, apiResponse -> {
            if (apiResponse != null && apiResponse.getCode() == 200) {
                updateTopicUI(apiResponse.getData());
            } else {
                Toast.makeText(this, "加载话题详情失败", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getCommentPostResult().observe(this, apiResponse -> {
            if (apiResponse == null) return;
            if (apiResponse.getCode() == 200) {
                Toast.makeText(this, "评论发送成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "评论发送失败，请重试", Toast.LENGTH_SHORT).show();
            }
            viewModel.resetCommentPostResult(); 
        });

        // 观察收藏状态切换的结果
        viewModel.getToggleFavoriteResult().observe(this, apiResponse -> {
             if (apiResponse != null && apiResponse.getCode() == 200) {
                Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show();
                // 刷新UI的操作已在ViewModel中通过重新获取详情实现
            } else if(apiResponse != null) {
                Toast.makeText(this, "操作失败", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.fetchTopicDetails(token, Integer.parseInt(topicId));
    }

    private void updateTopicUI(NewsArticle article) {
        // [已修复] 使用正确的ID: tv_title
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(article.getTitle());
        
        // [已修复] 根据isFavorite字段更新心形图标的状态
        // 假设您的drawable 'ic_topic_unselected' 和 'ic_topic_selected' 存在
        if (article.isFavorite()) {
            ivCollect.setImageResource(R.drawable.ic_topic_selected);
        } else {
            ivCollect.setImageResource(R.drawable.ic_topic_unselected);
        }
    }

    private void setupCommentInput() {
        LinearLayout llCommentInput = findViewById(R.id.ll_comment_input);
        llCommentInput.setOnClickListener(v -> showCommentInputDialog("发表评论...", 0, 1));
    }

    private void setupViewPager() {
        viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        if (viewPager != null && tabLayout != null) {
            DetailTopicPagerAdapter pagerAdapter = new DetailTopicPagerAdapter(this, Integer.parseInt(topicId), token);
            viewPager.setAdapter(pagerAdapter);
            viewPager.setCurrentItem(0);
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                tab.setText(position == 0 ? "资讯" : "评论");
            }).attach();
        }
    }

    @Override
    public void onShowCommentInput(String hint, int parentId) {
        showCommentInputDialog(hint, parentId, 2);
    }

    public void showCommentInputDialog(String hint, int parentId, int status) {
        this.currentReplyParentId = parentId;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_comment_input, null);
        builder.setView(dialogView);
        commentDialog = builder.create();

        Window window = commentDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        initDialogViews(dialogView, hint, status);
        commentDialog.show();
        showKeyboard();
    }

    private void initDialogViews(View dialogView, String hint, int status) {
        etCommentInput = dialogView.findViewById(R.id.et_comment_input);
        ImageButton btnSend = dialogView.findViewById(R.id.btn_send);
        etCommentInput.setHint(hint);

        btnSend.setOnClickListener(v -> {
            String commentText = etCommentInput.getText().toString().trim();
            if (!commentText.isEmpty()) {
                submitComment(commentText, status);
                hideKeyboard();
                commentDialog.dismiss();
            } else {
                Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitComment(String commentText, int status) {
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        Comment newComment = new Comment();
        newComment.setComment(commentText);
        newComment.setParentId(currentReplyParentId);
        newComment.setStatus(status);

        viewModel.postComment(token, topicId, newComment);
    }

    private void showKeyboard() {
        if (etCommentInput != null) {
            etCommentInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etCommentInput, InputMethodManager.SHOW_IMPLICIT);
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
}
