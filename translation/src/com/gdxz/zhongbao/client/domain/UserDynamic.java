package com.gdxz.zhongbao.client.domain;

public class UserDynamic
{

	public static final int DYNAMIC_TYPE_ALL= 0;//所有类型
	public static final int DYNAMIC_TYPE_FOLLOW = 1;//关注
	public static final int DYNAMIC_TYPE_ASK = 2;//提问
	public static final int DYNAMIC_TYPE_ANSWER = 3;//回答
	private Integer id;
	private int type;
	//	private User user;
	private Question question;
	private Answer answer;

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
