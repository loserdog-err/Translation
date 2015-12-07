package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gdxz.zhongbao.client.Service.TeamService;
import com.gdxz.zhongbao.client.Service.impl.TeamServiceImpl;
import com.gdxz.zhongbao.client.common.CommonAdapter;
import com.gdxz.zhongbao.client.common.MyApplication;
import com.gdxz.zhongbao.client.common.ViewHolder;
import com.gdxz.zhongbao.client.domain.Team;
import com.gdxz.zhongbao.client.utils.FileUtils;
import com.gdxz.zhongbao.client.utils.HttpUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by chenantao on 2015/7/18.
 * 显示团队列表的activity
 */
public class TeamGroupListActivity extends Activity
{
	public static final int CATEGORY_SEARCH = 0;//搜索团队
	public static final int CATEGORY_RANK = 1;//团队排名
	public static final int CATEGORY_RECOMMEND = 2;//推荐团队

	//handler常量
	public static final int MSG_LOAD_TEXT_COMPLETE = 0;//文本数据加载成功
	public static final int MSG_LOAD_TEXT_FAILURE = 1;//文本数据加载失败

	private ListView mListView;
	private CommonAdapter mAdapter;
	//从服务器得到的team
	List<Team> mTeams;

	//标题栏
	private ImageView mIvBack;
	private TextView mTvTitle;

	int category;//标识当前是显示哪种类型的列表。参照常量INTENT_CATEGORY_XXX
	boolean haveTeam;


	private TeamService teamService = new TeamServiceImpl();

	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case MSG_LOAD_TEXT_COMPLETE:
					mTeams = (List<Team>) msg.obj;
					initListView();
					break;
				case MSG_LOAD_TEXT_FAILURE:
					Toast.makeText(TeamGroupListActivity.this, "(┬＿┬)加载不到数据", Toast.LENGTH_SHORT)
							.show();
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
		setContentView(R.layout.team_group_list);
		MyApplication.getInstance().addActivity(this);
		category = getIntent().getIntExtra("category", -1);
		haveTeam = getIntent().getBooleanExtra("haveTeam", false);
		String searchContent = getIntent().getStringExtra("searchContent");
		initView();
		initData();
		//请求服务器端的数据(logo，队名)
		if (category != -1)
		{
			teamService.getTeamList(category, handler, searchContent);
		}
	}


	private void initView()
	{
		mListView = (ListView) findViewById(R.id.lv_team_group_list);
		mTvTitle = (TextView) findViewById(R.id.tv_title);
		mIvBack = (ImageView) findViewById(R.id.iv_back);
	}

	private void initData()
	{
		mIvBack.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				TeamGroupListActivity.this.finish();
			}
		});
		mTvTitle.setText("团队列表");
	}

	private void initListView()
	{
		mListView.setAdapter(new CommonAdapter<Team>(this, mTeams, R.layout.common_listview_item)
		{
			@Override
			public void convert(ViewHolder helper, final Team item, int position)
			{
				View wrapper = helper.getConvertView();
				helper.setText(R.id.tv_text, item.getName());
				String path = item.getLogo();
				if (path != null && !"".equals(path))
				{
					ImageView ivIcon = helper.getView(R.id.iv_icon);
					DisplayImageOptions options = FileUtils.getDefaultImageOptions();
					ImageLoader.getInstance().displayImage(HttpUtils.BASE_FILE_PATH + path,
							ivIcon, options);
				}
				final long teamId = item.getId();
				//选项的单击事件
				wrapper.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Intent intent = new Intent(TeamGroupListActivity.this,
								TeamGroupIntroductionActivity.class);
						intent.putExtra("teamId", teamId).putExtra("haveTeam", haveTeam);
						startActivity(intent);
					}
				});
			}
		});

	}


}
