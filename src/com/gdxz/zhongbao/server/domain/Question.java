package com.gdxz.zhongbao.server.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Question extends Article
{
	private String title;
	private Integer rewardAmount;// 悬赏金额
	private Integer browseCount; // 问题被浏览的次数
	private boolean isSolve;

	private Integer todayReplyChange;// 今日回复变化量

	//不需要映射到hbm.xml文件中的
//	 private Set<Answer> answers=new HashSet<Answer>();//问题中的回复

	 
	public static String getQuestionImageBasePath(int userId)
	{
		return "image/" + userId + "/question/";
	}

	public static String getQuestionVoiceBasePath(int userId)
	{
		return "voice/" + userId + "/question/";
	}

	public Question()
	{
	};

	public Integer getRewardAmount()
	{
		return rewardAmount;
	}

	public void setRewardAmount(Integer rewardAmount)
	{
		this.rewardAmount = rewardAmount;
	}

	public Integer getBrowseCount()
	{
		return browseCount;
	}

	public void setBrowseCount(Integer browseCount)
	{
		this.browseCount = browseCount;
	}

	public boolean getIsSolve()
	{
		return isSolve;
	}

	public void setIsSolve(boolean isSolve)
	{
		this.isSolve = isSolve;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public Integer getTodayReplyChange()
	{
		return todayReplyChange;
	}

	public void setTodayReplyChange(Integer todayReplyChange)
	{
		this.todayReplyChange = todayReplyChange;
	}

	// public Set<Answer> getAnswers()
	// {
	// return answers;
	// }
	//
	// public void setAnswers(Set<Answer> answers)
	// {
	// this.answers = answers;
	// }
}
