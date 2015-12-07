package com.gdxz.zhongbao.client.common;

import android.app.Activity;
import android.app.Application;
import android.graphics.Typeface;

import com.gdxz.zhongbao.client.utils.FileUtils;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.LinkedList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.im.android.api.JMessageClient;

public class MyApplication extends Application
{
	private List<Activity> mList = new LinkedList<Activity>();
	private static MyApplication instance;

	private static final String CANARO_EXTRA_BOLD_PATH = "fonts/canaro_extra_bold.otf";
	public static Typeface canaroExtraBold;

	public static boolean isAutoLogin = true;
	public static boolean isReceivePush = true;

	public MyApplication()
	{
	}

	public synchronized static MyApplication getInstance()
	{
		if (null == instance)
		{
			instance = new MyApplication();
		}
		return instance;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
//		initTypeface();
		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder
				(getApplicationContext())
				.memoryCache(new LruMemoryCache(2 * 1024 * 1024))
				.memoryCacheSize(2 * 1024 * 1024)
				.diskCacheSize(50 * 1024 * 1024)
				.diskCacheFileCount(100)
				.diskCache(new UnlimitedDiskCache(FileUtils.getImageCacheDir()))
//				.writeDebugLogs()
				.build();
		//Initialize ImageLoader with configuration.
		JMessageClient.init(getApplicationContext());
		JPushInterface.setDebugMode(true);
		JMessageClient.setNotificationMode(JMessageClient.NOTI_MODE_DEFAULT);
		ImageLoader.getInstance().init(configuration);
	}

	private void initTypeface()
	{
		canaroExtraBold = Typeface.createFromAsset(getAssets(), CANARO_EXTRA_BOLD_PATH);
	}


	// add Activity
	public void addActivity(Activity activity)
	{
		mList.add(activity);
	}

	public void exit()
	{
		try
		{
			for (Activity activity : mList)
			{
				if (activity != null)
					activity.finish();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			System.exit(0);
		}
	}

	/**
	 * 清除维护的activity，不退出应用
	 */
	public void clearActivity()
	{
		try
		{
			for (Activity activity : mList)
			{
				if (activity != null)
					activity.finish();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void onLowMemory()
	{
		super.onLowMemory();
		System.gc();
	}
}
