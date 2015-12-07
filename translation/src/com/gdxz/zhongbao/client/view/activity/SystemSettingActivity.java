package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gdxz.zhongbao.client.Service.impl.UserServiceImpl;
import com.gdxz.zhongbao.client.common.MyApplication;
import com.gdxz.zhongbao.client.domain.SystemConfig;
import com.gdxz.zhongbao.client.domain.SystemConfigItem;
import com.gdxz.zhongbao.client.utils.DatabaseUtils;
import com.gdxz.zhongbao.client.utils.FileUtils;
import com.gdxz.zhongbao.client.utils.L;
import com.gdxz.zhongbao.client.view.adapter.SystemSettingAdapter;
import com.gdxz.zhongbao.client.view.customView.MyHeaderListView;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.android.api.JPushInterface;
import me.xiaopan.switchbutton.SwitchButton;

/**
 * Created by chenantao on 2015/7/23.
 */
public class SystemSettingActivity extends Activity
{
	MyHeaderListView mLvOption;
	View header;
	//	CommonAdapter mAdapter;
	SystemSettingAdapter mAdapter;
	List<SystemConfigItem> mDatas;
	public static final int POSITION_CHANGE_ACCOUNT = 1;
	public static final int POSITION_LOGOUT_ACCOUNT = 2;
	public static final int POSITION_CLEAN_CACHE = 3;
	public static final int POSITION_EDITION_INFO = 4;
	public static final int POSITION_RECEIVE_PUSH = 5;
	public static final int POSITION_AUTO_LOGIN = 6;

