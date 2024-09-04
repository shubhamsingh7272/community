package com.pratik.iiits;

public class DynamicRvModel {
    String name,subject;
    private String image;

    public DynamicRvModel(String name,String subject,  String image) {
        this.name= name;
        this.subject= subject;
        this.image= image;
    }
    public String getSubject(){
        return subject;
    }
    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }
}
