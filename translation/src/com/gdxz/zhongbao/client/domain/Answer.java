package com.gdxz.zhongbao.client.domain;

import com.lidroid.xutils.db.annotation.Foreign;
import com.lidroid.xutils.db.annotation.NoAutoIncrement;

import java.io.Serializable;
import java.util.Date;

/**
 * answer
 *
 * @author chenantao
 */
public class Answer implements Serializable
{
	@NoAutoIncrement
	public int id;
	public String content;
	public Date postTime;
	public int replyCount;// 回复数量
	@Foreign(column = "userId", foreign = "id")
	public User author;

	private int praiseCount;// 点赞数
	private int despiseCount;// 鄙视次数
	private String title;

	private boolean isBest;// 是否为最佳答案

	@Foreign(column = "questionId", foreign = "id")
	private Question question;


	public Answer()
	{
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public Date getPostTime()
	{
		return postTime;
	}

	public void setPostTime(Date postTime)
	{
		this.postTime = postTime;
	}

	public int getReplyCount()
	{
		return replyCount;
	}

	public void setReplyCount(int replyCount)
	{
		this.replyCount = replyCount;
	}

	public User getAuthor()
	{
		return author;
	}

	public void setAuthor(User author)
	{
		this.author = author;
	}

	public void setBest(boolean isBest)
	{
		this.isBest = isBest;
	}


	public int getPraiseCount()
	{
		return praiseCount;
	}

	public void setPraiseCount(int praiseCount)
	{
		this.praiseCount = praiseCount;
	}

	public int getDespiseCount()
	{
		return despiseCount;
	}

	public void setDespiseCount(int despiseCount)
	{
		this.despiseCount = despiseCount;
	}

	public boolean getIsBest()
	{
		return isBest;
	}

	public void setIsBest(boolean isBest)
	{
		this.isBest = isBest;
	}

	public Question getQuestion()
	{
		return question;
	}

	public void setQuestion(Question question)
	{
		this.question = question;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}
}
