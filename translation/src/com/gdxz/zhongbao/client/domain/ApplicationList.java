package com.gdxz.zhongbao.client.domain;

import java.util.Date;

/**
 * 用户申请入群的列表
 *
 * @author chenantao
 */
public class ApplicationList
{
	Integer id;
	Date applyTime;//申请时间
	Team team;//用户申请加入的群
	User user;//申请用户
	private boolean isHandle;//标识是否已经处理

	public boolean getIsHandle()
	{
		return isHandle;
	}

	public void setIsHandle(boolean isHandle)
	{
		this.isHandle = isHandle;
	}

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public Date getApplyTime()
	{
		return applyTime;
	}

	public void setApplyTime(Date applyTime)
	{
		this.applyTime = applyTime;
	}

	public Team getTeam()
	{
		return team;
	}

	public void setTeam(Team team)
	{
		this.team = team;
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
