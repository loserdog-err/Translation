package com.gdxz.zhongbao.client.domain;

import java.util.Date;

public class Orders
{
	public static final int TYPE_WITHDRAW_CASH = 0;// 提现
	public static final int TYPE_BUY_POINT = 1;// 购买积分


	private int id;
	private String orderId;// 使用bmob生成的订单号
	private String goodName;
	private int type;
	private float price;// 价格
	private Date orderTime;
	private User user;

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}
	public String getGoodName()
	{
		return goodName;
	}

	public void setGoodName(String goodName)
	{
		this.goodName = goodName;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getOrderId()
	{
		return orderId;
	}

	public void setOrderId(String orderId)
	{
		this.orderId = orderId;
	}

	public float getPrice()
	{
		return price;
	}

	public void setPrice(float price)
	{
		this.price = price;
	}

	public Date getOrderTime()
	{
		return orderTime;
	}

	public void setOrderTime(Date orderTime)
	{
		this.orderTime = orderTime;
	}

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
	{
		this.user = user;
	}
}
