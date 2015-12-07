package com.gdxz.zhongbao.client.view.customView;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

import com.gdxz.zhongbao.client.view.activity.R;

/**
 * 自定义edittext，当输入框 为空时，删除按钮隐藏，当输入 框不为空时候，显示删除按钮
 * 
 * @author chenantao
 * 
 */
public class MyEditText extends EditText implements OnFocusChangeListener
{
	private Context context;
	private Drawable deleteDrawable;

	public MyEditText(Context context)
	{
		super(context);
		this.context = context;
		init();
	}

	public MyEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.context = context;
		init();
	}

	/**
	 * 初始化删除按纽以及添加edittext的文本改变的事件监听
	 */
	public void init()
	{
		deleteDrawable = context.getResources().getDrawable(
				R.drawable.et_delete);
		//添加对edittext的事件监听
		addTextChangedListener(new TextWatcher()
		{
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after)
			{
				
			}
			
			@Override
			public void afterTextChanged(Editable s)
			{
				setDrawable();
			}
		});
		setDrawable();

	}

	/**
	 * 控制删除按纽的显示和隐藏
	 */
	public void setDrawable()
	{
		if (getText().toString().trim().length() > 0)
		{
			setCompoundDrawablesWithIntrinsicBounds(null, null, deleteDrawable,
					null);
		} else
		{
			setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

		}
	}

	/**
	 * 监听单点触摸屏幕的动作，判断触摸位置是否和删除按钮的位置吻合
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		// 判断触摸点是否在水平范围内
		if (deleteDrawable != null
				&& event.getAction() == MotionEvent.ACTION_UP)
		{
			int x = (int) event.getX();
			// 判断触摸点是否在水平范围内
			boolean isInnerWidth = (x > (getWidth() - getTotalPaddingRight()))
					&& (x < (getWidth() - getPaddingRight()));

			Rect rect = deleteDrawable.getBounds();
			// 获取deleteDrawable的高度
			int height = rect.height();
			int y = (int) event.getY();
			// 计算图标底部到edittext底部的距离
			int distance = (getHeight() - height) / 2;
			// 判断触摸点是否在竖直范围内
			boolean isInnerHeight = (y > distance) && (y < (distance + height));

			if (isInnerWidth && isInnerHeight)
			{
				setText("");
				setDrawable();
			}
		}
		return super.onTouchEvent(event);
	}

	
	@Override
	public void onFocusChange(View v, boolean hasFocus)
	{
		setDrawable();
	}
}
