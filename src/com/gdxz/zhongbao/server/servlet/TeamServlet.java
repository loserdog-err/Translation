package com.gdxz.zhongbao.server.servlet;

import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.gdxz.zhongbao.server.domain.Team;
import com.gdxz.zhongbao.server.service.TeamService;
import com.google.gson.Gson;

@Controller
@Scope("prototype")
public class TeamServlet extends BaseServlet
{

	/**
	 * 获取团队列表时所要用到的常量值
	 */
	public static final int CATEGORY_SEARCH = 0;// 搜索团队
	public static final int CATEGORY_RANK = 1;// 团队排名
	public static final int CATEGORY_RECOMMEND = 2;// 推荐团队

	@Autowired
	TeamService teamService;

	/**
	 * 创建团队
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void createTeam(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		Gson gson = new Gson();
		Team team = gson.fromJson(req.getParameter("team"), Team.class);
		int userId = Integer.parseInt(req.getParameter("userId"));
		teamService.createTeam(team, userId);
	}

	/**
	 * 根据类型得到团队列表
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void getTeamList(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int category = Integer.parseInt(req.getParameter("category"));
		String condition = req.getParameter("condition");
		List<Team> teamList = teamService.getTeamList(category, condition);
		JSONObject json = new JSONObject();
		if (teamList != null && teamList.size() > 0)
		{
			json.put("haveTeamList", true);
			json.put("teamList", new Gson().toJson(teamList));
		}
		else
		{
			json.put("haveTeamList", false);
		}
		resp.getWriter().print(json.toString());
	}

	/**
	 * 通过teamId获得团队
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void getTeamById(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		long teamId = Long.parseLong(req.getParameter("teamId"));
		Team team = teamService.getTeamById(teamId);
		JSONObject json = new JSONObject();
		if (team != null)
		{
			json.put("haveTeam", true);
			json.put("team", new Gson().toJson(team));
		}
		else
		{
			json.put("haveTeam", false);
		}
		resp.getWriter().print(json.toString());
	}

	/**
	 * 添加群成员
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void addGroupMember(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		System.out.println("add member request");
		long teamId = Long.parseLong(req.getParameter("teamId"));
		int userId = Integer.parseInt(req.getParameter("userId"));
		teamService.addGroupMember(teamId, userId);
		JSONObject json = new JSONObject();
		json.put("isSuccess", true);
		resp.getWriter().print(json.toString());
	}

	/**
	 * 拒绝成员加入
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void disagreeJoin(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		System.out.println("disagree member request");
		int userId = Integer.parseInt(req.getParameter("userId"));
		long teamId = Long.parseLong(req.getParameter("teamId"));
		teamService.disagreeJoin(teamId, userId);
	}

	/**
	 * 退出群组
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void exitGroup(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int userId = Integer.parseInt(req.getParameter("userId"));
		long teamId = Long.parseLong(req.getParameter("teamId"));
		String newOwner = req.getParameter("newOwner");
		boolean result = teamService.exitGroup(userId, teamId, newOwner);
		JSONObject json = new JSONObject();
		json.put("isSuccess", result);
		resp.getWriter().print(json.toString());
	}

}
