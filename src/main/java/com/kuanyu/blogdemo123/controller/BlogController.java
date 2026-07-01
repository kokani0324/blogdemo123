package com.kuanyu.blogdemo123.controller;

import com.kuanyu.blogdemo123.dto.BlogQueryParms;
import com.kuanyu.blogdemo123.model.Blog;
import com.kuanyu.blogdemo123.model.BlogType;
import com.kuanyu.blogdemo123.service.BlogService;
import com.kuanyu.blogdemo123.util.Page;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
public class BlogController {

    @Autowired
    private BlogService blogService;

    @GetMapping("/blogs")
    public List<Blog> getAll() {
        return blogService.getAll();
    }

//    @GetMapping("/blogs")
//    public ResponseEntity<Page<Blog>> getBlogAll(
//            //查詢條件
//            @RequestParam(required = false) Integer blogTypeId,
//            @RequestParam(required = false) String search,
//            //排序
//            @RequestParam(defaultValue = "blog_time") String orderBy,
//            @RequestParam(defaultValue = "desc") String sort,
//            //分頁
//            @RequestParam(defaultValue = "5") @Max(1000) @Min(0) Integer limit ,
//            @RequestParam(defaultValue = "0") @Min(0) Integer offset
//            ) {
//        BlogQueryParms blogQueryParms = new BlogQueryParms();
//        blogQueryParms.setBlogTypeId(blogTypeId);
//        blogQueryParms.setSearch(search);
//        blogQueryParms.setOrderBy(orderBy);
//        blogQueryParms.setSort(sort);
//        blogQueryParms.setLimit(limit);
//        blogQueryParms.setOffset(offset);
//
//        List<Blog> blogList = blogService.getBlogs(blogQueryParms);

//        Integer total = blogService.countBlogs(blogQueryParms);

//        Page<Blog> page = new Page<>();

//        page.setLimit(limit);
//        page.setOffset(offset);
//        page.setTotal(total);
//        page.setResults(blogList);


//        return ResponseEntity.status(HttpStatus.OK).body(page);
//    }

    @GetMapping("/blogs/{blogId}")
    public ResponseEntity<Blog> getBlogOne(@PathVariable Integer blogId) {
       //從service 去抓blogId
        Blog blog = blogService.getBlogById(blogId);

        if(blog != null) {
            return ResponseEntity.status(HttpStatus.OK).body(blog);
        }else {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }



}
