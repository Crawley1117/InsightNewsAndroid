package com.example.insightnewsandroid.data.model;

import java.util.List;

@SuppressWarnings("unused")
public class TopicCollection {
    private List<Topic> topics;
    private int totalCount;

    public TopicCollection() {
    }

    public TopicCollection(List<Topic> topics, int totalCount) {
        this.topics = topics;
        this.totalCount = totalCount;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}