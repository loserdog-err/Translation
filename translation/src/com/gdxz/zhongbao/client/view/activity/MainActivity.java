package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.bmob.pay.tool.BmobPay;
import com.gdxz.zhongbao.client.Service.impl.UserServiceImpl;
import com.gdxz.zhongbao.client.common.MyApplication;

import java.util.Timer;
import java.util.TimerTask;

import cn.jpush.android.api.JPushInterface;

public class MainActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		String currentUserId = UserServiceImpl.getCurrentUserId(this);
		if (currentUserId == null || "".equals(currentUserId))//是第一次启动应用
		{
			gotoGuideActivity();
		}
		else
		{
			gotoLoginUI();
		}
		MyApplication.getInstance().addActivity(this);
		BmobPay.init(this, "d846b201d115d30761c967d88ebd773b");//bmob支付组件
	}

	/**
	 * 2秒后由欢迎界面转到登录界面
	 */
	public void gotoLoginUI()
	{
		final Intent it = new Intent(this, LoginActivity.class);
		Timer timer = new Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				startActivity(it);
				finish();
			}
		};
		timer.schedule(task, 2000);
	}

	public void gotoGuideActivity()
	{
		final Intent intent = new Intent(this, GuideActivity.class);
		Timer timer = new Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				startActivity(intent);
				finish();
			}
		};
		timer.schedule(task, 2000);
	}

	@Override
	protected void onResume()
	{
		JPushInterface.onResume(this);
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		JPushInterface.onPause(this);
		super.onPause();
	}
}


