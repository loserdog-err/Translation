package com.gdxz.zhongbao.client.utils;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by chenantao on 2015/8/6.
 */
public class DialogUtils
{
	public static ProgressDialog progressDialog;

	/**
	 * 显示加载对话框并隐藏listview
	 *
	 * @param title
	 */
	public static void showProgressDialog(String title, String msg, Context context)
	{
		if (progressDialog != null && progressDialog.isShowing())
		{
			return;
		}
		progressDialog = ProgressDialog.show(context, title, msg);
		progressDialog.setCancelable(true);
	}

	/**
	 * 隐藏加载对话框并显示listview
	 */
	public static void closeProgressDialog()
	{
		if (progressDialog != null && progressDialog.isShowing())
		{
			progressDialog.dismiss();
			progressDialog=null;
		}
	}

	public static void showDefaultDialog(Context context)
	{
		if (progressDialog != null && progressDialog.isShowing())
		{
			return;
		}
		progressDialog = ProgressDialog.show(context, "提醒", "数据加载中");
		progressDialog.setCancelable(true);
	}
}
