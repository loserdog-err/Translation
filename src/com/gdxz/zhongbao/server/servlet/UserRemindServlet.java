package com.gdxz.zhongbao.server.servlet;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.gdxz.zhongbao.server.domain.UserRemind;
import com.gdxz.zhongbao.server.service.UserRemindService;
import com.google.gson.Gson;

@Controller
@Scope("prototype")
public class UserRemindServlet extends BaseServlet
{
	@Autowired
	UserRemindService userRemindService;
	/**
	 * 得到用户提醒
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void getUserRemind(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int id=Integer.parseInt(req.getParameter("userId"));
		List<UserRemind> list=userRemindService.getUsreRemind(id);
		JSONObject json=new JSONObject();
		if(list!=null)
		{
			json.put("isSuccess", true);
			json.put("userRemindList", new Gson().toJson(list));
		}
		else
		{
			json.put("isSuccess", false);
		}
		resp.getWriter().print(json.toString());
	}
	
	/**
	 * 设置为已读
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void setHaveRead(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int userId=Integer.parseInt(req.getParameter("userId"));
		userRemindService.setHaveRead(userId);
	}

}
