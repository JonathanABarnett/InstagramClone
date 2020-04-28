package com.alaythiaproductions.instagramclone.models;

public class User {

    private String uid, name, email, search, phone, image, cover, online_status, typing_status;

    public User() {}

    public User(String uid, String name, String email, String search, String phone, String image, String cover, String onlineStatus, String typing) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.search = search;
        this.phone = phone;
        this.image = image;
        this.cover = cover;
        this.online_status = onlineStatus;
        this.typing_status = typing;
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

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getOnline_status() {
        return online_status;
    }

    public void setOnline_status(String online_status) {
        this.online_status = online_status;
    }

    public String getTyping_status() {
        return typing_status;
    }

    public void setTyping_status(String typing_status) {
        this.typing_status = typing_status;
    }
}
