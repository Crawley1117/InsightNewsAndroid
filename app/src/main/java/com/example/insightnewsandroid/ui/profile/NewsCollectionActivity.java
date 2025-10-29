package com.example.insightnewsandroid.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.data.model.NewsItem;
import com.example.insightnewsandroid.data.UserProfileManager;
import com.example.insightnewsandroid.data.model.UserProfile;
import com.example.insightnewsandroid.data.model.Topic;

import java.util.ArrayList;
import java.util.List;

public class NewsCollectionActivity extends AppCompatActivity {

    private RecyclerView rvNewsList;
    private NewsCollectionAdapter adapter;
    private List<NewsItem> newsList;
    private UserProfile currentUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_collection);

        // 获取当前用户资料（从持久化存储）
        currentUserProfile = UserProfileManager.INSTANCE.getCurrentProfile(this);

        initViews();
        setupClickListeners();
        loadUserNewsData(); // 加载用户特定的新闻数据
    }

    private void initViews() {
        // 返回按钮
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        // 搜索按钮
        ImageView ivSearch = findViewById(R.id.iv_search);
        ivSearch.setOnClickListener(v -> {
            // 跳转到搜索界面
            Intent intent = new Intent(NewsCollectionActivity.this, NewsSearchActivity.class);
            startActivity(intent);
        });

        // 初始化RecyclerView
        rvNewsList = findViewById(R.id.rv_news_list);
        rvNewsList.setLayoutManager(new LinearLayoutManager(this));

        newsList = new ArrayList<>();
        adapter = new NewsCollectionAdapter(newsList);
        rvNewsList.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // 设置适配器的点击监听器
        adapter.setOnActionListener(new NewsCollectionAdapter.OnActionListener() {
            @Override
            public void onLikeClick(int position) {
                NewsItem item = newsList.get(position);

                if (item.isLiked()) {
                    // 如果已经点赞，点击后变为中立
                    item.setLiked(false);
                    Toast.makeText(NewsCollectionActivity.this, "取消点赞", Toast.LENGTH_SHORT).show();
                } else {
                    // 如果未点赞，点击后点赞并取消拉踩
                    item.setLiked(true);
                    item.setDisliked(false);
                    Toast.makeText(NewsCollectionActivity.this, "点赞成功", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyItemChanged(position);
                saveUserNewsData(); // 保存到用户资料
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
                saveUserNewsData(); // 保存到用户资料
            }
        });
    }

    private void loadUserNewsData() {
        // 直接从当前用户的收藏中加载新闻
        List<NewsItem> userCollectedNews = currentUserProfile.getCollectedNews();

        if (userCollectedNews != null && !userCollectedNews.isEmpty()) {
            newsList.clear();
            newsList.addAll(userCollectedNews);
            adapter.notifyDataSetChanged();

            // 显示用户信息
            String username = currentUserProfile.getUsername();
            int newsCount = userCollectedNews.size();
            Toast.makeText(this, username + " 的收藏 (" + newsCount + "条)", Toast.LENGTH_SHORT).show();
        } else {
            // 如果用户没有收藏任何新闻，显示空状态
            newsList.clear();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "暂无收藏的新闻", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserNewsData() {
        try {
            // 获取当前用户资料
            UserProfile currentProfile = UserProfileManager.INSTANCE.getCurrentProfile(this);

            // 获取当前的收藏话题列表 - 需要先声明和初始化这个变量
            List<Topic> collectedTopics = UserProfileManager.INSTANCE.getCollectedTopics(this);
            if (collectedTopics == null) {
                collectedTopics = new ArrayList<>();
            }

            // 创建更新后的用户资料
            UserProfile updatedProfile = new UserProfile(
                    currentProfile.getUsername(),
                    currentProfile.getBio(),
                    currentProfile.getGender(),
                    currentProfile.getAvatarUri(),
                    newsList,  // 更新新闻收藏
                    currentProfile.getLikedNews(),
                    currentProfile.getDislikedNews(),
                    collectedTopics  // 使用上面定义的变量
            );

            UserProfileManager.INSTANCE.updateProfile(this, updatedProfile);
            currentUserProfile = updatedProfile;

            Toast.makeText(this, "收藏已更新", Toast.LENGTH_SHORT).show();

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
    }
}