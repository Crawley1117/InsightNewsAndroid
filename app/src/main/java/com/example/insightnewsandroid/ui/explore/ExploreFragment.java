package com.example.insightnewsandroid.ui.explore;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.auth.AuthRepository;
import com.example.insightnewsandroid.data.model.NewsArticle;
import com.example.insightnewsandroid.databinding.FragmentExploreBinding;
import com.example.insightnewsandroid.ui.detail.DetailTopicActivity;
import com.example.insightnewsandroid.ui.profile.TopicCollectionActivity;
import com.example.insightnewsandroid.ui.search.SearchActivity;

import java.util.List;

public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;
    private ExploreViewModel viewModel;
    private ExploreTopicAdapter topicAdapter;
    private AuthRepository authRepository;
    private String token;
    private String currentCategory = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authRepository = new AuthRepository(requireContext());
        token = authRepository.getAuthToken();
        viewModel = new ViewModelProvider(this).get(ExploreViewModel.class);

        setupViews();
        setupObservers();
        setupCategoryClicks();

        loadDataForCategory(currentCategory);
    }

    private void setupViews() {
        topicAdapter = new ExploreTopicAdapter();
        binding.rvTopics.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTopics.setAdapter(topicAdapter);

        topicAdapter.setOnItemClickListener(article -> {
            Intent intent = new Intent(getActivity(), DetailTopicActivity.class);
            intent.putExtra("TOPIC_ID", article.getId());
            startActivity(intent);
        });

        // [已修改] 为收藏按钮添加登录验证
        binding.ivCollect.setOnClickListener(v -> {
            if (token != null && !token.isEmpty()) {
                startActivity(new Intent(getActivity(), TopicCollectionActivity.class));
            } else {
                Toast.makeText(getContext(), "请先登录以查看收藏", Toast.LENGTH_SHORT).show();
            }
        });
        
        // 搜索按钮保持不变，因为搜索页面本身会处理登录状态
        binding.ivSearch.setOnClickListener(v -> startActivity(new Intent(getActivity(), SearchActivity.class)));
        
        binding.ivBingjian.setOnClickListener(v -> Toast.makeText(getContext(), "冰鉴-真相鉴定功能开发中", Toast.LENGTH_SHORT).show());
        binding.ivLingxin.setOnClickListener(v -> Toast.makeText(getContext(), "灵心功能开发中", Toast.LENGTH_SHORT).show());
        binding.ivNuanyang.setOnClickListener(v -> Toast.makeText(getContext(), "暖阳功能开发中", Toast.LENGTH_SHORT).show());
    }

    private void setupObservers() {
        viewModel.getHotTopics().observe(getViewLifecycleOwner(), apiResponse -> {
            binding.progressBar.setVisibility(View.GONE);
            if (apiResponse != null && apiResponse.getCode() == 200) {
                List<NewsArticle> articles = apiResponse.getData();
                topicAdapter.submitList(articles);
            } else {
                Toast.makeText(getContext(), "加载话题失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCategoryClicks() {
        View.OnClickListener categoryClickListener = v -> {
            String category = "";
            if (v.getId() == R.id.tvAll) category = ""; 
            else if (v.getId() == R.id.tvPolitics) category = "政治";
            else if (v.getId() == R.id.tvSociety) category = "社会";
            else if (v.getId() == R.id.tvTech) category = "科技";
            else if (v.getId() == R.id.tvCulture) category = "文化";
            else if (v.getId() == R.id.tvEconomy) category = "经济";
            
            currentCategory = category;
            updateCategorySelectionUI((TextView) v);
            loadDataForCategory(category);
        };

        binding.tvAll.setOnClickListener(categoryClickListener);
        binding.tvPolitics.setOnClickListener(categoryClickListener);
        binding.tvSociety.setOnClickListener(categoryClickListener);
        binding.tvTech.setOnClickListener(categoryClickListener);
        binding.tvCulture.setOnClickListener(categoryClickListener);
        binding.tvEconomy.setOnClickListener(categoryClickListener);

        updateCategorySelectionUI(binding.tvAll);
    }

    private void loadDataForCategory(String category) {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.fetchHotTopics(token, category);
    }

    private void updateCategorySelectionUI(TextView selectedView) {
        resetAllCategoryStyles();
        selectedView.setTextColor(ContextCompat.getColor(requireContext(), R.color.category_selected));
        selectedView.setTextSize(16);
    }

    private void resetAllCategoryStyles() {
        int defaultColor = ContextCompat.getColor(requireContext(), R.color.black);
        TextView[] textViews = {binding.tvAll, binding.tvPolitics, binding.tvSociety, binding.tvTech, binding.tvCulture, binding.tvEconomy};
        for (TextView textView : textViews) {
            textView.setTextColor(defaultColor);
            textView.setAlpha(0.75f);
            textView.setTextSize(14);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        token = authRepository.getAuthToken();
        loadDataForCategory(currentCategory);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
