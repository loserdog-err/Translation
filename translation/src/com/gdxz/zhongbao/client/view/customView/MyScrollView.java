package com.gdxz.zhongbao.client.view.customView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by Chean_antao on 2015/8/14.
 */
public class MyScrollView extends ScrollView
{

	public boolean isBottom;//标识是否已经滑动到底部

	public MyScrollView(Context context)
	{
		this(context, null);
	}

	public MyScrollView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt)
	{
		if (t + getHeight() >= computeVerticalScrollRange())
		{
			//ScrollView滑动到底部了
			isBottom = true;
//			L.e("到底部了");
		} else
		{
			isBottom = false;
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
//		L.e("getX:"+ev.getX()+",getY:"+ev.getY());
//		L.e("getScrollX:"+getScrollX()+",getScrollY:"+getScrollY());
		int action = ev.getAction();
		switch (action)
		{
			case MotionEvent.ACTION_MOVE:
				if (isBottom)
				{
					return false;
				}
		}
		return super.onInterceptTouchEvent(ev);
	}
}
