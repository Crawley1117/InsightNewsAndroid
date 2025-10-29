package com.example.insightnewsandroid.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.data.manager.TopicCollectionManager;
import com.example.insightnewsandroid.data.model.Topic;
import com.example.insightnewsandroid.ui.detail.DetailTopicActivity;

import java.util.ArrayList;
import java.util.List;

public class TopicCollectionActivity extends AppCompatActivity {

    private static final String TAG = "TopicCollectionActivity";

    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout topicContainer;
    private TextView tvEmptyState;
    private TopicCollectionManager topicCollectionManager;
    private final Handler handler = new Handler();

    /**
     * 立即诊断测试：快速检查问题所在
     */
    private void runImmediateDiagnosticTest() {
        try {
            Log.d(TAG, "🎯 === 立即诊断测试开始 ===");

            // 1. 检查管理器
            if (topicCollectionManager == null) {
                Log.e(TAG, "❌ 管理器为 null");
                Toast.makeText(this, "话题管理器初始化失败", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(TAG, "✅ 管理器正常");

            // 2. 检查收藏数量
            int count = topicCollectionManager.getCollectionCount();
            Log.d(TAG, "📊 当前收藏数量: " + count);

            // 3. 获取收藏列表
            List<Topic> collectedTopics = topicCollectionManager.getCollectedTopics();
            Log.d(TAG, "📋 收藏列表大小: " + (collectedTopics != null ? collectedTopics.size() : "null"));

            if (collectedTopics != null && !collectedTopics.isEmpty()) {
                Log.d(TAG, "🎉 发现收藏话题:");
                for (int i = 0; i < collectedTopics.size(); i++) {
                    Topic topic = collectedTopics.get(i);
                    Log.d(TAG, "   " + (i + 1) + ". " + topic.getTitle() + " (ID: " + topic.getId() + ")");
                }

                // 如果有收藏，立即显示
                loadCollectedTopics();
            } else {
                Log.d(TAG, "📭 没有收藏话题，添加测试数据");

                // 添加测试话题
                Topic testTopic = new Topic();
                testTopic.setId(10001);
                testTopic.setTitle("测试话题 - 诊断测试");
                testTopic.setContent("这是诊断测试自动添加的话题");
                testTopic.setCategory("测试");
                testTopic.setFollowCount(88);
                testTopic.setImageUrl("https://insightnews.oss-cn-hangzhou.aliyuncs.com/WechatIMG478.jpg");

                boolean success = topicCollectionManager.addToCollection(testTopic);
                Log.d(TAG, "添加测试话题结果: " + success);

                if (success) {
                    // 重新检查
                    int newCount = topicCollectionManager.getCollectionCount();
                    Log.d(TAG, "添加后收藏数量: " + newCount);

                    // 延迟后重新加载显示
                    handler.postDelayed(() -> {
                        Log.d(TAG, "延迟重新加载数据");
                        loadCollectedTopics();
                        Toast.makeText(this, "已添加测试话题，请查看是否显示", Toast.LENGTH_LONG).show();
                    }, 1000);
                }
            }

            Log.d(TAG, "🎯 === 立即诊断测试完成 ===");

        } catch (Exception e) {
            Log.e(TAG, "💥 诊断测试异常", e);
            Toast.makeText(this, "诊断失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d(TAG, "=== TopicCollectionActivity 启动 ===");

            // 步骤1：设置布局
            setContentView(R.layout.activity_topic_collection);
            Log.d(TAG, "步骤1: 布局设置成功");

            // 步骤2：初始化管理器
            topicCollectionManager = TopicCollectionManager.getInstance(this);
            Log.d(TAG, "步骤2: 管理器初始化成功");

            // 步骤3：初始化视图
            initViews();
            Log.d(TAG, "步骤3: 视图初始化成功");

            // 步骤4：设置返回按钮处理
            setupBackPressedHandler();
            Log.d(TAG, "步骤4: 返回按钮处理设置成功");

            // 步骤5：立即运行诊断测试
            Log.d(TAG, "步骤5: 立即运行诊断测试");
            runImmediateDiagnosticTest();

            Log.d(TAG, "=== 页面初始化完成 ===");

        } catch (Exception e) {
            Log.e(TAG, "=== 崩溃详情 ===", e);
            Log.e(TAG, "异常类型: " + e.getClass().getSimpleName());
            Log.e(TAG, "异常信息: " + e.getMessage());
            Log.e(TAG, "页面加载失败", e);

            Toast.makeText(this, "页面加载失败: " + e.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        // 返回按钮
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        // 搜索按钮
        ImageView ivSearch = findViewById(R.id.iv_search);
        ivSearch.setOnClickListener(v -> showSearchDialog());

        // 初始化视图
        swipeRefresh = findViewById(R.id.swipe_refresh);
        topicContainer = findViewById(R.id.ll_topic_container);
        tvEmptyState = findViewById(R.id.tv_empty_state);

        // 设置下拉刷新
        swipeRefresh.setOnRefreshListener(this::loadCollectedTopics);

        // 设置刷新颜色
        swipeRefresh.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        Log.d(TAG, "initViews: 视图初始化完成");
    }

    private void setupBackPressedHandler() {
        // 使用新的 OnBackPressedDispatcher API
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void loadCollectedTopics() {
        try {
            Log.d(TAG, "loadCollectedTopics: 开始加载收藏话题");

            // 步骤1：检查管理器是否初始化成功
            if (topicCollectionManager == null) {
                Log.e(TAG, "topicCollectionManager 为 null");
                Toast.makeText(this, "话题管理器未初始化", Toast.LENGTH_SHORT).show();
                showEmptyState();
                return;
            }

            // 步骤2：获取收藏话题
            Log.d(TAG, "准备调用 getCollectedTopics()");
            List<Topic> collectedTopics = topicCollectionManager.getCollectedTopics();
            Log.d(TAG, "getCollectedTopics() 调用完成，结果: " + (collectedTopics != null ? collectedTopics.size() : "null"));

            // 步骤3：清除现有视图
            topicContainer.removeAllViews();
            Log.d(TAG, "现有视图已清除");

            if (collectedTopics != null && !collectedTopics.isEmpty()) {
                Log.d(TAG, "有收藏话题，数量: " + collectedTopics.size());

                for (int i = 0; i < collectedTopics.size(); i++) {
                    Topic topic = collectedTopics.get(i);
                    Log.d(TAG, "处理第 " + (i + 1) + " 个话题: " + topic.getTitle() + ", ID: " + topic.getId());

                    // 步骤4：创建话题视图
                    View topicView = createTopicView(topic);
                    topicContainer.addView(topicView);
                    Log.d(TAG, "话题视图添加成功: " + topic.getTitle());
                }

                showContentState();
                Log.d(TAG, "所有话题视图添加完成");

            } else {
                Log.d(TAG, "没有收藏的话题或列表为空");
                showEmptyState();
                Toast.makeText(this, "暂无收藏的话题", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "loadCollectedTopics 发生异常", e);
            Toast.makeText(this, "加载收藏失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            showEmptyState();
        } finally {
            swipeRefresh.setRefreshing(false);
        }
    }

    private View createTopicView(Topic topic) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View topicView = inflater.inflate(R.layout.item_topic_collection, topicContainer, false);

        // 绑定数据
        TextView tvTitle = topicView.findViewById(R.id.tv_title);
        TextView tvContent = topicView.findViewById(R.id.tv_content);
        TextView tvTopicTag = topicView.findViewById(R.id.tv_topic_tag);
        TextView tvFollowCount = topicView.findViewById(R.id.tv_follow_count);
        ImageView ivThumb = topicView.findViewById(R.id.iv_thumb);

        tvTitle.setText(topic.getTitle());
        tvContent.setText(topic.getContent());
        tvTopicTag.setText(topic.getCategory());

        // 使用资源字符串格式化关注人数
        String followCountText = getString(R.string.follow_count_format, topic.getFollowCount());
        tvFollowCount.setText(followCountText);

        // 加载图片
        if (topic.getImageUrl() != null && !topic.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(topic.getImageUrl())
                    .placeholder(R.drawable.save_failed)
                    .error(R.drawable.edittext_comment_bg)
                    .into(ivThumb);
        } else {
            // 设置默认图片
            ivThumb.setImageResource(R.drawable.save_failed);
        }

        // 设置点击事件
        topicView.setOnClickListener(v -> {
            // 跳转到话题详情页
            Intent intent = new Intent(TopicCollectionActivity.this, DetailTopicActivity.class);
            intent.putExtra("TOPIC_ID", topic.getId());
            intent.putExtra("TOPIC_TITLE", topic.getTitle());
            intent.putExtra("TOPIC_CONTENT", topic.getContent());
            intent.putExtra("TOPIC_CATEGORY", topic.getCategory());
            intent.putExtra("TOPIC_FOLLOW_COUNT", topic.getFollowCount());
            intent.putExtra("TOPIC_IMAGE_URL", topic.getImageUrl());
            startActivity(intent);
        });

        return topicView;
    }

    private void showContentState() {
        topicContainer.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        topicContainer.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.VISIBLE);
    }

    private void showSearchDialog() {
        // 创建搜索对话框
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("搜索收藏话题");

        // 设置输入框
        final EditText input = new EditText(this);
        input.setHint("输入话题标题或关键词");
        input.setSingleLine();
        builder.setView(input);

        builder.setPositiveButton("搜索", (dialog, which) -> {
            String keyword = input.getText().toString().trim();
            if (!keyword.isEmpty()) {
                searchTopics(keyword);
            } else {
                Toast.makeText(this, "请输入搜索关键词", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void searchTopics(String keyword) {
        List<Topic> collectedTopics = topicCollectionManager.getCollectedTopics();
        List<Topic> filteredTopics = new ArrayList<>();

        if (collectedTopics != null) {
            for (Topic topic : collectedTopics) {
                // 根据标题和内容进行搜索
                if (topic.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                        topic.getContent().toLowerCase().contains(keyword.toLowerCase())) {
                    filteredTopics.add(topic);
                }
            }
        }

        // 更新UI显示搜索结果
        updateTopicList(filteredTopics, keyword);
    }

    private void updateTopicList(List<Topic> topics, String keyword) {
        topicContainer.removeAllViews();

        if (topics != null && !topics.isEmpty()) {
            for (Topic topic : topics) {
                View topicView = createTopicView(topic);
                topicContainer.addView(topicView);
            }

            showContentState();
            String resultText = getString(R.string.search_result_format, topics.size());
            Toast.makeText(this, resultText, Toast.LENGTH_SHORT).show();
        } else {
            showEmptyState();
            String noResultText = getString(R.string.no_search_result_format, keyword);
            Toast.makeText(this, noResultText, Toast.LENGTH_SHORT).show();
        }

        swipeRefresh.setRefreshing(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: 重新加载收藏话题");
        // 当从详情页返回时，重新加载数据（可能取消了收藏）
        loadCollectedTopics();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清除Handler中的回调，避免内存泄漏
        handler.removeCallbacksAndMessages(null);
    }
}