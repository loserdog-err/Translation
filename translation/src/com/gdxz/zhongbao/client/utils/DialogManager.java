package com.gdxz.zhongbao.client.utils;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gdxz.zhongbao.client.view.activity.R;

/**
 * Created by chenantao on 2015/7/9.
 */
public class DialogManager
{

  private Context mContext;
  private Dialog mDialog;
  private LayoutInflater mInflater;
  private ImageView ivRecoder;
  private ImageView ivVoiceLevel;
  private TextView tvText;
  private LinearLayout ll_container;

  public DialogManager(Context context)
  {
	mContext = context;
	mDialog = new Dialog(mContext, R.style.Theme_AudioDialog);
	mInflater = LayoutInflater.from(mContext);
	ll_container = (LinearLayout) mInflater.inflate(R.layout.recoder_dialog, null);
	mDialog.setContentView(ll_container);
	ivRecoder = (ImageView) ll_container.findViewById(R.id.iv_recoder);
	ivVoiceLevel = (ImageView) ll_container.findViewById(R.id.iv_voice_level);
	tvText = (TextView) ll_container.findViewById(R.id.tv_text);
  }

  public void showRecordingDialog()
  {
	ivRecoder.setVisibility(View.VISIBLE);
	ivVoiceLevel.setVisibility(View.VISIBLE);
	tvText.setVisibility(View.VISIBLE);
	ivRecoder.setBackgroundResource(R.drawable.recorder);
	tvText.setText("手指上滑,取消录音");
	mDialog.show();
//	Log.e("TAG","show");
  }

  public void showWantToCancelDialog()
  {
	ivRecoder.setVisibility(View.VISIBLE);
	ivVoiceLevel.setVisibility(View.GONE);
	tvText.setVisibility(View.VISIBLE);
	ivRecoder.setBackgroundResource(R.drawable.cancel);
	tvText.setText("松开手指,取消发送");
  }

  public void showCancelDialog()
  {
	ivRecoder.setVisibility(View.VISIBLE);
	ivVoiceLevel.setVisibility(View.GONE);
	tvText.setVisibility(View.VISIBLE);
	ivRecoder.setBackgroundResource(R.drawable.voice_to_short);
	tvText.setText("录音已取消");
  }

  public void showTooShortDialog()
  {
	ivRecoder.setVisibility(View.VISIBLE);
	ivVoiceLevel.setVisibility(View.GONE);
	tvText.setVisibility(View.VISIBLE);
	ivRecoder.setBackgroundResource(R.drawable.voice_to_short);
	tvText.setText("录制时间过短");
  }

  public void dismissDialog()
  {
	if (mDialog != null && mDialog.isShowing())
	{
	  Log.e("TAG","dismiss");
	  mDialog.dismiss();
	}
  }

  public void updateVoiceLevel(int level)
  {
	if (mDialog != null && mDialog.isShowing())
	{
	  int resId = mContext.getResources().getIdentifier("v" + level, "drawable", mContext.getPackageName());
	  ivVoiceLevel.setBackgroundResource(resId);
	}
  }


}
