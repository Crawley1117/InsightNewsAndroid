package com.example.insightnewsandroid.data.manager;

import android.content.Context;
import android.util.Log;

import com.example.insightnewsandroid.data.model.Topic;
import com.example.insightnewsandroid.data.UserProfileManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TopicCollectionManager {
    private static final String TAG = "TopicCollectionManager";
    private static final String COLLECTION_PREFS = "topic_collections";
    private static final String COLLECTED_TOPICS_KEY = "collected_topics";
    private static final String COLLECTION_COUNT_KEY = "collection_count";

    private static TopicCollectionManager instance;
    private Context context;
    private Gson gson;

    public static TopicCollectionManager getInstance(Context context) {
        if (instance == null) {
            instance = new TopicCollectionManager(context);
        }
        return instance;
    }

    private TopicCollectionManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();
    }

    /**
     * 获取所有收藏的话题
     */
    public List<Topic> getCollectedTopics() {
        if (context == null) {
            Log.e(TAG, "Context为null，无法获取收藏话题");
            return new ArrayList<>();
        }

        // 从UserProfileManager获取收藏的话题
        return UserProfileManager.INSTANCE.getCollectedTopics(context);
    }

    /**
     * 获取收藏的话题数量
     */
    public int getCollectionCount() {
        if (context == null) {
            Log.e(TAG, "Context为null，无法获取收藏数量");
            return 0;
        }
        return UserProfileManager.INSTANCE.getCollectedTopicsCount(context);
    }

    /**
     * 检查话题是否已收藏
     */
    public boolean isTopicCollected(int topicId) {
        if (context == null) {
            Log.e(TAG, "Context为null，无法检查收藏状态");
            return false;
        }

        return UserProfileManager.INSTANCE.isTopicCollected(context, topicId);
    }

    /**
     * 添加话题到收藏
     */
    public boolean addToCollection(Topic topic) {
        if (context == null) {
            Log.e(TAG, "Context为null，无法添加收藏");
            return false;
        }

        try {
            UserProfileManager.INSTANCE.addTopicToCollection(context, topic);
            Log.d(TAG, "Topic added to collection: " + topic.getTitle() + ", ID: " + topic.getId());
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error adding topic to collection: " + e.getMessage());
            return false;
        }
    }

    /**
     * 从收藏中移除话题
     */
    public boolean removeFromCollection(int topicId) {
        if (context == null) {
            Log.e(TAG, "Context为null，无法移除收藏");
            return false;
        }

        try {
            UserProfileManager.INSTANCE.removeTopicFromCollection(context, topicId);
            Log.d(TAG, "Topic removed from collection: ID: " + topicId);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error removing topic from collection: " + e.getMessage());
            return false;
        }
    }

    /**
     * 切换话题收藏状态
     */
    public boolean toggleTopicCollection(Topic topic) {
        if (context == null) {
            Log.e(TAG, "Context为null，无法切换收藏状态");
            return false;
        }

        try {
            UserProfileManager.INSTANCE.toggleTopicCollection(context, topic);
            boolean newStatus = UserProfileManager.INSTANCE.isTopicCollected(context, topic.getId());
            Log.d(TAG, "Topic collection toggled: " + topic.getTitle() + ", new status: " + newStatus);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error toggling topic collection: " + e.getMessage());
            return false;
        }
    }

    /**
     * 根据ID获取收藏的话题
     */
    public Topic getCollectedTopicById(int topicId) {
        if (context == null) {
            Log.e(TAG, "Context为null，无法获取收藏话题");
            return null;
        }

        List<Topic> collectedTopics = getCollectedTopics();
        for (Topic topic : collectedTopics) {
            if (topic.getId() == topicId) {
                return topic;
            }
        }
        return null;
    }

    /**
     * 更新收藏的话题信息
     */
    public boolean updateCollectedTopic(Topic updatedTopic) {
        if (context == null) {
            Log.e(TAG, "Context为null，无法更新收藏话题");
            return false;
        }

        try {
            // 先移除旧的话题，再添加更新后的话题
            removeFromCollection(updatedTopic.getId());
            addToCollection(updatedTopic);
            Log.d(TAG, "Topic updated in collection: " + updatedTopic.getTitle());
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error updating collected topic: " + e.getMessage());
            return false;
        }
    }

    /**
     * 批量添加话题到收藏
     */
    public void addAllToCollection(List<Topic> topics) {
        if (context == null) {
            Log.e(TAG, "Context为null，无法批量添加收藏");
            return;
        }

        try {
            for (Topic topic : topics) {
                if (!isTopicCollected(topic.getId())) {
                    addToCollection(topic);
                }
            }
            Log.d(TAG, "Added " + topics.size() + " topics to collection");
        } catch (Exception e) {
            Log.e(TAG, "Error adding multiple topics to collection: " + e.getMessage());
        }
    }

    /**
     * 搜索收藏的话题
     */
    public List<Topic> searchCollectedTopics(String query) {
        if (context == null) {
            Log.e(TAG, "Context为null，无法搜索收藏话题");
            return new ArrayList<>();
        }

        List<Topic> collectedTopics = getCollectedTopics();
        List<Topic> results = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            return collectedTopics;
        }

        String lowerQuery = query.toLowerCase().trim();
        for (Topic topic : collectedTopics) {
            if (topic.getTitle() != null && topic.getTitle().toLowerCase().contains(lowerQuery) ||
                    topic.getContent() != null && topic.getContent().toLowerCase().contains(lowerQuery) ||
                    topic.getCategory() != null && topic.getCategory().toLowerCase().contains(lowerQuery)) {
                results.add(topic);
            }
        }

        return results;
    }

    /**
     * 按分类筛选收藏的话题
     */
    public List<Topic> getCollectedTopicsByCategory(String category) {
        if (context == null) {
            Log.e(TAG, "Context为null，无法按分类筛选");
            return new ArrayList<>();
        }

        List<Topic> collectedTopics = getCollectedTopics();
        List<Topic> results = new ArrayList<>();

        if (category == null || category.trim().isEmpty()) {
            return collectedTopics;
        }

        String lowerCategory = category.toLowerCase().trim();
        for (Topic topic : collectedTopics) {
            if (topic.getCategory() != null && topic.getCategory().toLowerCase().equals(lowerCategory)) {
                results.add(topic);
            }
        }

        return results;
    }

    /**
     * 获取所有收藏话题的分类列表
     */
    public List<String> getCollectionCategories() {
        if (context == null) {
            Log.e(TAG, "Context为null，无法获取分类列表");
            return new ArrayList<>();
        }

        List<Topic> collectedTopics = getCollectedTopics();
        List<String> categories = new ArrayList<>();

        for (Topic topic : collectedTopics) {
            if (topic.getCategory() != null && !categories.contains(topic.getCategory())) {
                categories.add(topic.getCategory());
            }
        }

        return categories;
    }

    /**
     * 清空所有收藏
     */
    public void clearAllCollections() {
        if (context == null) {
            Log.e(TAG, "Context为null，无法清空收藏");
            return;
        }

        try {
            // 由于现在收藏数据存储在UserProfile中，我们需要通过UserProfileManager来清空
            // 这里需要UserProfileManager提供清空话题收藏的方法
            // 暂时通过移除所有收藏的话题来实现
            List<Topic> collectedTopics = getCollectedTopics();
            for (Topic topic : collectedTopics) {
                removeFromCollection(topic.getId());
            }
            Log.d(TAG, "All topic collections cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing collections: " + e.getMessage());
        }
    }

    /**
     * 导出收藏数据（JSON格式）
     */
    public String exportCollections() {
        if (context == null) {
            Log.e(TAG, "Context为null，无法导出收藏");
            return "[]";
        }

        try {
            List<Topic> collectedTopics = getCollectedTopics();
            return gson.toJson(collectedTopics);
        } catch (Exception e) {
            Log.e(TAG, "Error exporting collections: " + e.getMessage());
            return "[]";
        }
    }

    /**
     * 导入收藏数据
     */
    public boolean importCollections(String jsonData) {
        if (context == null) {
            Log.e(TAG, "Context为null，无法导入收藏");
            return false;
        }

        try {
            Type topicListType = new TypeToken<List<Topic>>(){}.getType();
            List<Topic> importedTopics = gson.fromJson(jsonData, topicListType);

            if (importedTopics != null && !importedTopics.isEmpty()) {
                addAllToCollection(importedTopics);
                Log.d(TAG, "Imported " + importedTopics.size() + " topics");
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error importing collections: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取收藏统计信息
     */
    public String getCollectionStats() {
        if (context == null) {
            Log.e(TAG, "Context为null，无法获取统计信息");
            return "无法获取统计信息";
        }

        int totalCount = getCollectionCount();
        List<String> categories = getCollectionCategories();

        return String.format("共收藏 %d 个话题，涉及 %d 个分类", totalCount, categories.size());
    }

    /**
     * 检查收藏是否为空
     */
    public boolean isEmpty() {
        if (context == null) {
            Log.e(TAG, "Context为null，无法检查是否为空");
            return true;
        }
        return getCollectedTopics().isEmpty();
    }

    /**
     * 获取最近收藏的话题
     */
    public List<Topic> getRecentCollections(int count) {
        if (context == null) {
            Log.e(TAG, "Context为null，无法获取最近收藏");
            return new ArrayList<>();
        }

        List<Topic> collectedTopics = getCollectedTopics();
        if (collectedTopics.size() <= count) {
            return collectedTopics;
        }
        return collectedTopics.subList(0, count);
    }

    /**
     * 检查话题是否存在
     */
    public boolean containsTopic(int topicId) {
        return isTopicCollected(topicId);
    }

    /**
     * 获取收藏的话题ID列表
     */
    public List<Integer> getCollectedTopicIds() {
        if (context == null) {
            Log.e(TAG, "Context为null，无法获取话题ID列表");
            return new ArrayList<>();
        }

        List<Topic> collectedTopics = getCollectedTopics();
        List<Integer> topicIds = new ArrayList<>();
        for (Topic topic : collectedTopics) {
            topicIds.add(topic.getId());
        }
        return topicIds;
    }
}