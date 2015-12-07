package com.gdxz.zhongbao.server.service;

import java.util.List;

import com.gdxz.zhongbao.server.DAO.DAOSupport;
import com.gdxz.zhongbao.server.domain.UserRemind;

public interface UserRemindService extends DAOSupport<UserRemind>
{

	List<UserRemind> getUsreRemind(int id);

	void setHaveRead(int userId);

}
