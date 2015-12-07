package com.gdxz.zhongbao.server.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gdxz.zhongbao.server.DAO.impl.DAOSupportImpl;
import com.gdxz.zhongbao.server.domain.UserRemind;
import com.gdxz.zhongbao.server.service.UserRemindService;

@Service("userRemindService")
public class UserRemindServiceImpl extends DAOSupportImpl<UserRemind> implements UserRemindService
{

	/**
	 * 得到用户提醒列表
	 */
	public List<UserRemind> getUsreRemind(int id)
	{
		String hql = "from UserRemind where userId=? order by remindTime desc";
		List<UserRemind> list = query(hql, new Object[] { id + "" });
		return list;

	}

	/**
	 * 设置消息为已读
	 */
	public void setHaveRead(int userId)
	{

		String hql = "update UserRemind set isRead=? where userId=?";
		executeHql(hql, new Object[] { true, userId + "" });
	}

}
