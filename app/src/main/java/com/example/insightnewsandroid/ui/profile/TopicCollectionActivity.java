package com.example.insightnewsandroid.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.insightnewsandroid.auth.AuthRepository;
import com.example.insightnewsandroid.data.model.NewsArticle;
import com.example.insightnewsandroid.databinding.ActivityTopicCollectionBinding;
import com.example.insightnewsandroid.databinding.ItemExploreTopicBinding;
import com.example.insightnewsandroid.ui.detail.DetailTopicActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TopicCollectionActivity extends AppCompatActivity {

    private ActivityTopicCollectionBinding binding;
    private TopicCollectionViewModel viewModel;
    private AuthRepository authRepository;
    
    // [已新增] 用于存储从服务器获取的完整列表和当前显示的列表
    private List<NewsArticle> allFavoriteTopics = new ArrayList<>();
    private List<NewsArticle> currentlyDisplayedTopics = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTopicCollectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authRepository = new AuthRepository(this);
        viewModel = new ViewModelProvider(this).get(TopicCollectionViewModel.class);

        initViews();
        setupObservers();
        loadFavoriteTopics();
    }

    private void initViews() {
        binding.ivBack.setOnClickListener(v -> finish());
        // [已修改] 点击搜索图标时，弹出搜索对话框
        binding.ivSearch.setOnClickListener(v -> showSearchDialog());
    }

    private void setupObservers() {
        viewModel.getFavoriteTopicsDetails().observe(this, articles -> {
            if (articles != null) {
                allFavoriteTopics.clear();
                allFavoriteTopics.addAll(articles);
                updateDisplayedTopics(allFavoriteTopics);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFavoriteTopics() {
        String token = authRepository.getAuthToken();
        if (token != null && !token.isEmpty()) {
            viewModel.fetchFavoriteTopics(token);
        } else {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            updateDisplayedTopics(new ArrayList<>());
        }
    }

    // [已新增] 核心方法：更新UI以显示指定的列表
    private void updateDisplayedTopics(List<NewsArticle> topics) {
        currentlyDisplayedTopics.clear();
        currentlyDisplayedTopics.addAll(topics);

        if (currentlyDisplayedTopics.isEmpty()) {
            binding.tvEmptyState.setVisibility(View.VISIBLE);
            binding.llTopicContainer.setVisibility(View.GONE);
        } else {
            binding.tvEmptyState.setVisibility(View.GONE);
            binding.llTopicContainer.setVisibility(View.VISIBLE);
            addTopicsToLayout(currentlyDisplayedTopics);
        }
    }

    private void addTopicsToLayout(List<NewsArticle> articles) {
        binding.llTopicContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (NewsArticle article : articles) {
            ItemExploreTopicBinding itemBinding = ItemExploreTopicBinding.inflate(inflater, binding.llTopicContainer, false);
            itemBinding.tvTitle.setText(article.getTitle());
            itemBinding.tvContent.setText(article.getContent());
            itemBinding.tvCategory.setVisibility(View.GONE);
            itemBinding.tvFollows.setVisibility(View.GONE);
            itemBinding.tvHashTag.setVisibility(View.GONE);

            itemBinding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(this, DetailTopicActivity.class);
                intent.putExtra("TOPIC_ID", article.getId());
                startActivity(intent);
            });

            binding.llTopicContainer.addView(itemBinding.getRoot());
        }
    }

    // [已新增] 显示搜索对话框的逻辑
    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("搜索收藏话题");

        final EditText input = new EditText(this);
        input.setHint("输入话题标题关键词");
        builder.setView(input);

        builder.setPositiveButton("搜索", (dialog, which) -> {
            String keyword = input.getText().toString().trim();
            if (!keyword.isEmpty()) {
                performLocalSearch(keyword);
            }
        });
        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // [已新增] 执行本地搜索的逻辑
    private void performLocalSearch(String keyword) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            List<NewsArticle> filteredList = allFavoriteTopics.stream()
                    .filter(article -> article.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
            updateDisplayedTopics(filteredList);
        } else {
            List<NewsArticle> filteredList = new ArrayList<>();
            for(NewsArticle article : allFavoriteTopics){
                if(article.getTitle().toLowerCase().contains(keyword.toLowerCase())){
                    filteredList.add(article);
                }
            }
            updateDisplayedTopics(filteredList);
        }
    }
}
