package com.kuanyu.blogdemo123.model;

import jakarta.persistence.*;

 //用好了
@Entity
@Table(name = "blog_type")
public class BlogType {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blog_type_id", updatable = false)
    private Integer blogTypeId;

    @Column(name = "blog_type_name")
    private String blogTypeName;

    @Lob
    @Column(name = "blog_type_img")
    private byte[] blogTypeImg;

    @Column(name = "blog_type_text")
    private String blogTypeText;


    public BlogType() {
        super();
    }

    public Integer getBlogTypeId() {
        return blogTypeId;
    }

    public void setBlogTypeId(Integer blogTypeId) {
        this.blogTypeId = blogTypeId;
    }

    public String getBlogTypeName() {
        return blogTypeName;
    }

    public void setBlogTypeName(String blogTypeName) {
        this.blogTypeName = blogTypeName;
    }

    public byte[] getBlogTypeImg() {
        return blogTypeImg;
    }

    public void setBlogTypeImg(byte[] blogTypeImg) {
        this.blogTypeImg = blogTypeImg;
    }

    public String getBlogTypeText() {
        return blogTypeText;
    }

    public void setBlogTypeText(String blogTypeText) {
        this.blogTypeText = blogTypeText;
    }
}
