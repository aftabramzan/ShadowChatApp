package com.example.shadowchatapp2;

import android.util.Log;

import java.util.List;
import java.util.ArrayList;
public class GlobalPosts {
    private static final String TAG = "GlobalPosts";
    private static GlobalPosts instance;
    private List<Post> posts;

    private GlobalPosts() {
        posts = new ArrayList<>();
        Log.d(TAG, "GlobalPosts initialized with empty list");
    }

    public static synchronized GlobalPosts getInstance() {
        if (instance == null) {
            instance = new GlobalPosts();
            Log.d(TAG, "Created new GlobalPosts instance");
        }
        return instance;
    }

    public List<Post> getPosts() {
        if (posts == null) {
            posts = new ArrayList<>();
            Log.d(TAG, "Posts list was null, created new empty list");
        }
        Log.d(TAG, "Getting posts, size: " + posts.size());
        return posts;
    }

    public void addPost(Post post) {
        if (posts == null) {
            posts = new ArrayList<>();
            Log.d(TAG, "Posts list was null, created new empty list");
        }
        // Add the new post at the beginning of the list
        posts.add(0, post);
        Log.d(TAG, "Added new post, total posts: " + posts.size());
    }

    public void updatePost(Post updatedPost) {
        if (posts == null) {
            posts = new ArrayList<>();
            Log.d(TAG, "Posts list was null, created new empty list");
            return;
        }

        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId().equals(updatedPost.getId())) {
                posts.set(i, updatedPost);
                Log.d(TAG, "Updated post at position " + i);
                break;
            }
        }
    }

    public void deletePost(String postId) {
        if (posts == null) {
            posts = new ArrayList<>();
            Log.d(TAG, "Posts list was null, created new empty list");
            return;
        }

        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId().equals(postId)) {
                posts.remove(i);
                Log.d(TAG, "Deleted post at position " + i);
                break;
            }
        }
    }
}