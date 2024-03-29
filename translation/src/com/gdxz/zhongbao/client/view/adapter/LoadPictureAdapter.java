package com.gdxz.zhongbao.client.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.gdxz.zhongbao.client.common.CommonAdapter;
import com.gdxz.zhongbao.client.common.ViewHolder;
import com.gdxz.zhongbao.client.view.activity.LoadPictureAcitivity;
import com.gdxz.zhongbao.client.view.activity.R;

import java.util.LinkedList;
import java.util.List;

public class LoadPictureAdapter extends CommonAdapter<String>
{

	public Context mContext;
	public int category;
	/**
	 * 用户选择的图片，存储为图片的完整路径
	 */
	public static List<String> mSelectedImage = new LinkedList<String>();

	/**
	 * 文件夹路径
	 */
	private String mDirPath;

	public LoadPictureAdapter(Context context, List<String> mDatas,
	                          int itemLayoutId, String dirPath, int category)
	{
		super(context, mDatas, itemLayoutId);
		this.mDirPath = dirPath;
		this.category = category;
		this.mContext = context;
	}

	@Override
	public void convert(final ViewHolder helper, final String item,
	                    final int position)
	{
		// 设置no_pic
		helper.setImageResource(R.id.id_item_image, R.drawable.pictures_no);
		// 设置no_selected
		helper.setImageResource(R.id.id_item_select, R.drawable.picture_unselected);
		// 设置图片
		helper.setImageByUrl(R.id.id_item_image, mDirPath + "/" + item);
		final ImageView mImageView = helper.getView(R.id.id_item_image);
		final ImageView mSelect = helper.getView(R.id.id_item_select);
		mImageView.setColorFilter(null);
		// 设置ImageView的点击事件
		mImageView.setOnClickListener(new OnClickListener()
		{
			// 选择，则将图片变暗，反之则反之
			@Override
			public void onClick(View v)
			{
				// 已经选择过该图片
				if (mSelectedImage.contains(mDirPath + "/" + item))
				{
					mSelectedImage.remove(mDirPath + "/" + item);
					mSelect.setImageResource(R.drawable.picture_unselected);
					mImageView.setColorFilter(null);
				} else
				// 未选择该图片
				{
					/**
					 * 如果类型是文本中的图片，不允许超过三张
					 */
					if (category == LoadPictureAcitivity.SET_CONTENT && mSelectedImage.size() > 2)
					{
						Toast.makeText(mContext, "不允许选择超过3张图片", Toast.LENGTH_SHORT).show();
						return;
					}
					/**
					 * 如果为设置头像或者设置队伍logo，判断是否已经选择过图片
					 * 1:如果选择过，直接返回
					 * 2：如果未选择，允许选择
					 */
					else if (category == LoadPictureAcitivity.SET_HEAD || category == LoadPictureAcitivity
							.SET_TEAM_LOGO)
					{
						if (mSelectedImage.size() > 0)
						{
							Toast.makeText(mContext, "不允许选择超过1张图片", Toast.LENGTH_SHORT).show();
							return;
						}
					}
					mSelectedImage.add(mDirPath + "/" + item);
					mSelect.setImageResource(R.drawable.pictures_selected);
					mImageView.setColorFilter(Color.parseColor("#77000000"));
				}

			}
		});
		/**
		 * 已经选择过的图片，显示出选择过的效果
		 */
		if (mSelectedImage.contains(mDirPath + "/" + item))
		{
			mSelect.setImageResource(R.drawable.pictures_selected);
			mImageView.setColorFilter(Color.parseColor("#77000000"));
		}

	}
}
