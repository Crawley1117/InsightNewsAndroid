// ui/explore/model/ExploreTopic.java
package com.example.insightnewsandroid.ui.explore.model;

import java.util.Objects;

public class ExploreTopic {
    private final int id;
    private final String title;
    private final String content;
    private final String topic;
    private final int follows;
    private final String thumbPhotoURL;

    public ExploreTopic(int id, String title, String content, String topic, int follows, String thumbPhotoURL) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.topic = topic;
        this.follows = follows;
        this.thumbPhotoURL = thumbPhotoURL;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getTopic() { return topic; }
    public int getFollows() { return follows; }
    public String getThumbPhotoURL() { return thumbPhotoURL; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExploreTopic that = (ExploreTopic) o;
        return id == that.id &&
                follows == that.follows &&
                Objects.equals(title, that.title) &&
                Objects.equals(content, that.content) &&
                Objects.equals(topic, that.topic) &&
                Objects.equals(thumbPhotoURL, that.thumbPhotoURL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, topic, follows, thumbPhotoURL);
    }
}