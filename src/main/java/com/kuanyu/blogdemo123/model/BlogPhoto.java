package com.kuanyu.blogdemo123.model;

import jakarta.persistence.*;

@Entity
@Table(name = "blog_photo")
public class BlogPhoto {


    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blog_photo_id")
    private Integer blogPhotoId;

    @Column(name = "blog_id")
    private Integer blogId;

    @Column(name = "photo")
    private byte[] photo;

    public Integer getBlogPhotoId() {
        return blogPhotoId;
    }

    public void setBlogPhotoId(Integer blogPhotoId) {
        this.blogPhotoId = blogPhotoId;
    }

    public Integer getBlogId() {
        return blogId;
    }

    public void setBlogId(Integer blogId) {
        this.blogId = blogId;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }
}
