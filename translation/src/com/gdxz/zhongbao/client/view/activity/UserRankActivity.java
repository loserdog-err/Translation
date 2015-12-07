package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gdxz.zhongbao.client.Service.UserService;
import com.gdxz.zhongbao.client.Service.impl.UserServiceImpl;
import com.gdxz.zhongbao.client.common.CommonAdapter;
import com.gdxz.zhongbao.client.common.ViewHolder;
import com.gdxz.zhongbao.client.domain.User;
import com.gdxz.zhongbao.client.utils.FileUtils;
import com.gdxz.zhongbao.client.utils.HttpUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by Chean_antao on 2015/8/15.
 * 用户排名的activity(根据采纳率)
 */
public class UserRankActivity extends Activity
{

	private ListView mListView;
	private CommonAdapter mAdapater;
	private List<User> mDatas;

	//titile
	private ImageView ivBack;
	private TextView tvTitle;

	private String questionId;

	UserService userService = new UserServiceImpl();

	public static final int MSG_DATA_LOAD_COMPLETE = 0;

	private Handler handler = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case MSG_DATA_LOAD_COMPLETE:
					mDatas = (List<User>) msg.obj;
					initData();
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
		setContentView(R.layout.user_rank_ui);
		questionId = getIntent().getStringExtra("questionId");
		if (questionId != null)
		{
			initView();
		}


	}

	private void initView()
	{
		ivBack = (ImageView) findViewById(R.id.iv_back);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvTitle.setText("牛人榜");
		ivBack.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				UserRankActivity.this.finish();
			}
		});
		mListView = (ListView) findViewById(R.id.lv_user_list);
		//加载服务器的数据
		userService.getUserList(handler);
	}

	private void initData()
	{
		mListView.setAdapter(mAdapater = new CommonAdapter<User>(this, mDatas, R.layout
				.item_1iv_2tv)
		{
			@Override
			public void convert(ViewHolder helper, User item, int position)
			{
				//异步加载用户头像
				DisplayImageOptions options = FileUtils.getDefaultImageOptions();
				String head = item.getHead();
				if (head != null && !"".equals(head))
				{
					ImageView ivHead = helper.getView(R.id.iv_icon);
					ImageLoader.getInstance().displayImage(HttpUtils.BASE_FILE_PATH + head,
							ivHead, options);
				}
				helper.setText(R.id.tv_first, item.getUsername());
				helper.setText(R.id.tv_second, Html.fromHtml("正确率:<font color='#63B8FF'>" + item
						.getCorrectRate() + "</font>%"));
			}
		});
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(UserRankActivity.this);
				builder.setMessage("确定要邀请此人回答吗?").setPositiveButton("确定", new DialogInterface
						.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						//向服务器发送请求，推送给此人
						userService.inviteToAnswer(mDatas.get(position).getId(), Integer.parseInt
								(questionId));
						Toast.makeText(UserRankActivity.this, "邀请成功", Toast.LENGTH_SHORT).show();
						dialog.dismiss();
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				}).create().show();
			}
		});
	}

}
