package com.gdxz.zhongbao.server.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.exception.DataException;

/**
 * 操作的date的工具类
 * @author chenantao
 *
 */
public class DateUtils
{

	/**
	 * 将特定格式的字符串转换为date对象
	 * @param date
	 * @param format 格式，如yyyy-MM-dd
	 * @return
	 */
	public static Date string2date(String date,String format)
	{
		try
		{
			SimpleDateFormat sdf=new SimpleDateFormat(format);
			Date result=sdf.parse(date);
			sdf=null;
			return result;
		} catch (Exception e)
		{
			return null;
		}
		
	}
	
	/**
	 * 采用默认的格式将字符串转换为date对象
	 * @param date
	 * @return
	 */
	public static Date string2date(String date)
	{
		try
		{
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
			Date result=sdf.parse(date);
			sdf=null;
			return result;
		} catch (Exception e)
		{
			return null;
		}
	}
	
	/**
	 * 将date对象转换 为特定格式的字符串
	 * @param date
	 * @param format
	 * @return
	 */
	public static String date2string(Date date,String format)
	{
		SimpleDateFormat sdf=new SimpleDateFormat(format);
		String result=sdf.format(date);
		sdf=null;
		return result;
	}
	
	/**
	 * 采用默认的格式将date转换为字符串
	 * @param date
	 * @return
	 */
	public static String date2string(Date date)
	{
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		String result=sdf.format(date);
		sdf=null;
		return result;
	}
	
	
	
}
