package com.example.shadowchatapp2;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GlobalPosts {
    private static final String TAG = "GlobalPosts";
    private static final String BASE_URL = "https://shadow-talk-server.vercel.app/api";
    private static final String PREFS_NAME = "GlobalPostsPrefs";
    private static final String KEY_POSTS = "saved_posts";
    private static GlobalPosts instance;
    private List<Post> posts;
    private OkHttpClient client;
    private boolean isLoading = false;
    private Context appContext;

    private GlobalPosts(Context context) {
        posts = new ArrayList<>();
        client = new OkHttpClient();
        appContext = context.getApplicationContext();
        loadPostsFromPrefs();
        Log.d(TAG, "GlobalPosts initialized with " + posts.size() + " posts from preferences");
    }

    public static synchronized GlobalPosts getInstance(Context context) {
        if (instance == null) {
            instance = new GlobalPosts(context);
            Log.d(TAG, "Created new GlobalPosts instance");
        }
        return instance;
    }

    private void loadPostsFromPrefs() {
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedPostsJson = prefs.getString(KEY_POSTS, "[]");
        try {
            JSONArray jsonArray = new JSONArray(savedPostsJson);
            posts.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonPost = jsonArray.getJSONObject(i);
                Post post = new Post(
                        jsonPost.getString("id"),
                        jsonPost.getString("userId"),
                        jsonPost.getString("username"),
                        jsonPost.optString("userProfilePic", ""),
                        jsonPost.getString("content"),
                        jsonPost.optString("imageUrl", null),
                        jsonPost.optString("location", null),
                        jsonPost.getLong("timestamp")
                );
                post.setLikesCount(jsonPost.optInt("likesCount", 0));
                post.setCommentsCount(jsonPost.optInt("commentsCount", 0));
                post.setLiked(jsonPost.optBoolean("isLiked", false));
                post.setBookmarked(jsonPost.optBoolean("isBookmarked", false));
                posts.add(post);
            }
            Log.d(TAG, "Loaded " + posts.size() + " posts from preferences");
        } catch (JSONException e) {
            Log.e(TAG, "Error loading posts from preferences: " + e.getMessage());
        }
    }

    private void savePostsToPrefs() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Post post : posts) {
                JSONObject jsonPost = new JSONObject();
                jsonPost.put("id", post.getId());
                jsonPost.put("userId", post.getUserId());
                jsonPost.put("username", post.getUsername());
                jsonPost.put("userProfilePic", post.getUserProfilePic());
                jsonPost.put("content", post.getContent());
                jsonPost.put("imageUrl", post.getImageUrl());
                jsonPost.put("location", post.getLocation());
                jsonPost.put("timestamp", post.getTimestamp());
                jsonPost.put("likesCount", post.getLikesCount());
                jsonPost.put("commentsCount", post.getCommentsCount());
                jsonPost.put("isLiked", post.isLiked());
                jsonPost.put("isBookmarked", post.isBookmarked());
                jsonArray.put(jsonPost);
            }

            SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_POSTS, jsonArray.toString()).apply();
            Log.d(TAG, "Saved " + posts.size() + " posts to preferences");
        } catch (JSONException e) {
            Log.e(TAG, "Error saving posts to preferences: " + e.getMessage());
        }
    }

    public List<Post> getPosts() {
        if (posts == null) {
            posts = new ArrayList<>();
            Log.d(TAG, "Posts list was null, created new empty list");
        }

        // Fetch posts from server if not already loading
        if (!isLoading) {
            fetchPostsFromServer();
        }

        Log.d(TAG, "Getting posts, size: " + posts.size());
        return posts;
    }

    private void fetchPostsFromServer() {
        isLoading = true;
        Log.d(TAG, "Starting to fetch posts from server...");

        Request request = new Request.Builder()
                .url(BASE_URL + "/get-posts")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to fetch posts: " + e.getMessage());
                isLoading = false;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "Received response from server. Code: " + response.code());

                if (!response.isSuccessful()) {
                    Log.e(TAG, "Server error: " + response.code());
                    isLoading = false;
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Server response body: " + responseBody);

                    JSONArray jsonArray = new JSONArray(responseBody);
                    List<Post> newPosts = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonPost = jsonArray.getJSONObject(i);
                        Log.d(TAG, "Processing post " + i + ": " + jsonPost.toString());

                        Post post = new Post(
                                jsonPost.getString("id"),
                                jsonPost.getString("user_id"),
                                jsonPost.getString("username"),
                                jsonPost.optString("user_image", ""),
                                jsonPost.getString("content"),
                                jsonPost.optString("image_url", null),
                                jsonPost.optString("location", null),
                                jsonPost.getLong("timestamp")
                        );
                        newPosts.add(post);
                    }

                    // Update posts list
                    posts.clear();
                    posts.addAll(newPosts);
                    // Save to preferences after successful fetch
                    savePostsToPrefs();
                    Log.d(TAG, "Successfully fetched and processed " + newPosts.size() + " posts from server");
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing posts: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    isLoading = false;
                }
            }
        });
    }

    public void addPost(Post post) {
        if (posts == null) {
            posts = new ArrayList<>();
            Log.d(TAG, "Posts list was null, created new empty list");
        }
        // Add the new post at the beginning of the list
        posts.add(0, post);
        // Save to preferences after adding new post
        savePostsToPrefs();
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
                // Save to preferences after updating post
                savePostsToPrefs();
                Log.d(TAG, "Updated post at position " + i);
                break;
            }
        }
    }
    // ... existing code ...
    // Add this method to update comments count for a post
    public void updateCommentsCount(String postId, int newCount) {
        if (posts == null) return;
        for (Post post : posts) {
            if (post.getId().equals(postId)) {
                post.setCommentsCount(newCount);
                savePostsToPrefs();
                break;
            }
        }
    }
    // ... existing code ...
    public void deletePost(String postId) {
        if (posts == null) {
            posts = new ArrayList<>();
            Log.d(TAG, "Posts list was null, created new empty list");
            return;
        }

        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).getId().equals(postId)) {
                posts.remove(i);
                // Save to preferences after deleting post
                savePostsToPrefs();
                Log.d(TAG, "Deleted post at position " + i);
                break;
            }
        }
    }
}