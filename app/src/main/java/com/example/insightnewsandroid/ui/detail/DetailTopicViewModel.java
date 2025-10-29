package com.example.insightnewsandroid.ui.detail;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.insightnewsandroid.data.UserProfileManager;
import com.example.insightnewsandroid.data.manager.TopicManager;
import com.example.insightnewsandroid.data.model.Comment;
import com.example.insightnewsandroid.data.model.NewsItem;
import com.example.insightnewsandroid.data.model.Topic;
import com.example.insightnewsandroid.data.model.UserProfile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailTopicViewModel extends ViewModel {

    private static final String TAG = "DetailTopicViewModel";

    private final MutableLiveData<NewsItem> topicDetail = new MutableLiveData<>();
    private final MutableLiveData<List<NewsItem>> relatedNews = new MutableLiveData<>();
    private final MutableLiveData<Boolean> commentSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> likeSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private Context appContext;

    public LiveData<List<NewsItem>> getRelatedNews() {
        return relatedNews;
    }

    public LiveData<Boolean> getCommentSuccess() {
        return commentSuccess;
    }

    public LiveData<Boolean> getLikeSuccess() {
        return likeSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setAppContext(Context context) {
        this.appContext = context.getApplicationContext();
        Log.d(TAG, "应用上下文已设置");
    }

    /**
     * 加载话题详情
     */
    public void loadTopicDetail(int topicId) {
        Log.d(TAG, "loadTopicDetail: 开始加载话题详情, topicId=" + topicId);

        if (appContext != null) {
            Topic topic = TopicManager.getInstance(appContext).getTopicById(topicId);
            if (topic != null) {
                Log.d(TAG, "找到话题: " + topic.getTitle() + ", 分类: " + topic.getCategory());

                // 转换为NewsItem用于UI显示（保持兼容性）
                NewsItem topicItem = new NewsItem();
                topicItem.setId(String.valueOf(topic.getId()));
                topicItem.setTitle(topic.getTitle());
                topicItem.setContent(topic.getContent());
                topicItem.setTopicCategory(topic.getCategory());
                topicItem.setViewCount(topic.getFollowCount());
                topicItem.setImageUrl(topic.getImageUrl());
                topicDetail.setValue(topicItem);

                // 增加浏览计数
                TopicManager.getInstance(appContext).incrementTopicViewCount(topicId);
                Log.d(TAG, "话题详情加载完成");
            } else {
                Log.e(TAG, "未找到话题, topicId=" + topicId);
                errorMessage.setValue("未找到话题信息");
            }
        } else {
            Log.e(TAG, "appContext为null，无法加载话题详情");
            errorMessage.setValue("系统错误");
        }
    }

    /**
     * 加载相关新闻
     */
    public void loadRelatedNewsForTopic(int topicId) {
        Log.d(TAG, "loadRelatedNewsForTopic: 开始加载相关新闻, topicId=" + topicId);

        if (appContext != null) {
            List<NewsItem> relatedNewsList = TopicManager.getInstance(appContext).getRelatedNewsForTopic(topicId);
            if (relatedNewsList != null && !relatedNewsList.isEmpty()) {
                Log.d(TAG, "找到相关新闻: " + relatedNewsList.size() + " 条");
                relatedNews.setValue(relatedNewsList);
            } else {
                Log.d(TAG, "没有相关新闻数据");
                relatedNews.setValue(new ArrayList<>());
            }
        } else {
            Log.e(TAG, "appContext为null，无法加载相关新闻");
            errorMessage.setValue("系统错误");
        }
    }

    /**
     * 添加评论
     */
    public void addComment(int topicId, String comment) {
        Log.d(TAG, "addComment: topicId=" + topicId + ", comment=" + comment);

        if (appContext == null) {
            errorMessage.setValue("系统错误");
            return;
        }

        if (comment == null || comment.trim().isEmpty()) {
            errorMessage.setValue("评论内容不能为空");
            return;
        }

        // 创建新评论
        Comment newComment = new Comment();
        newComment.setId((int) System.currentTimeMillis()); // 使用时间戳作为临时ID
        newComment.setUserId(getCurrentUserId());
        newComment.setUsername(getCurrentUsername());
        newComment.setUserImg(getCurrentUserAvatar());
        newComment.setComment(comment.trim());
        newComment.setTimestamp(System.currentTimeMillis()); // 设置时间戳
        newComment.setCreatedAt(getRelativeTime(System.currentTimeMillis())); // 设置相对时间
        newComment.setLikeCount(0);
        newComment.setLike(false);
        newComment.setMyComment(true);
        newComment.setChildren(new ArrayList<>());

        // 使用TopicManager保存评论
        TopicManager.getInstance(appContext).addCommentToTopic(topicId, newComment);

        Log.d(TAG, "评论添加成功: topicId=" + topicId);
        commentSuccess.setValue(true);
    }

    /**
     * 获取话题的评论列表
     */
    public List<Comment> getCommentsForTopic(int topicId) {
        Log.d(TAG, "getCommentsForTopic: topicId=" + topicId);

        if (appContext != null) {
            List<Comment> comments = TopicManager.getInstance(appContext).getCommentsForTopic(topicId);
            Log.d(TAG, "获取到评论数量: " + (comments != null ? comments.size() : 0));
            return comments;
        }
        Log.e(TAG, "appContext为null，无法获取评论");
        return new ArrayList<>();
    }

    /**
     * 点赞/取消点赞评论
     */
    public void toggleCommentLike(int topicId, int commentId, boolean newLikeStatus, int newLikeCount) {
        Log.d(TAG, "toggleCommentLike: topicId=" + topicId + ", commentId=" + commentId + ", newLikeStatus=" + newLikeStatus + ", newLikeCount=" + newLikeCount);

        if (appContext != null) {
            // 使用TopicManager更新点赞状态
            TopicManager.getInstance(appContext).updateCommentLikeStatus(
                    topicId, commentId, newLikeStatus, newLikeCount);

            Log.d(TAG, "点赞状态已更新到存储: topicId=" + topicId + ", commentId=" + commentId);
        } else {
            Log.e(TAG, "appContext为null，无法更新点赞状态");
            errorMessage.setValue("系统错误，无法更新点赞状态");
            return;
        }

        // 通知UI更新成功
        likeSuccess.setValue(true);
    }

    /**
     * 将时间戳转换为相对时间（如：刚刚、2分钟前、1小时前等）
     */
    private String getRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) { // 1分钟内
            return "刚刚";
        } else if (diff < 3600000) { // 1小时内
            long minutes = diff / 60000;
            return minutes + "分钟前";
        } else if (diff < 86400000) { // 24小时内
            long hours = diff / 3600000;
            return hours + "小时前";
        } else if (diff < 604800000) { // 7天内
            long days = diff / 86400000;
            return days + "天前";
        } else {
            // 超过7天显示具体日期
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }

    /**
     * 更新所有评论的相对时间
     */
    public void updateCommentsRelativeTime(int topicId) {
        Log.d(TAG, "updateCommentsRelativeTime: topicId=" + topicId);

        if (appContext != null) {
            List<Comment> comments = getCommentsForTopic(topicId);
            for (Comment comment : comments) {
                String relativeTime = getRelativeTime(comment.getTimestamp());
                comment.setCreatedAt(relativeTime);
            }
            Log.d(TAG, "评论相对时间更新完成");
        }
    }

    private int getCurrentUserId() {
        // 从 SharedPreferences 或用户管理类获取当前用户ID
        if (appContext != null) {
            SharedPreferences sharedPref = appContext.getSharedPreferences("user_profile", Context.MODE_PRIVATE);
            return sharedPref.getInt("user_id", 1);
        }
        return 1;
    }

    private String getCurrentUsername() {
        if (appContext != null) {
            UserProfile profile = UserProfileManager.INSTANCE.getCurrentProfile(appContext);
            String username = profile.getUsername();
            Log.d(TAG, "当前用户名: " + username);
            return username;
        }
        Log.w(TAG, "appContext为null，使用默认用户名");
        return "用户";
    }

    private String getCurrentUserAvatar() {
        if (appContext != null) {
            UserProfile profile = UserProfileManager.INSTANCE.getCurrentProfile(appContext);
            String avatarUri = profile.getAvatarUri();

            // 添加调试信息
            if (avatarUri == null || avatarUri.isEmpty()) {
                Log.d(TAG, "用户头像URI为空");
                return "";
            } else {
                Log.d(TAG, "用户头像URI: " + avatarUri);
                return avatarUri;
            }
        }
        Log.w(TAG, "appContext为null，无法获取用户头像");
        return "";
    }
}