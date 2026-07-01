package com.kuanyu.blogdemo123.rowmapper;

import com.kuanyu.blogdemo123.contstant.BlogStatus;
import com.kuanyu.blogdemo123.model.Blog;
import jdk.jshell.Snippet;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class BlogRowMapper implements RowMapper<Blog> {

    //從資料庫查到的資料轉成物件
    //從blog_id轉成BlogId
    @Override
    public Blog mapRow(ResultSet rs, int rowNum) throws SQLException {
         Blog blog = new Blog();
        blog.setBlogId(rs.getInt("blog_id"));
        blog.setBlogTitle(rs.getString("blog_title"));
        blog.setUserId(rs.getInt("user_id"));
        blog.setFarmerId(rs.getInt("farmer_id"));
        blog.setBlogTypeId(rs.getInt("blog_type_id"));
        blog.setProductId(rs.getInt("product_id"));
        blog.setBlogContent(rs.getString("blog_content"));
        blog.setBlogImg(rs.getBytes("blog_img"));
        blog.setBlogLikeCount(rs.getInt("blog_like_count"));
        blog.setBlogTime(rs.getTimestamp("blog_time"));
        String BlogStatusStr = rs.getString("blog_status");
        BlogStatus status = BlogStatus.valueOf(BlogStatusStr);
        blog.setBlogStatus(status);


        return blog;
    }
}
