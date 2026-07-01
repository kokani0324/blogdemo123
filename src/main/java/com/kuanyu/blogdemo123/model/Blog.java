package com.kuanyu.blogdemo123.model;

import com.kuanyu.blogdemo123.contstant.BlogStatus;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "blog")
public class Blog implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "blog_id", updatable = false)
	private Integer blogId;

	@Column(name = "blog_title")
	private String blogTitle;

	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "farmer_id")
	private Integer farmerId;

	@Column(name = "blog_type_id")
	private Integer blogTypeId;

	@Column(name = "product_id")
	private Integer productId;

	@Column(name = "blog_content")
	private String blogContent;

	@Lob
	@Column(name = "blog_img")
	private byte[] blogImg;

	@Column(name = "blog_like_count")
	private Integer blogLikeCount;

	@Column(name = "blog_time")
	private Timestamp blogTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "blog_status")
	private BlogStatus blogStatus;

	public Blog() {
		super();
	}

	public Integer getBlogId() {
		return blogId;
	}

	public void setBlogId(Integer blogId) {
		this.blogId = blogId;
	}

	public String getBlogTitle() {
		return blogTitle;
	}

	public void setBlogTitle(String blogTitle) {
		this.blogTitle = blogTitle;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getFarmerId() {
		return farmerId;
	}

	public void setFarmerId(Integer farmerId) {
		this.farmerId = farmerId;
	}

	public Integer getBlogTypeId() {
		return blogTypeId;
	}

	public void setBlogTypeId(Integer blogTypeId) {
		this.blogTypeId = blogTypeId;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public String getBlogContent() {
		return blogContent;
	}

	public void setBlogContent(String blogContent) {
		this.blogContent = blogContent;
	}

	public byte[] getBlogImg() {
		return blogImg;
	}

	public void setBlogImg(byte[] blogImg) {
		this.blogImg = blogImg;
	}

	public Integer getBlogLikeCount() {
		return blogLikeCount;
	}

	public void setBlogLikeCount(Integer blogLikeCount) {
		this.blogLikeCount = blogLikeCount;
	}

	public Timestamp getBlogTime() {
		return blogTime;
	}

	public void setBlogTime(Timestamp blogTime) {
		this.blogTime = blogTime;
	}

	public BlogStatus getBlogStatus() {
		return blogStatus;
	}

	public void setBlogStatus(BlogStatus blogStatus) {
		this.blogStatus = blogStatus;
	}
}
