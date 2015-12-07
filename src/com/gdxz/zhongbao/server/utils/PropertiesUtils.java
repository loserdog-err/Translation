package com.gdxz.zhongbao.server.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtils
{
	private static Properties prop;
	static
	{
		prop = new Properties();
		InputStream in = PropertiesUtils.class.getResourceAsStream("/properties.properties");
		try
		{
			prop.load(in);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static String getProperty(String key)
	{
		String value = prop.getProperty(key);
		if (value != null)
		{
			return value;
		}
		return null;
	}

}
