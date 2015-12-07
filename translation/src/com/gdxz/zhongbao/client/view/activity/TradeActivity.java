package com.gdxz.zhongbao.client.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bmob.pay.tool.BmobPay;
import com.bmob.pay.tool.OrderQueryListener;
import com.bmob.pay.tool.PayListener;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gdxz.zhongbao.client.Service.OrderService;
import com.gdxz.zhongbao.client.Service.UserService;
import com.gdxz.zhongbao.client.Service.impl.OrderServiceImpl;
import com.gdxz.zhongbao.client.Service.impl.UserServiceImpl;
import com.gdxz.zhongbao.client.domain.User;
import com.gdxz.zhongbao.client.utils.DialogUtils;
import com.gdxz.zhongbao.client.utils.L;
import com.gdxz.zhongbao.client.view.customView.MyEditText;
import com.yalantis.guillotine.animation.GuillotineAnimation;

/**
 * Created by Chean_antao on 2015/8/12.
 * 用户兑现和购买积分的页面
 */
public class TradeActivity extends ActionBarActivity implements View.OnClickListener
{
	public static final int RATE_POINT_RMB = 100000;//积分与人名币的比率
	private static final long RIPPLE_DURATION = 250;
	public static final int MSG_LOAD_USER_INFO_COMPLETE = 0;

	Toolbar toolbar;
	FrameLayout root;
	View contentHamburger;

	private GuillotineAnimation.GuillotineBuilder builder;

	//menu三个选项（资产分析，提现，流水账）
	LinearLayout llGeneralJournal;//流水账
	LinearLayout llEnchashment;//提现
	LinearLayout llAssetsAnalysis;//财产分析

	TextView tvBalance;//显示用户当前余额
	//购买流程必须
	MyEditText etBuyNumber;
	ButtonRectangle btnBuy;

	//提现流程必须
	MyEditText etGetNumber;
	ButtonRectangle btnGet;

