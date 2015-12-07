package com.gdxz.zhongbao.client.Service.impl;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.gdxz.zhongbao.client.Service.UserDynamicService;
import com.gdxz.zhongbao.client.domain.UserDynamic;
import com.gdxz.zhongbao.client.utils.HttpUtils;
import com.gdxz.zhongbao.client.view.activity.PersonalDynamicActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenantao on 2015/7/15.
 */
public class UserDynamicServiceImpl implements UserDynamicService
{

	@Override
	public void loadData(final int category, final String currentUserId, final Handler handler)
	{
		new AsyncTask<Void, Void, List<UserDynamic>>()
		{
			@Override
			protected List<UserDynamic> doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "UserDynamicServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("method", "loadDynamicData");
				rawParams.put("category", category + "");
				rawParams.put("userId", currentUserId);
				try
				{
					JSONObject json = new JSONObject(HttpUtils.postRequest(url, rawParams));
					if (json.getBoolean("isSuccess"))
					{
						Gson gson = new Gson();
						List<UserDynamic> data = gson.fromJson(json.getString("data"), new
								TypeToken<List<UserDynamic>>()
						{
						}.getType());
//						Log.e("TAG", data.get(0).getType() + "");
						return data;
					}

				} catch (Exception e)
				{
					e.printStackTrace();
					return null;
				}
				return null;
			}

			@Override
			protected void onPostExecute(List<UserDynamic> userDynamics)
			{
				if (userDynamics != null)
				{
					Message msg = Message.obtain();
					msg.obj = userDynamics;
					handler.handleMessage(msg);
				} else
				{
					handler.sendEmptyMessage(PersonalDynamicActivity.MSG_SERVER_ERROR);
				}
			}
		}.execute();
	}
}
