package com.example.insightnewsandroid.ui.detail;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.auth.AuthRepository;
import com.example.insightnewsandroid.data.model.ApiResponse;
import com.example.insightnewsandroid.data.model.Comment;
import com.example.insightnewsandroid.data.model.NewsArticle;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DetailTopicActivity extends AppCompatActivity implements TopicCommentsFragment.OnCommentInputListener {

    private DetailViewModel viewModel;
    private String token;
    private int topicId;
    private NewsArticle currentArticle;

    private Toolbar toolbar;
    private TextView tvTitle;
    private TextView tvDescription;
    private TextView tvCategory;
    private TextView tvFollowCount;
    private ImageView ivHeader;
    private ImageButton ivCollect;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    private EditText etComment;
    private ImageView ivSendComment;
    private LinearLayout llRealCommentInput;
    private LinearLayout llCommentInputHint;
    private int replyingToCommentId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_topic);

        initViews();
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // 获取token
        AuthRepository authRepository = new AuthRepository(this);
        token = authRepository.getAuthToken();

        // 如果token为空，使用默认token（仅用于测试）
        if (token == null || token.isEmpty()) {
            token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiJ5dWFuYWhhbzU3NEBnbWFpbC5jb20ifQ.dzfl_VKTVjxLvhBzmzLAENzxqL46xN6f9A6-fY8OqXw";
            Log.w("DetailTopicActivity", "使用默认token，请确保用户已登录");
        }

        topicId = getIntent().getIntExtra("TOPIC_ID", -1);
        if (topicId == -1) {
            Toast.makeText(this, "话题ID无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);
        observeViewModel();
        viewModel.fetchTopicDetails(token, topicId);

        setupViewPagerAndTabs();
        setupFavoriteButton();
        setupCommentInput();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.tv_title);
        tvDescription = findViewById(R.id.tv_description);
        tvCategory = findViewById(R.id.tv_category);
        tvFollowCount = findViewById(R.id.tv_follow_count);
        ivHeader = findViewById(R.id.iv_header);
        ivCollect = findViewById(R.id.ivCollect);
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        llRealCommentInput = findViewById(R.id.ll_real_comment_input);
        llCommentInputHint = findViewById(R.id.ll_comment_input);
        etComment = findViewById(R.id.et_comment);
        ivSendComment = findViewById(R.id.iv_send_comment);
    }

    private void observeViewModel() {
        viewModel.getTopicDetails().observe(this, apiResponse -> {
            if (apiResponse != null && apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                currentArticle = apiResponse.getData();
                updateUiWithArticle(currentArticle);
            } else {
                String errorMsg = "加载话题详情失败";
                if (apiResponse != null && apiResponse.getMsg() != null) {
                    errorMsg = apiResponse.getMsg();
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getToggleFavoriteResult().observe(this, apiResponse -> {
            if (apiResponse == null || apiResponse.getCode() != 200) {
                Toast.makeText(this, "操作失败，请重试", Toast.LENGTH_SHORT).show();
                if (currentArticle != null) {
                    currentArticle.setFavorited(!currentArticle.isFavorited());
                    updateFavoriteIcon(currentArticle.isFavorited());
                }
            }
        });

        viewModel.getCommentPostResult().observe(this, apiResponse -> {
            if (apiResponse != null) {
                if (apiResponse.getCode() == 200) {
                    Toast.makeText(this, "评论成功", Toast.LENGTH_SHORT).show();
                    resetCommentInput();

                    // 刷新评论列表
                    DetailViewPagerAdapter adapter = (DetailViewPagerAdapter) viewPager.getAdapter();
                    if (adapter != null) {
                        Fragment commentsFragment = adapter.getFragment(1);
                        if (commentsFragment instanceof TopicCommentsFragment) {
                            ((TopicCommentsFragment) commentsFragment).loadComments();
                        }
                    }
                } else {
                    String errorMsg = "评论失败";
                    if (apiResponse.getMsg() != null && !apiResponse.getMsg().isEmpty()) {
                        errorMsg = "评论失败: " + apiResponse.getMsg();
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUiWithArticle(NewsArticle article) {
        tvTitle.setText(article.getTitle());
        tvDescription.setText(article.getContent());
        tvCategory.setText(article.getCategory());

        Integer followCount = article.getAttentionNum();
        tvFollowCount.setText(String.format("%d人关注", followCount == null ? 0 : followCount));

        Glide.with(this)
                .load(article.getImageUrl())
                .placeholder(R.drawable.topic_background)
                .into(ivHeader);

        updateFavoriteIcon(article.isFavorited());
    }

    private void setupViewPagerAndTabs() {
        DetailViewPagerAdapter adapter = new DetailViewPagerAdapter(this, topicId, token);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("新闻");
            } else {
                tab.setText("评论区");
            }
        }).attach();
    }

    private void setupFavoriteButton() {
        ivCollect.setOnClickListener(v -> {
            if (currentArticle == null) return;

            boolean isNowFavorite = !currentArticle.isFavorite();
            currentArticle.setFavorite(isNowFavorite);
            updateFavoriteIcon(isNowFavorite);

            viewModel.toggleTopicFavorite(token, topicId);
        });
    }

    private void updateFavoriteIcon(boolean isFavorite) {
        ivCollect.setImageResource(isFavorite ? R.drawable.ic_topic_selected : R.drawable.ic_topic_unselected);
    }

    private void setupCommentInput() {
        ivSendComment.setOnClickListener(v -> {
            String commentText = etComment.getText().toString().trim();
            if (!commentText.isEmpty()) {
                sendComment(commentText);
            } else {
                Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show();
            }
        });

        llCommentInputHint.setOnClickListener(v -> showRealCommentInput("说点什么...", -1));
    }

    @Override
    public void onShowCommentInput(String hint, int commentId) {
        showRealCommentInput(hint, commentId);
    }

    private void showRealCommentInput(String hint, int commentId) {
        llCommentInputHint.setVisibility(View.GONE);
        llRealCommentInput.setVisibility(View.VISIBLE);
        etComment.setHint(hint);
        replyingToCommentId = commentId;
        etComment.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etComment, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void sendComment(String commentText) {
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        Comment comment = new Comment();
        comment.setComment(commentText);
        comment.setParentId(replyingToCommentId);

        viewModel.postComment(token, String.valueOf(topicId), comment);
    }

    private void resetCommentInput() {
        llRealCommentInput.setVisibility(View.GONE);
        llCommentInputHint.setVisibility(View.VISIBLE);
        etComment.setText("");
        replyingToCommentId = -1;
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
        }
    }

    // 添加getToken方法供Fragment使用
    public String getToken() {
        return token;
    }

    @Override
    public void onBackPressed() {
        if (llRealCommentInput.getVisibility() == View.VISIBLE) {
            resetCommentInput();
        } else {
            super.onBackPressed();
        }
    }
}