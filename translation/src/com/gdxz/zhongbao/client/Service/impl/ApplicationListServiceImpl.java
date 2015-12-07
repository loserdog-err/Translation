package com.gdxz.zhongbao.client.Service.impl;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.gdxz.zhongbao.client.Service.ApplicationListService;
import com.gdxz.zhongbao.client.domain.ApplicationList;
import com.gdxz.zhongbao.client.utils.HttpUtils;
import com.gdxz.zhongbao.client.view.activity.ApprovalJoinTeamActivity;
import com.gdxz.zhongbao.client.view.activity.TeamGroupIntroductionActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenantao on 2015/7/20.
 */
public class ApplicationListServiceImpl implements ApplicationListService
{
	/**
	 * 申请加入该群
	 *
	 * @param id
	 * @param currentUserId
	 * @param handler
	 */
	@Override
	public void applyJoinTeam(final Long id, final String currentUserId, final Handler handler)
	{
		new AsyncTask<Void, Void, Boolean>()
		{

			@Override
			protected Boolean doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "ApplicationListServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("method", "applyJoinGroup");
				rawParams.put("userId", currentUserId);
				rawParams.put("teamId", id + "");
				try
				{
					JSONObject jsonObject = new JSONObject(HttpUtils.postRequest(url, rawParams));
					if (jsonObject.getBoolean("isSuccess"))
					{
						return true;
					} else
					{
						return false;
					}
				} catch (Exception e)
				{
					e.printStackTrace();
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				if (result)
				{
					handler.sendEmptyMessage(TeamGroupIntroductionActivity
							.MSG_APPLY_JOIN_GROUP_SUCCESS);
				} else
				{
					handler.sendEmptyMessage(TeamGroupIntroductionActivity
							.MSG_APPLY_JOIN_GROUP_FAILURE);
				}
				super.onPostExecute(result);
			}
		}.execute();
	}

	/**
	 * 加载申请列表
	 *
	 * @param teamId
	 * @param handler
	 */
	@Override
	public void getApplicationList(final long teamId, final Handler handler)
	{
		new AsyncTask<Void, Void, List<ApplicationList>>()
		{

			@Override
			protected List<ApplicationList> doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "ApplicationListServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("method", "getApplicationList");
				rawParams.put("teamId", teamId + "");
				try
				{
					JSONObject json = new JSONObject(HttpUtils.postRequest(url, rawParams));
					if (json.getBoolean("isSuccess"))
					{
						Gson gson = new Gson();
						List<ApplicationList> datas = gson.fromJson(json.getString
								("applicationList"), new
								TypeToken<List<ApplicationList>>()
								{
								}.getType());
						return datas;
					}
				} catch (Exception e)
				{
					e.printStackTrace();
					return null;
				}
				return null;
			}

			@Override
			protected void onPostExecute(List<ApplicationList> applicationList)
			{
				if (applicationList != null)
				{
					Message msg = Message.obtain();
					msg.what = ApprovalJoinTeamActivity.MSG_LOAD_COMPLETE;
					msg.obj = applicationList;
					handler.sendMessage(msg);
				} else
				{
					handler.sendEmptyMessage(ApprovalJoinTeamActivity.MSG_LOAD_FAILURE);
				}
				super.onPostExecute(applicationList);
			}
		}.execute();
	}
}
