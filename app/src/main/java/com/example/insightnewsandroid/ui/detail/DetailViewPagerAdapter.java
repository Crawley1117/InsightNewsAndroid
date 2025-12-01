package com.example.insightnewsandroid.ui.detail;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.HashMap;
import java.util.Map;

public class DetailViewPagerAdapter extends FragmentStateAdapter {

    private final int topicId;
    private final String token;
    // [已修复] 添加一个Map来持有Fragment的引用
    private final Map<Integer, Fragment> createdFragments = new HashMap<>();

    public DetailViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, int topicId, String token) {
        super(fragmentActivity);
        this.topicId = topicId;
        this.token = token;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        if (position == 0) {
            fragment = TopicNewsFragment.newInstance(topicId);
        } else {
            fragment = TopicCommentsFragment.newInstance(topicId, token);
        }
        // [已修复] 在创建时将Fragment登记到Map中
        createdFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2; // 两个tab：新闻和评论区
    }

    // [已修复] 实现缺失的getFragment方法
    public Fragment getFragment(int position) {
        return createdFragments.get(position);
    }
}
