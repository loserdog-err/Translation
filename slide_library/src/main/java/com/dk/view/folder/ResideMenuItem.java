package com.dk.view.folder;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * User: special
 * Date: 13-12-10
 * Time: 下午11:05
 * Mail: specialcyci@gmail.com
 */
public class ResideMenuItem extends LinearLayout
{

	/**
	 * menu item  icon
	 */
	private ImageView iv_icon;
	/**
	 * menu item  title
	 */
	private TextView tv_title;

	public ResideMenuItem(Context context)
	{
		super(context);
		initViews(context);
	}

	public ResideMenuItem(Context context, int icon, int title)
	{
		super(context);
		initViews(context);
		iv_icon.setImageResource(icon);
		tv_title.setText(title);
	}

	public ResideMenuItem(Context context, int icon, String title)
	{
		super(context);
		initViews(context);
		iv_icon.setImageResource(icon);
		tv_title.setText(title);
	}

	private void initViews(Context context)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context
				.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.residemenu_item, this);
		iv_icon = (ImageView) findViewById(R.id.iv_icon);
		tv_title = (TextView) findViewById(R.id.tv_title);
	}

	/**
	 * set the icon color;
	 *
	 * @param icon
	 */
	public void setIcon(int icon)
	{
		iv_icon.setImageResource(icon);
	}

	/**
	 * set the title with resource
	 * ;
	 *
	 * @param title
	 */
	public void setTitle(int title)
	{
		tv_title.setText(title);
	}

	/**
	 * set the title with string;
	 *
	 * @param title
	 */
	public void setTitle(String title)
	{
		tv_title.setText(title);
	}



	/**
	 * 设置文字大小
	 */
	public void setTextSize(float size)
	{
		tv_title.setTextSize(size);
	}

	/**
	 * 设置图片大小
	 */
	public void setImageSize(int width, int height)
	{
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp2px(width), dp2px(height));
		iv_icon.setLayoutParams(lp);
	}

	public ImageView getIcon()
	{
		return iv_icon;
	}

	public TextView getTitle()
	{
		return tv_title;
	}


	public int dp2px(int dp)
	{
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources()
				.getDisplayMetrics());
		return px;
	}
}
