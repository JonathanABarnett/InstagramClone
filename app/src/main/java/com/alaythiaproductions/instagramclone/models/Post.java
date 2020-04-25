package com.alaythiaproductions.instagramclone.models;

public class Post {

    private String post_id, post_title, post_description, post_image, post_time, uid, name, email, image;

    public Post() {
    }

    public Post(String post_id, String post_title, String post_description, String post_image, String post_time, String uid, String name, String email, String image) {
        this.post_id = post_id;
        this.post_title = post_title;
        this.post_description = post_description;
        this.post_image = post_image;
        this.post_time = post_time;
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.image = image;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getPost_title() {
        return post_title;
    }

    public void setPost_title(String post_title) {
        this.post_title = post_title;
    }

    public String getPost_description() {
        return post_description;
    }

    public void setPost_description(String post_description) {
        this.post_description = post_description;
    }

    public String getPost_image() {
        return post_image;
    }

    public void setPost_image(String post_image) {
        this.post_image = post_image;
    }

    public String getPost_time() {
        return post_time;
    }

    public void setPost_time(String post_time) {
        this.post_time = post_time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
