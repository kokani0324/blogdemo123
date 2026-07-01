package com.kuanyu.blogdemo123.service;

import com.kuanyu.blogdemo123.model.Blog;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BlogService {

    List<Blog> getAll();

    Blog getBlogById(Integer id);


}
