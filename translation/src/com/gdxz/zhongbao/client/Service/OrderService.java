package com.gdxz.zhongbao.client.Service;

import android.os.Handler;

/**
 * Created by Chean_antao on 2015/8/13.
 */
public interface OrderService
{
	void saveOrder(String orderId, String currentUserId, float price);

	void getOrderByUserId(String currentUserId, Handler handler);

	void deleteOrderById(int id);

	void withdrawCash(String currentUserId, int point);
}
