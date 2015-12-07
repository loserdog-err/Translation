package com.gdxz.zhongbao.client.Service.impl;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.gdxz.zhongbao.client.Service.TeamService;
import com.gdxz.zhongbao.client.domain.Team;
import com.gdxz.zhongbao.client.domain.UploadConstant;
import com.gdxz.zhongbao.client.utils.HttpUtils;
import com.gdxz.zhongbao.client.utils.L;
import com.gdxz.zhongbao.client.view.activity.ApprovalJoinTeamActivity;
import com.gdxz.zhongbao.client.view.activity.GroupHomeActivity;
import com.gdxz.zhongbao.client.view.activity.TeamGroupIntroductionActivity;
import com.gdxz.zhongbao.client.view.activity.TeamGroupListActivity;
import com.gdxz.zhongbao.client.view.activity.WriteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.json.JSONObject;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.CreateGroupCallback;

/**
 * Created by chenantao on 2015/7/18.
 */
public class TeamServiceImpl implements TeamService
{

	/**
	 * 创建一个团队
	 * 先在Jmessage创建群组，然后根据返回来的groupId去服务器创建
	 *
	 * @param teamName        队名
	 * @param teamDeclaration 团队宣言
	 * @param teamLogo        团队logo
	 * @param handler
	 */
	@Override
	public void createTeam(final String teamName, final String teamDeclaration, final String
			teamLogo, final Handler
			                       handler, final String userId)
	{
		new AsyncTask<Void, Void, Void>()
		{

			@Override
			protected Void doInBackground(Void... params)
			{
				JMessageClient.createGroup(teamName, teamDeclaration, new CreateGroupCallback()
				{
					@Override
					public void gotResult(int i, String s, long groupId)
					{
						if (i == 0)
						{
							Log.e("TAG", "群组创建成功");
							//创建到自己的服务器上
							String url = HttpUtils.BASE_URL + "TeamServlet";
							Map<String, String> rawParams = new HashMap<>();
							Team team = new Team();
							team.setId(groupId);
							team.setName(teamName);
							team.setDeclaration(teamDeclaration);
							team.setBuildTime(new Date());
							rawParams.put("method", "createTeam");
							rawParams.put("team", new Gson().toJson(team));
							rawParams.put("userId", userId);
							try
							{
								HttpUtils.postRequest(url, rawParams);
							} catch (Exception e)
							{
								e.printStackTrace();
							}
							//如果logo不为空，上传图片
							if (teamLogo != null && !"".equals(teamLogo))
							{
								L.e("logo path:" + teamLogo);
								url = HttpUtils.BASE_URL + "UploadFileServlet";
								RequestParams params = new RequestParams();
								File file = new File(teamLogo);
								params.addBodyParameter("category", UploadConstant
										.CATEGORY_UPLOAD_TEAM_LOGO);
								params.addBodyParameter(file.getName(), file);
								params.addBodyParameter("groupId", groupId + "");
								com.lidroid.xutils.HttpUtils http = new com.lidroid.xutils
										.HttpUtils();
								http.send(HttpRequest.HttpMethod.POST,
										url,
										params,
										new RequestCallBack<String>()
										{
											@Override
											public void onSuccess(ResponseInfo<String>
													                      responseInfo)
											{
												L.e("上传team logo成功:" + responseInfo.result);
											}

											@Override
											public void onFailure(HttpException error, String msg)
											{
												L.e("上传team logo失败:" + msg);
											}

										});
							}
							handler.sendEmptyMessage(WriteMessage.CREATE_TEAM);
						} else
						{
							Log.e("TAG", "群组创建失败：" + i);
							handler.sendEmptyMessage(WriteMessage.HANDLER_SERVER_ERROR);
						}
					}
				});
				return null;
			}

		}.execute();

	}

