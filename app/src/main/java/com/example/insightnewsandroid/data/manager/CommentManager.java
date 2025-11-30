package com.example.insightnewsandroid.data.manager;

import com.example.insightnewsandroid.data.model.Comment;
import java.util.ArrayList;
import java.util.List;

public class CommentManager {
    private static CommentManager instance;
    private final List<Comment> comments = new ArrayList<>();

    private CommentManager() {
        // Private constructor for Singleton
        // Initialize with some dummy data if needed
    }

    public static synchronized CommentManager getInstance() {
        if (instance == null) {
            instance = new CommentManager();
        }
        return instance;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public List<Comment> getCommentsForTopic(int topicId) {
        // This is a simplified version. In a real app, you'd filter by topicId.
        return new ArrayList<>(comments);
    }
}
