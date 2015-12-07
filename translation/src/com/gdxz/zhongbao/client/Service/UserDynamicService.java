package com.gdxz.zhongbao.client.Service;

import android.os.Handler;

/**
 * Created by chenantao on 2015/7/15.
 */
public interface UserDynamicService
{
	void loadData(int category, String currentUserId,Handler handler);
}
