package com.example.studyflow.data.model;

import com.google.firebase.Timestamp;

public class User {
    private String userId;
    private String displayName;
    private String email;
    private String avatarUrl;
    private boolean notificationsEnabled;
    private Timestamp createdAt;

    // Constructor rỗng bắt buộc cho Firestore
    public User() {}

    public User(String userId, String displayName, String email) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.notificationsEnabled = true;
        this.createdAt = Timestamp.now();
    }

    // Getters & Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean v) { this.notificationsEnabled = v; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}