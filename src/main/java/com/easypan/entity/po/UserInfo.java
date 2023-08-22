package com.easypan.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 用户信息
 */
public class UserInfo implements Serializable {
    
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 昵称
     */
    private String nickName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * qq 头像
     */
    private String qqAvatar;
    
    /**
     * qq openID
     */
    private String qqOpenId;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 加入时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date joinTime;
    
    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;
    
    /**
     * 0:禁用 1:正常
     */
    private Integer status;
    
    /**
     * 使用空间单位byte
     */
    private Long useSpace;
    
    /**
     * 总空间单位byte
     */
    private Long totalSpace;
    
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUserId() {
        return this.userId;
    }
    
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    
    public String getNickName() {
        return this.nickName;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getEmail() {
        return this.email;
    }
    
    public void setQqAvatar(String qqAvatar) {
        this.qqAvatar = qqAvatar;
    }
    
    public String getQqAvatar() {
        return this.qqAvatar;
    }
    
    public void setQqOpenId(String qqOpenId) {
        this.qqOpenId = qqOpenId;
    }
    
    public String getQqOpenId() {
        return this.qqOpenId;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    public void setJoinTime(Date joinTime) {
        this.joinTime = joinTime;
    }
    
    public Date getJoinTime() {
        return this.joinTime;
    }
    
    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
    
    public Date getLastLoginTime() {
        return this.lastLoginTime;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Integer getStatus() {
        return this.status;
    }
    
    public void setUseSpace(Long useSpace) {
        this.useSpace = useSpace;
    }
    
    public Long getUseSpace() {
        return this.useSpace;
    }
    
    public void setTotalSpace(Long totalSpace) {
        this.totalSpace = totalSpace;
    }
    
    public Long getTotalSpace() {
        return this.totalSpace;
    }
    
   
}