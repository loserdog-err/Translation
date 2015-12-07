package com.gdxz.zhongbao.server.servlet;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.pattern.IntegerPatternConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.gdxz.zhongbao.server.domain.Orders;
import com.gdxz.zhongbao.server.service.OrderService;
import com.google.gson.Gson;

@Controller
@Scope("protype")
public class OrderServlet extends BaseServlet
{
	@Autowired
	private OrderService orderService;

	public void saveOrder(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int userId = Integer.parseInt(req.getParameter("userId"));
		String orderId = req.getParameter("orderId");
		float price = Float.parseFloat(req.getParameter("price"));
		orderService.saveOrder(userId, orderId, price, "购买积分");

	}

	/**
	 * 根据用户id获得订单列表
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void getOrderByUserId(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int userId = Integer.parseInt(req.getParameter("userId"));
		List<Orders> orderList = orderService.getOrderByUserId(userId);
		if (orderList == null)
		{
			orderList = new ArrayList<Orders>();
		}
		JSONObject json = new JSONObject();
		Gson gson = new Gson();
		json.put("isSuccess", true);
		json.put("orderList", gson.toJson(orderList));
		resp.getWriter().print(json.toString());
	}

	/**
	 * 根据id删除订单
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void deleteOrderById(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int orderId = Integer.parseInt(req.getParameter("orderId"));
		orderService.deleteById(orderId);

	}

	public void withdrawCash(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int userId = Integer.parseInt(req.getParameter("userId"));
		int point = Integer.parseInt(req.getParameter("point"));
		orderService.withdrawCash(userId, point);
	}

}
