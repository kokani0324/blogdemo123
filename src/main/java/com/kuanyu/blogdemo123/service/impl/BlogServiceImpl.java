package com.kuanyu.blogdemo123.service.impl;

import com.kuanyu.blogdemo123.dao.BlogDao;
import com.kuanyu.blogdemo123.model.Blog;
import com.kuanyu.blogdemo123.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BlogServiceImpl implements BlogService {

    @Autowired
    private BlogDao blogDao;

    @Override
    public List<Blog> getAll() {
        return blogDao.getAll();
    }

    @Override
    public Blog getBlogById(Integer blogId) {
        return blogDao.getBlogById(blogId);
    }

}
