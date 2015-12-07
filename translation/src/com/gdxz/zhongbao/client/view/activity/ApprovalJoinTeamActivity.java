package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gdxz.zhongbao.client.Service.ApplicationListService;
import com.gdxz.zhongbao.client.Service.TeamService;
import com.gdxz.zhongbao.client.Service.impl.ApplicationListServiceImpl;
import com.gdxz.zhongbao.client.Service.impl.TeamServiceImpl;
import com.gdxz.zhongbao.client.common.CommonAdapter;
import com.gdxz.zhongbao.client.common.MyApplication;
import com.gdxz.zhongbao.client.common.ViewHolder;
import com.gdxz.zhongbao.client.domain.ApplicationList;
import com.gdxz.zhongbao.client.utils.HandleResponseCode;
import com.gdxz.zhongbao.client.utils.L;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by chenantao on 2015/7/20.
 * 审批加群的activity
 */

public class ApprovalJoinTeamActivity extends Activity
{
	private ListView mListView;
	private CommonAdapter mAdapter;
	private List<ApplicationList> mDatas;

	public static final int MSG_LOAD_COMPLETE = 0;
	public static final int MSG_LOAD_FAILURE = 1;
	public static final int MSG_ADD_GROUP_MEMBER_SUCCESS = 2;
	public static final int MSG_ADD_GROUP_MEMBER_FAILURE = 3;
	//标题栏
	ImageView mIvBack;
	TextView mTvTitle;
	long teamId;//当前队伍的id
	String approvalUsername;//审批了哪个用户

	private ApplicationListService applicationListService = new ApplicationListServiceImpl();
	private TeamService teamService = new TeamServiceImpl();

	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case MSG_LOAD_COMPLETE:
					mDatas = (List<ApplicationList>) msg.obj;
					initListView();
					break;
				case MSG_ADD_GROUP_MEMBER_SUCCESS:
					L.e("添加群成员到服务器成功");
					onJoinTeamSuccess();
					break;
				case MSG_ADD_GROUP_MEMBER_FAILURE:
					Toast.makeText(ApprovalJoinTeamActivity.this, "加入失败(┬＿┬)", Toast
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
		setContentView(R.layout.approval_join);
		MyApplication.getInstance().addActivity(this);
		initView();
		initData();
		teamId = getIntent().getLongExtra("teamId", -1);
		if (teamId != -1)
		{
			applicationListService.getApplicationList(teamId, handler);
		}
	}


	private void initView()
	{
		mIvBack = (ImageView) findViewById(R.id.iv_back);
		mTvTitle = (TextView) findViewById(R.id.tv_title);
		mListView = (ListView) findViewById(R.id.lv_apply);
	}

	private void initData()
	{
		mIvBack.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ApprovalJoinTeamActivity.this.finish();
			}
		});
		mTvTitle.setText("申请列表");
	}


	private void initListView()
	{
		mListView.setAdapter(mAdapter = new CommonAdapter<ApplicationList>(this, mDatas, R.layout
				.approval_join_item)
		{
			@Override
			public void convert(ViewHolder helper, final ApplicationList item, int position)
			{
				helper.setText(R.id.tv_applyInfo, item.getUser().getUsername() + " 申请加入该群");
				final Button btnAgree = helper.getView(R.id.btn_agree);
				final Button btnDisagree = helper.getView(R.id.btn_disagree);
				if (item.getIsHandle())
				{
					btnAgree.setVisibility(View.INVISIBLE);
					btnDisagree.setText("已处理");
					setBtnDisable(btnAgree, btnDisagree);
				} else
				{
					btnAgree.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							teamService.addGroupMember(item.getUser().getId() + "", teamId,
									handler);
							approvalUsername = item.getUser().getUsername();
							setBtnDisable(btnAgree, btnDisagree);
						}
					});
					btnDisagree.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							teamService.disagreeJoin(teamId, item.getUser().getId());
							setBtnDisable(btnAgree, btnDisagree);
							Toast.makeText(ApprovalJoinTeamActivity.this, "已拒绝该用户申请", Toast
									.LENGTH_SHORT).show();
						}
					});
				}
			}
		});
	}

	/**
	 * 加群成功后的一些列操作
	 */
	public void onJoinTeamSuccess()
	{
		//添加到Jmessage
		List<String> members = new ArrayList<>();
		members.add(approvalUsername);
		JMessageClient.addGroupMembers(teamId, members, new BasicCallback()
		{
			@Override
			public void gotResult(int i, String s)
			{
				if (i == 0)
				{
					Toast.makeText(ApprovalJoinTeamActivity.this, "添加成功，该用户现在是你们的一员啦", Toast
							.LENGTH_SHORT).show();
					L.e("添加群成员到Jmessage成功");
				} else
				{
					L.e("添加群成员到Jmessage失败");
					HandleResponseCode.onHandle(ApprovalJoinTeamActivity.this, i);
				}
			}
		});
	}

	public void setBtnDisable(Button btn1, Button btn2)
	{
		btn1.setEnabled(false);
		btn2.setEnabled(false);
	}
}
