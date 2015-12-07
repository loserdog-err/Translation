package com.gdxz.zhongbao.client.Service.impl;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.gdxz.zhongbao.client.Service.AnswerService;
import com.gdxz.zhongbao.client.utils.HttpUtils;
import com.gdxz.zhongbao.client.view.activity.WriteMessage;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AnswerServiceimpl implements AnswerService
{
	//向服务器发送请求的method名称
	public static final String METHOD_NAME_ADD_PRAISE_COUNT = "addPraiseCount";
	public static final String METHOD_NAME_ADD_DESPISE_COUNT = "addDespiseCount";
	// 服务器返回来的json
	JSONObject jsonObject;

	/**
	 * 发布回复
	 */
	public void postAnswer(final Handler handler, final String answer,
	                       final String questionId, final String userId)
	{
		new AsyncTask<java.lang.Void, java.lang.Void, Boolean>()
		{
			@Override
			protected Boolean doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "AnswerServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("method", "postAnswer");
				rawParams.put("answer", answer);
				rawParams.put("questionId", questionId);
				rawParams.put("userId", userId);
				try
				{
					jsonObject = new JSONObject(
							HttpUtils.postRequest(url, rawParams));
					if (jsonObject.getBoolean("isSuccess"))
					{
						return true;
					}
				} catch (Exception e)
				{
					e.printStackTrace();
					return false;
				}
				return false;
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				Message msg = new Message();
				if (result)
				{
					msg.what = WriteMessage.WRITE_ANSWER;
				} else
				{
					msg.what = WriteMessage.HANDLER_SERVER_ERROR;
				}
				handler.sendMessage(msg);
			}
		}.execute();
	}

	public void getAnswerHead(final ImageView answerHead, final String path)
	{
		ImageLoader.getInstance().displayImage(
				HttpUtils.BASE_FILE_PATH + path, answerHead);
	}

	@Override
	public void addPraiseOrDespiseCount(final String answerId, final String methodName)
	{
		new AsyncTask<Void, Void, Void>()
		{
			@Override
			protected Void doInBackground(Void... params)
			{
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("method", methodName);
				rawParams.put("answerId", answerId);
				String url = HttpUtils.BASE_URL + "AnswerServlet";
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

	/**
	 * 设置问题为最佳答案
	 *
	 * @param answerId
	 */
	@Override
	public void setBestAnswer(final String answerId)
	{
		new AsyncTask<Void, Void, Void>()
		{
			@Override
			protected Void doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "AnswerServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("method", "setBestAnswer");
				rawParams.put("answerId", answerId);
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
