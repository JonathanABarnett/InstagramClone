package com.alaythiaproductions.instagramclone.models;

public class Comment {

    private String id, comment, timestamp, uid, email, profile_image, name;

    public Comment() {}

    public Comment(String id, String comment, String timestamp, String uid, String email, String profile_image, String name) {
        this.id = id;
        this.comment = comment;
        this.timestamp = timestamp;
        this.uid = uid;
        this.email = email;
        this.profile_image = profile_image;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

