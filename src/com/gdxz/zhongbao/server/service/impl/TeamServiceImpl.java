package com.gdxz.zhongbao.server.service.impl;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.jpush.api.JPushClient;

import com.gdxz.zhongbao.server.DAO.impl.DAOSupportImpl;
import com.gdxz.zhongbao.server.domain.ApplicationList;
import com.gdxz.zhongbao.server.domain.Team;
import com.gdxz.zhongbao.server.domain.User;
import com.gdxz.zhongbao.server.service.ApplicationListService;
import com.gdxz.zhongbao.server.service.TeamService;
import com.gdxz.zhongbao.server.service.UserService;
import com.gdxz.zhongbao.server.servlet.TeamServlet;
import com.gdxz.zhongbao.server.utils.FileUtils;
import com.gdxz.zhongbao.server.utils.JpushClientUtils;
import com.gdxz.zhongbao.server.utils.PropertiesUtils;

@Service("teamService")
public class TeamServiceImpl extends DAOSupportImpl<Team> implements TeamService
{
	@Autowired
	UserService userService;
	@Autowired
	ApplicationListService applicationListService;

	public void createTeam(Team team, int userId)
	{
		team.setScore(0);
		int count = queryCount("select count(*) from Team", null);
		team.setRank(count + 1);
		save(team);
		// 维护user属性
		User user = userService.getById(userId);
		user.setTeam(team);
		userService.update(user);
	}

	public List<Team> getTeamList(int category, String condition)
	{
		String hql = "";
		Object[] params = null;
		if (category == TeamServlet.CATEGORY_RECOMMEND)
		{
			hql = "from Team";
		}
		else if (category == TeamServlet.CATEGORY_SEARCH)
		{
			if (condition != null)
			{
				hql = "from Team where name like ?";
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < condition.toCharArray().length; i++)
				{
					sb.append("%" + condition.toCharArray()[i]);
				}
				condition = sb.toString() + "%";
				params = new Object[] { condition };
				System.out.println(condition);
			}
		}
		else if (category == TeamServlet.CATEGORY_RANK)
		{
			hql = "from Team order by score desc,buildTime desc";
		}
		List<Team> teamList = query(hql, params);
		return teamList;
	}

	/**
	 * 通过teamId获得队伍
	 */
	public Team getTeamById(long teamId)
	{
		return getById(teamId);
	}

	/**
	 * 添加群成员 1：维护user表，applicationList表 2：推送消息给该成员
	 */
	public void addGroupMember(long teamId, int userId)
	{
		System.out.println("teamId:" + teamId + " userId:" + userId);
		User user = userService.getById(userId);
		Team team = getById(teamId);
		user.setTeam(team);
		userService.update(user);
		String hql = "from ApplicationList where teamId=? and userId=?";
		ApplicationList applicationList = applicationListService.queryInUniqueResult(hql,
				new Object[] { teamId + "", userId + "" });
		applicationList.setIsHandle(true);
		applicationListService.update(applicationList);
		JpushClientUtils.sendPush(JpushClientUtils.buildPushObject_all_alias_alert(
				user.getUsername(), team.getName() + " 接受了你的加入申请"));
	}

	/**
	 * 拒绝该成员加入 1：维护applicationList表 2：推送消息给该成员
	 */
	public void disagreeJoin(long teamId, int userId)
	{
		String hql = "from ApplicationList where teamId=? and userId=?";
		ApplicationList applicationList = applicationListService.queryInUniqueResult(hql,
				new Object[] { teamId + "", userId + "" });
		applicationList.setIsHandle(true);
		applicationListService.update(applicationList);
		// 推送消息给该用户
		User user = userService.getById(userId);
		Team team = getById(teamId);
		JpushClientUtils.sendPush(JpushClientUtils.buildPushObject_all_alias_alert(
				user.getUsername(), team.getName() + " 拒绝了你的加入申请"));
	}

	/**
	 * 退出群组 1：更新用户信息 2：推送到新群主
	 */
	public boolean exitGroup(int userId, long teamId, String newOwner)
	{
		try
		{
			User user = userService.getById(userId);
			user.setTeam(null);
			userService.update(user);
			// 推送消息
			Team team = getById(teamId);
			if (newOwner == null)// 删除该群
			{
				deleteById(teamId);
			}
			else
			{
				JpushClientUtils.sendPush(JpushClientUtils.buildPushObject_all_alias_alert(
						newOwner, user.getUsername() + " 退出了 " + team.getName() + ",你现在是新群主了"));
			}

			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * 上传team logo
	 */
	public void uploadTeamLogo(Long groupId, Iterator<FileItem> it) throws Exception
	{
		while (it.hasNext())
		{
			FileItem fileItem = it.next();
			if (fileItem.isFormField())
			{
				continue;
			}
			else
			{
				String fileName = fileItem.getName();
				String fileType = FileUtils.getFileType(fileName);
				File saveFile = FileUtils.createFile(
						PropertiesUtils.getProperty("uploadFileBasePath")
								+ Team.getTeamImageBasePath(), fileType);
				fileItem.write(saveFile);
				Team team = getById(groupId);
				team.setLogo(Team.getTeamImageBasePath() + saveFile.getName());
				update(team);
			}
		}
	}

}
