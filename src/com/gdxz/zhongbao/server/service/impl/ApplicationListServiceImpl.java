package com.gdxz.zhongbao.server.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DaoSupport;
import org.springframework.stereotype.Service;

import com.gdxz.zhongbao.server.DAO.impl.DAOSupportImpl;
import com.gdxz.zhongbao.server.domain.ApplicationList;
import com.gdxz.zhongbao.server.domain.Team;
import com.gdxz.zhongbao.server.domain.User;
import com.gdxz.zhongbao.server.service.ApplicationListService;
import com.gdxz.zhongbao.server.service.TeamService;
import com.gdxz.zhongbao.server.service.UserService;

@Service("applicationListService")
public class ApplicationListServiceImpl extends DAOSupportImpl<ApplicationList> implements
		ApplicationListService
{
	@Autowired
	TeamService teamService;
	@Autowired
	UserService userService;

	/**
	 * 申请加群
	 */
	public void applyJoinTeam(int userId, long teamId)
	{
		Team team = teamService.getById(teamId);
		User user = userService.getById(userId);
		// 先查看用户是否申请加入过该群，如申请过，直接返回
		ApplicationList applicationList = (ApplicationList) queryInUniqueResult(
				"from ApplicationList where userId=? and teamId=?", new Object[] {
						user.getId() + "", team.getId() + "" });
		if (applicationList != null)
		{
			return;
		}
		else
		{
			applicationList = new ApplicationList();
			applicationList.setApplyTime(new Date());
			applicationList.setTeam(team);
			applicationList.setUser(user);
			save(applicationList);
		}

	}

	/**
	 * 得到对应队伍的申请列表
	 */
	public List<ApplicationList> getApplicationList(long teamId)
	{
		String hql="from ApplicationList where teamId=? order by applyTime desc";
		List<ApplicationList> list=query(hql, new Object[]{teamId+""});
		return list;
	}

}
