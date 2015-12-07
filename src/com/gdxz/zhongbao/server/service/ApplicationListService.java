package com.gdxz.zhongbao.server.service;

import java.util.List;

import com.gdxz.zhongbao.server.DAO.DAOSupport;
import com.gdxz.zhongbao.server.domain.ApplicationList;

public interface ApplicationListService extends DAOSupport<ApplicationList>
{
	void applyJoinTeam(int userId, long teamId);

	List<ApplicationList> getApplicationList(long teamId);
}
