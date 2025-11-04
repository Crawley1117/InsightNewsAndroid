package com.example.insightnewsandroid.data.model

// 确保这里也导入了 NewsItem
data class UserProfile(
    val userId: Int = 1,
    val username: String = "新用户",
    val bio: String = "这位用户很懒，什么都没有写",
    val gender: Int = 2,
    val avatarUri: String? = null,
    val collectedNews: List<NewsItem> = emptyList(),  // 这里使用 NewsItem
    val likedNews: List<String> = emptyList(),
    val dislikedNews: List<String> = emptyList(),
    val collectedTopics: List<Topic> = emptyList()
) {
    // 话题收藏相关方法（现有代码）
    fun addCollectedTopic(topic: Topic): UserProfile {
        val updatedTopics = collectedTopics.toMutableList()
        if (!updatedTopics.any { it.id == topic.id }) {
            updatedTopics.add(0, topic)
        }
        return this.copy(collectedTopics = updatedTopics)
    }

    fun removeCollectedTopic(topicId: Int): UserProfile {
        val updatedTopics = collectedTopics.filter { it.id != topicId }
        return this.copy(collectedTopics = updatedTopics)
    }

    fun isTopicCollected(topicId: Int): Boolean {
        return collectedTopics.any { it.id == topicId }
    }

    fun toggleTopicCollection(topic: Topic): UserProfile {
        return if (isTopicCollected(topic.id)) {
            removeCollectedTopic(topic.id)
        } else {
            addCollectedTopic(topic)
        }
    }

    fun getCollectedTopicsCount(): Int {
        return collectedTopics.size
    }

    // === 新增：新闻收藏相关方法 ===

    // 添加新闻到收藏
    fun addCollectedNews(news: NewsItem): UserProfile {
        val updatedNews = collectedNews.toMutableList()
        // 检查是否已经收藏
        if (!updatedNews.any { it.id == news.id }) {
            val newsToAdd = news.apply {
                // 在 Kotlin 中，我们需要创建一个新的 NewsItem 或者修改现有对象
                // 由于 NewsItem 是 Java 类，我们直接使用
            }
            updatedNews.add(0, newsToAdd)
        }
        return this.copy(collectedNews = updatedNews)
    }

    // 从收藏中移除新闻
    fun removeCollectedNews(newsId: String): UserProfile {
        val updatedNews = collectedNews.filter { it.id != newsId }
        return this.copy(collectedNews = updatedNews)
    }

    // 检查新闻是否已收藏
    fun isNewsCollected(newsId: String): Boolean {
        return collectedNews.any { it.id == newsId }
    }

    // 切换新闻收藏状态
    fun toggleNewsCollection(news: NewsItem): UserProfile {
        return if (isNewsCollected(news.id)) {
            removeCollectedNews(news.id)
        } else {
            addCollectedNews(news)
        }
    }

    // 获取收藏的新闻数量
    fun getCollectedNewsCount(): Int {
        return collectedNews.size
    }
}
