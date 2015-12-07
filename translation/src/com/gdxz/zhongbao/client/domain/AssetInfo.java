package com.gdxz.zhongbao.client.domain;

public class AssetInfo
{

	private int id;
	private float todayPay;// 今日支出
	private float monthPay;// 本月支出
	private float totalPay;// 总支出
	//	private String latelyPay;// 最近7天消费，以json的形式存储在数据库
	private float oneAgoPay;// 昨天消费
	private float twoAgoPay;// 前天消费
	private float threeAgoPay;// 大前天消费
	private float fourAgoPay;
	private float fiveAgoPay;
	private float sixAgoPay;

	private User user;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public float getTodayPay()
	{
		return todayPay;
	}

	public void setTodayPay(float todayPay)
	{
		this.todayPay = todayPay;
	}

	public float getMonthPay()
	{
		return monthPay;
	}

	public void setMonthPay(float monthPay)
	{
		this.monthPay = monthPay;
	}

	public float getTotalPay()
	{
		return totalPay;
	}

	public void setTotalPay(float totalPay)
	{
		this.totalPay = totalPay;
	}

	public float getOneAgoPay()
	{
		return oneAgoPay;
	}

	public void setOneAgoPay(float oneAgoPay)
	{
		this.oneAgoPay = oneAgoPay;
	}

	public float getTwoAgoPay()
	{
		return twoAgoPay;
	}

	public void setTwoAgoPay(float twoAgoPay)
	{
		this.twoAgoPay = twoAgoPay;
	}

	public float getThreeAgoPay()
	{
		return threeAgoPay;
	}

	public void setThreeAgoPay(float threeAgoPay)
	{
		this.threeAgoPay = threeAgoPay;
	}

	public float getFourAgoPay()
	{
		return fourAgoPay;
	}

	public void setFourAgoPay(float fourAgoPay)
	{
		this.fourAgoPay = fourAgoPay;
	}

	public float getFiveAgoPay()
	{
		return fiveAgoPay;
	}

	public void setFiveAgoPay(float fiveAgoPay)
	{
		this.fiveAgoPay = fiveAgoPay;
	}

	public float getSixAgoPay()
	{
		return sixAgoPay;
	}

	public void setSixAgoPay(float sixAgoPay)
	{
		this.sixAgoPay = sixAgoPay;
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
