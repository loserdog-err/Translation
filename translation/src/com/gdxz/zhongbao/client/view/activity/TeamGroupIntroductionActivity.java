package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gdxz.zhongbao.client.Service.ApplicationListService;
import com.gdxz.zhongbao.client.Service.TeamService;
import com.gdxz.zhongbao.client.Service.impl.ApplicationListServiceImpl;
import com.gdxz.zhongbao.client.Service.impl.TeamServiceImpl;
import com.gdxz.zhongbao.client.Service.impl.UserServiceImpl;
import com.gdxz.zhongbao.client.common.CommonAdapter;
import com.gdxz.zhongbao.client.common.MyApplication;
import com.gdxz.zhongbao.client.common.ViewHolder;
import com.gdxz.zhongbao.client.domain.Team;
import com.gdxz.zhongbao.client.utils.DateUtils;
import com.gdxz.zhongbao.client.utils.DialogUtils;
import com.gdxz.zhongbao.client.utils.HandleResponseCode;
import com.gdxz.zhongbao.client.utils.HttpUtils;
import com.gdxz.zhongbao.client.utils.StringUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.model.GroupInfo;

/**
 * Created by chenantao on 2015/7/18.
 * 团队简介的activity，用户可以在这里看到团队的基本介绍以及加入团队
 */
public class TeamGroupIntroductionActivity extends Activity
{
	ListView mListView;
	Adapter mAdapter;
	Button btnJoinTeam;
	List<String> mDatas;

	//标题栏
	ImageView mIvBack;
	TextView mTvTitle;
	//加入团队按钮

	//团队标题栏(团队头像以及团队名)
	ImageView mIvGroupLogo;
	TextView mTvGroupName;


	//本activity所要显示的team
	private Team team;
	//标识当前用户是否拥有队伍
	boolean haveTeam;
	//团队的创建者
	public String owner;
	public static final int MSG_LOAD_COMPLETE = 0;
	public static final int MSG_LOAD_FAILURE = 1;
	public static final int MSG_APPLY_JOIN_GROUP_SUCCESS = 4;
	public static final int MSG_APPLY_JOIN_GROUP_FAILURE = 5;

	private TeamService teamService = new TeamServiceImpl();
	private ApplicationListService applicationListService = new ApplicationListServiceImpl();

	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case MSG_LOAD_COMPLETE:
					DialogUtils.closeProgressDialog();
					team = (Team) msg.obj;
					initData();
					initListView();
					break;
				case MSG_LOAD_FAILURE:
					DialogUtils.closeProgressDialog();
					Toast.makeText(TeamGroupIntroductionActivity.this, "加载失败", Toast.LENGTH_SHORT)
							.show();
					TeamGroupIntroductionActivity.this.finish();
					break;
				case MSG_APPLY_JOIN_GROUP_SUCCESS:
					onApplyJoinTeamSuccess();
					break;
				case MSG_APPLY_JOIN_GROUP_FAILURE:
					Toast.makeText(TeamGroupIntroductionActivity.this, "申请失败(┬＿┬)", Toast
							.LENGTH_SHORT).show();
					break;

			}
			super.handleMessage(msg);
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.team_group_introduction);
		MyApplication.getInstance().addActivity(this);
		long teamId = getIntent().getLongExtra("teamId", -1);
		haveTeam = getIntent().getBooleanExtra("haveTeam", false);
		if (teamId != -1)
		{
			DialogUtils.showProgressDialog("骚等", "数据加载中o(∩_∩)o ", this);
			teamService.getTeamById(teamId, handler);
		}
		initView();
	}

	private void initView()
	{
		mListView = (ListView) findViewById(R.id.lv_group_info);
		mIvBack = (ImageView) findViewById(R.id.iv_back);
		mTvTitle = (TextView) findViewById(R.id.tv_title);
		mIvGroupLogo = (ImageView) findViewById(R.id.iv_group_logo);
		mTvGroupName = (TextView) findViewById(R.id.tv_group_name);
		btnJoinTeam = (Button) findViewById(R.id.btn_join_group);
	}

	private void initData()
	{
		//标题栏
		mIvBack.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				TeamGroupIntroductionActivity.this.finish();
			}
		});
		mTvTitle.setText("团队简介");
		if (team.getLogo() != null)
		{
			ImageLoader.getInstance().displayImage(HttpUtils.BASE_FILE_PATH + team.getLogo(),
					mIvGroupLogo);
		}
		mTvGroupName.setText(team.getName());
	}

	private void initListView()
	{
		mDatas = new ArrayList<>();
		mDatas.add("团队宣言");
		mDatas.add("创始人");
		mDatas.add("创始日期");
		mDatas.add("团队排名");
		mDatas.add("团队积分");
		mListView.setAdapter(new CommonAdapter<String>(this, mDatas, R.layout
				.team_group_introduction_item)
		{
			@Override
			public void convert(final ViewHolder helper, String item, int position)
			{
				helper.setText(R.id.tv_item_title, item);
				if (position == 0)//团队宣言
				{
					helper.setText(R.id.tv_item_info, team.getDeclaration());
				} else if (position == 1)//创始人
				{
					JMessageClient.getGroupInfo(team.getId(), new GetGroupInfoCallback()
					{
						@Override
						public void gotResult(int i, String s, GroupInfo groupInfo)
						{
							if (i == 0)
							{
								helper.setText(R.id.tv_item_info, groupInfo.getGroupOwner());
							} else
							{
								TeamGroupIntroductionActivity.this.finish();
								HandleResponseCode.onHandle(TeamGroupIntroductionActivity.this, i);
							}
						}
					});
//					getTeamOwner();
				} else if (position == 2)//创始日期
				{
					helper.setText(R.id.tv_item_info, DateUtils.date2string(team.getBuildTime()));
				} else if (position == 3)//团队排名
				{
					helper.setText(R.id.tv_item_info, team.getRank() + "");
				} else if (position == 4)//团队积分
				{
					helper.setText(R.id.tv_item_info, StringUtils.nullIntegerFilter(team.getScore
							()));
				}
			}
		});
	}

	/**
	 * 加入按钮的单击事件
	 *
	 * @param view
	 */
	public void joinTeam(View view)
	{
		if (haveTeam)
		{
			Toast.makeText(this, "你已经拥有队伍了", Toast.LENGTH_SHORT).show();
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("确定要加入" + team.getName() + "吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				applicationListService.applyJoinTeam(team.getId(), UserServiceImpl.getCurrentUserId
						(TeamGroupIntroductionActivity.this), handler);
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
	 * 申请加群成功后的一些初始化操作
	 */
	private void onApplyJoinTeamSuccess()
	{
		Toast.makeText(this, "申请加群成功，请耐心等待群主审批o(∩_∩)o ", Toast.LENGTH_SHORT).show();
		btnJoinTeam.setEnabled(false);
	}

}
