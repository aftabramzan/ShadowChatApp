package com.example.shadowchatapp2;

import java.io.Serializable;

public class Post  {

    private String id;
    private String userId;
    private String username;
    private String userProfilePic;
    private String content;
    private String imageUrl;
    private String location;
    private long timestamp;
    private int likesCount;
    private int commentsCount;
    private boolean isLiked;
    private boolean isBookmarked;

    public Post() {
        // Default constructor for Firebase
    }

    public Post(String id, String userId, String username, String userProfilePic, String content,
                String imageUrl, String location, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.userProfilePic = userProfilePic;
        this.content = content;
        this.imageUrl = imageUrl;
        this.location = location;
        this.timestamp = timestamp;
        this.likesCount = 0;
        this.commentsCount = 0;
        this.isLiked = false;
        this.isBookmarked = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserProfilePic() {
        return userProfilePic;
    }

    public void setUserProfilePic(String userProfilePic) {
        this.userProfilePic = userProfilePic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }
}