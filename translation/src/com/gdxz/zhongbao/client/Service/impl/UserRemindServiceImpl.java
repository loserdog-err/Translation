package com.gdxz.zhongbao.client.Service.impl;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.gdxz.zhongbao.client.Service.UserRemindService;
import com.gdxz.zhongbao.client.domain.UserRemind;
import com.gdxz.zhongbao.client.utils.HttpUtils;
import com.gdxz.zhongbao.client.view.activity.HomeActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Chean_antao on 2015/8/11.
 */
public class UserRemindServiceImpl implements UserRemindService
{
	/**
	 * �õ��û���������Ϣ
	 *
	 * @param userId
	 * @param handler
	 */
	@Override
	public void getUserRemind(final String userId, final Handler handler)
	{
		new AsyncTask<Void, Void, List<UserRemind>>()
		{
			@Override
			protected List<UserRemind> doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "UserRemindServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("userId", userId);
				rawParams.put("method", "getUserRemind");
				try
				{
					JSONObject json = new JSONObject(HttpUtils.postRequest(url, rawParams));
					if (json.getBoolean("isSuccess"))
					{
						Gson gson = new Gson();
						List<UserRemind> list = gson.fromJson(json.getString("userRemindList"), new
								TypeToken<List<UserRemind>>()
								{
								}.getType());
						if(list==null) list = new ArrayList<>();
						return list;
					}
				} catch (Exception e)
				{
					e.printStackTrace();
					return null;
				}
				return null;
			}

			@Override
			protected void onPostExecute(List<UserRemind> userReminds)
			{
				if (userReminds != null)
				{
					Message msg = Message.obtain();
					msg.obj=userReminds;
					msg.what = HomeActivity.USER_REMIND;
					handler.sendMessage(msg);
				}
				else
				{
					handler.sendEmptyMessage(HomeActivity.SERVER_ERROR);
				}
				super.onPostExecute(userReminds);
			}
		}.execute();
	}

	/**
	 * 设置用户提醒已读
	 * @param userId
	 */
	@Override
	public void setHaveRead(final String userId)
	{
		new AsyncTask<Void, Void, Void>()
		{

			@Override
			protected Void doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "UserRemindServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("method", "setHaveRead");
				rawParams.put("userId", userId);
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
