package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gdxz.zhongbao.client.Service.UserDynamicService;
import com.gdxz.zhongbao.client.Service.impl.UserDynamicServiceImpl;
import com.gdxz.zhongbao.client.Service.impl.UserServiceImpl;
import com.gdxz.zhongbao.client.common.CommonAdapter;
import com.gdxz.zhongbao.client.common.MyApplication;
import com.gdxz.zhongbao.client.common.ViewHolder;
import com.gdxz.zhongbao.client.domain.UserDynamic;
import com.gdxz.zhongbao.client.utils.DialogUtils;

import java.util.List;

/**
 * 用户动态信息展示页面
 * Created by chenantao on 2015/7/15.
 */
public class PersonalDynamicActivity extends Activity
{
	//handler的常量
	public static final int MSG_LOAD_COMPLETE = 0;
	public static final int MSG_SERVER_ERROR = 1;
	//顶部title的view
	ImageView mIvBack;
	TextView mTvTitle;

	ListView mLvDynamic;
	List<UserDynamic> mDatas;

	UserDynamicService userDynamicService = new UserDynamicServiceImpl();
	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case MSG_LOAD_COMPLETE:
					mDatas = (List<UserDynamic>) msg.obj;
					initData();
					DialogUtils.closeProgressDialog();
					break;
				case MSG_SERVER_ERROR:
					Toast.makeText(PersonalDynamicActivity.this, "服务器错误", Toast.LENGTH_SHORT)
							.show();
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
		setContentView(R.layout.personal_setting_dynamic_ui);
		MyApplication.getInstance().addActivity(this);
		DialogUtils.showProgressDialog("提醒", "用户动态信息加载中", this);
		initView();
		initTitle();
		int category = getIntent().getIntExtra("category", -1);
		Log.e("TAG", category + "");
		if (category != -1)
		{
			userDynamicService.loadData(category, UserServiceImpl.getCurrentUserId(this), handler);
		}

	}

	/**
	 * 初始化标题栏
	 */
	private void initTitle()
	{
		mIvBack.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				PersonalDynamicActivity.this.finish();
			}
		});
		mTvTitle.setText("用户动态");
	}

	/**
	 * 初始化数据
	 */
	private void initData()
	{
		mLvDynamic.setAdapter(new CommonAdapter<UserDynamic>(this, mDatas, R.layout
				.personal_setting_dynamic_item)
		{
			@Override
			public void convert(ViewHolder helper, UserDynamic item, int position)
			{
				LinearLayout wrapper = (LinearLayout) helper.getConvertView();
				TextView tvType = helper.getView(R.id.tv_type);
				TextView tvTitle = helper.getView(R.id.tv_title);
				TextView tvContent = helper.getView(R.id.tv_content);
				final String questionId;
				//如果类型是回答了问题的，显示内容区域
				if (item.getType() == UserDynamic.DYNAMIC_TYPE_ANSWER)
				{
					tvType.setText("回答了问题");
					tvContent.setText(item.getAnswer().getContent());
					tvTitle.setText(item.getAnswer().getQuestion().getTitle());
					questionId = item.getAnswer().getQuestion().getId() + "";
				} else
				{
					if (item.getType() == UserDynamic.DYNAMIC_TYPE_ASK)
					{
						tvType.setText("提出了问题");
					} else if (item.getType() == UserDynamic.DYNAMIC_TYPE_FOLLOW)
					{
						tvType.setText("收藏了问题");
					}
					tvContent.setText(item.getQuestion().getContent());
					tvTitle.setText(item.getQuestion().getTitle());
					questionId = item.getQuestion().getId() + "";
				}
				//各选项的单击事件，点击跳转到对应的问题页面
				wrapper.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Intent intent = new Intent(PersonalDynamicActivity.this,
								QuestionDetailActivity.class);
						intent.putExtra("questionId", questionId);
						startActivity(intent);
					}
				});
			}
		});
	}


	private void initView()
	{
		mIvBack = (ImageView) findViewById(R.id.iv_back);
		mTvTitle = (TextView) findViewById(R.id.tv_title);
		mLvDynamic = (ListView) findViewById(R.id.lv_dynamic);
	}


}
