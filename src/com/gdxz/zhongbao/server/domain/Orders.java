package com.gdxz.zhongbao.server.domain;

import java.util.Date;

public class Orders
{
	public static final int TYPE_WITHDRAW_CASH = 0;// 提现
	public static final int TYPE_BUY_POINT = 1;// 购买积分
	
	public static final int RATE_RMB_POINT=100000;
	private Integer id;

	private String orderId;// 使用bmob生成的订单号
	private float price;// 价格
	private String goodName;// 商品名
	private Integer type;

	public String getGoodName()
	{
		return goodName;
	}

	public void setGoodName(String goodName)
	{
		this.goodName = goodName;
	}

	private Date orderTime;
	private User user;

	public Orders(String orderId, float price, Date orderTime, User user, String goodName,
			Integer type)
	{
		super();
		this.orderId = orderId;
		this.price = price;
		this.orderTime = orderTime;
		this.goodName=goodName;
		this.user = user;
		this.type = type;
	}

	public Integer getType()
	{
		return type;
	}

	public void setType(Integer type)
	{
		this.type = type;
	}

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
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

	public Orders()
	{

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
