package com.example.insightnewsandroid.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Comment {
    @SerializedName("id")
    private int id;
    @SerializedName("user_id")
    private int userId;
    @SerializedName("username")
    private String username;
    @SerializedName("user_img")
    private String userImg;
    @SerializedName("comment")
    private String comment;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("like_count")
    private int likeCount;
    @SerializedName("is_like")
    private boolean isLike;
    @SerializedName("my_comment")
    private boolean myComment; // This might be redundant if we compare userId
    @SerializedName("parentId")
    private int parentId;
    @SerializedName("status")
    private int status;
    @SerializedName("reply_count") // [已新增] 回复数量
    private int replyCount; 
    @SerializedName("children")
    private List<Comment> children;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getUserImg() { return userImg; }
    public void setUserImg(String userImg) { this.userImg = userImg; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public boolean isLike() { return isLike; }
    public void setLike(boolean like) { isLike = like; }
    public boolean isMyComment() { return myComment; }
    public void setMyComment(boolean myComment) { this.myComment = myComment; }
    public int getParentId() { return parentId; }
    public void setParentId(int parentId) { this.parentId = parentId; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public List<Comment> getChildren() { return children; }
    public void setChildren(List<Comment> children) { this.children = children; }
    // [已新增] Getter for replyCount
    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }
}
