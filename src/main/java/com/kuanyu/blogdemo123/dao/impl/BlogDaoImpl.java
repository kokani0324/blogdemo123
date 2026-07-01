package com.kuanyu.blogdemo123.dao.impl;

import com.kuanyu.blogdemo123.dao.BlogDao;
import com.kuanyu.blogdemo123.model.Blog;
import com.kuanyu.blogdemo123.rowmapper.BlogRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BlogDaoImpl implements BlogDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<Blog> getAll() {
        String sql = "SELECT blog_id, blog_title, user_id, farmer_id, blog_type_id, product_id, blog_content, " +
                "blog_img, blog_like_count, blog_time, blog_status FROM Blog WHERE 1=1";

        Map<String, Object> map = new HashMap<>();


        List<Blog> blogList = namedParameterJdbcTemplate.query(sql,map,new BlogRowMapper() );

        return blogList;
    }

    @Override
    public Blog getBlogById(Integer blogId) {
        String sql = "SELECT blog_id, blog_title, user_id, farmer_id, blog_type_id, product_id, " +
                "blog_content, blog_img, blog_like_count, " +
                "blog_time, blog_status FROM Blog WHERE blog_id = :blogId" ;

        Map<String, Object> map = new HashMap<>();
        map.put("blogId", blogId);

        //結果用blogRowMapper 轉成 List<Blog>
        List<Blog> blogList = namedParameterJdbcTemplate.query(sql, map, new BlogRowMapper());
        //query執行三個參數 sql：要執行的SQL。 map：SQL裡參數的值。new BlogRowMapper()：把每一列轉成Product物件的工具

        if (blogList.size() > 0) {
            //回傳第一筆
            return blogList.get(0);
        }else {
            return null;
        }

    }
}
