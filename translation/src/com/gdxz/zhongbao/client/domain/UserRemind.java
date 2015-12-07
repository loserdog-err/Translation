package com.gdxz.zhongbao.client.domain;

import java.io.Serializable;

/**
 * 用户提醒
 *
 * @author Chean_antao
 */
public class UserRemind implements Serializable
{
	public static final int TYPE_NEW_ANSWER = 0;
	public static final int TYPE_ANSWER_QUESTION_SOLVE = 1;
	public static final int TYPE_FOLLOW_QUESTION_SOLVE = 2;
	public static final int TYPE_ANSWER_ADOPT = 3;
	private Integer id;
	boolean isRead;

	public boolean getIsRead()
	{
		return isRead;
	}

	public void setIsRead(boolean isRead)
	{
		this.isRead = isRead;
	}

	private int type; // 0:有新的回复 1:回复的问题已解决 2:收藏的问题已解决
	// 3:回复被采纳

	private Answer answer;
	private User user;


	public UserRemind()
	{
	}

	;

	public UserRemind(int type, Answer answer, User user)
	{
		super();
		this.type = type;
		this.answer = answer;
		this.user = user;
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

	public Answer getAnswer()
	{
		return answer;
	}

	public void setAnswer(Answer answer)
	{
		this.answer = answer;
	}

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
	{
		this.user = user;
	}
}
