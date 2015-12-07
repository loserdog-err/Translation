package com.gdxz.zhongbao.client.Service.impl;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.gdxz.zhongbao.client.Service.OrderService;
import com.gdxz.zhongbao.client.domain.Orders;
import com.gdxz.zhongbao.client.utils.HttpUtils;
import com.gdxz.zhongbao.client.view.activity.GeneralJournalActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Chean_antao on 2015/8/13.
 */
public class OrderServiceImpl implements OrderService
{
	/**
	 * 提交订单号
	 *
	 * @param orderId
	 * @param currentUserId
	 * @param price
	 */
	@Override
	public void saveOrder(final String orderId, final String currentUserId, final float price)
	{
		new AsyncTask<Void, Void, Void>()
		{

			@Override
			protected Void doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "OrderServlet";
				Map<String, String> rawParams = new HashMap();
				rawParams.put("method", "saveOrder");
				rawParams.put("userId", currentUserId);
				rawParams.put("orderId", orderId);
				rawParams.put("price", price + "");
				try
				{
					HttpUtils.postRequest(url, rawParams);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				return null;
			}
		}.execute();
	}

	/**
	 * 根据用户id获得对应的订单号
	 * @param currentUserId
	 * @param handler
	 */
	@Override
	public void getOrderByUserId(final String currentUserId, final Handler handler)
	{
		new AsyncTask<Void, Void, List<Orders>>()
		{

			@Override
			protected List<Orders> doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "OrderServlet";
				Map<String, String> rawParams = new HashMap<String, String>();
				rawParams.put("method", "getOrderByUserId");
				rawParams.put("userId", currentUserId);
				try
				{
					JSONObject json = new JSONObject(HttpUtils.postRequest(url, rawParams));
					if (json.getBoolean("isSuccess"))
					{
						Gson gson = new Gson();
						List<Orders> orderList=gson.fromJson(json.getString("orderList"), new TypeToken<List<Orders>>()
						{
						}.getType());
						if(orderList==null) orderList = new ArrayList<Orders>();
						return orderList;
					}
				} catch (Exception e)
				{
					e.printStackTrace();
					return null;
				}
				return null;
			}

			@Override
			protected void onPostExecute(List<Orders> orderses)
			{
				if (orderses != null)
				{
					Message msg=Message.obtain();
					msg.what = GeneralJournalActivity.MSG_LOAD_ORDER_COMPLETE;
					msg.obj = orderses;
					handler.sendMessage(msg);
				}
				else
				{
					handler.sendEmptyMessage(GeneralJournalActivity.MSG_LOAD_ORDER_ERROR);
				}
				super.onPostExecute(orderses);
			}
		}.execute();
	}

	@Override
	public void deleteOrderById(final int id)
	{
		new AsyncTask<Void, Void, Void>()
		{

			@Override
			protected Void doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "OrderServlet";
				Map<String, String> rawParamas = new HashMap<String, String>();
				rawParamas.put("method", "deleteOrderById");
				rawParamas.put("orderId", id + "");
				try
				{
					HttpUtils.postRequest(url, rawParamas);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				return null;
			}
		}.execute();
	}

	/**
	 * 提现申请
	 * @param currentUserId
	 * @param point
	 */
	@Override
	public void withdrawCash(final String currentUserId, final int point)
	{
		new AsyncTask<Void, Void, Void>()
		{

			@Override
			protected Void doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "OrderServlet";
				Map<String, String> rawParams = new HashMap<String, String>();
				rawParams.put("method", "withdrawCash");
				rawParams.put("userId", currentUserId);
				rawParams.put("point", point + "");
				try
				{
					HttpUtils.postRequest(url, rawParams);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				return null;
			}
		}.execute();
	}
}
