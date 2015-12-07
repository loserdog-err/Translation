package com.gdxz.zhongbao.server.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.gdxz.zhongbao.server.DAO.impl.DAOSupportImpl;
import com.gdxz.zhongbao.server.domain.User;
import com.gdxz.zhongbao.server.domain.UserDynamic;
import com.gdxz.zhongbao.server.service.UserDynamicService;
import com.gdxz.zhongbao.server.servlet.UserDynamicServlet;

@Service("userDynamicService")
public class UserDynamicServiceImpl extends DAOSupportImpl<UserDynamic> implements
		UserDynamicService
{

	/**
	 * 加载用户的动态信息
	 */
	public List<UserDynamic> loadDynamicData(int category, int userId)
	{
		String hql = "";
		Object[] params;
		if (category == UserDynamic.DYNAMIC_TYPE_ALL)
		{
			hql = "from UserDynamic where userId=?";
			params = new Object[] { userId };
		}
		else
		{
			hql = "from UserDynamic where userId=? and type=?";
			params = new Object[] { userId, category };
		}
		hql += " order by updateTime desc";
		List<UserDynamic> list = query(hql, params);
		return list;
	}

	/**
	 * 得到用户所有动态信息的map对象，包含： 1：全部动态 2：我的回答 3：我的提问 4：我的关注
	 */
	public Map<String, String> getUserDynamicCount(Integer id)
	{
		Map<String, String> userDynamicCount = new HashMap<String, String>();
		userDynamicCount.put(UserDynamic.DYNAMIC_TYPE_ALL + "", getUserAllDynamicCount(id));
		userDynamicCount.put(UserDynamic.DYNAMIC_TYPE_ANSWER + "", getUserAnswerDynamicCount(id));
		userDynamicCount.put(UserDynamic.DYNAMIC_TYPE_ASK + "", getUserAskDynamicCount(id));
		userDynamicCount.put(UserDynamic.DYNAMIC_TYPE_FOLLOW + "", getUserFollowDynamicCount(id));
		Set<String> set = userDynamicCount.keySet();
		return userDynamicCount;
	}

	/**
	 * 得到用户所有动态信息的数量
	 */
	public String getUserAllDynamicCount(Integer id)
	{
		String hql = "select count(*) from UserDynamic where userId=? ";
		return queryCount(hql, new Object[] { id }) + "";
	}

	/**
	 * 得到用户回复的数量
	 */
	public String getUserAnswerDynamicCount(Integer id)
	{
		String hql = "select count(*) from UserDynamic where userId=? and type=?";
		return queryCount(hql, new Object[] { id, UserDynamic.DYNAMIC_TYPE_ANSWER }) + "";
	}

	/**
	 * 得到用户提问的数量
	 */
	public String getUserAskDynamicCount(Integer id)
	{
		String hql = "select count(*) from UserDynamic where userId=? and type=?";
		return queryCount(hql, new Object[] { id, UserDynamic.DYNAMIC_TYPE_ASK }) + "";
	}

	/**
	 * 得到用户关注问题的数量
	 */
	public String getUserFollowDynamicCount(Integer id)
	{
		String hql = "select count(*) from UserDynamic where userId=? and type=?";
		return queryCount(hql, new Object[] { id, UserDynamic.DYNAMIC_TYPE_FOLLOW }) + "";
	}

	/**
	 * 根据问题id获得关注该问题的用户
	 */
	public List<User> getFollwersByQuestionId(Integer id)
	{
		String hql = "from UserDynamic where userId=? and type=?";
		List<UserDynamic> dynamicLists=query(hql, new Object[] { id, UserDynamic.DYNAMIC_TYPE_FOLLOW });
		//获得动态信息所归属的用户
		List<User> list=new ArrayList<User>();
		for(UserDynamic userDynamic:dynamicLists)
		{
			list.add(userDynamic.getUser());
		}
		return list;
	}
}
