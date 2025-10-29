package com.example.insightnewsandroid.ui.detail;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class DetailTopicPagerAdapter extends FragmentStateAdapter {

    private final int topicId;
    private final String token;

    public DetailTopicPagerAdapter(@NonNull FragmentActivity fragmentActivity, int topicId, String token) {
        super(fragmentActivity);
        this.topicId = topicId;
        this.token = token;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return TopicNewsFragment.newInstance(topicId, token);
        } else {
            return TopicCommentsFragment.newInstance(topicId, token);
        }
    }

    @Override
    public int getItemCount() {
        return 2; // 两个tab：新闻和评论区
    }

    public void refreshComments() {
        // 可以通过EventBus或其他方式通知Fragment刷新
        // 或者通过ViewModel来通知刷新
    }
}