package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gdxz.zhongbao.client.Service.UserService;
import com.gdxz.zhongbao.client.Service.impl.UserServiceImpl;
import com.gdxz.zhongbao.client.common.MyApplication;
import com.gdxz.zhongbao.client.domain.User;

import java.util.List;

public class RegistActivity extends Activity
{
	public static final int MSG_USERNAME_EXISTS = 0;
	public static final int MSG_REGISTER_SUCCESS = 1;
	public static final int MSG_REGISTER_INFO_ERROR = 2;
	public static final int MSG_USERNAME_NOT_EXISTS = 3;
	public static final int MSG_REGISTER_FROM_JMESSAGE_ERROR = 4;//注册Jmessage失败
	public static final int MOBILEPHONE_LENGTH = 11;

	public boolean canRegist = false;//标识当前信息是否允许注册

	ImageView imageViewBack;

	EditText editTextUsername;

	EditText editTextPassword;

	EditText editTextRePassword;

	EditText editTextMobielPhone;

	EditText editTextEmail;

	TextView textViewError;

	private UserService userService = new UserServiceImpl();

	private Handler handler = new Handler()
	{
		@Override
		public boolean sendMessageAtTime(Message msg, long uptimeMillis)
		{
			switch (msg.what)
			{
				case MSG_USERNAME_EXISTS:
					textViewError.setText("用户名已存在");
					canRegist = false;
					break;
				case MSG_REGISTER_INFO_ERROR:
					List<String> errors = (List<String>) msg.obj;
					Toast.makeText(RegistActivity.this, "注册信息有误", Toast.LENGTH_SHORT).show();
					if (errors != null && errors.size() > 0)
					{
						textViewError.setText(errors.get(0));
					}
					break;
				case MSG_REGISTER_SUCCESS:
					Toast.makeText(RegistActivity.this, "注册成功", Toast.LENGTH_LONG).show();
					User user = (User) msg.obj;
					//将用户信息写进sp
					UserServiceImpl.writeMsgToSp(RegistActivity.this, user);
					Intent intent = new Intent(RegistActivity.this, LoginActivity.class);
					intent.addCategory(Intent.CATEGORY_HOME);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					RegistActivity.this.startActivity(intent);
					RegistActivity.this.finish();
					break;
				case MSG_USERNAME_NOT_EXISTS:
					textViewError.setText("");
					Toast.makeText(RegistActivity.this, "用户名可用", Toast.LENGTH_SHORT).show();
					break;
				case MSG_REGISTER_FROM_JMESSAGE_ERROR:
					Toast.makeText(RegistActivity.this, "注册失败,换个账号注册吧", Toast.LENGTH_SHORT).show();
					break;
			}
			return super.sendMessageAtTime(msg, uptimeMillis);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.regist_ui);
		MyApplication.getInstance().addActivity(this);
		init();
	}

	public void init()
	{
		imageViewBack = (ImageView) findViewById(R.id.iv_back);
		editTextEmail = (EditText) findViewById(R.id.et_email);
		editTextMobielPhone = (EditText) findViewById(R.id.et_mobiephone);
		editTextPassword = (EditText) findViewById(R.id.et_password);
		editTextRePassword = (EditText) findViewById(R.id.et_rePassword);
		editTextUsername = (EditText) findViewById(R.id.et_username);
		textViewError = (TextView) findViewById(R.id.tv_errorInfo);
		//用户名失去焦点事件，异步请求服务器查看用户名是否存在
		editTextUsername.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (!hasFocus)
				{
					userService.checkUsernameExistsAsync(editTextUsername.getText().toString(),
							handler);
				}
			}
		});
		//返回图片单击事件
		imageViewBack.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				RegistActivity.this.finish();
			}
		});
	}

	public void register(View view)
	{
		textViewError.setText("");
		User user = new User();
		user.setUsername(editTextUsername.getText().toString().trim());
		user.setPassword(editTextPassword.getText().toString().trim());
		user.setRePassword(editTextRePassword.getText().toString().trim());
		user.setMobilePhone(editTextMobielPhone.getText().toString().trim());
		user.setEmail(editTextEmail.getText().toString().trim());
		userService.regist(user, handler);
	}
}
