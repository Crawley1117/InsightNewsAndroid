package com.example.insightnewsandroid.ui.detail;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.insightnewsandroid.databinding.FragmentTopicNewsBinding;
import com.example.insightnewsandroid.data.model.NewsItem;

import java.util.ArrayList;
import java.util.List;

public class TopicNewsFragment extends Fragment {

    private FragmentTopicNewsBinding binding;
    private DetailTopicViewModel viewModel;
    private NewsAdapter newsAdapter;

    private int topicId;

    public static TopicNewsFragment newInstance(int topicId, String token) {
        TopicNewsFragment fragment = new TopicNewsFragment();
        Bundle args = new Bundle();
        args.putInt("TOPIC_ID", topicId);
        args.putString("TOKEN", token);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTopicNewsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            topicId = getArguments().getInt("TOPIC_ID");
            // token 不再存储为字段，因为未被使用
        }

        initView();
        initObserver();
        loadNews();
    }

    private void initView() {
        newsAdapter = new NewsAdapter(new ArrayList<>());
        binding.recyclerViewNews.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewNews.setAdapter(newsAdapter);

        // 设置新闻点击监听
        newsAdapter.setOnNewsClickListener(newsItem ->
                // 处理新闻点击事件
                Log.d("TopicNewsFragment", "新闻被点击: " + newsItem.getTitle())
        );

        binding.swipeRefresh.setOnRefreshListener(this::loadNews);
    }

    private void initObserver() {
        viewModel = new ViewModelProvider(requireActivity()).get(DetailTopicViewModel.class);

        viewModel.getRelatedNews().observe(getViewLifecycleOwner(), newsList -> {
            if (newsList != null && !newsList.isEmpty()) {
                newsAdapter.updateNews(newsList);
                binding.tvPlaceholder.setVisibility(View.GONE);
                binding.recyclerViewNews.setVisibility(View.VISIBLE);
            } else {
                binding.tvPlaceholder.setVisibility(View.VISIBLE);
                binding.recyclerViewNews.setVisibility(View.GONE);
            }
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    private void loadNews() {
        if (getContext() == null) return;
        viewModel.loadRelatedNewsForTopic(topicId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}