package com.gdxz.zhongbao.client.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 一些字符串操作的工具类
 *
 * @author chenantao
 */
public class StringUtils
{
	/**
	 * 得到文本的一部分，超过指定长度的部分用...代替
	 *
	 * @param lenght
	 * @param text
	 * @return
	 */
	public static String getPartOfText(int lenght, String text)
	{
		String result = "";
		if (text.length() > lenght)
		{
			result = text.substring(0, lenght - 1) + "...";
		} else
		{
			result = text;
		}
		return result;
	}

	/**
	 * 判断是否为中文
	 */
	public static boolean checkIsChinese(String val)
	{
		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(val);
		return m.find();
	}

	/**
	 * 空字符串的过滤器
	 *
	 * @param str
	 * @return 返回默认的""
	 */
	public static String nullStringFilter(String str)
	{
		return str == null ? "" : str;
	}

	/**
	 * 空字符串的过滤器
	 *
	 * @param former
	 * @return 返回指定的字符串
	 */
	public static String nullStringFilter(String former, String dest)
	{
		return former == null ? dest : former;
	}

	/**
	 * null integer的过滤器
	 *
	 * @param val
	 * @return 返回默认的字符串
	 */
	public static String nullIntegerFilter(Integer val)
	{
		return val == null ? "0" : val + "";
	}

	/**
	 * null integer的过滤器
	 *
	 * @param val
	 * @return 返回指定的字符串
	 */
	public static String nullIntegerFilter(Integer val, String dest)
	{
		return val == null ? dest : val + "";
	}

}
