package com.gdxz.zhongbao.client.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.gdxz.zhongbao.client.utils.L;
import com.gdxz.zhongbao.client.view.activity.R;

/**
 * Created by Chean_antao on 2015/8/14.
 */
public class GuideFragment extends Fragment
{
	int category;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		category = getArguments().getInt("category");
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
			savedInstanceState)
	{
		LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_guide, null);
		ImageView iv = (ImageView) linearLayout.findViewById(R.id.iv_guide);
		L.e("category:"+category);
		if (category == 1)
		{
			iv.setBackgroundResource(R.drawable.guide_first);
		} else
		{
			iv.setBackgroundResource(R.drawable.guide_second);
		}
		return linearLayout;
	}
}
