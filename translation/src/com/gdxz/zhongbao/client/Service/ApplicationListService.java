package com.gdxz.zhongbao.client.Service;

import android.os.Handler;

/**
 * Created by chenantao on 2015/7/20.
 */
public interface ApplicationListService
{
	void applyJoinTeam(final Long id, final String currentUserId, final Handler handler);

	void getApplicationList(long teamId, Handler handler);
}
