package com.gdxz.zhongbao.client.Service.impl;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.gdxz.zhongbao.client.Service.AssetInfoService;
import com.gdxz.zhongbao.client.domain.AssetInfo;
import com.gdxz.zhongbao.client.utils.HttpUtils;
import com.gdxz.zhongbao.client.view.activity.AssetsAnalysisActivity;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chean_antao on 2015/8/13.
 */
public class AssetInfoServiceImpl implements AssetInfoService
{
	@Override
	public void getAssetInfo(final String currentUserId, final Handler handler)
	{
		new AsyncTask<Void, Void, AssetInfo>()
		{

			@Override
			protected AssetInfo doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "AssetInfoServlet";
				Map<String, String> rawParams = new HashMap<String, String>();
				rawParams.put("method", "getAssetInfo");
				rawParams.put("userId", currentUserId);
				try
				{
					JSONObject json = new JSONObject(HttpUtils.postRequest(url, rawParams));
					if (json.getBoolean("isSuccess"))
					{
						Gson gson = new Gson();
						AssetInfo assetInfo = gson.fromJson(json.getString("assetInfo"), AssetInfo
								.class);
						return assetInfo;
					}
				} catch (Exception e)
				{
					e.printStackTrace();
					return null;
				}
				return null;
			}

			@Override
			protected void onPostExecute(AssetInfo assetInfo)
			{
				if (assetInfo != null)
				{
					Message msg = Message.obtain();
					msg.what = AssetsAnalysisActivity.PlaceholderFragment
							.MSG_LOAD_ASSET_INFO_COMPLETE;
					msg.obj = assetInfo;
					handler.sendMessage(msg);
				} else
				{
					handler.sendEmptyMessage(AssetsAnalysisActivity.PlaceholderFragment
							.MSG_LOAD_ASSET_INFO_FALIURE);
				}
				super.onPostExecute(assetInfo);
			}
		}.execute();
	}
}
