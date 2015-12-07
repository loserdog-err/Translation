package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import com.gdxz.zhongbao.client.utils.DialogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenantao on 2015/7/18.
 * 团队首页的activity
 */
public class TeamIndexActivity extends Activity implements View.OnClickListener
{
	public static final int POSITION_ENTER_MY_TEAM = 0;//进入我的团队
	public static final int POSITION_VIEW_TEAM_RANK = 1;//查看团队排名
	public static final int POSITION_RECOMMEND_TEAM = 2;//推荐团队
	public static final int POSITION_CREATE_MY_TEAM = 3;//创建自己的团队

	public static final int MSG_GET_TEAM = 0;

	boolean isOncreate;

	ListView mListView;
	CommonAdapter mAdapter;

	//搜索栏
	Button btnSearch;
	EditText etSearch;

	//标题栏
	ImageView mIvBack;
	TextView mTvTitle;
	List<String> mLvDatas;//listview中item的文本数据

	//标识是否已经有团队
	boolean haveTeam;
	//当前用户所归属的团队
	Team team;

	private TeamService teamService = new TeamServiceImpl();
	private UserService userService = new UserServiceImpl();

	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case MSG_GET_TEAM:
					isOncreate = true;
					DialogUtils.closeProgressDialog();
					if (msg.obj != null)//已有团队
					{
						team = (Team) msg.obj;
						haveTeam = true;
					} else
					{
						haveTeam = false;
					}
					initListView();
					break;
				default:
					DialogUtils.closeProgressDialog();
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
		setContentView(R.layout.team_index);
		MyApplication.getInstance().addActivity(this);
		initView();
		userService.getTeam(UserServiceImpl.getCurrentUserId(this), handler);
//		DialogUtils.showProgressDialog("提醒", "加载中，请稍后", this);
	}

	private void initView()
	{
		mListView = (ListView) findViewById(R.id.lv_option);
		mIvBack = (ImageView) findViewById(R.id.iv_back);
		mTvTitle = (TextView) findViewById(R.id.tv_title);
		btnSearch = (Button) findViewById(R.id.btn_search);
		etSearch = (EditText) findViewById(R.id.et_search);
		initData();
	}

	private void initData()
	{
		mTvTitle.setText("团队首页");
		mIvBack.setOnClickListener(this);
		btnSearch.setOnClickListener(this);
	}


	private void initListView()
	{
		mLvDatas = new ArrayList<>();
		mLvDatas.add("进入我的团队");
		mLvDatas.add("查看团队排名");
		mLvDatas.add("推荐加入团队");
		mLvDatas.add("创建团队");
		mListView.setAdapter(new CommonAdapter<String>(this, mLvDatas, R.layout
				.common_listview_item)
		{
			@Override
			public void convert(ViewHolder helper, String item, int position)
			{
				helper.setText(R.id.tv_text, item);
				if (position == POSITION_ENTER_MY_TEAM)
				{
					helper.setImageResource(R.id.iv_icon, R.drawable.enter_my_team);
				} else if (position == POSITION_VIEW_TEAM_RANK)
				{
					helper.setImageResource(R.id.iv_icon, R.drawable.view_team_ranking);
				} else if (position == POSITION_RECOMMEND_TEAM)
				{
					helper.setImageResource(R.id.iv_icon, R.drawable.team_group_logo);
				} else if (position == POSITION_CREATE_MY_TEAM)
				{
					helper.setImageResource(R.id.iv_icon, R.drawable.create_team);
				}

			}
		});
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			private Intent intent;

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				switch (position)
				{
					case POSITION_ENTER_MY_TEAM://点击进入我的团队
						if (haveTeam)
						{
							intent = new Intent(TeamIndexActivity.this, GroupHomeActivity.class);
							Bundle bundle = new Bundle();
							bundle.putSerializable("team", team);
							intent.putExtras(bundle);
							startActivity(intent);
						} else
						{
							Toast.makeText(TeamIndexActivity.this, "你还没有加入任何团队哦！", Toast
									.LENGTH_SHORT).show();
						}
						break;
					case POSITION_VIEW_TEAM_RANK://点击查看团队排名
						intent = new Intent(TeamIndexActivity.this, TeamGroupListActivity.class);
						intent.putExtra("category", TeamGroupListActivity.CATEGORY_RANK);
						startActivity(intent);
						break;
					case POSITION_RECOMMEND_TEAM://点击查看推荐团队
						intent = new Intent(TeamIndexActivity.this, TeamGroupListActivity.class);
						intent.putExtra("haveTeam", haveTeam).putExtra("category",
								TeamGroupListActivity.CATEGORY_RECOMMEND);
						startActivity(intent);
						break;
					case POSITION_CREATE_MY_TEAM://点击创建团队
						if (haveTeam)
						{
							Toast.makeText(TeamIndexActivity.this, "用户只能有一个团队", Toast
									.LENGTH_SHORT).show();
						} else
						{
							intent = new Intent(TeamIndexActivity.this, WriteMessage.class);
							Bundle bundle = new Bundle();
							bundle.putInt("category", WriteMessage.CREATE_TEAM);
							intent.putExtra("info", bundle);
							startActivity(intent);
						}
				}
			}
		});
	}


	@Override
	protected void onNewIntent(Intent intent)
	{
		userService.getTeam(UserServiceImpl.getCurrentUserId(this), handler);
		super.onNewIntent(intent);
	}

	@Override
	protected void onResume()
	{
		if (isOncreate)
		{
			userService.getTeam(UserServiceImpl.getCurrentUserId(this), handler);
		}
		super.onResume();
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.iv_back:
				TeamIndexActivity.this.finish();
				break;
			case R.id.btn_search:
				String searchContent = etSearch.getText().toString().trim();
				if (searchContent.equals("") || searchContent.length() > 14)
				{
					Toast.makeText(this, "搜索内容格式有误", Toast.LENGTH_SHORT).show();
				} else
				{
					Intent intent = new Intent(this, TeamGroupListActivity.class);
					intent.putExtra("category", TeamGroupListActivity.CATEGORY_SEARCH).putExtra
							("searchContent", searchContent);
					startActivity(intent);
				}
				break;
		}
	}
}
