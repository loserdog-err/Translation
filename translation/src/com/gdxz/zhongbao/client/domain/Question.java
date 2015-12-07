package com.gdxz.zhongbao.client.domain;

import com.lidroid.xutils.db.annotation.Finder;
import com.lidroid.xutils.db.annotation.Foreign;
import com.lidroid.xutils.db.annotation.NoAutoIncrement;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Question implements Serializable
{

	@NoAutoIncrement
	public int id;
	public String content;
	public Date postTime;
	public int replyCount;// 回复数量
	@Foreign(column = "userId", foreign = "id")
	public User author;
	private String title;
	private Integer rewardAmount;// 悬赏金额
	private Integer browseCount; // 问题被浏览的次数
	private boolean isSolve;

	private Integer todayReplyChange;// 今日回复变化量

	public static String getQuestionImagePath(String userId)
	{
		return "image/" + userId + "/question/";
	}
//	public static String getQuestionVoicePath(String userId)
//	{
//		return "voice/"+userId+"/question/";
//	}


	//xutils需要的
	@Finder(valueColumn = "id", targetColumn = "questionId")
	private List<Answer> answers;

	public List<Answer> getAnswers()
	{
		return answers;
	}

	public void setAnswers(List<Answer> answers)
	{
		this.answers = answers;
	}

	public Integer getTodayReplyChange()
	{
		return todayReplyChange;
	}

	public void setTodayReplyChange(Integer todayReplyChange)
	{
		this.todayReplyChange = todayReplyChange;
	}
//	public Set<Answer> getAnswers()
//	{
//		return answers;
//	}
//
//	public void setAnswers(Set<Answer> answers)
//	{
//		this.answers = answers;
//	}


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

	public void setSolve(boolean isSolve)
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
}
