package com.gdxz.zhongbao.server.domain;

import java.util.Date;

/**
 * 用户提醒
 * 
 * @author Chean_antao
 * 
 */
public class UserRemind
{
	public static final int TYPE_NEW_ANSWER = 0;
	public static final int TYPE_ANSWER_QUESTION_SOLVE = 1;
	public static final int TYPE_FOLLOW_QUESTION_SOLVE = 2;
	public static final int TYPE_ANSWER_ADOPT = 3;
	private Integer id;
	private boolean isRead;// 是否已读
	private Date remindTime;
	private int type; // 0:有新的回复 1:回复的问题已解决 2:收藏的问题已解决
						// 3:回复被采纳
//	private Question question;

	private Answer answer;// 在类型为0时该answer表示的是新回复，在类型1到3表示的最佳回答
	private User user;

	public boolean getIsRead()
	{
		return isRead;
	}

	public void setIsRead(boolean isRead)
	{
		this.isRead = isRead;
	}

	public UserRemind()
	{
	};

	public UserRemind(int type, Answer answer, User user, Date remindTime)
	{
		super();
		this.type = type;
		this.answer = answer;
		this.user = user;
		this.remindTime = remindTime;
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

	public Date getRemindTime()
	{
		return remindTime;
	}

	public void setRemindTime(Date remindTime)
	{
		this.remindTime = remindTime;
	}
}
