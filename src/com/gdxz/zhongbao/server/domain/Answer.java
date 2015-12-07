package com.gdxz.zhongbao.server.domain;

/**
 * answer
 * @author chenantao
 *
 */
public class Answer extends Article
{

	private Integer praiseCount;// 点赞数
	private Integer despiseCount;// 鄙视次数
	private String title;
	
	private boolean isBest;//是否为最佳答案
	
	private Question question;

	public Integer getPraiseCount()
	{
		return praiseCount;
	}

	public void setPraiseCount(Integer praiseCount)
	{
		this.praiseCount = praiseCount;
	}

	public Integer getDespiseCount()
	{
		return despiseCount;
	}

	public void setDespiseCount(Integer despiseCount)
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
