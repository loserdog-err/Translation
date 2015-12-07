package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.view.KeyEvent;

import com.gdxz.zhongbao.client.utils.DialogUtils;

/**
 * Created by chenantao on 2015/8/7.
 */
public class BaseActivity extends Activity
{
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			DialogUtils.closeProgressDialog();
		}
		return super.onKeyDown(keyCode, event);
	}
}
