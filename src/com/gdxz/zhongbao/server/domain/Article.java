package com.gdxz.zhongbao.server.domain;

import java.util.Date;

/**
 * 各类文章类（问题类，回答类）的父类
 * 
 * @author chenantao
 * 
 */
public class Article
{

	public Integer id;
	public String content;
	public Date postTime;
	public Integer replyCount;// 回复数量

	public User author;

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public User getAuthor()
	{
		return author;
	}

	public void setAuthor(User author)
	{
		this.author = author;
	}

	public Date getPostTime()
	{
		return postTime;
	}

	public void setPostTime(Date postTime)
	{
		this.postTime = postTime;
	}

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public Integer getReplyCount()
	{
		return replyCount;
	}

	public void setReplyCount(Integer replyCount)
	{
		this.replyCount = replyCount;
	}
}
