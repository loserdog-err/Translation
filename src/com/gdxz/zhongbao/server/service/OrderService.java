package com.gdxz.zhongbao.server.service;

import java.util.List;

import com.gdxz.zhongbao.server.DAO.DAOSupport;
import com.gdxz.zhongbao.server.domain.Orders;

public interface OrderService extends DAOSupport<Orders>
{

	void saveOrder(int userId, String orderId, float price, String goodName);

	List<Orders> getOrderByUserId(int userId);

	void withdrawCash(int userId, int point);

}
