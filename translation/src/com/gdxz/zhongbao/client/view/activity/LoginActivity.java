package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gdxz.zhongbao.client.Service.UserService;
import com.gdxz.zhongbao.client.Service.impl.UserServiceImpl;
import com.gdxz.zhongbao.client.common.MyApplication;
import com.gdxz.zhongbao.client.domain.User;
import com.gdxz.zhongbao.client.utils.DialogUtils;
import com.gdxz.zhongbao.client.utils.L;
import com.gdxz.zhongbao.client.utils.NetUtils;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;

public class LoginActivity extends Activity
{
	//代表有验证码的handler常量
	public static final int HAS_VERIFYCODE = 0;

	//代表登录失败
	public static final int LOGIN_FAILER = 1;

	//代表登录成功
	public static final int LOGIN_SUCCESS = 2;
	public static final int LOGIN_JMESSAGE_SUCCESS = 0x11;//登录到jmessage成功

	public static final int VERIFYCODE_LENGTH = 4;

	TextView errorTextView;

	EditText editTextUsername;

	EditText editTextPassword;

	ImageView verifyCodeImage;

	EditText editTextVerifyCode;

	TextView textViewRegist;

	LinearLayout llVerifyCode;

	public boolean hasVerifyCode;

	ProgressDialog progressDialog;

	UserService userService = new UserServiceImpl();
	private Handler handler = new Handler()
	{
		public void handleMessage(Message message)
		{
			switch (message.what)
			{
				case HAS_VERIFYCODE:
					DialogUtils.closeProgressDialog();
					hasVerifyCode = true;
					llVerifyCode.setVisibility(View.VISIBLE);
					userService.setVerifyCodeImage(verifyCodeImage);
					break;
				case LOGIN_FAILER:
					errorTextView.setText((String) message.obj);
					DialogUtils.closeProgressDialog();
					break;
				case LOGIN_SUCCESS:
					String username = UserServiceImpl.getCurrentUserStringInfo(LoginActivity.this,
							"username");
					String password = UserServiceImpl.getCurrentUserStringInfo(LoginActivity.this,
							"password");
					UserInfo info = JMessageClient.getMyInfo();
					L.e("应用服务器登录成功");
					if (info != null)
					{
						String jUsername = info.getUserName();
						L.e("jUsername:" + jUsername + ",username:" + username);
						if (jUsername != null && jUsername.equals(username))
						{
							handler.sendEmptyMessage(LOGIN_JMESSAGE_SUCCESS);
						} else
						{
							L.e("jmessage login 中");
							UserServiceImpl.loginInJmessage(username, password, handler);
						}
					} else
					{
						L.e("jmessage login 中");
						UserServiceImpl.loginInJmessage(username, password, handler);

					}
					break;
				case LOGIN_JMESSAGE_SUCCESS://登录到jmessage成功
					DialogUtils.closeProgressDialog();
					Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					UserServiceImpl.loadGlobalValue(LoginActivity.this);
					LoginActivity.this.startActivity(intent);
					LoginActivity.this.finish();
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
		MyApplication.getInstance().clearActivity();
		setContentView(R.layout.login_ui);
		String category = getIntent().getStringExtra("category");
		if (category == null || !category.equals("noByCache"))
		{
			UserServiceImpl.loadGlobalValue(this);
			if (MyApplication.isAutoLogin)
			{
				L.e("login by cache");
				loginByCache();
			}

		}
		init();
	}

	public void init()
	{
		errorTextView = (TextView) findViewById(R.id.tv_errorInfo);
		editTextUsername = (EditText) findViewById(R.id.et_username);
		editTextPassword = (EditText) findViewById(R.id.et_password);
		verifyCodeImage = (ImageView) findViewById(R.id.iv_verifyCode);
		editTextVerifyCode = (EditText) findViewById(R.id.et_verifyCode);
		textViewRegist = (TextView) findViewById(R.id.tv_regist);
		llVerifyCode = (LinearLayout) findViewById(R.id.ll_veryfyCode);
		//异步设置验证码
//		userService.setVerifyCodeImage(verifyCodeImage);
		//设置验证码图片点击事件，点击换验证码
		verifyCodeImage.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				userService.setVerifyCodeImage(verifyCodeImage);
			}
		});
		//注册
		textViewRegist.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(LoginActivity.this, RegistActivity.class);
				LoginActivity.this.finish();
				startActivity(intent);
			}
		});

	}

	public void login(View view)
	{
		errorTextView.setText("");
		User user = new User();
		user.setUsername(editTextUsername.getText().toString().trim());
		user.setPassword(editTextPassword.getText().toString().trim());
		if (hasVerifyCode)
		{
			user.setVerifyCode(editTextVerifyCode.getText().toString().trim());
		}
		userService.login(user, this, hasVerifyCode, handler);
		DialogUtils.showProgressDialog("提醒", "正在登录", this);
	}


	public void loginByCache()
	{
		String currentId = UserServiceImpl.getCurrentUserId(this);
		String username = UserServiceImpl.getCurrentUserStringInfo(this, "username");
		String password = UserServiceImpl.getCurrentUserStringInfo(this, "password");
		if (!"".equals(currentId) && !"".equals(username))
		{
			L.e("id:"+currentId +" ," + "username:" + username + ",password:" + password);
			if (!NetUtils.isConnected(this))
			{
				Intent intent = new Intent(this, HomeActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				UserServiceImpl.loadGlobalValue(LoginActivity.this);
				this.finish();
				return;
			}
			DialogUtils.showProgressDialog("提醒", "正在登录", this);
			User user = new User();
			user.setUsername(UserServiceImpl.getCurrentUserStringInfo(this, "username"));
			user.setPassword(password);
			userService.login(user, this, false, handler);
		}
	}

}
