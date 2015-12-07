package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dk.view.folder.ResideMenu;
import com.dk.view.folder.ResideMenuItem;
import com.gdxz.zhongbao.client.Service.UserRemindService;
import com.gdxz.zhongbao.client.Service.UserService;
import com.gdxz.zhongbao.client.Service.impl.UserRemindServiceImpl;
import com.gdxz.zhongbao.client.Service.impl.UserServiceImpl;
import com.gdxz.zhongbao.client.common.MyApplication;
import com.gdxz.zhongbao.client.domain.User;
import com.gdxz.zhongbao.client.domain.UserRemind;
import com.gdxz.zhongbao.client.utils.DialogUtils;
import com.gdxz.zhongbao.client.utils.NetUtils;
import com.gdxz.zhongbao.client.view.fragment.HomeNewlyQuestionFragment;
import com.gdxz.zhongbao.client.view.fragment.HomeTodayHotRankingFragment;
import com.gdxz.zhongbao.client.view.fragment.HomeTotalRankingFragment;
import com.gdxz.zhongbao.client.view.fragment.LazyFragment;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import github.chenupt.multiplemodel.viewpager.ModelPagerAdapter;
import github.chenupt.multiplemodel.viewpager.PagerModelManager;
import github.chenupt.springindicator.SpringIndicator;

public class HomeActivity extends FragmentActivity implements View.OnClickListener
{
	//标识oncreate是否执行完毕
	private boolean isOnCreate;
	// handler常量
	public static final int SERVER_ERROR = 0;// 服务器错误
	public static final int NEWLY_QUESTION = 1;// 请求最新提问的数据
	public static final int TODAY_HOT_RANKING = 2;// 请求今日热榜的数据
	public static final int TOTAL_RANKING = 3;// 请求总排行榜的数据
	public static final int REFRESH_DATA = 4;// 代表发出刷新请求
	public static final int ADD_MORE_DATA = 5;// 代表发出添加数据的请求
	public static final int NETWORK_NOT_CONNECT = 6;//代表网络未连接
	public static final int USER_REMIND = 7;
	//request code常量
	public static final int REQ_CODE_PERSONAL_SETTING = 0;
	public static final int REQ_CODE_WRITE_QUESTION = 1;


	// -----------------------------------menu相关变量-------------------------------
	public static final int OPEN_MENU_EDGE_LIMIT_X = 70;//打开侧滑菜单边缘的范围
	ListView listView;
	private ResideMenu resideMenu;
	ModelPagerAdapter mAdapter;
	//侧滑菜单的item
	ResideMenuItem userInfo;
	ResideMenuItem personalCenter;
	ResideMenuItem teamCenter;
	ResideMenuItem askQuestion;
	ResideMenuItem systemSetting;
	ResideMenuItem tradeCenter;
	ResideMenuItem commonProblem;
	// private LinearLayout openMenu;

	//title
	private TextView openMenu;
	private ImageView ivRemind;
	// -----------------------------------content相关变量-------------------------------
	private ViewPager mViewPager;
	SpringIndicator springIndicator;

	// 存放三个fragment的list
	private List<Fragment> mFragments = new ArrayList<>();

	//隐藏的打开网络的textview
	private TextView tvOpenNetwork;

	private ProgressDialog dialog;

