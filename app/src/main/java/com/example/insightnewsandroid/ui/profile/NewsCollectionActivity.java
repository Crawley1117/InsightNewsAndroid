package com.example.insightnewsandroid.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.data.UserProfileManager;
import com.example.insightnewsandroid.data.model.NewsItem;
import com.example.insightnewsandroid.data.model.Topic;
import com.example.insightnewsandroid.data.model.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class NewsCollectionActivity extends AppCompatActivity {

    private RecyclerView rvNewsList;
    private NewsCollectionAdapter adapter;
    private List<NewsItem> newsList;
    private UserProfile currentUserProfile;
    private boolean dataChanged = false;
    private boolean isSearchMode = false;
    private String currentSearchKeyword = "";

    // 记录哪些新闻被取消了收藏（用于误触恢复）
    private List<NewsItem> temporarilyUncollectedNews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_collection);

        // 获取当前用户资料
        currentUserProfile = UserProfileManager.INSTANCE.getCurrentProfile(this);

        initViews();
        setupClickListeners();
        loadUserNewsData();
    }

    private void initViews() {
        // 返回按钮
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> {
            if (isSearchMode) {
                // 如果是搜索模式，退出搜索模式
                exitSearchMode();
            } else {
                // 正常退出
                handleExit();
            }
        });

        // 搜索按钮
        ImageView ivSearch = findViewById(R.id.iv_search);
        ivSearch.setOnClickListener(v -> {
            showSearchDialog();
        });

        // 初始化RecyclerView
        rvNewsList = findViewById(R.id.rv_news_list);
        rvNewsList.setLayoutManager(new LinearLayoutManager(this));

        newsList = new ArrayList<>();
        adapter = new NewsCollectionAdapter(newsList);
        rvNewsList.setAdapter(adapter);
    }

    private void showSearchDialog() {
        // 创建搜索对话框
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("搜索收藏新闻");

        // 设置输入框
        final EditText input = new EditText(this);
        input.setHint("输入新闻标题关键词");
        input.setSingleLine();
        builder.setView(input);

        builder.setPositiveButton("搜索", (dialog, which) -> {
            String keyword = input.getText().toString().trim();
            if (!keyword.isEmpty()) {
                searchNews(keyword);
            } else {
                Toast.makeText(this, "请输入搜索关键词", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void searchNews(String keyword) {
        List<NewsItem> collectedNews = UserProfileManager.INSTANCE.getCollectedNews(this);
        List<NewsItem> filteredNews = new ArrayList<>();

        if (collectedNews != null) {
            for (NewsItem news : collectedNews) {
                // 根据标题进行搜索
                if (news.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                    filteredNews.add(news);
                }
            }
        }

        // 更新UI显示搜索结果
        updateNewsListForSearch(filteredNews, keyword);
    }

    private void updateNewsListForSearch(List<NewsItem> filteredNews, String keyword) {
        isSearchMode = true;
        currentSearchKeyword = keyword;

        if (filteredNews != null && !filteredNews.isEmpty()) {
            newsList.clear();
            newsList.addAll(filteredNews);
            adapter.notifyDataSetChanged();

            String resultText = "找到 " + filteredNews.size() + " 条相关新闻";
            Toast.makeText(this, resultText, Toast.LENGTH_SHORT).show();
        } else {
            newsList.clear();
            adapter.notifyDataSetChanged();
            String noResultText = "未找到包含 \"" + keyword + "\" 的新闻";
            Toast.makeText(this, noResultText, Toast.LENGTH_SHORT).show();
        }
    }

    private void exitSearchMode() {
        isSearchMode = false;
        currentSearchKeyword = "";
        loadUserNewsData();
        Toast.makeText(this, "已退出搜索模式", Toast.LENGTH_SHORT).show();
    }

    // 新增：处理退出逻辑
    private void handleExit() {
        if (dataChanged) {
            // 从列表中永久移除临时取消收藏的新闻
            List<NewsItem> newsToRemove = new ArrayList<>();
            for (NewsItem news : newsList) {
                if (!news.isCollected()) {
                    newsToRemove.add(news);
                }
            }
            newsList.removeAll(newsToRemove);

            // 保存到持久化存储
            saveUserNewsData();
            Toast.makeText(this, "收藏已更新", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void setupClickListeners() {
        // 设置适配器的点击监听器
        adapter.setOnActionListener(new NewsCollectionAdapter.OnActionListener() {
            @Override
            public void onCollectClick(int position) {
                NewsItem item = newsList.get(position);

                if (item.isCollected()) {
                    // 如果已经收藏，取消收藏（临时状态）
                    item.setCollected(false);
                    adapter.notifyItemChanged(position);
                    Toast.makeText(NewsCollectionActivity.this, "已取消收藏（可点击恢复）", Toast.LENGTH_SHORT).show();

                    // 记录临时取消收藏的新闻
                    if (!temporarilyUncollectedNews.contains(item)) {
                        temporarilyUncollectedNews.add(item);
                    }
                } else {
                    // 如果未收藏（可能是误触恢复），重新收藏
                    item.setCollected(true);
                    adapter.notifyItemChanged(position);
                    Toast.makeText(NewsCollectionActivity.this, "已恢复收藏", Toast.LENGTH_SHORT).show();

                    // 从临时取消收藏列表中移除
                    temporarilyUncollectedNews.remove(item);
                }

                // 标记数据已改变
                dataChanged = true;
            }

            @Override
            public void onLikeClick(int position) {
                NewsItem item = newsList.get(position);

                if (item.isLiked()) {
                    // 如果已经点赞，点击后变为中立
                    item.setLiked(false);
                    item.setLikeCount(Math.max(0, item.getLikeCount() - 1));
                    Toast.makeText(NewsCollectionActivity.this, "取消点赞", Toast.LENGTH_SHORT).show();
                } else {
                    // 如果未点赞，点击后点赞并取消拉踩
                    item.setLiked(true);
                    item.setLikeCount(item.getLikeCount() + 1);
                    item.setDisliked(false);
                    Toast.makeText(NewsCollectionActivity.this, "点赞成功", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyItemChanged(position);
                dataChanged = true;
            }

            @Override
            public void onDislikeClick(int position) {
                NewsItem item = newsList.get(position);

                if (item.isDisliked()) {
                    // 如果已经拉踩，点击后变为中立
                    item.setDisliked(false);
                    Toast.makeText(NewsCollectionActivity.this, "取消拉踩", Toast.LENGTH_SHORT).show();
                } else {
                    // 如果未拉踩，点击后拉踩并取消点赞
                    item.setDisliked(true);
                    item.setLiked(false);
                    Toast.makeText(NewsCollectionActivity.this, "已拉踩", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyItemChanged(position);
                dataChanged = true;
            }

            @Override
            public void onItemClick(int position) {
                // 处理新闻项点击，可以跳转到新闻详情页
                NewsItem item = newsList.get(position);
                Toast.makeText(NewsCollectionActivity.this, "打开新闻详情: " + item.getTitle(), Toast.LENGTH_SHORT).show();

                // 增加浏览数
                item.setViewCount(item.getViewCount() + 1);
                adapter.notifyItemChanged(position);
                dataChanged = true;
            }
        });
    }

    private void loadUserNewsData() {
        // 直接从当前用户的收藏中加载新闻
        List<NewsItem> userCollectedNews = UserProfileManager.INSTANCE.getCollectedNews(this);

        if (userCollectedNews != null && !userCollectedNews.isEmpty()) {
            newsList.clear();
            newsList.addAll(userCollectedNews);
            adapter.notifyDataSetChanged();

            // 显示用户信息
            String username = currentUserProfile.getUsername();
            int newsCount = newsList.size();

            if (isSearchMode && !currentSearchKeyword.isEmpty()) {
                Toast.makeText(this, "搜索结果 (" + newsCount + "条)", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, username + " 的收藏 (" + newsCount + "条)", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 如果用户没有收藏任何新闻，显示空状态
            newsList.clear();
            adapter.notifyDataSetChanged();

            if (isSearchMode && !currentSearchKeyword.isEmpty()) {
                Toast.makeText(this, "未找到相关新闻", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "暂无收藏的新闻", Toast.LENGTH_SHORT).show();
            }
        }

        // 重置数据改变标志（搜索时不需要重置）
        if (!isSearchMode) {
            dataChanged = false;
            temporarilyUncollectedNews.clear();
        }
    }

    private void saveUserNewsData() {
        try {
            // 获取当前用户资料
            UserProfile currentProfile = UserProfileManager.INSTANCE.getCurrentProfile(this);

            // 获取当前的收藏话题列表
            List<Topic> collectedTopics = UserProfileManager.INSTANCE.getCollectedTopics(this);
            if (collectedTopics == null) {
                collectedTopics = new ArrayList<>();
            }

            // 创建更新后的用户资料 - 只包含仍然收藏的新闻
            List<NewsItem> finalNewsList = new ArrayList<>();
            for (NewsItem news : newsList) {
                if (news.isCollected()) {
                    finalNewsList.add(news);
                }
            }

            UserProfile updatedProfile = new UserProfile(
                    currentProfile.getUserId(),
                    currentProfile.getUsername(),
                    currentProfile.getBio(),
                    currentProfile.getGender(),
                    currentProfile.getAvatarUri(),
                    finalNewsList,  // 使用最终收藏列表
                    currentProfile.getLikedNews(),
                    currentProfile.getDislikedNews(),
                    collectedTopics
            );

            UserProfileManager.INSTANCE.updateProfile(this, updatedProfile);
            currentUserProfile = updatedProfile;

            dataChanged = false;
            temporarilyUncollectedNews.clear();

            // 调试日志
            System.out.println("数据已保存，收藏新闻数量: " + finalNewsList.size());

        } catch (Exception e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 当从其他页面返回时，重新加载数据
        currentUserProfile = UserProfileManager.INSTANCE.getCurrentProfile(this);
        loadUserNewsData();

        // 调试日志
        List<NewsItem> currentCollectedNews = UserProfileManager.INSTANCE.getCollectedNews(this);
        System.out.println("重新进入，收藏新闻数量: " + (currentCollectedNews != null ? currentCollectedNews.size() : 0));
    }

    @Override
    public void onBackPressed() {
        if (isSearchMode) {
            // 如果是搜索模式，退出搜索模式
            exitSearchMode();
        } else {
            // 处理物理返回键
            handleExit();
        }
    }
}
