package com.gdxz.zhongbao.client.view.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gdxz.zhongbao.client.Service.TeamService;
import com.gdxz.zhongbao.client.Service.UserService;
import com.gdxz.zhongbao.client.Service.impl.TeamServiceImpl;
import com.gdxz.zhongbao.client.Service.impl.UserServiceImpl;
import com.gdxz.zhongbao.client.common.CommonAdapter;
import com.gdxz.zhongbao.client.common.MyApplication;
import com.gdxz.zhongbao.client.common.ViewHolder;
import com.gdxz.zhongbao.client.domain.Team;
import com.gdxz.zhongbao.client.domain.User;
import com.gdxz.zhongbao.client.utils.HandleResponseCode;
import com.gdxz.zhongbao.client.utils.L;
import com.ikimuhendis.ldrawer.ActionBarDrawerToggle;
import com.ikimuhendis.ldrawer.DrawerArrowDrawable;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by chenantao on 2015/7/19.
 * 群组内部的activity
 */
public class GroupHomeActivity extends Activity
{
	public static final int MSG_EXIT_GROUP_SUCCESS = 0;
	public static final int MSG_EXIT_GROUP_FAILURE = 1;

	//侧滑菜单listview
	private ListView mListView;
	private CommonAdapter mAdapter;
	private List<Map<String, String>> mDatas;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerArrowDrawable drawerArrow;
	private boolean drawerArrowColor;

	//群聊listview
	private ListView mConvListView;
	private CommonAdapter mConvAdapter;
	private List<Conversation> mConvDatas;
	//标题栏
	ImageView ivBack;
	TextView tvTitle;

	//用户的会话列表
	List<Conversation> mConvs;
	Conversation mGroupConv;

	//当前队伍的对象
	Team team;

	String owner;
	//侧滑菜单
//	SlidingMenu mMenu;

	UserService userService = new UserServiceImpl();
	TeamService teamService = new TeamServiceImpl();


	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case MSG_EXIT_GROUP_SUCCESS:
					Toast.makeText(GroupHomeActivity.this, "退出成功", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(GroupHomeActivity.this,
							HomeActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(intent);
					break;
				case MSG_EXIT_GROUP_FAILURE:
					Toast.makeText(GroupHomeActivity.this, "退出失败,系统即将奔溃", Toast.LENGTH_SHORT)
							.show();
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.team_group_home);
		MyApplication.getInstance().addActivity(this);
		JMessageClient.registerEventReceiver(this);
		team = (Team) getIntent().getExtras().getSerializable("team");
		initView();
		initData();
		initUserInfo();
		initConversationList();
	}

	private void initConversationList()
	{
		mConvListView = (ListView) findViewById(R.id.lv_conversation);
		if (mConvDatas == null)
		{
			mConvDatas = new ArrayList<>();
		}
		mConvDatas.add(JMessageClient.getGroupConversation(team.getId()));
		L.e(JMessageClient.getGroupConversation(team.getId()) + "");
		mConvListView.setAdapter(mConvAdapter = new CommonAdapter<Conversation>(this,
				mConvDatas,
				R.layout.conv_list_item)
		{
			@Override
			public void convert(ViewHolder helper, Conversation item, int position)
			{
				L.e(item + "");
				if (item != null)
				{
					helper.setText(R.id.tv_msg, item.getLatestText());
				}
				helper.getConvertView().setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						enterGroupChat();
					}
				});

			}
		});

	}


	private void initView()
	{
		mListView = (ListView) findViewById(R.id.lv_menu);
		ivBack = (ImageView) findViewById(R.id.iv_back);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		ActionBar ab = getActionBar();
		ab.setTitle("群中心");
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowHomeEnabled(false);
//		ab.setHomeButtonEnabled(true);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.navdrawer);
		drawerArrow = new DrawerArrowDrawable(this)
		{
			@Override
			public boolean isLayoutRtl()
			{
				return false;
			}
		};
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				drawerArrow, R.string.open,
				R.string.close)
		{

			public void onDrawerClosed(View view)
			{
				Log.e("TAG", "onDrawerClosed");
				super.onDrawerClosed(view);
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView)
			{
				Log.e("TAG", "onDrawerOpened");
				super.onDrawerOpened(drawerView);
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();

	}

	private void initData()
	{
//		ivBack.setOnClickListener(new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				GroupHomeActivity.this.finish();
//			}
//		});
//		tvTitle.setText(team.getName());
	}

	/**
	 * 初始化menu中用户的信息(姓名、头像)
	 */
	private void initUserInfo()
	{
		View header = LayoutInflater.from(this).inflate(R.layout.group_home_menu_header, null);
		ImageView ivHead = (ImageView) header.findViewById(R.id.iv_head);
		TextView tvUsername = (TextView) header.findViewById(R.id.tv_username);
		String username = UserServiceImpl.getCurrentUserStringInfo(this, "username");
		tvUsername.setText(username);
		String headInSp = UserServiceImpl.getCurrentUserStringInfo(this, User.SP_HEAD);
		//如果图片有缓存在本地则使用缓存，否则从服务器加载
		if (!"".equals(headInSp))
		{
			Log.e("TAG", "getByCache");
			ImageLoader.getInstance().displayImage("file://" + headInSp, ivHead);
		} else
		{
			userService.getUserHead(UserServiceImpl.getCurrentUserId(this), ivHead);
		}
		mDrawerList.addHeaderView(header);
		//得到创始人的名字后才允许初始化lisview
		JMessageClient.getGroupInfo(team.getId(), new GetGroupInfoCallback()
		{
			@Override
			public void gotResult(int i, String s, GroupInfo groupInfo)
			{
				if (i == 0)
				{
					owner = groupInfo.getGroupOwner();
					L.e(owner);
					initListView();
				} else
				{
					L.e("failure:" + i);
					HandleResponseCode.onHandle(GroupHomeActivity.this, i);
					GroupHomeActivity.this.finish();
				}

			}
		});
	}

	private void initListView()
	{
		Map<String, String> item = new HashMap<>();
		item.put("icon", R.drawable.enter_my_team + "");
		item.put("text", "审批申请");
		Map<String, String> item2 = new HashMap<>();
		item2.put("icon", R.drawable.enter_my_team + "");
		item2.put("text", "退出群组");
		mDatas = new ArrayList<>();
		mDatas.add(item);
		mDatas.add(item2);
		mDrawerList.setAdapter(mAdapter = new CommonAdapter<Map<String, String>>(this, mDatas, R
				.layout
				.home_menu_item)
		{
			@Override
			public void convert(ViewHolder helper, Map<String, String> item, int position)
			{
				View wrapper = helper.getConvertView();
				helper.setImageResource(R.id.iv_menu_iconItem, Integer.parseInt(item.get("icon")));
				TextView textView = helper.getView(R.id.tv_menu_textItem);
				textView.setText(item.get("text"));
				if (position == 0)//审批申请
				{
					if (!owner.equals(UserServiceImpl.getCurrentUserStringInfo(GroupHomeActivity
									.this,
							"username")))
					{
						//如果非群主本人，隐藏审批申请选项
						wrapper.setVisibility(View.GONE);
					}
					wrapper.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							enterApproval();
						}
					});
				} else if (position == 1)//退出群组
				{
					wrapper.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							exitGroup();
						}
					});

				}
			}
		});
