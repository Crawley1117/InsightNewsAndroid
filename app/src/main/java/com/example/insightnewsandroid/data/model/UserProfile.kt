package com.example.insightnewsandroid.data.model

data class UserProfile(
    val userId: Int = 1, // 添加用户ID，默认为1
    val username: String = "新用户",
    val bio: String = "这位用户很懒，什么都没有写",
    val gender: Int = 2, // 0: male, 1: female, 2: secret
    val avatarUri: String? = null,
    val collectedNews: List<NewsItem> = emptyList(),
    val likedNews: List<String> = emptyList(),
    val dislikedNews: List<String> = emptyList(),
    val collectedTopics: List<Topic> = emptyList()
) {
    // 添加话题到收藏 - 简化版本
    fun addCollectedTopic(topic: Topic): UserProfile {
        val updatedTopics = collectedTopics.toMutableList()
        // 检查是否已经收藏
        if (!updatedTopics.any { it.id == topic.id }) {
            updatedTopics.add(0, topic) // 新的在前
        }
        return this.copy(collectedTopics = updatedTopics)
    }

    // 从收藏中移除话题
    fun removeCollectedTopic(topicId: Int): UserProfile {
        val updatedTopics = collectedTopics.filter { it.id != topicId }
        return this.copy(collectedTopics = updatedTopics)
    }

    // 检查话题是否已收藏
    fun isTopicCollected(topicId: Int): Boolean {
        return collectedTopics.any { it.id == topicId }
    }

    // 切换话题收藏状态
    fun toggleTopicCollection(topic: Topic): UserProfile {
        return if (isTopicCollected(topic.id)) {
            removeCollectedTopic(topic.id)
        } else {
            addCollectedTopic(topic)
        }
    }

    // 获取收藏的话题数量
    fun getCollectedTopicsCount(): Int {
        return collectedTopics.size
    }
}
