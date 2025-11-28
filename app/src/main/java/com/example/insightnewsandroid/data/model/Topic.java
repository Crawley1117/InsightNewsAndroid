package com.example.insightnewsandroid.data.model;

import java.util.ArrayList;
import java.util.List;

public class Topic {
    private int id;
    private String title;
    private String content;
    private String category;
    private int followCount;
    private String imageUrl;
    private boolean collected;
    private List<NewsItem> relatedNews;
    private List<Comment> comments;
    private int viewCount; // 添加浏览数字段

    public Topic() {
        this.relatedNews = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    public Topic(int id, String title, String content, String category, int followCount, String imageUrl) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.followCount = followCount;
        this.imageUrl = imageUrl;
        this.relatedNews = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getFollowCount() {
        return followCount;
    }

    public void setFollowCount(int followCount) {
        this.followCount = followCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }

    public List<NewsItem> getRelatedNews() {
        return relatedNews;
    }

    public void setRelatedNews(List<NewsItem> relatedNews) {
        this.relatedNews = relatedNews;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    // 新闻相关方法
    public void addNews(NewsItem news) {
        if (this.relatedNews == null) {
            this.relatedNews = new ArrayList<>();
        }
        this.relatedNews.add(news);
    }

    public void addAllNews(List<NewsItem> newsList) {
        if (this.relatedNews == null) {
            this.relatedNews = new ArrayList<>();
        }
        this.relatedNews.addAll(newsList);
    }

    public void removeNews(NewsItem news) {
        if (this.relatedNews != null) {
            this.relatedNews.remove(news);
        }
    }

    public void removeNewsById(String newsId) {
        if (this.relatedNews != null) {
            for (int i = 0; i < this.relatedNews.size(); i++) {
                if (this.relatedNews.get(i).getId().equals(newsId)) {
                    this.relatedNews.remove(i);
                    break;
                }
            }
        }
    }

    public void clearRelatedNews() {
        if (this.relatedNews != null) {
            this.relatedNews.clear();
        }
    }

    public int getRelatedNewsCount() {
        return this.relatedNews != null ? this.relatedNews.size() : 0;
    }

    public boolean containsNews(NewsItem news) {
        if (this.relatedNews == null) return false;
        for (NewsItem item : this.relatedNews) {
            if (item.getId().equals(news.getId())) {
                return true;
            }
        }
        return false;
    }

    // 评论相关方法
    public void addComment(Comment comment) {
        if (this.comments == null) {
            this.comments = new ArrayList<>();
        }
        this.comments.add(comment);
    }

    public void addAllComments(List<Comment> commentList) {
        if (this.comments == null) {
            this.comments = new ArrayList<>();
        }
        this.comments.addAll(commentList);
    }

    public void removeCommentById(int commentId) {
        if (this.comments != null) {
            for (int i = 0; i < this.comments.size(); i++) {
                if (this.comments.get(i).getId() == commentId) {
                    this.comments.remove(i);
                    break;
                }
            }
        }
    }

    public void removeCommentsByUserId(int userId) {
        if (this.comments != null) {
            List<Comment> toRemove = new ArrayList<>();
            for (Comment comment : this.comments) {
                if (comment.getUserId() == userId) {
                    toRemove.add(comment);
                }
            }
            this.comments.removeAll(toRemove);
        }
    }

    public void clearComments() {
        if (this.comments != null) {
            this.comments.clear();
        }
    }

    public int getCommentCount() {
        return this.comments != null ? this.comments.size() : 0;
    }

    public Comment getCommentById(int commentId) {
        if (this.comments != null) {
            for (Comment comment : this.comments) {
                if (comment.getId() == commentId) {
                    return comment;
                }
            }
        }
        return null;
    }

    public List<Comment> getCommentsByUserId(int userId) {
        List<Comment> userComments = new ArrayList<>();
        if (this.comments != null) {
            for (Comment comment : this.comments) {
                if (comment.getUserId() == userId) {
                    userComments.add(comment);
                }
            }
        }
        return userComments;
    }

    public boolean updateComment(int commentId, String newContent) {
        if (this.comments != null) {
            for (Comment comment : this.comments) {
                if (comment.getId() == commentId) {
                    comment.setComment(newContent);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean updateCommentLikeStatus(int commentId, boolean liked, int likeCount) {
        if (this.comments != null) {
            for (Comment comment : this.comments) {
                if (comment.getId() == commentId) {
                    comment.setLike(liked);
                    comment.setLikeCount(likeCount);
                    return true;
                }
            }
        }
        return false;
    }

    public List<Comment> getPopularComments(int limit) {
        if (this.comments == null || this.comments.isEmpty()) {
            return new ArrayList<>();
        }

        List<Comment> sortedComments = new ArrayList<>(this.comments);
        sortedComments.sort((c1, c2) -> Integer.compare(c2.getLikeCount(), c1.getLikeCount()));

        if (limit > 0 && limit < sortedComments.size()) {
            return sortedComments.subList(0, limit);
        }
        return sortedComments;
    }

    public List<Comment> getRecentComments(int limit) {
        if (this.comments == null || this.comments.isEmpty()) {
            return new ArrayList<>();
        }

        List<Comment> sortedComments = new ArrayList<>(this.comments);
        // [已修复] 按 createdAt 字符串排序
        sortedComments.sort((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()));

        if (limit > 0 && limit < sortedComments.size()) {
            return sortedComments.subList(0, limit);
        }
        return sortedComments;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Topic topic = (Topic) obj;
        return id == topic.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Topic{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", followCount=" + followCount +
                '}';
    }
}
