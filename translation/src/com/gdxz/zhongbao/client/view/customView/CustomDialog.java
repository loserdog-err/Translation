package com.gdxz.zhongbao.client.view.customView;

import com.gdxz.zhongbao.client.view.activity.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.SpannedString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 
 * 创建自定义对话框
 * 
 */
public class CustomDialog extends Dialog
{
	public CustomDialog(Context context, int theme)
	{
		super(context, theme);
	}

	public CustomDialog(Context context)
	{
		super(context);
	}
	
	

	/**
	 * Helper class for creating a custom dialog
	 */
	public static class Builder
	{

		private TextView mMsgTextView;
		private Context context;
		private CharSequence message;
		private Drawable icon;
		private String positiveButtonText;
		private String negativeButtonText;
		private View contentView;

		private DialogInterface.OnClickListener positiveButtonClickListener,
				negativeButtonClickListener;

		public Builder(Context context)
		{
			this.context = context;
		}

		/**
		 * 设置图片
		 * 
		 * @param title
		 * @return
		 */
		public Builder setImage(Drawable icon)
		{
			this.icon = icon;
			return this;
		}

		/**
		 * 设置自定义的view显示在content上
		 * 
		 * @param v
		 * @return
		 */
		public Builder setContentView(View v)
		{
			this.contentView = v;
			return this;
		}

		/**
		 * 设置提示信息
		 * 
		 * @param message
		 * @return
		 */
		public Builder setMessage(CharSequence message)
		{
			this.message = message;
			return this;
		}


		/**
		 * 设置确定按钮的监听以及文本
		 * 
		 * @param positiveButtonText
		 * @param listener
		 * @return
		 */
		public Builder setPositiveButton(String positiveButtonText,
				DialogInterface.OnClickListener listener)
		{
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * 设置取消按钮的监听以及文本
		 * 
		 * @param negativeButtonText
		 * @param listener
		 * @return
		 */
		public Builder setNegativeButton(String negativeButtonText,
				DialogInterface.OnClickListener listener)
		{
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}

		/**
		 * 创建dialog
		 */
		public CustomDialog create()
		{
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final CustomDialog dialog = new CustomDialog(context,
					R.style.Dialog);
			dialog.setCanceledOnTouchOutside(false);
			View layout = inflater.inflate(R.layout.dialog_layout, null);
			dialog.addContentView(layout, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			mMsgTextView=((TextView) layout.findViewById(R.id.tv_message));
			mMsgTextView.setText(message);
			if (positiveButtonText != null)
			{
				((Button) layout.findViewById(R.id.btn_ok))
						.setText(positiveButtonText);
				if (positiveButtonClickListener != null)
				{
					((Button) layout.findViewById(R.id.btn_ok))
							.setOnClickListener(new View.OnClickListener()
							{
								public void onClick(View v)
								{
									positiveButtonClickListener.onClick(dialog,
											DialogInterface.BUTTON_POSITIVE);
								}
							});
				}
			} else
			{
				layout.findViewById(R.id.btn_ok).setVisibility(View.GONE);
			}

			if (negativeButtonText != null)
			{
				((Button) layout.findViewById(R.id.btn_cancel))
						.setText(negativeButtonText);
				if (negativeButtonClickListener != null)
				{
					((Button) layout.findViewById(R.id.btn_cancel))
							.setOnClickListener(new View.OnClickListener()
							{
								public void onClick(View v)
								{
									negativeButtonClickListener.onClick(dialog,
											DialogInterface.BUTTON_NEGATIVE);
								}
							});
				}
			} else
			{
				layout.findViewById(R.id.btn_cancel).setVisibility(View.GONE);
			}

			if (icon != null)
			{
				((ImageView) layout.findViewById(R.id.iv_icon))
						.setBackgroundDrawable(icon);
			} else if (contentView != null)
			{
				((LinearLayout) layout.findViewById(R.id.ll_content))
						.removeAllViews();
				((LinearLayout) layout.findViewById(R.id.ll_content)).addView(
						contentView, new LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.WRAP_CONTENT));
			}
			dialog.setContentView(layout);
			return dialog;
		}
		public TextView getMsgTextView()
		{
			return mMsgTextView;
		}

	}


}
