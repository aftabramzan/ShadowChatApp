package com.example.shadowchatapp2;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserSearchResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("users")
    private List<UserProfile> users;

    @SerializedName("message")
    private String message;

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<UserProfile> getUsers() {
        return users;
    }

    public void setUsers(List<UserProfile> users) {
        this.users = users;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
