package com.gdxz.zhongbao.client.view.customView;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.gdxz.zhongbao.client.utils.AudioManager;
import com.gdxz.zhongbao.client.utils.DialogManager;
import com.gdxz.zhongbao.client.view.activity.R;

/**
 * Created by chenantao on 2015/7/9.
 */
public class RecorderButton extends Button implements com.gdxz.zhongbao.client.utils.AudioManager
		.OnAudioPrepare
{

	public final int RECORDING = 1;
	public final int WANT_TO_CANCEL = 2;
	public final int NORMAL = 3;

	public int currentState;//标志当前按钮处于何种状态

	private boolean isRecording = false;
	private boolean isCancel;
	DialogManager mDialogManager;
	AudioManager mAudioManager;

	public float mRecordTime = 0;

	public final int MSG_MEDIA_RECODER_PREPARED = 0x1;//代表media recoder准备完毕
	public final int MSG_UPDATE_VOICE_LEVEL = 0x2;
	public final int MSG_DIALOG_DISMISS = 0x3;

	private Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case MSG_MEDIA_RECODER_PREPARED:
					setCurrentState(RECORDING);
					isRecording = true;
					//开启一条线程测量音量并更新ui
					new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							while (isRecording)
							{
								try
								{
									Thread.sleep(100);
									mRecordTime += 0.1f;
								} catch (Exception e)
								{
									e.printStackTrace();
								}
								mHandler.sendEmptyMessage(MSG_UPDATE_VOICE_LEVEL);
							}
						}
					}).start();
				case MSG_UPDATE_VOICE_LEVEL:
					int level = mAudioManager.getVoiceLevel();
					mDialogManager.updateVoiceLevel(level);
					break;
				case MSG_DIALOG_DISMISS:
					mDialogManager.dismissDialog();
					break;
			}
			super.handleMessage(msg);
		}
	};


	//回调接口，代表录音已经完成
	public interface OnAudioRecordFinishListener
	{
		void finish(float seconds, String path);
	}

	;
	public OnAudioRecordFinishListener mAudioRecordFinishListener;

	public void setAudioRecordFinishListener(OnAudioRecordFinishListener listener)
	{
		mAudioRecordFinishListener = listener;
	}


	public RecorderButton(Context context)
	{
		this(context, null);
	}

	public RecorderButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mDialogManager = new DialogManager(context);
		mAudioManager = AudioManager.getInstance(Environment.getExternalStorageDirectory() +
				"/chenantao_recoder_audio");
		this.setOnLongClickListener(new OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View v)
			{
//				Log.e("TAG", "long click");
				isRecording = true;
				mAudioManager.prepareAudio();
				return false;
			}
		});
		mAudioManager.setAudioState(this);
	}

	/**
	 * 回调方法，代表MediaRecoder准备完毕
	 */
	@Override
	public void hadPrepare()
	{
		Log.e("TAG", "havePrepare");
		Message msg = Message.obtain();
		msg.what = MSG_MEDIA_RECODER_PREPARED;
		mHandler.sendMessage(msg);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();
		switch (action)
		{
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:
				//如果超出了按钮的范围，设置状态为want to cancel
				if (isRecording)
				{
					if ((x > getWidth() || x < 0) || (y > getHeight() || y < 0))
					{
						setCurrentState(WANT_TO_CANCEL);
					} else
					{
						setCurrentState(RECORDING);
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				if (currentState == RECORDING)
				{
					if (isRecording && mRecordTime > 0.5f)//代表正常录制结束
					{
						setCurrentState(NORMAL);
						if (mAudioRecordFinishListener != null)
						{
							mAudioRecordFinishListener.finish(mRecordTime, mAudioManager
									.getFilePath());
						}
						mAudioManager.release();
					} else
					{
						mDialogManager.showTooShortDialog();
						mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DISMISS, 1000);
						mAudioManager.cancel();
					}
				} else if (currentState == WANT_TO_CANCEL)
				{
					setCurrentState(NORMAL);
					mAudioManager.cancel();
				}
				reset();
				break;
		}
		return super.onTouchEvent(event);

	}

	//
	private void reset()
	{
		isRecording = false;
		setCurrentState(NORMAL);
		mDialogManager.dismissDialog();
	}

	public void setCurrentState(int state)
	{
		switch (state)
		{
			case NORMAL:
				setBackgroundResource(R.drawable.btn_recorder_normal);
				currentState = NORMAL;
				this.setText("按住说话");
				break;
			case WANT_TO_CANCEL:
				currentState = WANT_TO_CANCEL;
				setBackgroundResource(R.drawable.btn_recorder_normal);
				setText("松开手指，取消发送");
				mDialogManager.showWantToCancelDialog();
				break;
			case RECORDING:
				currentState = RECORDING;
				setBackgroundResource(R.drawable.btn_recorder_recoding);
				setText("松开完成录音");
				mDialogManager.showRecordingDialog();
				break;
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event)
	{
		getParent().requestDisallowInterceptTouchEvent(true);
		return super.dispatchTouchEvent(event);
	}
}

