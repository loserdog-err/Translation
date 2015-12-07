package com.gdxz.zhongbao.client.domain;

import java.util.Date;

/**
 * Created by chenantao on 2015/7/4.
 */
public class ChatBean {
    public ChatBean(String text, Date postTime) {
        this.text = text;
        this.postTime = postTime;
    }

    public ChatBean() {
    }

    ;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getPostTime() {
        return postTime;
    }

    public void setPostTime(Date postTime) {
        this.postTime = postTime;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    private String text;
    private Date postTime;
    private Type type;

    public enum Type {
        OutComing, InComing
    }
}
