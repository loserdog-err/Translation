package com.gdxz.zhongbao.server.servlet;

import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.gdxz.zhongbao.server.domain.ApplicationList;
import com.gdxz.zhongbao.server.service.ApplicationListService;
import com.gdxz.zhongbao.server.service.TeamService;
import com.google.gson.Gson;

import net.sf.json.JSONObject;

@Controller
@Scope("prototype")
public class ApplicationListServlet extends BaseServlet
{

	@Autowired
	TeamService teamService;
	@Autowired
	ApplicationListService applicationListService;

	/**
	 * 申请加入群
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void applyJoinGroup(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		System.out.println("apply add requst");
		int userId = Integer.parseInt(req.getParameter("userId"));
		long teamId = Long.parseLong(req.getParameter("teamId"));
		applicationListService.applyJoinTeam(userId, teamId);
		JSONObject json = new JSONObject();
		json.put("isSuccess", true);
		resp.getWriter().print(json.toString());
	}

	/**
	 * 得到队伍的申请列表
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void getApplicationList(HttpServletRequest req, HttpServletResponse resp)
			throws Exception
	{
		long teamId = Long.parseLong(req.getParameter("teamId"));
		List<ApplicationList> list = applicationListService.getApplicationList(teamId);
		JSONObject json = new JSONObject();
		if (list != null)
		{
			json.put("isSuccess", true);
			json.put("applicationList", new Gson().toJson(list));
		}
		else
		{
			json.put("isSuccess", false);
		}
		resp.getWriter().print(json.toString());
	}
}
