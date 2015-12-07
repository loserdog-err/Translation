package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.gdxz.zhongbao.client.common.MyApplication;
import com.gdxz.zhongbao.client.utils.HandleResponseCode;
import com.gdxz.zhongbao.client.utils.L;
import com.gdxz.zhongbao.client.utils.NativeImageLoader;
import com.gdxz.zhongbao.client.view.adapter.ChatAdapter;
import com.gdxz.zhongbao.client.view.customView.RecorderButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupMembersCallback;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by chenantao on 2015/7/19.
 * 群聊的主界面
 */
public class GroupChatActivity extends Activity implements View.OnClickListener
{
	private ListView mLvChat;
	private List<cn.jpush.im.android.api.model.Message> mDatas;
	private ChatAdapter mAdapter;

	private Conversation mConversation;//当前群会话
	private long mGroupId;//当前群的id

	//当前群聊成员的list
	List<String> mMembers;

	List<cn.jpush.im.android.api.model.Message> msgs;//接收到的msg集合

	private ImageButton mIbToggle;//切换文字和语音的按钮
	private RecorderButton mBtnRecordVoice;//录音按钮
	private Button mBtnSend;
	private EditText mEtSendContent;
	public boolean isTextMode = true;//当前状态是否为文本模式
	public Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.group_chat_ui);
		MyApplication.getInstance().addActivity(this);
		JMessageClient.registerEventReceiver(this);
		mGroupId = getIntent().getLongExtra("groupId", -1);
		if (mGroupId != -1)
		{
			mConversation = JMessageClient.getGroupConversation(mGroupId);
			JMessageClient.enterGroupConversation(mGroupId);
		}
		if (mConversation == null)
		{
			mConversation = Conversation.createConversation(ConversationType.group, mGroupId);
		}
		mConversation.resetUnreadCount();//重置未读的消息数
		mDatas = mConversation.getAllMessage();
		initView();
		initData();
	}

	private void initView()
	{
		mLvChat = (ListView) findViewById(R.id.lv_content);
		mBtnSend = (Button) findViewById(R.id.btn_send);
		mEtSendContent = (EditText) findViewById(R.id.et_send_content);
		mIbToggle = (ImageButton) findViewById(R.id.ib_toggle);
		mBtnRecordVoice = (RecorderButton) findViewById(R.id.voice_btn);
	}


	private void initData()
	{
		if (mDatas == null)
		{
			mDatas = new ArrayList<>();
		}
		//切换输入模式的按钮
		mIbToggle.setOnClickListener(this);
		mBtnSend.setOnClickListener(this);
		//录音按钮的录音事件
		mBtnRecordVoice.setAudioRecordFinishListener(new RecorderButton
				.OnAudioRecordFinishListener()
		{
			@Override
			public void finish(float seconds, String path)
			{
				onRecordComplete(seconds, path);
			}
		});
		mAdapter = new ChatAdapter(this, mDatas, mGroupId);
		mLvChat.setAdapter(mAdapter);
		setToBottom();
		initUsersAvatar();
	}

	/**
	 * 录音完成后调用的事件
	 */
	private void onRecordComplete(float seconds, String path)
	{
		try
		{
			int duration = (int) seconds;
			if (duration < 1)
			{
				duration = 1;
			}
			VoiceContent voiceContent = new VoiceContent(new File((path)), duration);
			cn.jpush.im.android.api.model.Message msg = mConversation.createSendMessage
					(voiceContent);
			//本地先更新到listview，然后再上传
			mAdapter.addMsgToList(msg);
			setToBottom();
			msg.setOnSendCompleteCallback(new BasicCallback()
			{
				@Override
				public void gotResult(int i, String s)
				{
					if (i == 0)
					{
						L.e("上传语音完毕");
					} else
					{
						L.e("上传语音失败：" + i);
						HandleResponseCode.onHandle(GroupChatActivity.this, i);
					}
					mAdapter.refresh();
				}
			});
			JMessageClient.sendMessage(msg);

		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 初始化群聊用户的头像
	 * 1: 如果是用户自身，直接从本地加载
	 * 2: 如果是其他用户，从内存缓存中加载
	 * 3：如果缓存中没有，从服务器加载，然后放入内存缓存中
	 */
	private void initUsersAvatar()
	{
		JMessageClient.getGroupMembers(mGroupId, new GetGroupMembersCallback()
		{
			@Override
			public void gotResult(int i, String s, List<String> list)
			{
				if (i == 0)
				{
					L.e("获取群成员列表成功");
					NativeImageLoader.getInstance().setAvatarCache(list, (int) (50 *
							new DisplayMetrics().density), new NativeImageLoader
							.cacheAvatarCallBack()
					{
						@Override
						public void onCacheAvatarCallBack(int status)
						{
							runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									mAdapter.notifyDataSetChanged();
								}
							});
						}
					});
				}
			}
		});
	}

	/**
	 * 释放资源
	 */
	@Override
	protected void onDestroy()
	{
		// TODO Auto-generated method stub
		JMessageClient.unRegisterEventReceiver(this);
		super.onDestroy();
	}


	/**
	 * 接收消息类事件
	 *
	 * @param event 消息事件
	 */
	public void onEvent(MessageEvent event)
	{
		L.e("onEvent MessageEvent execute");
//		cn.jpush.im.android.api.model.Message msg = event.getMessage();
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mAdapter.refresh();
				moveToNext();
			}
		});
	}

	/**
	 * 移动到底部
	 */
	public void setToBottom()
	{
		mLvChat.post(new Runnable()
		{
			@Override
			public void run()
			{
				mLvChat.setSelection(mLvChat.getBottom());
			}
		});
	}

	/**
	 * 移动到下一行
	 */

	public void moveToNext()
	{
		mLvChat.post(new Runnable()
		{
			@Override
			public void run()
			{
				mLvChat.smoothScrollToPosition(mLvChat.getLastVisiblePosition() + 1);
			}
		});
	}


	/**
	 * 切换输入模式(语音，文字)
	 * 默认文字
	 */
	public void toggleInputMode()
	{
		isTextMode = !isTextMode;
		if (isTextMode)
		{
			mBtnRecordVoice.setVisibility(View.GONE);
			mEtSendContent.setVisibility(View.VISIBLE);
			mBtnSend.setVisibility(View.VISIBLE);
			mIbToggle.setBackgroundResource(R.drawable.voice);
		} else//隐藏文本输入框，显示录音按钮
		{
			dismissSoftInput();
			mBtnRecordVoice.setVisibility(View.VISIBLE);
			mEtSendContent.setVisibility(View.GONE);
			mBtnSend.setVisibility(View.INVISIBLE);
			mIbToggle.setBackgroundResource(R.drawable.keyboard);
		}
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.voice_btn:
				break;
			case R.id.ib_toggle:
				toggleInputMode();
				break;
			case R.id.btn_send:
				if (!TextUtils.isEmpty(mEtSendContent.getText()))
				{
//					Toast.makeText(GroupChatActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
					String msgContent = mEtSendContent.getText().toString();
					TextContent content = new TextContent(msgContent);
					final cn.jpush.im.android.api.model.Message msg = mConversation
							.createSendMessage(content);
					msg.setOnSendCompleteCallback(new BasicCallback()
					{

						@Override
						public void gotResult(final int status, String desc)
						{
							Log.i("ChatController", "send callback " + status + " desc " +
									desc);
							if (status == 0)
							{
								L.e("发送成功");
							} else
							{
								L.e("发送失败:" + status);
							}
							// 发送成功或失败都要刷新一次
							mAdapter.refresh();
							//置空编辑框
							mEtSendContent.setText("");
							//移动到底部
							setToBottom();
						}
					});
					mAdapter.addMsgToList(msg);
					JMessageClient.sendMessage(msg);

				}
				break;
		}
	}

	/**
	 * 隐藏软键盘
	 */
	public void dismissSoftInput()
	{
		InputMethodManager imm = ((InputMethodManager) this
				.getSystemService(Activity.INPUT_METHOD_SERVICE));
		if (this.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams
				.SOFT_INPUT_STATE_HIDDEN)
		{
			if (this.getCurrentFocus() != null)
				imm.hideSoftInputFromWindow(this
								.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}


}