//		mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener()
//		{
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view,
//			                        int position, long id)
//			{
//				switch (position)
//				{
//					case
//				}
//
//			}
//		});
	}

	/**
	 * 退出群组
	 */
	public void exitGroup()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("确定要退出" + team.getName() + "吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				JMessageClient.exitGroup(team.getId(), new BasicCallback()
				{
					@Override
					public void gotResult(int i, String s)
					{
						if (i == 0)
						{
							L.e("退出成功");
							JMessageClient.getGroupInfo(team.getId(), new GetGroupInfoCallback()
							{
								@Override
								public void gotResult(int i, String s, GroupInfo groupInfo)
								{
									if (i == 0)
									{
										L.e("取得群信息成功,新群主为：" + groupInfo.getGroupOwner());
										teamService.exitGroup(UserServiceImpl.getCurrentUserId
												(GroupHomeActivity.this)
												, team.getId(), handler, groupInfo.getGroupOwner
												());
									} else if (i == 898006)//代表此群已经被删除
									{
										//群已被删除
										teamService.exitGroup(UserServiceImpl.getCurrentUserId
												(GroupHomeActivity.this)
												, team.getId(), handler, null);
									} else
									{
										L.e("取得群信息失败");
										HandleResponseCode.onHandle(GroupHomeActivity.this, i);
									}
								}
							});

						} else
						{
							L.e("退出失败");
							HandleResponseCode.onHandle(GroupHomeActivity.this, i);
						}
					}
				});
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		builder.create().show();

	}

	/**
	 * 进入群聊
	 */
	public void enterGroupChat()
	{
		Intent intent = new Intent(GroupHomeActivity.this, GroupChatActivity.class);
		long teamId = team.getId();
		intent.putExtra("groupId", teamId);
		startActivity(intent);
	}

	/**
	 * 进入审批
	 */
	public void enterApproval()
	{
		Intent intent = new Intent(GroupHomeActivity.this,
				ApprovalJoinTeamActivity
						.class);
		long teamId = team.getId();
		intent.putExtra("teamId", teamId);
		startActivity(intent);
	}

	/**
	 * 在会话列表中接收消息
	 *
	 * @param event
	 */
	public void onEventMainThread(MessageEvent event)
	{
		L.e("onEventMainThread MessageEvent execute");
		refreshGroupChat();
	}

	/**
	 * 刷新群聊列表
	 */
	private void refreshGroupChat()
	{
		if (mGroupConv == null)
		{
			mGroupConv = JMessageClient.getGroupConversation(team.getId());
			mConvDatas.set(0, mGroupConv);
		}
		mConvAdapter.notifyDataSetChanged();
	}


	@Override
	public void onDestroy()
	{
		JMessageClient.unRegisterEventReceiver(this);
		JMessageClient.exitConversaion();
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// TODO Auto-generated method stub
		if (item.getItemId() == android.R.id.home)
		{
			if (mDrawerLayout.isDrawerOpen(Gravity.LEFT))
			{
				mDrawerLayout.closeDrawer(Gravity.LEFT);
			} else
			{
				mDrawerLayout.openDrawer(Gravity.LEFT);

			}
		}
		return super.onOptionsItemSelected(item);
	}
}
