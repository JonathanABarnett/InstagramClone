package com.alaythiaproductions.instagramclone.models;

public class Notification {

    private String post_id, post_uid, timestamp, notification, uid, username, email, profile_image;

    public Notification() {}

    public Notification(String post_id, String post_uid, String timestamp, String notification, String uid, String username, String email, String profile_image) {
        this.post_id = post_id;
        this.post_uid = post_uid;
        this.timestamp = timestamp;
        this.notification = notification;
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.profile_image = profile_image;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getPost_uid() {
        return post_uid;
    }

    public void setPost_uid(String post_uid) {
        this.post_uid = post_uid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }
}