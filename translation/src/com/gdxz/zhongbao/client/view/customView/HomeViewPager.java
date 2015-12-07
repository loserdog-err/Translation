package com.gdxz.zhongbao.client.view.customView;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.TextView;

public class HomeViewPager extends ViewPager
{
	// 顶部的三个textview
	private TextView tvNewlyQuestion;
	private TextView tvTodayHotRanking;
	private TextView tvTotalRanking;

	public HomeViewPager(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

//	@Override
//	public boolean dispatchTouchEvent(MotionEvent ev)
//	{
//		// System.out.println("parent:"+getParent());
//		if (ev.getAction() == MotionEvent.ACTION_DOWN)
//		{
//			Log.e("TAG", "receive");
//		}
//		return super.dispatchTouchEvent(ev);
//	}
//
//	@Override
//	public boolean onTouchEvent(MotionEvent ev)
//	{
//		int action = ev.getAction();
//		switch (action)
//		{
//			case MotionEvent.ACTION_DOWN:
//				Log.e("TAG", "child down");
//				break;
//			default:
//				break;
//
//		}
//		try
//		{
//			super.onTouchEvent(ev);
//		} catch (Exception ex)
//		{
//		}
//		return true;
//	}
//
//	@Override
//	public boolean onInterceptTouchEvent(MotionEvent ev)
//	{
//		int action = ev.getAction();
//		switch (action)
//		{
//			case MotionEvent.ACTION_DOWN:
//				Log.e("TAG", "child Intercept down");
//				return  true;
//			case MotionEvent.ACTION_MOVE:
//				Log.e("TAG", "child Intercept move");
//				return true;
//			default:
//				break;
//		}
//		return super.onInterceptTouchEvent(ev);
//	}
}
