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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.insightnewsandroid.databinding.FragmentTopicNewsBinding;

import java.util.Collections;

public class TopicNewsFragment extends Fragment {

    private FragmentTopicNewsBinding binding;
    private DetailViewModel viewModel;
    private TopicNewsAdapter newsAdapter;
    private int topicId;
    private String token;

    public static TopicNewsFragment newInstance(int topicId) {
        TopicNewsFragment fragment = new TopicNewsFragment();
        Bundle args = new Bundle();
        args.putInt("TOPIC_ID", topicId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            topicId = getArguments().getInt("TOPIC_ID");
            Log.d("TopicNewsFragment", "onCreate: topicId=" + topicId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTopicNewsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("TopicNewsFragment", "onViewCreated: 初始化RecyclerView");

        // 获取token
        if (getActivity() instanceof DetailTopicActivity) {
            DetailTopicActivity activity = (DetailTopicActivity) getActivity();
            token = activity.getToken();
            Log.d("TopicNewsFragment", "获取到token: " + (token != null ? "是" : "否"));
        }

        // 1. 初始化Adapter和RecyclerView
        setupRecyclerView();

        // 2. 获取与Activity共享的ViewModel - 使用DetailViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(DetailViewModel.class);

        // 3. 观察数据变化
        observeTopicDetails();

        // 4. 设置下拉刷新（如果有的话）
        setupSwipeRefresh();
    }

    private void setupRecyclerView() {
        newsAdapter = new TopicNewsAdapter();

        // 正确的ID：news_recycler_view -> newsRecyclerView
        binding.newsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.newsRecyclerView.setAdapter(newsAdapter);
        Log.d("TopicNewsFragment", "RecyclerView设置完成");
    }

    private void setupSwipeRefresh() {
        // 设置下拉刷新监听器
        binding.swipeRefresh.setOnRefreshListener(() -> {
            // 刷新数据
            if (getActivity() instanceof DetailTopicActivity) {
                DetailTopicActivity activity = (DetailTopicActivity) getActivity();
                String refreshToken = activity.getToken();
                Log.d("TopicNewsFragment", "下拉刷新: topicId=" + topicId);
                viewModel.fetchTopicDetails(refreshToken, topicId);
            }

            // 延迟1秒停止刷新，确保数据加载完成
            binding.swipeRefresh.postDelayed(() -> {
                binding.swipeRefresh.setRefreshing(false);
            }, 1000);
        });
    }

    private void observeTopicDetails() {
        viewModel.getTopicDetails().observe(getViewLifecycleOwner(), apiResponse -> {
            Log.d("TopicNewsFragment", "收到话题详情响应");

            // 停止刷新动画
            if (binding.swipeRefresh.isRefreshing()) {
                binding.swipeRefresh.setRefreshing(false);
            }

            if (apiResponse != null) {
                Log.d("TopicNewsFragment", "响应码: " + apiResponse.getCode());
                Log.d("TopicNewsFragment", "响应消息: " + apiResponse.getMsg());

                if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                    Log.d("TopicNewsFragment", "数据不为空，开始处理新闻列表");

                    if (apiResponse.getData().getNews() != null && !apiResponse.getData().getNews().isEmpty()) {
                        Log.d("TopicNewsFragment", "新闻列表数量: " + apiResponse.getData().getNews().size());
                        // 打印第一条新闻的标题作为示例
                        if (apiResponse.getData().getNews().size() > 0) {
                            Log.d("TopicNewsFragment", "第一条新闻标题: " + apiResponse.getData().getNews().get(0).getTitle());
                        }

                        // 提交新闻列表给Adapter
                        newsAdapter.submitList(apiResponse.getData().getNews());

                        Log.d("TopicNewsFragment", "新闻列表已提交给Adapter，应该显示");
                    } else {
                        Log.d("TopicNewsFragment", "新闻列表为空");
                        newsAdapter.submitList(Collections.emptyList());
                    }
                } else {
                    Log.d("TopicNewsFragment", "响应失败或数据为空");
                    newsAdapter.submitList(Collections.emptyList());
                }
            } else {
                Log.d("TopicNewsFragment", "apiResponse为null");
                newsAdapter.submitList(Collections.emptyList());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d("TopicNewsFragment", "onDestroyView");
    }
}