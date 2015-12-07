package com.gdxz.zhongbao.server.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.gdxz.zhongbao.server.domain.AssetInfo;
import com.gdxz.zhongbao.server.service.AssetInfoService;
import com.google.gson.Gson;

@Controller
@Scope("prototype")
public class AssetInfoServlet extends BaseServlet
{
	@Autowired
	AssetInfoService assetInfoService;

	public void getAssetInfo(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int userId = Integer.parseInt(req.getParameter("userId"));
		AssetInfo assetInfo = assetInfoService.getAssetInfo(userId);
		JSONObject json = new JSONObject();
		json.put("isSuccess", true);
		json.put("assetInfo", new Gson().toJson(assetInfo));
		resp.getWriter().print(json.toString());

	}

}
