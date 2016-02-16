package com.gdxz.zhongbao.client.Service.impl;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.gdxz.zhongbao.client.Service.QuestionService;
import com.gdxz.zhongbao.client.domain.Answer;
import com.gdxz.zhongbao.client.domain.Question;
import com.gdxz.zhongbao.client.domain.UploadConstant;
import com.gdxz.zhongbao.client.utils.FileUtils;
import com.gdxz.zhongbao.client.utils.HttpUtils;
import com.gdxz.zhongbao.client.utils.L;
import com.gdxz.zhongbao.client.view.activity.HomeActivity;
import com.gdxz.zhongbao.client.view.activity.QuestionDetailActivity;
import com.gdxz.zhongbao.client.view.activity.ShowPictureActivity;
import com.gdxz.zhongbao.client.view.activity.WriteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionServiceImpl implements QuestionService
{
	//请求服务器servlet的方法名称
	public static final String METHOD_NAME_ADD_BROWSE_COUNT = "addBrowseCount";
	public static final String METHOD_NAME_ADD_REPLY_COUNT = "addReplyCount";
	// 服务器返回的json
	JSONObject jsonObject;

	JSONArray jsonArray;
	final SpannableString spannableString = new SpannableString("");

	/**
	 * 加载问题数据
	 */
	public void loadQuestionData(final Handler handler, final String pageNow,
	                             final String category, final int action,
	                             final PullToRefreshListView pullToRefreshListView)
	{
		new AsyncTask<java.lang.Void, java.lang.Void, java.util.List<Question>>()
		{
			@Override
			protected List<Question> doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "QuestionServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("method", "loadQuestionData");
				rawParams.put("pageNow", pageNow);
				rawParams.put("category", category);
				try
				{
					jsonObject = new JSONObject(
							HttpUtils.postRequest(url, rawParams));
					if (jsonObject.getBoolean("isSuccess"))
					{
						String result = jsonObject.getString("questionData");
						Gson gson = new Gson();
						// 将数据封装成list
						return gson.fromJson(result,
								new TypeToken<List<Question>>()
								{
								}.getType());

					} else
					{
						return null;
					}

				} catch (Exception e)
				{
					e.printStackTrace();
					return null;
				}
			}

			@Override
			protected void onPostExecute(List<Question> result)
			{
				if (result != null)
				{
					Message msg = Message.obtain();
					if (action == HomeActivity.REFRESH_DATA)
					{
						msg.what = HomeActivity.REFRESH_DATA;
					} else if (action == HomeActivity.ADD_MORE_DATA)
					{
						msg.what = HomeActivity.ADD_MORE_DATA;
					}
					msg.obj = result;
					handler.sendMessage(msg);
					pullToRefreshListView.onRefreshComplete();
				} else
				{
					handler.sendEmptyMessage(HomeActivity.SERVER_ERROR);
				}
			}
		}.execute();
	}

	/**
	 * 加载服务器端的详细数据
	 */
	public void loadQuestionDetailData(final Handler handler,
	                                   final String questionId, final String pageNow,
	                                   final PullToRefreshListView pullToRefreshListView)
	{
		new AsyncTask<java.lang.Void, java.lang.Void, Map<String, Object>>()
		{

			@Override
			protected Map<String, Object> doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "QuestionServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("method", "loadQuestionDetailData");
				rawParams.put("pageNow", pageNow);
				rawParams.put("questionId", questionId);
				rawParams.put("userId", UserServiceImpl.getCurrentUserId(pullToRefreshListView
						.getContext()));
				try
				{
					jsonObject = new JSONObject(
							HttpUtils.postRequest(url, rawParams));
					if (jsonObject.getBoolean("isSuccess"))
					{
						Gson gson = new GsonBuilder().create();
						JSONObject questionJson = jsonObject
								.getJSONObject("question");
						// 转换为Question对象
						Question question = gson.fromJson(questionJson.toString(),
								Question.class);
						// 取出answers的json，并转换为list<Answer>
						List<Answer> answers = gson.fromJson(jsonObject
										.getJSONArray("answers").toString(),
								new TypeToken<List<Answer>>()
								{
								}.getType());
						// 封装成map
						Map<String, Object> data = new HashMap<>();
						data.put("question", question);
						data.put("answers", answers);
						//是否已经关注
						data.put("hadFollow", jsonObject.getBoolean("hadFollow"));
//						//存进数据库
//						DbUtils dbUtils = DatabaseUtils.getDbUtils(pullToRefreshListView
//								.getContext());
//						for (Answer answer : answers)
//						{
//							answer.setQuestion(question);
//							dbUtils.saveBindingId(answer);
//						}
						return data;
					}

				} catch (Exception e)
				{
					e.printStackTrace();
					return null;
				}
				return null;
			}

			protected void onPostExecute(java.util.Map<String, Object> result)
			{
				if (result != null && result.size() > 0)
				{
					Message msg = new Message();
					msg.what = QuestionDetailActivity.TEXT_DATA_LOAD_COMPLETE;
					msg.obj = result;
					handler.sendMessage(msg);
				} else
				{
					handler.sendEmptyMessage(QuestionDetailActivity.SERVER_ERROR);
				}

			}


		}.execute();
	}

	/**
	 * 发布一个问题
	 */
	@Override
	public void postQuestion(final Handler handler, final String questionContent,
	                         final String userId, final String rewardAmount, final String title,
	                         final List<String> paths, final String voicePath)
	{
		boolean haveFile = false;
		RequestParams params = new RequestParams();
//		params.setContentType("multipart/form-data");
		params.addBodyParameter("userId", userId + "");
		params.addBodyParameter("rewardAmount", rewardAmount);
		params.addBodyParameter("title", title);
		params.addBodyParameter("questionContent", questionContent);
		params.addBodyParameter("category", UploadConstant.CATEGORY_WRITE_QUESTION);
		if (paths != null && paths.size() > 0)
		{
			haveFile = true;
			for (int i = 0; i < paths.size(); i++)
			{
				L.e(paths.get(i) + ":path");
				File file = new File(paths.get(i));
//				params.addBodyParameter(file.getName(), file);
				byte[] compressBitmap = FileUtils.getCompressBitmapFromFile(file.getPath
						());
				ByteArrayInputStream bais = new ByteArrayInputStream(compressBitmap);
				params.addBodyParameter(file.getName(), bais, compressBitmap.length, file.getName
						());
			}
		}
		if (voicePath != null && !("".equals(voicePath)))
		{
			L.e("voicePath:" + voicePath);
			haveFile = true;
			File file = new File(voicePath);
			params.addBodyParameter(file.getName(), file);
		}
		com.lidroid.xutils.HttpUtils http = new com.lidroid.xutils.HttpUtils();
		String url = "";
		//判断是否有文件，有则上传到UploadServlet,没有则发送请求到questionServlet
		if (haveFile) url = HttpUtils.BASE_URL + "UploadFileServlet";
		else
		{
			url = HttpUtils.BASE_URL + "QuestionServlet";
			params.addBodyParameter("method", "postQuestion");
		}
		http.send(HttpRequest.HttpMethod.POST,
				url,
				params,
				new RequestCallBack<String>()
				{
					@Override
					public void onSuccess(ResponseInfo<String> responseInfo)
					{
						String result = responseInfo.result;
						Message msg = Message.obtain();
						try
						{
							JSONObject json = new JSONObject(result);
							if (json.getBoolean("isSuccess"))
							{
								handler.sendEmptyMessage(WriteMessage.WRITE_QUESTION);
								return;
							} else
							{
								msg.what = WriteMessage.WRITE_QUESTION_FAILURE;
								msg.obj = json.getString("error");
								L.e("error:" + json.getString("error"));
								handler.sendMessage(msg);
							}
						} catch (Exception e)
						{
							handler.sendEmptyMessage(WriteMessage.HANDLER_SERVER_ERROR);
							e.printStackTrace();
						}
					}

					@Override
					public void onFailure(HttpException error, String msg)
					{
						handler.sendEmptyMessage(WriteMessage.HANDLER_SERVER_ERROR);
					}

				});
	}

	/**
	 * 加载问题中的图片
	 */
	@Override
	public void loadQuestionImage(final String path, final ImageView imageView)
	{
		L.e("路径:" + path);
		DisplayImageOptions options = FileUtils.getDefaultImageOptions();
		ImageLoader.getInstance().displayImage(HttpUtils.BASE_FILE_PATH + path,
				imageView, options);
		imageView.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(imageView.getContext(),
						ShowPictureActivity.class);
				intent.putExtra("path", path);
				imageView.getContext().startActivity(intent);
			}
		});
	}

	/**
	 * 加载音频文件
	 */
	@Override
	public void loadVoiceFile(final Handler handler, final String questionVoicePath)
	{
		new AsyncTask<java.lang.Void, java.lang.Void, String>()
		{

			@Override
			protected String doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "QuestionServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("method", "getQuestionVoice");
				rawParams.put("path", questionVoicePath);
				InputStream is;
				String path;
				FileOutputStream fos = null;
				try
				{
					is = HttpUtils.getResourceInputStream(url, rawParams);
					path = FileUtils.createVoiceFile();
					File file = new File(path);
					fos = new FileOutputStream(file);
					int len;
					byte[] buffer = new byte[1024];
					while ((len = is.read(buffer)) != -1)
					{
						fos.write(buffer, 0, len);
					}
				} catch (Exception e)
				{
					e.printStackTrace();
					return null;
				} finally
				{
					try
					{
						if (fos != null)
						{
							fos.close();
						}
					} catch (Exception e2)
					{
						e2.printStackTrace();
						return null;
					}
				}
				return path;
			}

			protected void onPostExecute(String result)
			{
				if (result != null && !result.equals(""))
				{
					Message msg = new Message();
					msg.what = QuestionDetailActivity.VOICE_DATA_LOAD_COMPLETE;
					msg.obj = result;
					handler.sendMessage(msg);
				} else
				{
					Message msg = Message.obtain();
					msg.what = QuestionDetailActivity.SERVER_ERROR;
					handler.sendMessage(msg);
				}
			}
		}.execute();

	}

	/**
	 * 增加问题的浏览次数
	 *
	 * @param questionId
	 */
	@Override
	public void addBrowseOrReplyCount(final String questionId, final String methodName)
	{
		new AsyncTask<Void, Void, Void>()
		{
			@Override
			protected Void doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "QuestionServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("questionId", questionId);
				rawParams.put("method", methodName);
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
	 * &#x6536;&#x85cf;&#x4e00;&#x4e2a;&#x95ee;&#x9898;
	 *
	 * @param currentUserId
	 * @param questionId
	 */
	@Override
	public void followQuestion(final String currentUserId, final int questionId, final Handler
			handler)
	{
		new AsyncTask<Void, Void, Boolean>()
		{
			@Override
			protected Boolean doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "QuestionServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("method", "followQuestion");
				rawParams.put("userId", currentUserId);
				rawParams.put("questionId", questionId + "");
				try
				{
					HttpUtils.postRequest(url, rawParams);
				} catch (Exception e)
				{
					e.printStackTrace();
					return null;
				}
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				if (result)
				{
					handler.sendEmptyMessage(QuestionDetailActivity.FOLLOW_QUESTION_SUCCESS);
				} else
				{
					handler.sendEmptyMessage(QuestionDetailActivity.SERVER_ERROR);
				}
				super.onPostExecute(result);
			}
		}.execute();
	}

}
