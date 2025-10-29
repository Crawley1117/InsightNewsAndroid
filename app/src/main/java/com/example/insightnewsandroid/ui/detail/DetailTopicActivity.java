package com.example.insightnewsandroid.ui.detail;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.databinding.ActivityDetailTopicBinding;
import com.example.insightnewsandroid.data.model.Topic;
import com.example.insightnewsandroid.data.manager.TopicCollectionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DetailTopicActivity extends AppCompatActivity {

    private static final String TAG = "DetailTopicActivity";

    private ActivityDetailTopicBinding binding;
    private DetailTopicViewModel viewModel;
    private DetailTopicPagerAdapter pagerAdapter;
    private TopicCollectionManager topicCollectionManager;

    private int topicId;
    private String topicTitle;
    private String topicContent;
    private String topicCategory;
    private int topicFollowCount;
    private String topicImageUrl;

    private boolean isCollect = false;
    private String token;
    private Topic currentTopic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailTopicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d(TAG, "onCreate: 开始初始化话题详情页");

        // 获取传递的话题数据
        topicId = getIntent().getIntExtra("TOPIC_ID", 0);
        topicTitle = getIntent().getStringExtra("TOPIC_TITLE");
        topicContent = getIntent().getStringExtra("TOPIC_CONTENT");
        topicCategory = getIntent().getStringExtra("TOPIC_CATEGORY");
        topicFollowCount = getIntent().getIntExtra("TOPIC_FOLLOW_COUNT", 0);
        topicImageUrl = getIntent().getStringExtra("TOPIC_IMAGE_URL");

        Log.d(TAG, "接收到的数据 - ID: " + topicId +
                ", 标题: " + topicTitle +
                ", 分类: " + topicCategory +
                ", 关注数: " + topicFollowCount);

        // 创建话题对象
        currentTopic = new Topic(topicId, topicTitle, topicContent, topicCategory, topicFollowCount, topicImageUrl);

        // 初始化话题收藏管理器
        topicCollectionManager = TopicCollectionManager.getInstance(this);
        Log.d(TAG, "话题收藏管理器初始化完成");

        // 临时：设置一个模拟的token，让收藏功能可以工作
        token = "mock_token_for_testing";
        Log.d(TAG, "使用模拟token: " + token);

        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(DetailTopicViewModel.class);

        // 设置应用上下文
        viewModel.setAppContext(getApplicationContext());

        Log.d(TAG, "onCreate: ViewModel初始化完成");

        initView();
        initObserver();
        loadData();
    }

    private void initView() {
        Log.d(TAG, "initView: 开始初始化视图");

        // 设置Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // 设置ViewPager和TabLayout
        setupViewPager();

        // 收藏按钮点击事件 - 使用真正的收藏功能
        if (binding.ivCollect != null) {
            Log.d(TAG, "initView: 收藏按钮找到，设置点击监听器");

            // 初始化收藏状态
            isCollect = topicCollectionManager.isTopicCollected(topicId);
            updateCollectButton();

            binding.ivCollect.setOnClickListener(v -> {
                Log.d(TAG, "收藏按钮被点击, topicId=" + topicId + ", 当前状态: " + isCollect);

                // 使用真正的收藏功能
                toggleTopicCollection();
            });

            // 设置按钮为可点击
            binding.ivCollect.setClickable(true);
            binding.ivCollect.setFocusable(true);
        } else {
            Log.e(TAG, "initView: 收藏按钮未找到！");
        }

        // 评论输入框
        setupCommentInput();

        Log.d(TAG, "initView: 视图初始化完成");
    }

    /**
     * 切换话题收藏状态 - 真正的收藏功能
     */
    private void toggleTopicCollection() {
        Log.d(TAG, "toggleTopicCollection: 切换收藏状态");

        boolean success = topicCollectionManager.toggleTopicCollection(currentTopic);

        if (success) {
            // 获取新的收藏状态
            isCollect = topicCollectionManager.isTopicCollected(topicId);
            updateCollectButton();

            String message = isCollect ? "收藏成功" : "取消收藏";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            Log.d(TAG, "收藏操作成功，新状态: " + isCollect);
        } else {
            Log.e(TAG, "收藏操作失败");
            Toast.makeText(this, "操作失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupViewPager() {
        Log.d(TAG, "setupViewPager: 设置ViewPager");

        pagerAdapter = new DetailTopicPagerAdapter(this, topicId, token);
        binding.viewPager.setAdapter(pagerAdapter);

        // 连接TabLayout和ViewPager
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("新闻");
                    } else {
                        tab.setText("评论区");
                    }
                }
        ).attach();

        // Tab切换监听
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "Tab选中: position=" + tab.getPosition());
                // 切换到评论区时显示输入框，切换到新闻时隐藏
                if (tab.getPosition() == 1) {
                    binding.llCommentInput.setVisibility(View.VISIBLE);
                    Log.d(TAG, "显示评论输入框");
                } else {
                    binding.llCommentInput.setVisibility(View.GONE);
                    Log.d(TAG, "隐藏评论输入框");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupCommentInput() {
        Log.d(TAG, "setupCommentInput: 设置评论输入框");

        // 评论输入监听
        binding.etComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.toString().trim().length() > 0;
                binding.btnSend.setVisibility(hasText ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 发送评论 - 也不检查token
        binding.btnSend.setOnClickListener(v -> {
            String comment = binding.etComment.getText().toString().trim();
            Log.d(TAG, "发送评论点击: comment=" + comment);
            if (!comment.isEmpty()) {
                viewModel.addComment(topicId, comment);  // 只传递 topicId 和 comment
                binding.etComment.setText("");
            } else {
                Toast.makeText(this, "评论内容不能为空", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initObserver() {
        Log.d(TAG, "initObserver: 开始设置观察者");

        // 监听评论发送
        viewModel.getCommentSuccess().observe(this, success -> {
            Log.d(TAG, "收到评论发送结果: success=" + success);
            if (success) {
                Toast.makeText(this, "评论成功", Toast.LENGTH_SHORT).show();
                // 刷新评论区
                if (pagerAdapter != null) {
                    pagerAdapter.refreshComments();
                }
            }
        });

        // 监听错误信息
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "收到错误信息: " + error);
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        Log.d(TAG, "initObserver: 观察者设置完成");
    }

    private void updateTopicUI() {
        Log.d(TAG, "updateTopicUI: 更新话题UI");

        // 根据类别设置不同的标题
        String displayTitle = getDisplayTitleByCategory(topicCategory, topicTitle);
        binding.tvTitle.setText(displayTitle);

        // 使用 content 字段作为描述，如果没有就使用 title
        String description = topicContent != null && !topicContent.isEmpty()
                ? topicContent
                : topicTitle;
        binding.tvDescription.setText(description);

        binding.tvCategory.setText(topicCategory);
        binding.tvFollowCount.setText(topicFollowCount + "人关注");

        // 加载头部图片
        if (topicImageUrl != null && !topicImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(topicImageUrl)
                    .placeholder(R.drawable.topic_background)
                    .into(binding.ivHeader);
        } else {
            binding.ivHeader.setImageResource(R.drawable.topic_background);
        }

        // 根据类别设置不同的样式
        setCategoryStyle(topicCategory);

        Log.d(TAG, "updateTopicUI: 话题UI更新完成");
    }

    /**
     * 根据类别返回不同的标题格式
     */
    private String getDisplayTitleByCategory(String category, String originalTitle) {
        if (category == null) return originalTitle;

        switch (category) {
            case "政治":
                return "【政治聚焦】" + originalTitle;
            case "经济":
                return "💰 " + originalTitle;
            case "文化":
                return "🎭 " + originalTitle;
            case "科技":
                return "🚀 " + originalTitle;
            case "体育":
                return "⚽ " + originalTitle;
            case "娱乐":
                return "🎬 " + originalTitle;
            case "社会":
                return "🏘️ " + originalTitle;
            default:
                return originalTitle;
        }
    }

    private void setCategoryStyle(String category) {
        int textColor = getCategoryTextColor(category);
        int bgColor = getCategoryBgColor(category);

        binding.tvCategory.setTextColor(textColor);
        binding.tvCategory.setBackgroundColor(bgColor);
    }

    private int getCategoryTextColor(String category) {
        switch (category) {
            case "政治": return ContextCompat.getColor(this, R.color.white);
            case "经济": return ContextCompat.getColor(this, R.color.white);
            case "文化": return ContextCompat.getColor(this, R.color.white);
            case "科技": return ContextCompat.getColor(this, R.color.white);
            case "社会": return ContextCompat.getColor(this, R.color.white);
            default: return ContextCompat.getColor(this, R.color.gray);
        }
    }

    private int getCategoryBgColor(String category) {
        switch (category) {
            case "政治": return ContextCompat.getColor(this, R.color.red);
            case "经济": return ContextCompat.getColor(this, R.color.green);
            case "文化": return ContextCompat.getColor(this, R.color.blue);
            case "科技": return ContextCompat.getColor(this, R.color.purple_light);
            case "社会": return ContextCompat.getColor(this, R.color.orange);
            default: return ContextCompat.getColor(this, R.color.gray_light);
        }
    }

    /**
     * 更新收藏按钮状态
     */
    private void updateCollectButton() {
        Log.d(TAG, "updateCollectButton: 更新收藏按钮，当前状态=" + isCollect);

        int resId = isCollect ? R.drawable.ic_topic_selected : R.drawable.ic_topic_unselected;
        Log.d(TAG, "设置收藏按钮图片资源: " + resId);

        // 检查图片资源是否存在
        try {
            binding.ivCollect.setImageResource(resId);
            Log.d(TAG, "图片资源设置成功");
        } catch (Exception e) {
            Log.e(TAG, "图片资源设置失败: " + e.getMessage());
            // 如果图片资源不存在，使用默认图标
            binding.ivCollect.setImageResource(R.drawable.ic_topic_unselected);
        }

        // 添加点击动画效果
        binding.ivCollect.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(150)
                .withEndAction(() -> binding.ivCollect.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .start())
                .start();

        Log.d(TAG, "updateCollectButton: 收藏按钮更新完成");
    }

    private void loadData() {
        Log.d(TAG, "loadData: 开始加载数据");
        // 直接使用传递过来的数据更新UI，不再调用ViewModel的模拟数据
        updateTopicUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: 重新检查收藏状态");
        // 当从其他页面返回时，重新检查收藏状态
        isCollect = topicCollectionManager.isTopicCollected(topicId);
        updateCollectButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: 销毁Activity");
        binding = null;
    }
}