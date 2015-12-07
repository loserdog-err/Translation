package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gdxz.zhongbao.client.common.MyApplication;
import com.gdxz.zhongbao.client.domain.ImageFloder;
import com.gdxz.zhongbao.client.utils.DialogUtils;
import com.gdxz.zhongbao.client.view.adapter.LoadPictureAdapter;
import com.gdxz.zhongbao.client.view.customView.ListImageDirPopupWindow;
import com.gdxz.zhongbao.client.view.customView.ListImageDirPopupWindow.OnImageDirSelected;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class LoadPictureAcitivity extends Activity implements OnImageDirSelected
{
	private ProgressDialog mProgressDialog;

	public static final int SET_HEAD = 1;// 设置头像
	public static final int SET_CONTENT = 2;// 设置文本内容的图片
	public static final int SET_TEAM_LOGO = 3;//设置团队的logo

	// 标识，区别是用户设置头像还是设置内容中的图片
	private int category;

	/**
	 * 存储文件夹中的图片数量
	 */
	private int mPicsSize;
	/**
	 * 图片数量最多的文件夹
	 */
	private File mImgDir;
	/**
	 * 所有的图片
	 */
	private List<String> mImgs;

	private GridView mGirdView;
	private LoadPictureAdapter mAdapter;
	/**
	 * 临时的辅助类，用于防止同一个文件夹的多次扫描
	 */
	private HashSet<String> mDirPaths = new HashSet<String>();

	/**
	 * 扫描拿到所有的图片文件夹
	 */
	private List<ImageFloder> mImageFloders = new ArrayList<ImageFloder>();

	private RelativeLayout mBottomLy;

	private TextView mChooseDir;
	private TextView mImageCount;
	// actionbar
	ImageView ivBack;
	TextView tvTitle;
	TextView tvTitleRight;

	int totalCount = 0;

	private int mScreenHeight;

	private ListImageDirPopupWindow mListImageDirPopupWindow;

	private Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			DialogUtils.closeProgressDialog();
			// 为View绑定数据
			data2View();
			// 初始化展示文件夹的popupWindw
			initListDirPopupWindw();
		}
	};

	/**
	 * 为View绑定数据
	 */
	private void data2View()
	{
		if (mImgDir == null)
		{
			Toast.makeText(getApplicationContext(), "擦，一张图片没扫描到", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		mImgs = Arrays.asList(mImgDir.list());
		// 反转一下list
		Collections.reverse(mImgs);
		/**
		 * 可以看到文件夹的路径和图片的路径分开保存，极大的减少了内存的消耗；
		 */
		mAdapter = new LoadPictureAdapter(getApplicationContext(), mImgs,
				R.layout.grid_item, mImgDir.getAbsolutePath(), category);
		mGirdView.setAdapter(mAdapter);
		mImageCount.setText(totalCount + "张");
	}

	;

	/**
	 * 初始化展示文件夹的popupWindw
	 */
	private void initListDirPopupWindw()
	{
		mListImageDirPopupWindow = new ListImageDirPopupWindow(
				LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.7),
				mImageFloders, LayoutInflater.from(getApplicationContext()).inflate(
				R.layout.list_dir, null));
		mListImageDirPopupWindow.setOnDismissListener(new OnDismissListener()
		{

			@Override
			public void onDismiss()
			{
				// 设置背景颜色变暗
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				lp.alpha = 1.0f;
				getWindow().setAttributes(lp);
			}
		});
		// 设置选择文件夹的回调
		mListImageDirPopupWindow.setOnImageDirSelected(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.load_picture_ui);
		MyApplication.getInstance().addActivity(this);
		DisplayMetrics outMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		mScreenHeight = outMetrics.heightPixels;
		category = getIntent().getIntExtra("category", -1);
		initView();
		initActionBar();
		getImages();
		initEvent();

	}

	/**
	 * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中 完成图片的扫描，最终获得jpg最多的那个文件夹
	 */
	private void getImages()
	{
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			Toast.makeText(this, "暂无外部存储", Toast.LENGTH_SHORT).show();
			return;
		}
		// 显示进度条
		DialogUtils.showProgressDialog("提示", "正在加载", this);
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				String firstImage = null;
				Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				ContentResolver mContentResolver = LoadPictureAcitivity.this
						.getContentResolver();
				// 只查询jpeg和png的图片
				Cursor mCursor = mContentResolver.query(mImageUri, null,
						MediaStore.Images.Media.MIME_TYPE + "=? or "
								+ MediaStore.Images.Media.MIME_TYPE + "=?",
						new String[]{"image/jpeg", "image/png"},
						MediaStore.Images.Media.DATE_MODIFIED);
				while (mCursor.moveToNext())
				{
					// 获取图片的路径
					String path = mCursor.getString(mCursor
							.getColumnIndex(MediaStore.Images.Media.DATA));
					// 拿到第一张图片的路径
					if (firstImage == null)
						firstImage = path;
					// 获取该图片的父路径名
					File parentFile = new File(path).getParentFile();
					if (parentFile == null)
						continue;
					String dirPath = parentFile.getAbsolutePath();
					ImageFloder imageFloder = null;
					// 利用一个HashSet防止多次扫描同一个文件夹（不加这个判断，图片多起来还是相当恐怖的~~）
					if (mDirPaths.contains(dirPath))
					{
						continue;
					} else
					{
						mDirPaths.add(dirPath);
						// 初始化imageFloder
						imageFloder = new ImageFloder();
						imageFloder.setDir(dirPath);
						imageFloder.setFirstImagePath(path);
					}
					int picSize = parentFile.list(new FilenameFilter()
					{
						@Override
						public boolean accept(File dir, String filename)
						{
							if (filename.endsWith(".jpg")
									|| filename.endsWith(".png")
									|| filename.endsWith(".jpeg"))
								return true;
							return false;
						}
					}).length;
					totalCount += picSize;
					imageFloder.setCount(picSize);
					mImageFloders.add(imageFloder);
					if (picSize > mPicsSize)
					{
						mPicsSize = picSize;
						mImgDir = parentFile;
					}
				}
				mCursor.close();
				// 扫描完成，辅助的HashSet也就可以释放内存了
				mDirPaths = null;
				// 通知Handler扫描图片完成
				mHandler.sendEmptyMessage(0x110);

			}
		}).start();

	}

	/**
	 * 初始化actionbar
	 */
	public void initActionBar()
	{
		ivBack.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				LoadPictureAcitivity.this.finish();
			}
		});
		tvTitle.setText("选择照片");
		tvTitleRight.setText("确定");
		tvTitleRight.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				String[] result = new String[LoadPictureAdapter.mSelectedImage
						.size()];
				bundle.putStringArray("image",
						LoadPictureAdapter.mSelectedImage.toArray(result));
				intent.putExtras(bundle);
				setResult(Activity.RESULT_OK, intent);
				LoadPictureAdapter.mSelectedImage = new LinkedList<>();
				finish();
			}
		});
	}

	/**
	 * 初始化View
	 */
	private void initView()
	{
		mGirdView = (GridView) findViewById(R.id.id_gridView);
		mChooseDir = (TextView) findViewById(R.id.id_choose_dir);
		mImageCount = (TextView) findViewById(R.id.id_total_count);
		// init actionbar
		ivBack = (ImageView) findViewById(R.id.iv_back);
		tvTitleRight = (TextView) findViewById(R.id.tv_title_right);
		tvTitleRight.setVisibility(View.VISIBLE);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		mBottomLy = (RelativeLayout) findViewById(R.id.id_bottom_ly);

	}

	private void initEvent()
	{
		/**
		 * 为底部的布局设置点击事件，弹出popupWindow
		 */
		mBottomLy.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				mListImageDirPopupWindow.setAnimationStyle(R.style.anim_popup_dir);
				mListImageDirPopupWindow.showAsDropDown(mBottomLy, 0, 0);
				// 设置背景颜色变暗
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				lp.alpha = .3f;
				getWindow().setAttributes(lp);
			}
		});
	}

	@Override
	public void selected(ImageFloder floder)
	{
		mImgDir = new File(floder.getDir());
		mImgs = Arrays.asList(mImgDir.list(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String filename)
			{
				if (filename.endsWith(".jpg") || filename.endsWith(".png")
						|| filename.endsWith(".jpeg"))
					return true;
				return false;
			}
		}));
		/**
		 * 因为图片默认是按修改时间从远到近修改，所以需要反转一下list
		 */
		Collections.reverse(mImgs);
		/**
		 * 可以看到文件夹的路径和图片的路径分开保存，极大的减少了内存的消耗；
		 */
		mAdapter = new LoadPictureAdapter(getApplicationContext(), mImgs,
				R.layout.grid_item, mImgDir.getAbsolutePath(), category);
		mGirdView.setAdapter(mAdapter);
		// mAdapter.notifyDataSetChanged();
		mImageCount.setText(floder.getCount() + "张");
		mChooseDir.setText(floder.getName());
		mListImageDirPopupWindow.dismiss();
	}

	@Override
	protected void onDestroy()
	{
		LoadPictureAdapter.mSelectedImage = new LinkedList<>();
		super.onDestroy();
	}
}
