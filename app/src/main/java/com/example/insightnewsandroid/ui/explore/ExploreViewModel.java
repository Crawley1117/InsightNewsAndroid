// ui/explore/ExploreViewModel.java
package com.example.insightnewsandroid.ui.explore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.insightnewsandroid.ui.explore.model.ExploreTopic;

import java.util.ArrayList;
import java.util.List;

public class ExploreViewModel extends ViewModel {

    private MutableLiveData<List<ExploreTopic>> homeTopics = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> collectDataLoaded = new MutableLiveData<>();

    public LiveData<List<ExploreTopic>> getHomeTopics() {
        return homeTopics;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getCollectDataLoaded() {
        return collectDataLoaded;
    }

    public void fetchHotTopics(String token, String category) {
        isLoading.setValue(true);

        // 模拟异步操作
        new Thread(() -> {
            try {
                Thread.sleep(1000); // 模拟网络延迟

                // 在主线程更新数据
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> {
                    try {
                        List<ExploreTopic> topics;
                        // 空字符串或null表示显示全部话题
                        if (category == null || category.isEmpty()) {
                            topics = getMockTopics(); // 显示所有话题
                        } else {
                            topics = filterTopicsByCategory(getMockTopics(), category);
                        }
                        homeTopics.setValue(topics);
                    } catch (Exception e) {
                        errorMessage.setValue("加载话题失败: " + e.getMessage());
                    } finally {
                        isLoading.setValue(false);
                    }
                });
            } catch (InterruptedException e) {
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> {
                    errorMessage.setValue("加载话题失败: " + e.getMessage());
                    isLoading.setValue(false);
                });
            }
        }).start();
    }

    public void fetchTopicCollect(String token) {
        new Thread(() -> {
            try {
                Thread.sleep(500); // 模拟网络延迟

                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> {
                    collectDataLoaded.setValue(true);
                });
            } catch (InterruptedException e) {
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> {
                    errorMessage.setValue("加载收藏失败: " + e.getMessage());
                });
            }
        }).start();
    }

    private List<ExploreTopic> getMockTopics() {
        List<ExploreTopic> topics = new ArrayList<>();

        topics.add(new ExploreTopic(
                500,
                "别让谣言 '闹海'！《哪吒 2》真假大揭秘",
                "剖析假消息传播，反思观众如何理性守护国产动画。",
                "文化",
                30355,
                "https://insightnews.oss-cn-hangzhou.aliyuncs.com/WechatIMG478.jpg"
        ));

        topics.add(new ExploreTopic(
                501,
                "AI 换脸技术滥用，如何识别虚假视频？",
                "深度解析AI换脸技术原理及防范措施，帮助用户识别虚假内容。",
                "科技",
                15234,
                "https://insightnews.oss-cn-hangzhou.aliyuncs.com/ai_face_thumb.jpg"
        ));

        topics.add(new ExploreTopic(
                502,
                "健康谣言大揭秘：这些养生方法真的有效吗？",
                "科学解析常见健康谣言的真相，提供权威医学建议。",
                "社会",
                28761,
                "https://insightnews.oss-cn-hangzhou.aliyuncs.com/health_thumb.jpg"
        ));

        topics.add(new ExploreTopic(
                503,
                "经济数据解读：如何识别虚假经济报道",
                "教你识别经济新闻中的虚假信息和误导性数据。",
                "经济",
                18945,
                "https://insightnews.oss-cn-hangzhou.aliyuncs.com/economy_thumb.jpg"
        ));

        topics.add(new ExploreTopic(
                504,
                "政治谣言识别指南",
                "分析政治领域常见谣言的传播模式和识别方法。",
                "政治",
                32467,
                "https://insightnews.oss-cn-hangzhou.aliyuncs.com/politics_thumb.jpg"
        ));

        return topics;
    }

    private List<ExploreTopic> filterTopicsByCategory(List<ExploreTopic> topics, String category) {
        // 如果分类为空，返回所有话题
        if (category == null || category.isEmpty()) {
            return topics;
        }

        // 其他分类，只返回对应分类的话题
        List<ExploreTopic> filtered = new ArrayList<>();
        for (ExploreTopic topic : topics) {
            if (category.equals(topic.getTopic())) {
                filtered.add(topic);
            }
        }
        return filtered;
    }
}