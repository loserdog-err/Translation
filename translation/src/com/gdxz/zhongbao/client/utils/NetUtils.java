package com.gdxz.zhongbao.client.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

/**
 * Created by chenantao on 2015/7/11.
 */
public class NetUtils
{
	public static final int REQ_CODE_OPEN_SETTING = 1;

	private NetUtils()
	{
		/* cannot be instantiated */
		throw new UnsupportedOperationException("cannot be instantiated");
	}

	/**
	 * 判断网络是否连接
	 *
	 * @param context
	 * @return
	 */
	public static boolean isConnected(Context context)
	{
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (null != connectivity)
		{
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (null != info && info.isConnected())
			{
				if (info.getState() == NetworkInfo.State.CONNECTED)
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 判断是否是wifi连接
	 */
	public static boolean isWifi(Context context)
	{
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null)
			return false;
		return cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;

	}

	/**
	 * 打开网络设置界面
	 */
	public static void openSetting(Activity activity)
	{
		Intent intent = null;
		if (android.os.Build.VERSION.SDK_INT > 10)
		{
			intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
		} else
		{
			intent = new Intent();
			ComponentName component = new ComponentName("com.android.settings", "com.android.settings" +
					".WirelessSettings");
			intent.setComponent(component);
			intent.setAction("android.intent.action.VIEW");
		}
//		Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS );
		activity.startActivityForResult(intent, REQ_CODE_OPEN_SETTING);
	}
}
