package com.example.insightnewsandroid.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.data.UserProfileManager;
import com.example.insightnewsandroid.data.model.UserProfile;
import com.example.insightnewsandroid.data.model.NewsItem;
import com.example.insightnewsandroid.data.model.Topic;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class NewsSearchActivity extends AppCompatActivity {

    private RecyclerView rvSearchResults;
    private NewsCollectionAdapter adapter;
    private List<NewsItem> searchResults;
    private List<NewsItem> allNewsList; // 所有新闻数据
    private EditText etSearch;
    private ImageView ivBack;
    private UserProfile currentUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_search);

        // 获取当前用户资料
        currentUserProfile = UserProfileManager.INSTANCE.getCurrentProfile(this);

        initViews();
        setupListeners();
        loadAllNewsData();

        searchResults = new ArrayList<>();
        adapter = new NewsCollectionAdapter(searchResults);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(adapter);

        // 设置适配器点击监听
        adapter.setOnActionListener(new NewsCollectionAdapter.OnActionListener() {
            @Override
            public void onLikeClick(int position) {
                handleLikeClick(position);
            }

            @Override
            public void onDislikeClick(int position) {
                handleDislikeClick(position);
            }
        });
    }

    private void initViews() {
        rvSearchResults = findViewById(R.id.rv_search_results);
        etSearch = findViewById(R.id.et_search);
        ivBack = findViewById(R.id.iv_back);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        // 确定按钮点击事件
        Button btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(v -> {
            performSearch(etSearch.getText().toString());
        });

        // 键盘搜索按钮监听
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            performSearch(etSearch.getText().toString());
            return true;
        });
    }

    private void loadAllNewsData() {
        // 从 UserProfileManager 加载所有新闻数据
        allNewsList = currentUserProfile.getCollectedNews();

        if (allNewsList == null) {
            allNewsList = new ArrayList<>();
        }

        System.out.println("用户: " + currentUserProfile.getUsername());
        System.out.println("收藏新闻数量: " + allNewsList.size());
    }

    private void performSearch(String query) {
        if (TextUtils.isEmpty(query.trim())) {
            Toast.makeText(this, "请输入搜索内容", Toast.LENGTH_SHORT).show();
            return;
        }

        // 每次搜索前重新加载数据，确保获取最新收藏
        reloadCurrentUserData();

        // 清空之前的结果
        searchResults.clear();

        // 执行搜索
        for (NewsItem item : allNewsList) {
            if (item.getTitle().toLowerCase().contains(query.toLowerCase())) {
                searchResults.add(item);
            }
        }

        if (searchResults.isEmpty()) {
            Toast.makeText(this, "未找到相关新闻", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "找到 " + searchResults.size() + " 条相关新闻", Toast.LENGTH_SHORT).show();
        }

        adapter.notifyDataSetChanged();
    }

    private void reloadCurrentUserData() {
        // 重新加载当前用户数据
        currentUserProfile = UserProfileManager.INSTANCE.getCurrentProfile(this);
        allNewsList = currentUserProfile.getCollectedNews();

        if (allNewsList == null) {
            allNewsList = new ArrayList<>();
        }
    }

    private void handleLikeClick(int position) {
        NewsItem item = searchResults.get(position);

        if (item.isLiked()) {
            // 如果已经点赞，点击后变为中立
            item.setLiked(false);
            Toast.makeText(this, "取消点赞", Toast.LENGTH_SHORT).show();
        } else {
            // 如果未点赞，点击后点赞并取消拉踩
            item.setLiked(true);
            item.setDisliked(false);
            Toast.makeText(this, "点赞成功", Toast.LENGTH_SHORT).show();
        }
        adapter.notifyItemChanged(position);
        saveUserNewsData();
    }

    private void handleDislikeClick(int position) {
        NewsItem item = searchResults.get(position);

        if (item.isDisliked()) {
            // 如果已经拉踩，点击后变为中立
            item.setDisliked(false);
            Toast.makeText(this, "取消拉踩", Toast.LENGTH_SHORT).show();
        } else {
            // 如果未拉踩，点击后拉踩并取消点赞
            item.setDisliked(true);
            item.setLiked(false);
            Toast.makeText(this, "已拉踩", Toast.LENGTH_SHORT).show();
        }
        adapter.notifyItemChanged(position);
        saveUserNewsData();
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

            // 创建更新后的用户资料 - 修正参数匹配问题
            UserProfile updatedProfile = new UserProfile(
                    currentProfile.getUserId(),      // 添加 userId
                    currentProfile.getUsername(),    // 用户名
                    currentProfile.getBio(),         // 简介
                    currentProfile.getGender(),      // 性别
                    currentProfile.getAvatarUri(),   // 头像
                    allNewsList,                     // 使用 allNewsList
                    currentProfile.getLikedNews(),   // 点赞的新闻
                    currentProfile.getDislikedNews(),// 点踩的新闻
                    collectedTopics                  // 收藏的话题
            );

            // 保存更新后的资料
            UserProfileManager.INSTANCE.updateProfile(this, updatedProfile);
            currentUserProfile = updatedProfile;

            Toast.makeText(this, "操作已保存", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 当从其他页面返回时，重新加载数据
        reloadCurrentUserData();
    }
}
