package com.gdxz.zhongbao.client.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.gc.materialdesign.views.ButtonRectangle;
import com.gdxz.zhongbao.client.view.fragment.GuideFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chean_antao on 2015/8/14.
 */
public class GuideActivity extends FragmentActivity
{
	ViewPager mViewPager;
	List<GuideFragment> fragments;

	ImageView ivFirst;
	ImageView ivSecond;

	private ButtonRectangle btnGo;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.guide_layout);
		mViewPager = (ViewPager) findViewById(R.id.vp_guide);
		ivFirst = (ImageView) findViewById(R.id.iv_first);
		ivSecond = (ImageView) findViewById(R.id.iv_second);
		btnGo = (ButtonRectangle) findViewById(R.id.btn_go);
		fragments = new ArrayList<>();
		GuideFragment f1 = new GuideFragment();
		Bundle bundle = new Bundle();
		bundle.putInt("category", 1);
		f1.setArguments(bundle);
		GuideFragment f2 = new GuideFragment();
		Bundle bundle2 = new Bundle();
		bundle2.putInt("category", 2);
		f2.setArguments(bundle2);
		fragments.add(f1);
		fragments.add(f2);
		mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
		{
			@Override
			public void onPageScrolled(int position, float positionOffset, int
					positionOffsetPixels)
			{
			}

			@Override
			public void onPageSelected(int position)
			{
				if (position == 0)
				{
					ivFirst.setImageResource(R.drawable.guide_indicate_selected);
					ivSecond.setImageResource(R.drawable.guide_indicate_unselected);
					btnGo.setVisibility(View.INVISIBLE);
				} else
				{
					ivFirst.setImageResource(R.drawable.guide_indicate_unselected);
					ivSecond.setImageResource(R.drawable.guide_indicate_selected);
					btnGo.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onPageScrollStateChanged(int state)
			{
			}
		});
		mViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager())
		{
			@Override
			public Fragment getItem(int position)
			{
				return fragments.get(position);
			}

			@Override
			public int getCount()
			{
				return fragments.size();
			}
		});
	}

	public void go(View view)
	{
		Intent intent = new Intent(GuideActivity.this, LoginActivity.class);
		startActivity(intent);
		this.finish();
	}
}