	//标题栏
	ImageView mIvBack;
	TextView mTvTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.system_setting);
		MyApplication.getInstance().addActivity(this);
		initView();
		initListView();
	}


	private void initView()
	{
		mIvBack = (ImageView) findViewById(R.id.iv_back);
		mTvTitle = (TextView) findViewById(R.id.tv_title);
		mLvOption = (MyHeaderListView) findViewById(R.id.lv_option);
		mIvBack.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				SystemSettingActivity.this.finish();
			}
		});
		mTvTitle.setText("系统设置");
	}

	private void initListView()
	{
		header = View.inflate(this, R.layout.system_setting_listview_header, null);
		mLvOption.addHeaderView(header);
		//从数据库加载配置信息
		mDatas = new ArrayList<>();
		String cacheSize = calcCacheSize();
		SystemConfigItem changeAccount = new SystemConfigItem("切换账号", "");
		SystemConfigItem logoutAccount = new SystemConfigItem("退出账号", "");
		final SystemConfigItem clearCache = new SystemConfigItem("清空缓存", cacheSize);
		SystemConfigItem editionInfo = new SystemConfigItem("版本信息", "1.0");
		SystemConfigItem receivePush = new SystemConfigItem("接收推送", MyApplication.isReceivePush);
		SystemConfigItem autoLogin = new SystemConfigItem("自动登录", MyApplication.isAutoLogin);
		mDatas.add(changeAccount);
		mDatas.add(logoutAccount);
		mDatas.add(clearCache);
		mDatas.add(editionInfo);
		mDatas.add(receivePush);
		mDatas.add(autoLogin);
		mAdapter = new SystemSettingAdapter(this, mDatas);
		mLvOption.setAdapter(mAdapter);
		mLvOption.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Intent intent;
				switch (position)
				{
					case POSITION_CHANGE_ACCOUNT://切换帐号
						intent = new Intent(SystemSettingActivity.this, LoginActivity.class);
						intent.putExtra("category", "noByCache");
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(intent);
						SystemSettingActivity.this.finish();
						break;
					case POSITION_LOGOUT_ACCOUNT://退出帐号
						logout();
						break;
					case POSITION_CLEAN_CACHE:
						clearCache();
						break;
				}
			}
		});
	}

	/**
	 * 计算缓存文件的大小
	 *
	 * @return
	 */
	private String calcCacheSize()
	{
		File imageFile = FileUtils.getImageCacheDir();
		File voiceFile = FileUtils.getVoiceCacheDir();
		float byteSize = 0.0f;
		if (imageFile != null && imageFile.exists())
		{
			byteSize += FileUtils.getFileSize(imageFile);
		}
		if (voiceFile != null && voiceFile.exists())
		{
			byteSize += FileUtils.getFileSize(voiceFile);
		}
		float mbSize = byteSize / (1024 * 1024);
		if (mbSize < 0.01f)//如果保留两位小数全是0，则返回kb单位的大小
		{
			return ((int) (byteSize / 1024 * 100) / 100) + " kb";
		} else
		{
			return ((int) (mbSize * 100) / 100.0f) + " M";//将大小转换为mb并且保留两位小数
		}
	}


	/**
	 * 注销用户
	 */
	private void logout()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(SystemSettingActivity.this);
		builder.setMessage("确定要退出并清空登录信息吗");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				UserServiceImpl.setCurrentUserId(SystemSettingActivity.this, "");
				MyApplication.getInstance().exit();
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
	 * 清空缓存
	 */
	public void clearCache()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(SystemSettingActivity.this);
		builder.setMessage("确定要清空缓存吗？")
				.setNegativeButton("取消", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				}).setPositiveButton("确定", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				deleteCacheFile();
				Toast.makeText(SystemSettingActivity.this, "清理成功", Toast.LENGTH_SHORT)
						.show();

				SystemConfigItem item = mDatas.get(POSITION_CLEAN_CACHE-1);
				item.setConfigValue("0kb");
//				mDatas.set(POSITION_CLEAN_CACHE, item);
				mAdapter.notifyDataSetChanged();
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	/**
	 * 删除缓存文件
	 */
	private void deleteCacheFile()
	{
		File imageFile = FileUtils.getImageCacheDir();
		File voiceFile = FileUtils.getVoiceCacheDir();
		L.e(imageFile.isDirectory() + "");
		if (imageFile != null && imageFile.exists())
		{
			FileUtils.deleteDir(imageFile);
		}
		if (voiceFile != null && voiceFile.exists())
		{
			FileUtils.deleteDir(voiceFile);
		}
	}


	/**
	 * 退出activity时，将配置信息写进数据库，更新全局变量
	 */
	@Override
	protected void onDestroy()
	{
		DbUtils dbUtils = DatabaseUtils.getDbUtils(this);
		SystemConfig config = new SystemConfig();
		config.setId(Integer.parseInt(UserServiceImpl.getCurrentUserId(this)));
		for (int i = 5; i < mLvOption.getCount(); i++)//因为有一个header，所以下标要从5开始
		{
			View view = mLvOption.getChildAt(i);
			SwitchButton switchButton = (SwitchButton) view.findViewById(R.id.switch_button);
			boolean isCheck = switchButton.isChecked();
			if (i == POSITION_RECEIVE_PUSH)
			{
				config.setIsReceivePush(isCheck);
				MyApplication.isReceivePush = isCheck;
			} else if (i == POSITION_AUTO_LOGIN)
			{
				config.setIsAutoLogin(isCheck);
				MyApplication.isAutoLogin = isCheck;
			}
//			L.e("destroy:"+isCheck);
		}
		if (config.isReceivePush())
		{
			if (JPushInterface.isPushStopped(getApplicationContext()))
			{
				L.e("resume");
				JPushInterface.resumePush(getApplicationContext());
			}

		} else
		{
			if (!JPushInterface.isPushStopped(getApplicationContext()))
			{
				L.e("stop");
				JPushInterface.stopPush(getApplicationContext());
			}
		}
		try
		{
			dbUtils.saveOrUpdate(config);
		} catch (DbException e)
		{
			e.printStackTrace();
		}
		super.onDestroy();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		mLvOption.initImageView((ImageView) header.findViewById(R.id.iv_header));
		super.onWindowFocusChanged(hasFocus);
	}
}
