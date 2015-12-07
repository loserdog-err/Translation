package com.gdxz.zhongbao.client.domain;

import com.lidroid.xutils.db.annotation.NoAutoIncrement;

/**
 * Created by Chean_antao on 2015/8/9.
 */
public class SystemConfig
{
	@NoAutoIncrement
	int id;//

	boolean isReceivePush;
	boolean isAutoLogin;

	public SystemConfig(){};
	public SystemConfig(int id, boolean isReceivePush, boolean isAutoLogin)
	{
		this.id = id;
		this.isReceivePush = isReceivePush;
		this.isAutoLogin = isAutoLogin;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public boolean isReceivePush()
	{
		return isReceivePush;
	}

	public void setIsReceivePush(boolean isReceivePush)
	{
		this.isReceivePush = isReceivePush;
	}

	public boolean isAutoLogin()
	{
		return isAutoLogin;
	}

	public void setIsAutoLogin(boolean isAutoLogin)
	{
		this.isAutoLogin = isAutoLogin;
	}
}
