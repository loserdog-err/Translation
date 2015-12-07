package com.gdxz.zhongbao.client.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.gdxz.zhongbao.client.utils.DateUtils;
import com.gdxz.zhongbao.client.utils.L;
import com.gdxz.zhongbao.client.utils.MediaManager;
import com.gdxz.zhongbao.client.utils.NativeImageLoader;
import com.gdxz.zhongbao.client.view.activity.R;

import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;

/**
 * Created by chenantao on 2015/7/4.
 */
public class ChatAdapter extends BaseAdapter
{
	List<Message> mDatas;
	LayoutInflater mInflater;
	Context context;


	long groupId;

	Conversation mConversation;

	public ChatAdapter(Context context, List<Message> mDatas, long groupId)
	{
		super();
		this.mDatas = mDatas;
		this.context = context;
		this.groupId = groupId;
	}

	@Override
	public int getCount()
	{
		return mDatas.size();
	}

	@Override
	public int getViewTypeCount()
	{
		return 4;
	}

	@Override
	public int getItemViewType(int position)
	{
		Message msg = mDatas.get(position);
		MessageDirect direct = msg.getDirect();
		if (msg.getContentType() == ContentType.text)//类型是文本
		{
			if (direct.equals(MessageDirect.send))
			{
				return 0;//发送出去
			} else
			{
				return 1;//接收
			}
		} else
		{
			if (direct.equals(MessageDirect.receive))
			{
				return 2;
			} else
			{
				return 3;
			}
		}

	}

	@Override
	public Object getItem(int position)
	{
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		L.e(mDatas.size() + "");
		final ViewHolder holder;
		Message msg = mDatas.get(position);
//        Log.e("TAG",position+":"+convertView);
		if (convertView == null)
		{
			holder = new ViewHolder();
			mInflater = LayoutInflater.from(context);
			if (msg.getContentType() == ContentType.text)//文本类型
			{
				if (msg.getDirect().equals(MessageDirect.receive))
				{
					convertView = mInflater.inflate(R.layout.chat_item_from_text, null);
				} else
				{
					convertView = mInflater.inflate(R.layout.chat_item_to_text, null);
				}
				holder.tvMsg = (TextView) convertView.findViewById(R.id.tv_text);

			} else if (msg.getContentType() == ContentType.voice)
			{
				if (msg.getDirect().equals(MessageDirect.receive))
				{
					convertView = mInflater.inflate(R.layout.chat_item_from_voice, null);

				} else
				{
					convertView = mInflater.inflate(R.layout.chat_item_to_voice, null);
				}
				holder.flVoiceWrap = (FrameLayout) convertView.findViewById(R.id.fl_voice_wrap);
				holder.tvDuration = (TextView) convertView.findViewById(R.id.tv_recorder_time);
				holder.ivRecorderAnim = (ImageView) convertView.findViewById(R.id
						.iv_recorder_anim);
			} else
			{
				convertView = mInflater.inflate(R.layout.chat_item_from_exit_group, null);
			}
			holder.tvPostTime = (TextView) convertView.findViewById(R.id.tv_postTime);
			holder.tvAuthor = (TextView) convertView.findViewById(R.id.tv_author);
			holder.ivAvatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
			convertView.setTag(holder);
		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		switch (msg.getContentType())
		{
			case text:
				String content = ((TextContent) msg.getContent()).getText();
				holder.tvMsg.setText(content);
				break;
			case voice:
				handleVoiceMsg(msg, holder);
				break;
		}
		//设置数据
		long mill = msg.getCreateTime();
		holder.tvPostTime.setText(DateUtils.long2date(mill));
		holder.tvAuthor.setText(msg.getFromName());
		//显示头像
		if (holder.ivAvatar != null)
		{
			Bitmap bitmap = NativeImageLoader.getInstance().getBitmapFromMemCache(msg.getFromID());
			if (bitmap != null)
				holder.ivAvatar.setImageBitmap(bitmap);
		}
		if (holder.flVoiceWrap != null && holder.tvDuration != null)
		{
		}
		return convertView;
	}

	/**
	 * 处理语音消息
	 *
	 * @param msg
	 * @param holder
	 */
	private void handleVoiceMsg(Message msg, final ViewHolder holder)
	{
		int mMaxItemWith;
		int mMinItemWith;
//
		final VoiceContent voiceContent = (VoiceContent) msg.getContent();
		int length = voiceContent.getDuration();
		//动态设置语音框的长度
		WindowManager wManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wManager.getDefaultDisplay().getMetrics(outMetrics);
		mMaxItemWith = (int) (outMetrics.widthPixels * 0.7f);
		mMinItemWith = (int) (outMetrics.widthPixels * 0.15f);
		ViewGroup.LayoutParams lp = holder.flVoiceWrap.getLayoutParams();
		lp.width = (int) (mMinItemWith + mMaxItemWith / 120f * length);
		holder.flVoiceWrap.setLayoutParams(lp);
		holder.tvDuration.setText(length + "\"");
		//单击语音图片播放声音
		holder.flVoiceWrap.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//设置语音文件的背景图片并开始播放背景动画
				holder.ivRecorderAnim.setBackgroundResource(R.drawable.play_recorder_anim);
				AnimationDrawable drawable = (AnimationDrawable) holder.ivRecorderAnim
						.getBackground();
				drawable.start();
				MediaManager.playSound(voiceContent.getLocalPath(),
						new MediaPlayer.OnCompletionListener()
						{
							@Override
							public void onCompletion(MediaPlayer mp)
							{
								holder.ivRecorderAnim.setBackgroundResource(R.drawable
										.adj);
							}
						});
			}
		});
	}

	/**
	 * 添加一条消息到list
	 *
	 * @param msg
	 */
	public void addMsgToList(Message msg)
	{
		mDatas.add(msg);
		notifyDataSetChanged();
	}

	public class ViewHolder
	{
		TextView tvPostTime;
		TextView tvMsg;
		TextView tvAuthor;
		ImageView ivAvatar;
		TextView tvDuration;
		FrameLayout flVoiceWrap;
		ImageView ivRecorderAnim;
	}

	public void refresh()
	{
		mDatas.clear();
		mConversation = JMessageClient.getGroupConversation(groupId);
		if (mConversation != null)
		{
			mDatas = mConversation.getAllMessage();
			notifyDataSetChanged();
		}
	}


	public void setData(List<Message> datas)
	{
		this.mDatas = datas;
	}

	@Override
	public boolean areAllItemsEnabled()
	{
		return false;
	}

	@Override
	public boolean isEnabled(int position)
	{
		return false;
	}


}
