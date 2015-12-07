package com.gdxz.zhongbao.client.utils;

import android.content.Context;

import com.lidroid.xutils.DbUtils;

/**
 * Created by chenantao on 2015/8/7.
 */
public class DatabaseUtils
{

	public static DbUtils getDbUtils(Context context)
	{
		DbUtils dbUtils = DbUtils.create(context);
		dbUtils.configAllowTransaction(true);
		dbUtils.configDebug(true);
		return dbUtils;
	}
}
