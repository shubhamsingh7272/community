package com.pratik.iiits.Models;

public class UserModel {

    String uid;
    String name;
    String email;
    String postinIIIT;
    String imageUri;
    String status;
    public UserModel() {
    }

    public UserModel(String uid, String name, String email, String postinIIIT, String imageUri, String status) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.postinIIIT = postinIIIT;
        this.imageUri = imageUri;
        this.status=  status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
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

    public String getPostinIIIT() {
        return postinIIIT;
    }

    public void setPostinIIIT(String postinIIIT) {
        this.postinIIIT = postinIIIT;
    }
}
