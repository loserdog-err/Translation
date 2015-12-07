package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.gdxz.zhongbao.client.common.MyApplication;
import com.gdxz.zhongbao.client.utils.DialogUtils;
import com.gdxz.zhongbao.client.utils.HttpUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * 展示图片打图的activity
 * 
 * @author chenantao
 * 
 */
public class ShowPictureActivity extends Activity
{
	ImageView ivDetailPicture;
	ImageView ivBack;
	TextView tvTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.show_picture);
		MyApplication.getInstance().addActivity(this);
		ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				ShowPictureActivity.this.finish();
			}
		});
		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvTitle.setText("图片");
		DialogUtils.showProgressDialog("图片", "高清图片加载中..",this);
		ivDetailPicture = (ImageView) findViewById(R.id.iv_detail_picture);
		String path = getIntent().getStringExtra("path");
		ImageLoader.getInstance().loadImage(HttpUtils.BASE_FILE_PATH+path, new ImageLoadingListener()
		{

			@Override
			public void onLoadingStarted(String arg0, View arg1)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void onLoadingFailed(String arg0, View arg1, FailReason arg2)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void onLoadingComplete(String arg0, View arg1, Bitmap bitmap)
			{
				ivDetailPicture.setImageBitmap(bitmap);
				DialogUtils.closeProgressDialog();
			}

			@Override
			public void onLoadingCancelled(String arg0, View arg1)
			{
				// TODO Auto-generated method stub

			}
		});
	}
}
