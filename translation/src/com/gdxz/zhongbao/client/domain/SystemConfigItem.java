package com.gdxz.zhongbao.client.domain;

/**
 * Created by Chean_antao on 2015/8/9.
 */
public class SystemConfigItem
{
	String configName;

	Object configValue;

	public SystemConfigItem(String configName, Object configValue)
	{
		this.configName=configName;
		this.configValue=configValue;
	}


	public String getConfigName()
	{
		return configName;
	}

	public void setConfigName(String configName)
	{
		this.configName = configName;
	}

	public Object getConfigValue()
	{
		return configValue;
	}

	public void setConfigValue(Object configValue)
	{
		this.configValue = configValue;
	}
}
