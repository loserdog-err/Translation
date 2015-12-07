package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gdxz.zhongbao.client.Service.UserService;
import com.gdxz.zhongbao.client.Service.impl.UserServiceImpl;
import com.gdxz.zhongbao.client.common.CommonAdapter;
import com.gdxz.zhongbao.client.common.MyApplication;
import com.gdxz.zhongbao.client.common.ViewHolder;
import com.gdxz.zhongbao.client.domain.User;
import com.gdxz.zhongbao.client.domain.UserDynamic;
import com.gdxz.zhongbao.client.utils.DialogUtils;
import com.gdxz.zhongbao.client.utils.FileUtils;
import com.gdxz.zhongbao.client.utils.HandleResponseCode;
import com.gdxz.zhongbao.client.utils.L;
import com.gdxz.zhongbao.client.utils.StringUtils;
import com.gdxz.zhongbao.client.view.customView.CustomDialog;
import com.gdxz.zhongbao.client.view.customView.SplashView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;

public class PersonalSettingActivity extends Activity
{
	public final static int REQ_CODE_GET_BY_GALLERY = 0;
	public final static int REQ_CODE_GET_BY_SELFIE = 1;

	//handler常量
	public static final int HANDLER_LOAD_DATA_COMPLETE = 0;
	//动态信息listview的item位置常量
	public static final int POSITION_DYNAMIC_ALL = 0;//全部动态
	public static final int POSITION_DYNAMIC_ANSWER = 1;//回答
	public static final int POSITION_DYNAMIC_ASK = 2;//提问
	public static final int POSITION_DYNAMIC_FOLLOW = 3;//收藏

	// 点击头像时弹出的选择对话框
	CustomDialog dialog;

	public static final String CATEGORY_UPLOAD_HEAD = "uploadHead";

	// 详细信息的位置
	public static final int POSITION_NORMAL_DETAIL = 2;

	//标识头像是否改变了
	public boolean isHeadChange = false;
	//用户头像路径
	public String headPath;

	ImageView ivHead;

	private TextView tvTitle;

	private List<Map<String, String>> items;

	private ListView normalListView;

	private ListView dynamicListView;

	private ImageView ivBack;

	// 定义一个List存放普通信息listview的item文本
	private List<String> tvNormalTitle;
	// 定义一个List去存放普通信息listview的item的第二个文本
	private List<String> tvNormalInfo;

	// 定义一个List存放动态信息listview的item文本
	private List<String> tvDynamicTitle;
	// 定义一个List去存放动态信息listview的item的第二个文本
	private List<String> tvDynamicInfo;

	// 布局根元素的linearLayout
	LinearLayout wrapper;
	// 用户名textview
	TextView tvUsername;

	// 从服务器端返回的User对象
	private User user;
	Map<String, String> userDynamicCount;//从服务端返回的显示用户各类动态数量的map
	//自拍产生的文件
	File cameraFile = null;
	//动画view
	private SplashView msSplashView;