	public ArrayList<UserRemind> userRemindList;//用户提醒list
	//业务组件
	private UserService userService = new UserServiceImpl();
	private UserRemindService userRemindService = new UserRemindServiceImpl();

	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case USER_REMIND:
					isOnCreate = true;
					userRemindList = (ArrayList<UserRemind>) msg.obj;
					ivRemind.setEnabled(true);
					boolean isRead = false;//遍历消息集合，如果有一条未读，则设置图标为有提醒
					if (userRemindList != null && userRemindList.size() > 0)
					{
						for (UserRemind userRemind : userRemindList)
						{
							if (!userRemind.getIsRead())
							{
								isRead = true;
								ivRemind.setImageResource(R.drawable.have_remind);
								break;
							}
						}
					}
					break;
				case SERVER_ERROR:
					Toast.makeText(HomeActivity.this, "服务器错误", Toast.LENGTH_SHORT).show();
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
		setContentView(R.layout.home_ui);
		isOnCreate = false;
		MyApplication.getInstance().addActivity(this);
		setUpMenu();
//		menu = CommonSlidingMenu.getSlidingMenu(this, R.layout.home_menu);
		//如果网络未连接，显示上方的提示框
		if (!NetUtils.isConnected(this))
		{
			tvOpenNetwork = (TextView) findViewById(R.id.open_network);
			tvOpenNetwork.setVisibility(View.VISIBLE);
			tvOpenNetwork.setOnClickListener(this);
		}
		init();

	}

	/**
	 * 初始化侧滑菜单
	 */
	private void setUpMenu()
	{
		// attach to current activity;
		resideMenu = new ResideMenu(this);
		resideMenu.setBackground(R.drawable.home_background);
		resideMenu.attachToActivity(this);
		//valid scale factor is between 0.0f and 1.0f. leftmenu'width is 150dip.
		resideMenu.setScaleValue(0.7f);
		resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);
		// create menu items;h
		userInfo = new ResideMenuItem(this, R.drawable.head, "陈岸涛");
		personalCenter = new ResideMenuItem(this, android.R.drawable
				.ic_menu_myplaces, "个人中心");
		teamCenter = new ResideMenuItem(this, android.R.drawable.ic_menu_view,
				"团队中心");
		askQuestion = new ResideMenuItem(this, android.R.drawable.ic_menu_help,
				"提问");
		systemSetting = new ResideMenuItem(this, android.R.drawable.ic_menu_manage,
				"系统设置");
		tradeCenter = new ResideMenuItem(this, android.R.drawable.ic_menu_share, "交易中心");
		commonProblem = new ResideMenuItem(this, android.R.drawable.ic_menu_crop, "常见问题");
		userInfo.setTextSize(20);
		userInfo.setImageSize(45, 45);
		personalCenter.setOnClickListener(this);
		teamCenter.setOnClickListener(this);
		askQuestion.setOnClickListener(this);
		systemSetting.setOnClickListener(this);
		tradeCenter.setOnClickListener(this);
		commonProblem.setOnClickListener(this);
		resideMenu.addMenuItem(userInfo, ResideMenu.DIRECTION_LEFT);
		resideMenu.addMenuItem(personalCenter, ResideMenu.DIRECTION_LEFT);
		resideMenu.addMenuItem(teamCenter, ResideMenu.DIRECTION_LEFT);
		resideMenu.addMenuItem(tradeCenter, ResideMenu.DIRECTION_LEFT);
		resideMenu.addMenuItem(askQuestion, ResideMenu.DIRECTION_LEFT);
		resideMenu.addMenuItem(systemSetting, ResideMenu.DIRECTION_LEFT);
		resideMenu.addMenuItem(commonProblem, ResideMenu.DIRECTION_LEFT);
	}


	/**
	 * 初始化各组件
	 */
	public void init()
	{
		openMenu = (TextView) findViewById(R.id.tv_openMenu);
		openMenu.setOnClickListener(this);
		ivRemind = (ImageView) findViewById(R.id.remind);
		ivRemind.setOnClickListener(this);
		ivRemind.setEnabled(false);
		//初始化用户信息(姓名以及头像)
		initUserInfo();
		// 初始化menu
//		initListView();
		// 初始化content
		initContent();
	}


	/**
	 * 初始化menu中用户的信息(姓名、头像)
	 */
	private void initUserInfo()
	{
		TextView tvUsername = userInfo.getTitle();
		ImageView ivHead = userInfo.getIcon();
		String username = UserServiceImpl.getCurrentUserStringInfo(this, "username");
		tvUsername.setText(username);
		String headInSp = UserServiceImpl.getCurrentUserStringInfo(this, User.SP_HEAD);
		//如果图片有缓存在本地则使用缓存，否则从服务器加载
		if (!"".equals(headInSp))
		{
//		Log.e("TAG", "getByCache:"+headInSp);
			ImageLoader.getInstance().displayImage("file://" + headInSp, ivHead);
		} else
		{
//		Log.e("TAG", "getByServer");
			if (NetUtils.isConnected(this))
			{
				userService.getUserHead(UserServiceImpl.getCurrentUserId(this), ivHead);
			}
		}

	}

	/**
	 * 初始化content
	 * 1：content的实现是仿微信的viewpager
	 * 2:content中包括今日热榜，总排行榜，最新提问三个fragment
	 * 3:需要一个指示器代表当前滑倒哪个fragment,原理是用户滑动的时候不断改变指示器的左边距
	 * 4：滑动到哪个fragment的时候需要改变对应的textview的颜色
	 */
	public void initContent()
	{
		springIndicator = (SpringIndicator) findViewById(R.id.indicator);
		mViewPager = (ViewPager) findViewById(R.id.vp_main);
		//异步从网络获取用户提醒的信息
		userRemindService.getUserRemind(UserServiceImpl.getCurrentUserId(this), handler);
		HomeNewlyQuestionFragment tab1 = new HomeNewlyQuestionFragment();
		HomeTodayHotRankingFragment tab2 = new HomeTodayHotRankingFragment();
		HomeTotalRankingFragment tab3 = new HomeTotalRankingFragment();
		mFragments.add(tab1);
		mFragments.add(tab2);
		mFragments.add(tab3);
		List<String> titles = new ArrayList<>();
		titles.add("最新提问");
		titles.add("今日最热");
		titles.add("总排行榜");
		PagerModelManager manager = new PagerModelManager();
		manager.addCommonFragment(mFragments, titles);
		mAdapter = new ModelPagerAdapter(getSupportFragmentManager(), manager);
		mViewPager.setAdapter(mAdapter);
		springIndicator.setViewPager(mViewPager);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
//		Log.e("TAG", "onActivityResult");
		if (requestCode == REQ_CODE_PERSONAL_SETTING)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				String path = data.getStringExtra("headPath");
//				Log.e("TAG", path);
				//将path存进sp中
				UserServiceImpl.setCurrentUserStringInfo(this, User.SP_HEAD, path);
				//更新用户信息
				ImageLoader.getInstance().displayImage("file://" + path, userInfo.getIcon());
			}
		} else if (requestCode == REQ_CODE_WRITE_QUESTION)
		{
			if (resultCode == RESULT_OK)
			{
				int currentIndex = mViewPager.getCurrentItem();
				((LazyFragment) this.mAdapter.getItem(currentIndex)).lazyLoad();

			}
		} else if (requestCode == NetUtils.REQ_CODE_OPEN_SETTING)
		{
			if (NetUtils.isConnected(HomeActivity.this))
			{
				tvOpenNetwork.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * 菜单的单击事件
	 *
	 * @param view
	 */
	@Override
	public void onClick(View view)
	{
		if (!NetUtils.isConnected(this) && view != systemSetting)
		{
			Toast.makeText(this, "網絡異常", Toast.LENGTH_SHORT).show();
			return;
		}
		if (view == personalCenter)
		{
			Intent intent = new Intent(HomeActivity.this,
					PersonalSettingActivity.class);
			HomeActivity.this.startActivityForResult(intent,
					REQ_CODE_PERSONAL_SETTING);
		} else if (view == teamCenter)
		{
			goToTeamCenter();
		} else if (view == askQuestion)
		{
			Intent intent = new Intent(HomeActivity.this, WriteMessage.class);
			Bundle bundle = new Bundle();
			bundle.putInt("category", WriteMessage.WRITE_QUESTION);
			intent.putExtra("info", bundle);
			HomeActivity.this.startActivityForResult(intent, REQ_CODE_WRITE_QUESTION);
		} else if (view == systemSetting)
		{
			Intent intent = new Intent(HomeActivity.this, SystemSettingActivity.class);
			startActivity(intent);
		} else if (view == tradeCenter)
		{
			Intent intent = new Intent(HomeActivity.this, TradeActivity.class);
			startActivity(intent);
		} else if (view == openMenu)
		{
			toggleMenu();
		} else if (view == ivRemind)//提醒
		{
			//向服务器发送请求，表明消息已读
			userRemindService.setHaveRead(UserServiceImpl.getCurrentUserId(this));
			ivRemind.setImageResource(R.drawable.no_remind);
			Intent intent = new Intent(this, UserRemindActivity.class);
			intent.putExtra("userRemindList", userRemindList);
			startActivity(intent);
		} else if (view == tvOpenNetwork)
		{
			NetUtils.openSetting(HomeActivity.this);
		} else if (view == commonProblem)//常见问题
		{
			Intent intent = new Intent(this, CommonProblemActivity.class);
			startActivity(intent);
		}
	}

	/**
	 * 进入团队中心
	 */
	private void goToTeamCenter()
	{
		final String username = UserServiceImpl.getCurrentUserStringInfo
				(HomeActivity.this,
						"username");
		String password = UserServiceImpl.getCurrentUserStringInfo(HomeActivity
				.this, "password");
		if (NetUtils.isConnected(this))
		{
			Intent intent = new Intent(HomeActivity.this, TeamIndexActivity
					.class);
			HomeActivity.this.startActivity(intent);
		} else
		{
			Toast.makeText(this, "請檢查你的網絡是否連接", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	protected void onResume()
	{
		if (isOnCreate)
		{
			//异步从网络获取用户提醒的信息
			userRemindService.getUserRemind(UserServiceImpl.getCurrentUserId(this), handler);
		}
		if (tvOpenNetwork != null)
		{
			if (NetUtils.isConnected(this))
			{
				tvOpenNetwork.setVisibility(View.GONE);

			} else
			{
				tvOpenNetwork.setVisibility(View.VISIBLE);
			}
		}
		super.onResume();
	}

	/**
	 * 打开或关闭侧滑菜单
	 */
	private void toggleMenu()
	{
		if (resideMenu.isOpened())
			resideMenu.closeMenu();
		else
			resideMenu.openMenu(ResideMenu
					.DIRECTION_LEFT);
	}

	/**
	 *  * 菜单、返回键响应
	 *  
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			DialogUtils.closeProgressDialog();
			exitBy2Click(); //调用双击退出函数
		}
		return false;
	}

	/**
	 *  * 双击退出函数
	 *  
	 */
	private static Boolean isExit = false;

	private void exitBy2Click()
	{
		Timer tExit = null;
		if (isExit == false)
		{
			isExit = true; // 准备退出
			Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
			tExit = new Timer();
			tExit.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					isExit = false; // 取消退出
				}

			}, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
		} else
		{
			finish();
			System.exit(0);
		}
	}

	/**
	 * 边缘滑动才能打开侧滑菜单
	 *
	 * @param ev
	 * @return
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		int action = ev.getAction();
		switch (action)
		{
			case MotionEvent.ACTION_DOWN:
				if (ev.getX() > OPEN_MENU_EDGE_LIMIT_X)
				{
					resideMenu.addIgnoredView(mViewPager);
				} else
				{
					resideMenu.clearIgnoredViewList();
				}
				break;
		}
		return resideMenu.dispatchTouchEvent(ev);
	}
}
