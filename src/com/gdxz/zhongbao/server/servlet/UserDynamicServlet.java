package com.gdxz.zhongbao.server.servlet;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.gdxz.zhongbao.server.domain.UserDynamic;
import com.gdxz.zhongbao.server.service.UserDynamicService;
import com.google.gson.Gson;

@Controller
@Scope("prototype")
public class UserDynamicServlet extends BaseServlet
{
	@Autowired
	UserDynamicService userDynamicService;

	public void loadDynamicData(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int category = Integer.parseInt(req.getParameter("category"));
		int userId = Integer.parseInt(req.getParameter("userId"));
		List<UserDynamic> list = userDynamicService.loadDynamicData(category, userId);
		JSONObject json = new JSONObject();
		if (list != null)
		{
			json.put("isSuccess", true);
			if (list.size() < 1)
			{
				list = new ArrayList<UserDynamic>();
			}
			Gson gson = new Gson();
			String data = gson.toJson(list);
			json.put("data", data);
		}
		else
		{
			json.put("isSuccess", false);
		}
		resp.getWriter().print(json.toString());
	}
}
