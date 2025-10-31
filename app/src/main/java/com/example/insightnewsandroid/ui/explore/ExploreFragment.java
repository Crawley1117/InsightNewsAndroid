// ui/explore/ExploreFragment.java
package com.example.insightnewsandroid.ui.explore;

import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.databinding.FragmentExploreBinding;
import com.example.insightnewsandroid.ui.explore.model.ExploreTopic;
import com.example.insightnewsandroid.ui.detail.DetailTopicActivity;
import com.example.insightnewsandroid.ui.profile.TopicCollectionActivity;

public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;
    private ExploreViewModel viewModel;
    private ExploreTopicAdapter topicAdapter;

    private String token;
    private int currentCategory = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 获取token
        token = getTokenFromSharedPrefs();

        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(ExploreViewModel.class);

        setupViews();
        setupObservers();
        loadData();

        // 初始化分类选择状态
        initializeCategorySelection();
    }

    private void setupViews() {

        // 设置RecyclerView
        topicAdapter = new ExploreTopicAdapter();
        binding.rvTopics.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTopics.setAdapter(topicAdapter);

        // 设置收藏点击 - 修改为跳转到话题收藏页面
        binding.ivCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到话题收藏页面
                Intent intent = new Intent(getActivity(), TopicCollectionActivity.class);
                startActivity(intent);
            }
        });

        // 设置搜索点击
        binding.ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "搜索功能开发中", Toast.LENGTH_SHORT).show();
            }
        });

        // 设置真相侦探图片点击
        binding.ivBingjian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "冰鉴-真相鉴定功能开发中", Toast.LENGTH_SHORT).show();
            }
        });

        binding.ivLingxin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "灵心功能开发中", Toast.LENGTH_SHORT).show();
            }
        });

        binding.ivNuanyang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "暖阳功能开发中", Toast.LENGTH_SHORT).show();
            }
        });

        // 设置分类点击
        setupCategoryClicks();

        // 设置话题点击
        topicAdapter.setOnItemClickListener(new ExploreTopicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ExploreTopic topic) {
                // 跳转到详情页面
                onTopicItemClick(topic);
            }
        });
    }

    private void onTopicItemClick(ExploreTopic topic) {
        Intent intent = new Intent(getActivity(), DetailTopicActivity.class);
        intent.putExtra("TOPIC_ID", topic.getId());
        intent.putExtra("TOPIC_TITLE", topic.getTitle());
        intent.putExtra("TOPIC_CONTENT", topic.getContent());
        intent.putExtra("TOPIC_CATEGORY", topic.getTopic());
        intent.putExtra("TOPIC_FOLLOW_COUNT", topic.getFollows());
        intent.putExtra("TOPIC_IMAGE_URL", topic.getThumbPhotoURL());
        startActivity(intent);
    }

    private void initializeCategorySelection() {
        // 重置所有分类样式
        resetAllCategoryStyles();

        // 设置"全部"为选中状态
        setCategorySelected(binding.tvAll);

        // 移动指示器到"全部"位置
        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                float indicatorOffset = calculateOffsetByTextWidth(0);
                moveIndicator(indicatorOffset);
            }
        });

        viewModel.fetchHotTopics(token, "");
    }

    private void setupCategoryClicks() {
        View.OnClickListener categoryClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = 0;
                String category = "";

                if (v.getId() == R.id.tvAll) {
                    position = 0;
                    category = ""; // 空字符串表示全部
                } else if (v.getId() == R.id.tvPolitics) {
                    position = 1;
                    category = "政治";
                } else if (v.getId() == R.id.tvSociety) {
                    position = 2;
                    category = "社会";
                } else if (v.getId() == R.id.tvTech) {
                    position = 3;
                    category = "科技";
                } else if (v.getId() == R.id.tvCulture) {
                    position = 4;
                    category = "文化";
                } else if (v.getId() == R.id.tvEconomy) {
                    position = 5;
                    category = "经济";
                }

                updateCategorySelection(position, category, (TextView) v);
            }
        };

        binding.tvAll.setOnClickListener(categoryClickListener);
        binding.tvPolitics.setOnClickListener(categoryClickListener);
        binding.tvSociety.setOnClickListener(categoryClickListener);
        binding.tvTech.setOnClickListener(categoryClickListener);
        binding.tvCulture.setOnClickListener(categoryClickListener);
        binding.tvEconomy.setOnClickListener(categoryClickListener);
    }

    private void updateCategorySelection(int position, String category, TextView selectedView) {
        currentCategory = position;

        resetAllCategoryStyles();
        setCategorySelected(selectedView);

        // 基于文本宽度计算
        float indicatorOffset = calculateOffsetByTextWidth(position);
        moveIndicator(indicatorOffset);

        viewModel.fetchHotTopics(token, category);
    }

    private float calculateOffsetByTextWidth(int position) {
        // 分类文本
        String[] categories = {"全部", "政治", "社会", "科技", "文化", "经济"};

        // 基础偏移 - 减小这个值让整体往左移
        float baseOffset = 1f; // 从7f减小到2f
        float currentOffset = baseOffset;

        // 文本测量
        TextView tempView = binding.tvAll; // 借用任意TextView获取paint
        android.text.TextPaint paint = tempView.getPaint();

        for (int i = 0; i <= position; i++) {
            if (i < categories.length) {
                // 测量文本宽度
                float textWidth = paint.measureText(categories[i]);
                // 加上内边距（左右各8dp）
                float itemWidth = textWidth + dipToPx(16f);

                if (i < position) {
                    currentOffset += itemWidth;
                } else {
                    // 当前选中项，使用中心位置
                    currentOffset += itemWidth / 2f;
                }
            }
        }

        // 减去指示器宽度的一半，再额外减去一些让整体左移
        return currentOffset - (binding.indicator.getWidth() / 2f) - dipToPx(5f);
    }

    private float dipToPx(float dip) {
        return dip * getResources().getDisplayMetrics().density;
    }

    private void calculateImmediateOffset(int position) {
        // 预定义的每个分类项的近似宽度（包括间距）
        float itemWidth = 60f; // 根据实际情况调整

        // 基础偏移量
        float baseOffset = 7f;

        // 计算位置
        float indicatorX = baseOffset + (position * itemWidth);

        moveIndicator(indicatorX);
    }

    private float calculateOffsetByPosition(int position) {
        // 基础偏移量 - 根据实际布局调整这些值
        float[] offsets = {7f, 46f, 85f, 122f, 161f, 198f};

        if (position >= 0 && position < offsets.length) {
            return offsets[position];
        }
        return 7f; // 默认值
    }

    private float getPadding(TextView textView) {
        // 估算内边距，根据实际布局调整
        return 16f; // 8dp * 2
    }

    private void setCategorySelected(TextView textView) {
        int selectedColor = getResources().getColor(R.color.category_selected);
        textView.setTextColor(selectedColor);
        textView.setAlpha(1f);
        textView.setTextSize(16);
    }

    private void resetAllCategoryStyles() {
        int defaultColor = getResources().getColor(R.color.black);

        binding.tvAll.setTextColor(defaultColor);
        binding.tvAll.setAlpha(0.75f);
        binding.tvAll.setTextSize(14);

        binding.tvPolitics.setTextColor(defaultColor);
        binding.tvPolitics.setAlpha(0.75f);
        binding.tvPolitics.setTextSize(14);

        binding.tvSociety.setTextColor(defaultColor);
        binding.tvSociety.setAlpha(0.75f);
        binding.tvSociety.setTextSize(14);

        binding.tvTech.setTextColor(defaultColor);
        binding.tvTech.setAlpha(0.75f);
        binding.tvTech.setTextSize(14);

        binding.tvCulture.setTextColor(defaultColor);
        binding.tvCulture.setAlpha(0.75f);
        binding.tvCulture.setTextSize(14);

        binding.tvEconomy.setTextColor(defaultColor);
        binding.tvEconomy.setAlpha(0.75f);
        binding.tvEconomy.setTextSize(14);
    }

    private void moveIndicator(float translationX) {
        binding.indicator.animate()
                .translationX(translationX)
                .setDuration(200)
                .start();
    }

    private void setupObservers() {
        viewModel.getHomeTopics().observe(getViewLifecycleOwner(), topics -> {
            topicAdapter.submitList(topics);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getCollectDataLoaded().observe(getViewLifecycleOwner(), loaded -> {
            if (loaded) {
                Toast.makeText(getContext(), "收藏数据加载完成", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        if (token != null) {
            // 首次加载使用空字符串（全部分类）
            viewModel.fetchHotTopics(token, "");
        } else {
            Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
        }
    }

    private String whichTopicString(int whichTopic) {
        switch (whichTopic) {
            case 0: return "";
            case 1: return "政治";
            case 2: return "社会";
            case 3: return "科技";
            case 4: return "文化";
            case 5: return "经济";
            default: return "";
        }
    }

    private String getTokenFromSharedPrefs() {
        if (getContext() == null) return null;
        android.content.SharedPreferences sharedPref = getContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        return sharedPref.getString("token", null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (token != null) {
            viewModel.fetchHotTopics(token, whichTopicString(currentCategory));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
