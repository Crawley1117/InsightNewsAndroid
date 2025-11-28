package com.example.insightnewsandroid.ui.detail;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.HashMap;
import java.util.Map;

public class DetailTopicPagerAdapter extends FragmentStateAdapter {

    private final int topicId;
    private final String token;
    private Map<Integer, Fragment> fragmentMap = new HashMap<>();

    public DetailTopicPagerAdapter(@NonNull FragmentActivity fragmentActivity, int topicId, String token) {
        super(fragmentActivity);
        this.topicId = topicId;
        this.token = token;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        if (position == 0) {
            fragment = TopicNewsFragment.newInstance(topicId, token);
        } else {
            fragment = TopicCommentsFragment.newInstance(topicId, token);
        }
        fragmentMap.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2; // 两个tab：新闻和评论区
    }

    public Fragment getFragment(int position) {
        return fragmentMap.get(position);
    }

    public void removeFragment(int position) {
        fragmentMap.remove(position);
    }

    public void refreshComments() {
        Fragment commentsFragment = getFragment(1);
        if (commentsFragment instanceof TopicCommentsFragment) {
            ((TopicCommentsFragment) commentsFragment).loadComments();
        }
    }
}