	/**
	 * 根据类型加载队伍列表
	 *
	 * @param category
	 * @param handler
	 */
	@Override
	public void getTeamList(final int category, final Handler handler, final String condition)
	{
		new AsyncTask<Void, Void, List<Team>>()
		{
			@Override
			protected List<Team> doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "TeamServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("method", "getTeamList");
				rawParams.put("category", category + "");
				if (condition != null && !"".equals(condition))
				{
					rawParams.put("condition", condition);
				}
				try
				{
					JSONObject json = new JSONObject(HttpUtils.postRequest(url, rawParams));
					if (json.getBoolean("haveTeamList"))
					{
						Gson gson = new Gson();
						List<Team> teamList = gson.fromJson(json.getString("teamList").toString(),
								new
										TypeToken<List<Team>>()
										{
										}.getType());
						return teamList;
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
			protected void onPostExecute(List<Team> teams)
			{
				if (teams != null)
				{
					Message msg = Message.obtain();
					msg.what = TeamGroupListActivity.MSG_LOAD_TEXT_COMPLETE;
					msg.obj = teams;
					handler.sendMessage(msg);
				} else
				{
					handler.sendEmptyMessage(TeamGroupListActivity.MSG_LOAD_TEXT_FAILURE);
				}
				super.onPostExecute(teams);
			}
		}.execute();
	}

	/**
	 * 根据队伍id加载队伍
	 *
	 * @param teamId
	 * @param handler
	 */
	@Override
	public void getTeamById(final long teamId, final Handler handler)
	{
		new AsyncTask<Void, Void, Team>()
		{
			@Override
			protected Team doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "TeamServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("method", "getTeamById");
				rawParams.put("teamId", teamId + "");
				try
				{
					JSONObject jsonObject = new JSONObject(HttpUtils.postRequest(url, rawParams));
					if (jsonObject.getBoolean("haveTeam"))
					{
						Team team = new Gson().fromJson(jsonObject.get("team").toString(), Team
								.class);
						return team;
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
			protected void onPostExecute(Team team)
			{
				if (team != null)
				{
					Message msg = Message.obtain();
					msg.what = TeamGroupIntroductionActivity.MSG_LOAD_COMPLETE;
					msg.obj = team;
					handler.sendMessage(msg);
				} else
				{
					handler.sendEmptyMessage(TeamGroupIntroductionActivity.MSG_LOAD_FAILURE);
				}
				super.onPostExecute(team);
			}
		}.execute();

	}

	/**
	 * 添加群成员
	 *
	 * @param currentUserId
	 * @param id
	 * @param handler
	 * @param
	 */
	@Override
	public void addGroupMember(final String currentUserId, final Long id, final Handler handler)
	{
		new AsyncTask<Void, Void, Boolean>()
		{

			@Override
			protected Boolean doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "TeamServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("method", "addGroupMember");
				rawParams.put("teamId", id + "");
				rawParams.put("userId", currentUserId);
				try
				{
					JSONObject json = new JSONObject(HttpUtils.postRequest(url, rawParams));
					if (json.getBoolean("isSuccess"))
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
					handler.sendEmptyMessage(ApprovalJoinTeamActivity
							.MSG_ADD_GROUP_MEMBER_SUCCESS);
				} else
				{
					handler.sendEmptyMessage(ApprovalJoinTeamActivity
							.MSG_ADD_GROUP_MEMBER_FAILURE);
				}
				super.onPostExecute(result);
			}
		}.execute();
	}

	/**
	 * 拒绝该成员加入
	 *
	 * @param teamId
	 * @param id
	 */
	@Override
	public void disagreeJoin(final long teamId, final Integer id)
	{
		new AsyncTask<Void, Void, Void>()
		{

			@Override
			protected Void doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "TeamServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("method", "disagreeJoin");
				rawParams.put("userId", id + "");
				rawParams.put("teamId", teamId + "");
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
	 * 退出群组
	 *
	 * @param currentUserId
	 * @param id
	 * @param handler
	 * @param groupOwner
	 */
	@Override
	public void exitGroup(final String currentUserId, final Long id, final Handler handler, final
	String
			groupOwner)
	{
		new AsyncTask<Void, Void, Boolean>()
		{

			@Override
			protected Boolean doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "TeamServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("method", "exitGroup");
				rawParams.put("userId", currentUserId);
				rawParams.put("teamId", id + "");
				if (groupOwner != null)
				{
					rawParams.put("newOwner", groupOwner);
				}
				try
				{
					JSONObject json = new JSONObject(HttpUtils.postRequest(url, rawParams));
					if (json.getBoolean("isSuccess"))
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
					handler.sendEmptyMessage(GroupHomeActivity.MSG_EXIT_GROUP_SUCCESS);
				} else
				{
					handler.sendEmptyMessage(GroupHomeActivity.MSG_EXIT_GROUP_FAILURE);
				}
				super.onPostExecute(result);
			}
		}.execute();
	}


}