	//业务组件
	OrderService orderService = new OrderServiceImpl();
	UserService userService = new UserServiceImpl();
	//购买价格
	float price;
	//订单id
	String orderId;
	//当前账号的user对象
	private User user;

	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case PersonalDetailInfoSettingActivity.SET_USER_DETAIL_INFO:
					user = (User) msg.obj;
					tvBalance.setText(Html.fromHtml("用户当前余额:" + "<font color='blue'>" + user
							.getPoint() + "</font>"));
					UserServiceImpl.setCurrentUserStringInfo(TradeActivity.this, "point", user
							.getPoint() + "");
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
		setContentView(R.layout.trade_main_ui);
		root = (FrameLayout) findViewById(R.id.root);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		contentHamburger = findViewById(R.id.content_hamburger);
		if (toolbar != null)
		{
			setSupportActionBar(toolbar);
			getSupportActionBar().setTitle(null);
		}
		View guillotineMenu = LayoutInflater.from(this).inflate(R.layout.guillotine, null);
		root.addView(guillotineMenu);
		builder = new GuillotineAnimation.GuillotineBuilder
				(guillotineMenu,
						guillotineMenu.findViewById(R.id
								.guillotine_hamburger), contentHamburger);
		builder.setActionBarViewForAnimation(toolbar).setClosedOnStart(true)
				.build();
		initView();
		userService.getUserInfo(UserServiceImpl.getCurrentUserId(this), handler, true);
		DialogUtils.showDefaultDialog(this);

	}

	private void initView()
	{
		llGeneralJournal = (LinearLayout) findViewById(R.id.ll_general_journal);
		llEnchashment = (LinearLayout) findViewById(R.id.ll_enchashment);
		llAssetsAnalysis = (LinearLayout) findViewById(R.id.ll_assets_analysis);
		llGeneralJournal.setOnClickListener(this);
		llEnchashment.setOnClickListener(this);
		llAssetsAnalysis.setOnClickListener(this);
		etBuyNumber = (MyEditText) findViewById(R.id.et_buy_number);
		btnBuy = (ButtonRectangle) findViewById(R.id.btn_buy);
		etGetNumber = (MyEditText) findViewById(R.id.et_get_number);
		btnGet = (ButtonRectangle) findViewById(R.id.btn_get);
		btnGet.setOnClickListener(this);
		btnBuy.setOnClickListener(this);
		tvBalance = (TextView) findViewById(R.id.tv_balance);

	}

	@Override
	public void onClick(View v)
	{
		Intent intent = null;
		switch (v.getId())
		{
			case R.id.ll_general_journal:
				intent = new Intent(this, GeneralJournalActivity.class);
				startActivity(intent);
				break;
			case R.id.ll_enchashment:
				Toast.makeText(this, "还未开放此功能，敬请期待！", Toast.LENGTH_SHORT).show();
				break;
			case R.id.ll_assets_analysis:
				intent = new Intent(this, AssetsAnalysisActivity.class);
				startActivity(intent);
				break;
			case R.id.btn_buy://购买按钮
				String value = etBuyNumber.getText().toString();
				L.e("value：" + value);
				if (value != null && !"".equals(value))
				{
					float floatValue = Float.parseFloat(value);
					L.e("float value:" + floatValue);
					if (validateInput(floatValue))
					{
						L.e("buy");
						price = floatValue;
						new BmobPay(this).pay(floatValue, "用户积分", new payListener());
					}
				}
				break;
			case R.id.btn_get://提现按钮
				String getValue = etGetNumber.getText().toString();
				if (getValue != null && !"".equals(getValue))
				{
					float floatValue = Float.parseFloat(getValue);
					L.e("float value:"+floatValue);
					L.e("value%1000="+floatValue%1000);
					if (floatValue % 1000 != 0)
					{
						Toast.makeText(this, "提现积分必须为1000的正整数倍", Toast.LENGTH_SHORT).show();
					} else
					{
						orderService.withdrawCash(UserServiceImpl.getCurrentUserId(this), (int)
								floatValue);
						Toast.makeText(this, "提现申请提交，请耐心等待审核", Toast.LENGTH_SHORT).show();
					}
				}
				break;
			default:
				break;
		}
	}

	/**
	 * 检验输入
	 */
	private boolean validateInput(float value)
	{
		if (value < 0.01f)
		{
			Toast.makeText(this, "金额数必须大于0.01", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	public class payListener implements PayListener
	{

		@Override
		public void orderId(String orderId)
		{
			L.e("orderId");
			TradeActivity.this.orderId = orderId;
		}

		@Override
		public void succeed()
		{
			L.e("success");
			new BmobPay(TradeActivity.this).query(orderId, new OrderQueryListener()
			{
				@Override
				public void succeed(String status)
				{
					Toast.makeText(TradeActivity.this, "支付成功，获得积分 " + RATE_POINT_RMB * price, Toast
							.LENGTH_SHORT).show();
					//更新本地存储的积分
					String stringPoint = UserServiceImpl.getCurrentUserStringInfo
							(TradeActivity.this, "point");
					if (stringPoint != null && !"".equals(stringPoint))
					{
						int point = Integer.parseInt(stringPoint);
						int resultPoint = (int) (point + RATE_POINT_RMB * price);
						UserServiceImpl.setCurrentUserStringInfo(TradeActivity.this, "point",
								resultPoint + "");
						//更新textview
						tvBalance.setText(Html.fromHtml("用户当前余额:" + "<font color='blue'>" +
								resultPoint + "</font>"));
					}
					L.e("result point:" + UserServiceImpl.getCurrentUserStringInfo(TradeActivity
							.this, "point"));
					//将订单号提交到服务器
					orderService.saveOrder(orderId, UserServiceImpl.getCurrentUserId(TradeActivity
							.this), price);
				}

				@Override
				public void fail(int i, String s)
				{
					Toast.makeText(TradeActivity.this, "异常," + i + ":" + s, Toast.LENGTH_SHORT)
							.show();
				}
			});
		}

		@Override
		public void fail(int i, String s)
		{
			Toast.makeText(TradeActivity.this, "支付失败:" + s, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void unknow()
		{
			Toast.makeText(TradeActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
		}
	}


}
