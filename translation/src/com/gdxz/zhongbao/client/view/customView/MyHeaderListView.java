package com.gdxz.zhongbao.client.view.customView;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.ListView;

import com.gdxz.zhongbao.client.utils.L;

/**
 * Created by chenantao on 2015/7/30.
 */
public class MyHeaderListView extends ListView
{
	private int mIvOriginalHeight;
	private ImageView mImageView;

	private int mLimitHeight = 550;

	public MyHeaderListView(Context context)
	{
		this(context, null);
	}

	public MyHeaderListView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public MyHeaderListView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		mLimitHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mLimitHeight,
				getResources()
						.getDisplayMetrics());
		L.e(mLimitHeight+":height");
	}

	public void initImageView(ImageView imageView)
	{
		mImageView = imageView;
		mIvOriginalHeight = imageView.getHeight();
	}

	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int
			scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean
			                               isTouchEvent)
	{
//		Log.e("TAG", "deltaX:" + deltaX + ",deltaY" + deltaY);
		if (deltaY < 0 && mImageView.getHeight() < mLimitHeight)
		{
			mImageView.getLayoutParams().height = mImageView.getHeight() - deltaY;
			mImageView.requestLayout();
		}
		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY,
				maxOverScrollX, maxOverScrollY, isTouchEvent);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt)
	{
		if (mImageView != null)
		{
			View header = (View) mImageView.getParent();
			if (header.getTop() < 0 && mImageView.getHeight() > mIvOriginalHeight)
			{
//				Log.e("TAG", "top:" + header.getTop());
//				Log.e("TAG", "front height:" + mImageView.getHeight());
				mImageView.getLayoutParams().height = mImageView.getHeight() + header.getTop();
				header.layout(header.getLeft(), 0, header.getRight(), header.getBottom());
				mImageView.requestLayout();
				Log.e("TAG", "imageView Height:" + mImageView.getHeight());
			}
		}
		super.onScrollChanged(l, t, oldl, oldt);
	}

	class ResetAnimation extends Animation
	{
		//当前的高度与初始高度之差
		int extraHeight = mImageView.getHeight() - mIvOriginalHeight;
		//释放时的高度
		int targetHeight = mImageView.getHeight();

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t)
		{
			//interpolatedTime: 0.0~1.0
//			Log.e("TAG", "interpolatedTime:" + interpolatedTime);
//			Log.e("TAG", "extraHeight:" + extraHeight);
			mImageView.getLayoutParams().height = (int) (targetHeight - interpolatedTime
					* extraHeight);
			mImageView.requestLayout();
			super.applyTransformation(interpolatedTime, t);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		int action = ev.getAction();
		switch (action)
		{
			case MotionEvent.ACTION_UP:
				if (mImageView.getHeight() > mIvOriginalHeight)
				{
//					Log.e("TAG", "start");
					Animation animation = new ResetAnimation();
					animation.setDuration(300);
					mImageView.startAnimation(animation);
				}

		}
		return super.onTouchEvent(ev);
	}
}