	UserService userService = new UserServiceImpl();

	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case HANDLER_LOAD_DATA_COMPLETE:
					msSplashView.loadFinish();
					Map<String, Object> data = (Map<String, Object>) msg.obj;
					user = (User) data.get("userInfo");
					userDynamicCount = (Map<String, String>) data.get("userDynamicCount");
					if (data != null)
					{
						initData();
					} else
					{
						Toast.makeText(PersonalSettingActivity.this, "信息加载失败", Toast.LENGTH_SHORT)
								.show();
					}
					break;
				default:
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.personal_setting_ui);
		MyApplication.getInstance().addActivity(this);
		// 隐藏布局，显示进度条对话框
		msSplashView = (SplashView) findViewById(R.id.splashView);
		initTitle();
		userService.getUserInfo(
				UserServiceImpl.getCurrentUserId(this), handler, false);
	}

	/**
	 * 初始化数据
	 */
	public void initData()
	{
		tvNormalTitle = new ArrayList<>();
		tvNormalTitle.add("心情");
		tvNormalTitle.add("用户积分");
		tvNormalTitle.add("详细信息");
		tvNormalInfo = new ArrayList<>();
		tvNormalInfo.add(StringUtils.getPartOfText(10, user.getMood()));
		Integer point = user.getPoint();
		tvNormalInfo.add(point == null ? "" : point + "");
		tvNormalInfo.add("");
		tvDynamicTitle = new ArrayList<>();
		tvDynamicTitle.add("全部动态");
		tvDynamicTitle.add("我的回答");
		tvDynamicTitle.add("我的提问");
		tvDynamicTitle.add("我的收藏");
		tvDynamicInfo = new ArrayList<>();
		tvDynamicInfo.add(userDynamicCount.get(UserDynamic.DYNAMIC_TYPE_ALL + ""));
		tvDynamicInfo.add(userDynamicCount.get(UserDynamic.DYNAMIC_TYPE_ANSWER + ""));
		tvDynamicInfo.add(userDynamicCount.get(UserDynamic.DYNAMIC_TYPE_ASK + ""));
		tvDynamicInfo.add(userDynamicCount.get(UserDynamic.DYNAMIC_TYPE_FOLLOW + ""));
		tvUsername = (TextView) findViewById(R.id.tv_username);
		ivHead = (ImageView) findViewById(R.id.iv_head);
		// 点击头像时弹出的选择对话框
		CustomDialog.Builder builder = new CustomDialog.Builder(
				PersonalSettingActivity.this);
		builder.setMessage("选择头像");
		// 这里使用选择性别的布局文件
		LayoutInflater inflater = LayoutInflater.from(PersonalSettingActivity.this);
		View view = inflater.inflate(R.layout.select_gender_view, null);
		TextView tv1 = (TextView) view.findViewById(R.id.tv_man);
		TextView tv2 = (TextView) view.findViewById(R.id.tv_women);
		tv1.setText("自拍一张");
		tv1.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				cameraFile = new File(FileUtils.createImageFile());
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile));
				startActivityForResult(intent, REQ_CODE_GET_BY_SELFIE);
			}
		});
		tv2.setText("从相册中选择");
		tv2.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)// 从相册中选择的单击事件
			{
				Intent intent = new Intent(PersonalSettingActivity.this,
						LoadPictureAcitivity.class);
				intent.putExtra("category", LoadPictureAcitivity.SET_HEAD);
				startActivityForResult(intent, 0);
				dialog.dismiss();
			}
		});
		builder.setContentView(view);
		dialog = builder.create();
		// 头像的单击事件，从图库或者自拍一张照片
		ivHead.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialog.show();
			}
		});
		tvUsername.setText(user.getUsername());
		// 判断用户头像是否为空，如果为空，则使用默认的图片，如果不为空则需判断是从本地加载还是从服务器加载
		if (user.getHead() != null && !("".equals(user.getHead())))
		{
			String headPath = UserServiceImpl.getCurrentUserStringInfo(this, User.SP_HEAD);
			//判断本地是否缓存着用户的头像，如果有则设置，没有就从服务器加载
			if (headPath != null && !("").equals(headPath))//表示在手机找到用户的头像
			{
				ImageLoader.getInstance().displayImage("file://" + headPath, ivHead);
			} else//从服务器加载用户头像
			{
				userService.getUserHead(UserServiceImpl.getCurrentUserId(this), ivHead);
			}
		}
		initNormalListView();
		initDynamicListView();
	}

	public void initTitle()
	{
		ivBack = (ImageView) findViewById(R.id.iv_back);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvTitle.setText("个人中心");
		ivBack.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onReturn();
			}
		});

	}

	// 初始化基本信息listview
	public void initNormalListView()
	{
		items = new ArrayList<>();
		normalListView = (ListView) findViewById(R.id.lv_normal);
		for (int i = 0; i < tvNormalInfo.size(); i++)
		{
			Map<String, String> item = new HashMap<>();
			item.put("tvNormalTitle", tvNormalTitle.get(i));
			item.put("tvNormalInfo", tvNormalInfo.get(i));
			items.add(item);
		}
		normalListView.setAdapter(new CommonAdapter<Map<String, String>>(
				getApplicationContext(), items, R.layout.personal_setting_item)
		{
			@Override
			public void convert(ViewHolder helper, Map<String, String> item,
			                    int position)
			{
				RelativeLayout wrapper = (RelativeLayout) helper.getConvertView();
				TextView tvNormalTitle = helper.getView(R.id.tv_item_title);
				tvNormalTitle.setText(item.get("tvNormalTitle"));
				TextView tvNormalInfo = helper.getView(R.id.tv_item_info);
				tvNormalInfo.setText(item.get("tvNormalInfo"));
				if (position == POSITION_NORMAL_DETAIL)// 详细信息的单击事件
				{
					wrapper.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							Intent intent = new Intent(PersonalSettingActivity.this,
									PersonalDetailInfoSettingActivity.class);
							startActivity(intent);
						}
					});
				}
				else {
					wrapper.setEnabled(false);
				}
			}
		});
		items = null;
	}

	// 初始化动态类信息listview
	public void initDynamicListView()
	{
		items = new ArrayList<>();
		dynamicListView = (ListView) findViewById(R.id.lv_dynamic);
		for (int i = 0; i < tvDynamicTitle.size(); i++)
		{
			Map<String, String> item = new HashMap<>();
			item.put("tvDynamicTitle", tvDynamicTitle.get(i));
			item.put("tvDynamicInfo", tvDynamicInfo.get(i));
			items.add(item);
		}
		dynamicListView.setAdapter(new CommonAdapter<Map<String, String>>(
				getApplicationContext(), items, R.layout.personal_setting_item)
		{
			@Override
			public void convert(ViewHolder helper, Map<String, String> item,
			                    int position)
			{
				RelativeLayout wrapper = (RelativeLayout) (helper.getConvertView());
				TextView tvDynamicTitle = helper.getView(R.id.tv_item_title);
				tvDynamicTitle.setText(item.get("tvDynamicTitle"));
				TextView tvDynamicInfo = helper.getView(R.id.tv_item_info);
				tvDynamicInfo.setBackgroundResource(R.drawable.personal_dynamic_count_wrap);
				tvDynamicInfo.setTextAppearance(PersonalSettingActivity.this, R.style
						.font0_black_bold);
				RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tvDynamicInfo
						.getLayoutParams();
				lp.setMargins((int) getResources().getDimension(R.dimen.x10), 0, 0, 0);
				tvDynamicInfo.setLayoutParams(lp);
				tvDynamicInfo.setText(item.get("tvDynamicInfo"));
				if (position == POSITION_DYNAMIC_ALL)//点击了全部动态
				{
					wrapper.setOnClickListener(new OnDynamicItemClickListener
							(POSITION_DYNAMIC_ALL));
				} else if (position == POSITION_DYNAMIC_ASK)//点击了我的问题
				{
					wrapper.setOnClickListener(new OnDynamicItemClickListener
							(POSITION_DYNAMIC_ASK));
				} else if (position == POSITION_DYNAMIC_ANSWER)//点击了我的回答
				{
					wrapper.setOnClickListener(new OnDynamicItemClickListener
							(POSITION_DYNAMIC_ANSWER));
				} else if (position == POSITION_DYNAMIC_FOLLOW)//点击了我的关注
				{
					wrapper.setOnClickListener(new OnDynamicItemClickListener
							(POSITION_DYNAMIC_FOLLOW));
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (REQ_CODE_GET_BY_GALLERY == requestCode)//通过从相册选择的
		{
			if (resultCode == Activity.RESULT_OK)
			{
				Bundle bundle = data.getExtras();
				String[] headPaths = bundle.getStringArray("image");
				if (headPaths.length > 0)
				{
					headPath = headPaths[0];
					//拿到路径，先异步上传图片，上传成功后更新imageview
					userService.uploadHead(UserServiceImpl.getCurrentUserId(this), headPath,
							ivHead);
					DialogUtils.showProgressDialog("提醒", "上传图片中", PersonalSettingActivity.this);
					isHeadChange = true;
					updateAvatarInJmessage(headPath);
				}

			}
		} else if (requestCode == REQ_CODE_GET_BY_SELFIE)//通过自拍选择的
		{
			if (resultCode == RESULT_OK)
			{
//		Log.e("TAG", "success:" + cameraFile.getAbsolutePath());
				headPath = cameraFile.getAbsolutePath();
				userService.uploadHead(UserServiceImpl.getCurrentUserId(this), headPath, ivHead);
				isHeadChange = true;
				updateAvatarInJmessage(headPath);
				dialog.dismiss();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 动态信息条目的单击listener
	 */
	class OnDynamicItemClickListener implements OnClickListener
	{

		public int position;

		public OnDynamicItemClickListener(int position)
		{
			this.position = position;
		}

		@Override
		public void onClick(View v)
		{
			Intent intent = new Intent(PersonalSettingActivity.this, PersonalDynamicActivity
					.class);
			int category = 0;
			if (position == POSITION_DYNAMIC_ALL)
			{
				category = UserDynamic.DYNAMIC_TYPE_ALL;
			} else if (position == POSITION_DYNAMIC_ANSWER)
			{
				category = UserDynamic.DYNAMIC_TYPE_ANSWER;
			} else if (position == POSITION_DYNAMIC_ASK)
			{
				category = UserDynamic.DYNAMIC_TYPE_ASK;
			} else if (position == POSITION_DYNAMIC_FOLLOW)
			{
				category = UserDynamic.DYNAMIC_TYPE_FOLLOW;
			}
			intent.putExtra("category", category);
			startActivity(intent);
		}
	}

	/**
	 * 更新用户头像到Jmessage
	 */
	public void updateAvatarInJmessage(final String path)
	{
		JMessageClient.updateUserAvatar(new File(path), new BasicCallback(false)
		{
			@Override
			public void gotResult(final int status, final String desc)
			{
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						if (status == 0)
						{
							L.e("Update avatar succeed path " + path);
						} else
						{
							HandleResponseCode.onHandle(PersonalSettingActivity.this, status);
						}
					}
				});
			}
		});
	}

	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0)
		{
			DialogUtils.closeProgressDialog();
			onReturn();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 当用户点击返回图标或者按钮应该触发的方法
	 */
	public void onReturn()
	{
		//如果头像改变且头像路径不为空，通知首页改变头像
		if (isHeadChange && (headPath != null && !("".equals(headPath))))
		{
			Intent intent = new Intent();
			intent.putExtra("headPath", headPath);
			setResult(Activity.RESULT_OK, intent);
		}
		this.finish();
	}
}
