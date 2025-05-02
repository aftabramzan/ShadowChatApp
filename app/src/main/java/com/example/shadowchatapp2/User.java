package com.example.shadowchatapp2;

public class User {
    private String username;
    private String name;
    private String profileImage;

    public User(String username, String name, String profileImage) {
        this.username = username;
        this.name = name;
        this.profileImage = profileImage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}