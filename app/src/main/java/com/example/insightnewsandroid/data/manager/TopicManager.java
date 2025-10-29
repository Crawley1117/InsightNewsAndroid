package com.example.insightnewsandroid.data.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.insightnewsandroid.data.model.Topic;
import com.example.insightnewsandroid.data.model.NewsItem;
import com.example.insightnewsandroid.data.model.Comment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopicManager {
    private static final String TAG = "TopicManager";
    private static final String TOPIC_PREFS = "topic_data";
    private static final String KEY_TOPICS = "topics_map";

    private static TopicManager instance;
    private Context context;
    private Map<Integer, Topic> topicsMap;
    private Gson gson;

    private TopicManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();
        loadTopicsFromStorage();
        initializeMockData();
    }

    public static synchronized TopicManager getInstance(Context context) {
        if (instance == null) {
            instance = new TopicManager(context);
        }
        return instance;
    }

    // 加载话题数据
    private void loadTopicsFromStorage() {
        SharedPreferences sharedPref = context.getSharedPreferences(TOPIC_PREFS, Context.MODE_PRIVATE);
        String topicsJson = sharedPref.getString(KEY_TOPICS, null);

        if (topicsJson != null) {
            Type type = new TypeToken<HashMap<Integer, Topic>>(){}.getType();
            topicsMap = gson.fromJson(topicsJson, type);
            Log.d(TAG, "从存储加载话题数据，数量: " + (topicsMap != null ? topicsMap.size() : 0));
        } else {
            topicsMap = new HashMap<>();
            Log.d(TAG, "创建新的话题数据存储");
        }
    }

    // 保存话题数据
    private void saveTopicsToStorage() {
        SharedPreferences sharedPref = context.getSharedPreferences(TOPIC_PREFS, Context.MODE_PRIVATE);
        String topicsJson = gson.toJson(topicsMap);
        sharedPref.edit().putString(KEY_TOPICS, topicsJson).apply();
        Log.d(TAG, "话题数据已保存到存储");
    }

    // 初始化模拟数据
    private void initializeMockData() {
        if (topicsMap.isEmpty()) {
            Log.d(TAG, "初始化模拟话题数据");

            // 话题1: 哪吒2谣言
            Topic topic1 = new Topic(500,
                    "别让谣言 '闹海'！《哪吒 2》真假大揭秘",
                    "剖析假消息传播，反思观众如何理性守护国产动画。",
                    "文化", 30355,
                    "https://insightnews.oss-cn-hangzhou.aliyuncs.com/WechatIMG478.jpg");
            initializeTopic1News(topic1);
            initializeTopic1Comments(topic1);

            // 话题2: AI换脸技术
            Topic topic2 = new Topic(501,
                    "AI 换脸技术滥用，如何识别虚假视频？",
                    "深度解析AI换脸技术原理及防范措施，帮助用户识别虚假内容。",
                    "科技", 15234,
                    "https://insightnews.oss-cn-hangzhou.aliyuncs.com/ai_face_thumb.jpg");
            initializeTopic2News(topic2);
            initializeTopic2Comments(topic2);

            // 话题3: 健康谣言
            Topic topic3 = new Topic(502,
                    "健康谣言大揭秘：这些养生方法真的有效吗？",
                    "科学解析常见健康谣言的真相，提供权威医学建议。",
                    "社会", 28761,
                    "https://insightnews.oss-cn-hangzhou.aliyuncs.com/health_thumb.jpg");
            initializeTopic3News(topic3);

            // 话题4: 经济数据
            Topic topic4 = new Topic(503,
                    "经济数据解读：如何识别虚假经济报道",
                    "教你识别经济新闻中的虚假信息和误导性数据。",
                    "经济", 18945,
                    "https://insightnews.oss-cn-hangzhou.aliyuncs.com/economy_thumb.jpg");
            initializeTopic4News(topic4);

            // 话题5: 政治谣言
            Topic topic5 = new Topic(504,
                    "政治谣言识别指南",
                    "分析政治领域常见谣言的传播模式和识别方法。",
                    "政治", 32467,
                    "https://insightnews.oss-cn-hangzhou.aliyuncs.com/politics_thumb.jpg");
            initializeTopic5News(topic5);

            // 添加到映射
            topicsMap.put(topic1.getId(), topic1);
            topicsMap.put(topic2.getId(), topic2);
            topicsMap.put(topic3.getId(), topic3);
            topicsMap.put(topic4.getId(), topic4);
            topicsMap.put(topic5.getId(), topic5);

            saveTopicsToStorage();
        }
    }

    // 初始化话题1的相关新闻
    private void initializeTopic1News(Topic topic) {
        NewsItem news1 = new NewsItem();
        news1.setId("1001");
        news1.setTitle("《哪吒2》最新预告片发布，视觉效果惊艳");
        news1.setDate("2024-03-26");
        news1.setImageUrl("https://example.com/news1.jpg");
        news1.setViewCount(120);
        news1.setLikeCount(45);
        news1.setCommentCount(30);
        news1.setContent("《哪吒2》最新预告片今日发布，画面精美，特效震撼...");
        news1.setTopicCategory(topic.getCategory());

        NewsItem news2 = new NewsItem();
        news2.setId("1002");
        news2.setTitle("导演谈《哪吒2》创作理念：传承与创新");
        news2.setDate("2024-03-25");
        news2.setImageUrl("https://example.com/news2.jpg");
        news2.setViewCount(89);
        news2.setLikeCount(23);
        news2.setCommentCount(15);
        news2.setContent("导演在接受采访时表示，《哪吒2》在传承经典的同时大胆创新...");
        news2.setTopicCategory(topic.getCategory());

        topic.addNews(news1);
        topic.addNews(news2);
    }

    // 初始化话题1的评论
    private void initializeTopic1Comments(Topic topic) {
        Comment comment1 = new Comment();
        comment1.setId(2001);
        comment1.setUserId(1);
        comment1.setUsername("电影爱好者");
        comment1.setUserImg("https://example.com/avatar1.jpg");
        comment1.setComment("期待这部电影很久了，希望不要被谣言影响！");
        comment1.setTimestamp(System.currentTimeMillis() - 2 * 60 * 60 * 1000); // 2小时前
        comment1.setCreatedAt("2小时前");
        comment1.setLikeCount(15);
        comment1.setLike(true);
        comment1.setMyComment(false);

        Comment comment2 = new Comment();
        comment2.setId(2002);
        comment2.setUserId(2);
        comment2.setUsername("动画迷");
        comment2.setUserImg("https://example.com/avatar2.jpg");
        comment2.setComment("国产动画越来越好了，支持正版，抵制谣言！");
        comment2.setTimestamp(System.currentTimeMillis() - 60 * 60 * 1000); // 1小时前
        comment2.setCreatedAt("1小时前");
        comment2.setLikeCount(8);
        comment2.setLike(false);
        comment2.setMyComment(false);

        topic.addComment(comment1);
        topic.addComment(comment2);
    }

    // 初始化话题2的相关新闻
    private void initializeTopic2News(Topic topic) {
        NewsItem news = new NewsItem();
        news.setId("1003");
        news.setTitle("AI换脸技术安全隐患分析");
        news.setDate("2024-03-24");
        news.setContent("专家分析AI换脸技术可能带来的安全隐患...");
        news.setTopicCategory(topic.getCategory());
        topic.addNews(news);
    }

    // 初始化话题2的评论
    private void initializeTopic2Comments(Topic topic) {
        Comment comment = new Comment();
        comment.setId(2003);
        comment.setUserId(3);
        comment.setUsername("科技达人");
        comment.setUserImg("https://example.com/avatar3.jpg");
        comment.setComment("AI技术发展很快，但也要注意防范滥用。");
        comment.setTimestamp(System.currentTimeMillis() - 3 * 60 * 60 * 1000); // 3小时前
        comment.setCreatedAt("3小时前");
        comment.setLikeCount(12);
        comment.setLike(true);
        comment.setMyComment(false);
        topic.addComment(comment);
    }

    // 初始化话题3的相关新闻
    private void initializeTopic3News(Topic topic) {
        NewsItem news = new NewsItem();
        news.setId("1004");
        news.setTitle("常见健康谣言盘点");
        news.setDate("2024-03-23");
        news.setContent("盘点那些年我们信过的健康谣言...");
        news.setTopicCategory(topic.getCategory());
        topic.addNews(news);
    }

    // 初始化话题4的相关新闻
    private void initializeTopic4News(Topic topic) {
        NewsItem news = new NewsItem();
        news.setId("1005");
        news.setTitle("经济数据解读指南");
        news.setDate("2024-03-22");
        news.setContent("如何正确解读经济数据，避免被误导...");
        news.setTopicCategory(topic.getCategory());
        topic.addNews(news);
    }

    // 初始化话题5的相关新闻
    private void initializeTopic5News(Topic topic) {
        NewsItem news = new NewsItem();
        news.setId("1006");
        news.setTitle("政治谣言识别方法");
        news.setDate("2024-03-21");
        news.setContent("教你几招识别政治谣言的方法...");
        news.setTopicCategory(topic.getCategory());
        topic.addNews(news);
    }

    // 公共方法
    public Topic getTopicById(int topicId) {
        return topicsMap.get(topicId);
    }

    public List<Topic> getAllTopics() {
        return new ArrayList<>(topicsMap.values());
    }

    public List<Topic> getTopicsByCategory(String category) {
        List<Topic> result = new ArrayList<>();
        for (Topic topic : topicsMap.values()) {
            if (category == null || category.isEmpty() || category.equals(topic.getCategory())) {
                result.add(topic);
            }
        }
        return result;
    }

    public void addCommentToTopic(int topicId, Comment comment) {
        Topic topic = topicsMap.get(topicId);
        if (topic != null) {
            topic.addComment(comment);
            saveTopicsToStorage();
            Log.d(TAG, "评论已添加到话题: " + topicId);
        }
    }

    public void updateCommentLikeStatus(int topicId, int commentId, boolean liked, int likeCount) {
        Topic topic = topicsMap.get(topicId);
        if (topic != null) {
            topic.updateCommentLikeStatus(commentId, liked, likeCount);
            saveTopicsToStorage();
            Log.d(TAG, "评论点赞状态已更新: topicId=" + topicId + ", commentId=" + commentId);
        }
    }

    public void incrementTopicViewCount(int topicId) {
        Topic topic = topicsMap.get(topicId);
        if (topic != null) {
            topic.setFollowCount(topic.getFollowCount() + 1);
            saveTopicsToStorage();
        }
    }

    public List<NewsItem> getRelatedNewsForTopic(int topicId) {
        Topic topic = topicsMap.get(topicId);
        return topic != null ? topic.getRelatedNews() : new ArrayList<>();
    }

    public List<Comment> getCommentsForTopic(int topicId) {
        Topic topic = topicsMap.get(topicId);
        return topic != null ? topic.getComments() : new ArrayList<>();
    }

    /**
     * 获取热门话题（按关注数排序）
     */
    public List<Topic> getPopularTopics(int limit) {
        List<Topic> allTopics = getAllTopics();
        allTopics.sort((t1, t2) -> Integer.compare(t2.getFollowCount(), t1.getFollowCount()));

        if (limit > 0 && limit < allTopics.size()) {
            return allTopics.subList(0, limit);
        }
        return allTopics;
    }

    /**
     * 搜索话题
     */
    public List<Topic> searchTopics(String query) {
        List<Topic> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getAllTopics();
        }

        String lowerQuery = query.toLowerCase().trim();
        for (Topic topic : topicsMap.values()) {
            if (topic.getTitle() != null && topic.getTitle().toLowerCase().contains(lowerQuery) ||
                    topic.getContent() != null && topic.getContent().toLowerCase().contains(lowerQuery) ||
                    topic.getCategory() != null && topic.getCategory().toLowerCase().contains(lowerQuery)) {
                results.add(topic);
            }
        }
        return results;
    }

    /**
     * 获取所有分类
     */
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        for (Topic topic : topicsMap.values()) {
            if (topic.getCategory() != null && !categories.contains(topic.getCategory())) {
                categories.add(topic.getCategory());
            }
        }
        return categories;
    }
}