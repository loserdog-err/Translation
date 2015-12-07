package com.gdxz.zhongbao.client.Service;

import android.os.Handler;

/**
 * Created by chenantao on 2015/7/18.
 */
public interface TeamService
{
	void createTeam(String teamName, String teamDeclaration, String teamLogo, Handler handler,String userId);

	void getTeamList(int category, Handler handler,String condition);

	void getTeamById(long teamId, Handler handler);

	void addGroupMember(String currentUserId, Long id, Handler handler);

	void disagreeJoin(long teamId, Integer id);

	void exitGroup(String currentUserId, Long id, Handler handler, String groupOwner);
}
