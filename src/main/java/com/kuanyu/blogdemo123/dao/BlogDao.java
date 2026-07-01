package com.kuanyu.blogdemo123.dao;

import com.kuanyu.blogdemo123.model.Blog;

import java.util.List;


public interface BlogDao {

    List<Blog> getAll();

    Blog getBlogById(Integer blogId);



}
