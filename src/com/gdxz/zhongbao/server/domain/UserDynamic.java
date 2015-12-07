package com.gdxz.zhongbao.server.domain;

import java.util.Date;

/**
 * 
 * @author Chean_antao 
 * 注意： 1：类型为 关注，提问 具有Question和User外键//
 *         2：类型为 回答 具有Answer 和User外键//
 */
public class UserDynamic
{
	public static final int DYNAMIC_TYPE_ALL = 0;// 所有类型
	public static final int DYNAMIC_TYPE_FOLLOW = 1;// 关注
	public static final int DYNAMIC_TYPE_ASK = 2;// 提问
	public static final int DYNAMIC_TYPE_ANSWER = 3;// 回答
	private Date updateTime;

	public Date getUpdateTime()
	{
		return updateTime;
	}

	public void setUpdateTime(Date updateTime)
	{
		this.updateTime = updateTime;
	}

	private Integer id;
	private int type;
	private User user;
	private Question question;
	private Answer answer;

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
	{
		this.user = user;
	}

	public Answer getAnswer()
	{
		return answer;
	}

	public void setAnswer(Answer answer)
	{
		this.answer = answer;
	}

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public Question getQuestion()
	{
		return question;
	}

	public void setQuestion(Question question)
	{
		this.question = question;
	}
}
