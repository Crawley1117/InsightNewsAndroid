package com.example.insightnewsandroid.data.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.insightnewsandroid.data.model.Comment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CommentManager {
    private static final String TAG = "CommentManager";
    private static final String COMMENT_PREFS = "comment_data";
    private static final String KEY_COMMENTS = "comments_map";

    private static CommentManager instance;
    private final Context context;
    private final Gson gson;

    private CommentManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();
    }

    public static synchronized CommentManager getInstance(Context context) {
        if (instance == null) {
            instance = new CommentManager(context);
        }
        return instance;
    }

    /**
     * 获取话题的评论列表
     */
    public List<Comment> getCommentsForTopic(int topicId) {
        try {
            SharedPreferences sharedPref = context.getSharedPreferences(COMMENT_PREFS, Context.MODE_PRIVATE);
            String commentsJson = sharedPref.getString(KEY_COMMENTS + "_" + topicId, null);

            if (commentsJson != null) {
                Type type = new TypeToken<List<Comment>>(){}.getType();
                List<Comment> comments = gson.fromJson(commentsJson, type);
                return comments != null ? comments : new ArrayList<>();
            } else {
                // 如果没有存储的评论，返回模拟数据
                return getMockCommentsForTopic(topicId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting comments for topic: " + e.getMessage());
            return getMockCommentsForTopic(topicId);
        }
    }

    /**
     * 保存评论列表
     */
    public void saveCommentsForTopic(int topicId, List<Comment> comments) {
        try {
            SharedPreferences sharedPref = context.getSharedPreferences(COMMENT_PREFS, Context.MODE_PRIVATE);
            String commentsJson = gson.toJson(comments);
            sharedPref.edit().putString(KEY_COMMENTS + "_" + topicId, commentsJson).apply();
            Log.d(TAG, "Comments saved for topic: " + topicId + ", count: " + comments.size());
        } catch (Exception e) {
            Log.e(TAG, "Error saving comments: " + e.getMessage());
        }
    }

    /**
     * 添加评论到话题
     */
    public void addCommentToTopic(int topicId, Comment comment) {
        List<Comment> comments = getCommentsForTopic(topicId);
        comments.add(0, comment); // 新的评论放在最前面
        saveCommentsForTopic(topicId, comments);
        Log.d(TAG, "Comment added to topic: " + topicId);
    }

    /**
     * 更新评论点赞状态
     */
    public void updateCommentLikeStatus(int topicId, int commentId, boolean liked, int likeCount) {
        List<Comment> comments = getCommentsForTopic(topicId);
        for (Comment comment : comments) {
            if (comment.getId() == commentId) {
                comment.setLike(liked);
                comment.setLikeCount(likeCount);
                saveCommentsForTopic(topicId, comments);
                Log.d(TAG, "Comment like status updated: " + commentId);
                break;
            }
        }
    }

    /**
     * 删除评论
     */
    public void deleteComment(int topicId, int commentId) {
        List<Comment> comments = getCommentsForTopic(topicId);
        List<Comment> updatedComments = new ArrayList<>();

        for (Comment comment : comments) {
            if (comment.getId() != commentId) {
                updatedComments.add(comment);
            }
        }

        saveCommentsForTopic(topicId, updatedComments);
        Log.d(TAG, "Comment deleted: " + commentId);
    }

    /**
     * 获取模拟评论数据
     */
    private List<Comment> getMockCommentsForTopic(int topicId) {
        List<Comment> mockComments = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        switch (topicId) {
            case 500: // 哪吒2话题
                Comment comment1 = new Comment();
                comment1.setId(2001);
                comment1.setUserId(1);
                comment1.setUsername("电影爱好者");
                comment1.setUserImg("https://example.com/avatar1.jpg");
                comment1.setComment("期待这部电影很久了，希望不要被谣言影响！");
                comment1.setTimestamp(currentTime - 2 * 60 * 60 * 1000); // 2小时前
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
                comment2.setTimestamp(currentTime - 60 * 60 * 1000); // 1小时前
                comment2.setCreatedAt("1小时前");
                comment2.setLikeCount(8);
                comment2.setLike(false);
                comment2.setMyComment(false);

                mockComments.add(comment1);
                mockComments.add(comment2);
                break;

            case 501: // AI换脸话题
                Comment comment3 = new Comment();
                comment3.setId(2003);
                comment3.setUserId(3);
                comment3.setUsername("科技达人");
                comment3.setUserImg("https://example.com/avatar3.jpg");
                comment3.setComment("AI技术发展很快，但也要注意防范滥用。");
                comment3.setTimestamp(currentTime - 3 * 60 * 60 * 1000); // 3小时前
                comment3.setCreatedAt("3小时前");
                comment3.setLikeCount(12);
                comment3.setLike(true);
                comment3.setMyComment(false);

                mockComments.add(comment3);
                break;

            default:
                // 默认评论
                Comment defaultComment = new Comment();
                defaultComment.setId(3000);
                defaultComment.setUserId(0);
                defaultComment.setUsername("热心网友");
                defaultComment.setUserImg("https://example.com/default_avatar.jpg");
                defaultComment.setComment("这个话题很有意思，期待更多讨论！");
                defaultComment.setTimestamp(currentTime - 24 * 60 * 60 * 1000); // 1天前
                defaultComment.setCreatedAt("1天前");
                defaultComment.setLikeCount(5);
                defaultComment.setLike(false);
                defaultComment.setMyComment(false);

                mockComments.add(defaultComment);
                break;
        }

        return mockComments;
    }
}