package com.example.insightnewsandroid.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.insightnewsandroid.data.model.UserProfile
import com.example.insightnewsandroid.data.model.Topic
import com.example.insightnewsandroid.data.model.NewsItem
import com.google.gson.Gson

object UserProfileManager {

    fun getCurrentProfile(context: Context): UserProfile {
        val sharedPreferences = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        val userJson = sharedPreferences.getString("current_user", null)

        return if (userJson != null) {
            Gson().fromJson(userJson, UserProfile::class.java)
        } else {
            UserProfile()  // 使用默认值
        }
    }

    fun updateProfile(context: Context, profile: UserProfile) {
        val sharedPreferences = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        val userJson = Gson().toJson(profile)
        sharedPreferences.edit {
            putString("current_user", userJson)
        }
    }

    // 话题收藏相关方法
    fun addTopicToCollection(context: Context, topic: Topic) {
        val currentProfile = getCurrentProfile(context)
        val updatedProfile = currentProfile.addCollectedTopic(topic)
        updateProfile(context, updatedProfile)
    }

    fun removeTopicFromCollection(context: Context, topicId: Int) {
        val currentProfile = getCurrentProfile(context)
        val updatedProfile = currentProfile.removeCollectedTopic(topicId)
        updateProfile(context, updatedProfile)
    }

    fun toggleTopicCollection(context: Context, topic: Topic) {
        val currentProfile = getCurrentProfile(context)
        val updatedProfile = currentProfile.toggleTopicCollection(topic)
        updateProfile(context, updatedProfile)
    }

    fun isTopicCollected(context: Context, topicId: Int): Boolean {
        val currentProfile = getCurrentProfile(context)
        return currentProfile.isTopicCollected(topicId)
    }

    fun getCollectedTopics(context: Context): List<Topic> {
        val currentProfile = getCurrentProfile(context)
        return currentProfile.collectedTopics
    }

    fun getCollectedTopicsCount(context: Context): Int {
        val currentProfile = getCurrentProfile(context)
        return currentProfile.getCollectedTopicsCount()
    }

    // 在 UserProfileManager.kt 中添加

    // 新闻收藏相关方法
    fun addNewsToCollection(context: Context, news: NewsItem) {
        val currentProfile = getCurrentProfile(context)
        val updatedProfile = currentProfile.addCollectedNews(news)
        updateProfile(context, updatedProfile)
    }

    fun removeNewsFromCollection(context: Context, newsId: String) {
        val currentProfile = getCurrentProfile(context)
        val updatedProfile = currentProfile.removeCollectedNews(newsId)
        updateProfile(context, updatedProfile)
    }

    fun toggleNewsCollection(context: Context, news: NewsItem) {
        val currentProfile = getCurrentProfile(context)
        val updatedProfile = currentProfile.toggleNewsCollection(news)
        updateProfile(context, updatedProfile)
    }

    fun isNewsCollected(context: Context, newsId: String): Boolean {
        val currentProfile = getCurrentProfile(context)
        return currentProfile.isNewsCollected(newsId)
    }

    fun getCollectedNews(context: Context): List<NewsItem> {
        val currentProfile = getCurrentProfile(context)
        return currentProfile.collectedNews
    }

    fun getCollectedNewsCount(context: Context): Int {
        val currentProfile = getCurrentProfile(context)
        return currentProfile.getCollectedNewsCount()
    }
}
