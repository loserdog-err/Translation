package com.gdxz.zhongbao.server.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdxz.zhongbao.server.DAO.impl.DAOSupportImpl;
import com.gdxz.zhongbao.server.domain.AssetInfo;
import com.gdxz.zhongbao.server.domain.Orders;
import com.gdxz.zhongbao.server.domain.User;
import com.gdxz.zhongbao.server.service.AssetInfoService;
import com.gdxz.zhongbao.server.service.OrderService;
import com.gdxz.zhongbao.server.service.UserService;

@Service("orderService")
public class OrderServiceImpl extends DAOSupportImpl<Orders> implements OrderService
{
	@Autowired
	UserService userService;
	@Autowired
	AssetInfoService assetInfoService;

	public void saveOrder(int userId, String orderId, float price, String goodName)
	{
		User user = userService.getById(userId);
		Orders order = new Orders(orderId, price, new Date(), user, goodName, Orders.TYPE_BUY_POINT);
		// 维护用户资产表
		AssetInfo assetInfo = assetInfoService.queryInUniqueResult("from AssetInfo where userId=?",
				new Object[] { userId + "" });
		if (assetInfo == null)
		{
			assetInfo = new AssetInfo();
			assetInfo.setUser(user);
			assetInfo.setTodayPay(order.getPrice());
			assetInfo.setTotalPay(order.getPrice());
			assetInfo.setMonthPay(order.getPrice());
			assetInfoService.save(assetInfo);
		}
		else
		{
			assetInfo.setTodayPay(assetInfo.getTodayPay() + order.getPrice());
			assetInfo.setMonthPay(assetInfo.getMonthPay() + order.getPrice());
			assetInfo.setTotalPay(assetInfo.getTotalPay() + order.getPrice());
			assetInfoService.update(assetInfo);
		}
		// 更新用户积分//1块钱等于10万积分
		Integer point = user.getPoint();
		user.setPoint(point == null ? (int) (Orders.RATE_RMB_POINT * price)
				: (int) (point + Orders.RATE_RMB_POINT * price));
		save(order);
	}

	/**
	 * 根据用户id获得对应的订单列表
	 */
	public List<Orders> getOrderByUserId(int userId)
	{
		String hql = "from Orders where userId=?";
		List<Orders> orderList = query(hql, new Object[] { userId + "" });
		return orderList;
	}

	/**
	 * 提现申请
	 */
	public void withdrawCash(int userId, int point)
	{
		User user = userService.getById(userId);
		Orders orders = new Orders(UUID.randomUUID().toString(), (float) point
				/ Orders.RATE_RMB_POINT, new Date(), user, "提现(审核中)", Orders.TYPE_WITHDRAW_CASH);
		save(orders);
	}

}
