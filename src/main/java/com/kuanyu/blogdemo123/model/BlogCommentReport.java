package com.kuanyu.blogdemo123.model;


import com.kuanyu.blogdemo123.contstant.BlogReportStatus;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "blog_comment_report")
public class BlogCommentReport {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_comment_id", updatable = false)
    private Integer reportCommentId;

    @Column(name = "blog_id")
    private Integer blogId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "admin_id")
    private Integer adminId;

    @Column(name = "report_time")
    private Timestamp reportTime;

    @Column(name = "report_reason")
    private String reportReason ;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status")
    private BlogReportStatus reportStatus;


    public Integer getReportCommentId() {
        return reportCommentId;
    }

    public void setReportCommentId(Integer reportCommentId) {
        this.reportCommentId = reportCommentId;
    }

    public Integer getBlogId() {
        return blogId;
    }

    public void setBlogId(Integer blogId) {
        this.blogId = blogId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public Timestamp getReportTime() {
        return reportTime;
    }

    public void setReportTime(Timestamp reportTime) {
        this.reportTime = reportTime;
    }

    public String getReportReason() {
        return reportReason;
    }

    public void setReportReason(String reportReason) {
        this.reportReason = reportReason;
    }

    public BlogReportStatus getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(BlogReportStatus reportStatus) {
        this.reportStatus = reportStatus;
    }
}
