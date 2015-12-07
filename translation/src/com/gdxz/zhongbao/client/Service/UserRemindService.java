package com.gdxz.zhongbao.client.Service;

import android.os.Handler;

/**
 * Created by Chean_antao on 2015/8/11.
 */
public interface UserRemindService
{
	void getUserRemind(String userId, Handler handler);

	void setHaveRead(String userId);
}
