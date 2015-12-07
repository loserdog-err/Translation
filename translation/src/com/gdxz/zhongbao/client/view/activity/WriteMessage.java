package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Html;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gdxz.zhongbao.client.Service.AnswerService;
import com.gdxz.zhongbao.client.Service.QuestionService;
import com.gdxz.zhongbao.client.Service.TeamService;
import com.gdxz.zhongbao.client.Service.UserService;
import com.gdxz.zhongbao.client.Service.impl.AnswerServiceimpl;
import com.gdxz.zhongbao.client.Service.impl.QuestionServiceImpl;
import com.gdxz.zhongbao.client.Service.impl.TeamServiceImpl;
import com.gdxz.zhongbao.client.Service.impl.UserServiceImpl;
import com.gdxz.zhongbao.client.common.MyApplication;
import com.gdxz.zhongbao.client.common.MyImageLoader;
import com.gdxz.zhongbao.client.utils.DialogUtils;
import com.gdxz.zhongbao.client.utils.FileUtils;
import com.gdxz.zhongbao.client.utils.L;
import com.gdxz.zhongbao.client.utils.MediaManager;
import com.gdxz.zhongbao.client.utils.NetUtils;
import com.gdxz.zhongbao.client.view.customView.CustomDialog;
import com.gdxz.zhongbao.client.view.customView.RecorderButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 输入信息的activity
 * 包括发布问题，问题回复，输入心情等
 *
 * @author chenantao
 */
public class WriteMessage extends Activity
{
	public static final int WRITE_ANSWER = 1;// 发表回复
	public static final int WRITE_MOOD = 2;// 写心情
	public static final int WRITE_QUESTION = 3;// 提问
	public static final int CREATE_TEAM = 4;//创建团队
	public static final int WRITE_QUESTION_FAILURE = 6;//发布问题失败

	public static final int REQUEST_CODE_CAPTURE_CAMERA = 6;// 拍照

	public static final int HANDLER_SERVER_ERROR = 5;

	// public int uploadFileCount=0;//要上传的图片数量
	public List<String> imagePaths = new ArrayList<>();
	String voicePath;// 语音文件，不为空时判断为已有语音文件
	// 业务组件
	private UserService userService = new UserServiceImpl();
	private AnswerService answerService = new AnswerServiceimpl();
	private QuestionService questionService = new QuestionServiceImpl();
	private TeamService teamService = new TeamServiceImpl();

	// actionbar
	ImageView ivBack;
	TextView tvTitle;
	TextView tvTitleRight;
	// 标题（发布问题时才需要显示）
	LinearLayout llTitle;
	EditText etTitle;
	// 文本输入框
	EditText etMessage;
	// 上传图片栏
	LinearLayout llUploadImage;
	// 上传语音栏
	RelativeLayout rlPlayVoice;
	ImageView ivRecorderAnim;
	FrameLayout flVoiceWrap;
	TextView tvRecorderTime;
	// 删除按钮
	ImageView ivDelete;
	// 底部快捷操作栏
	LinearLayout llOperation;
	ImageView ivAddIcon;
	RecorderButton btnAddVoice;
	ImageView ivAddOwnPhoto;
	// 上个activity传过来的数据
	Bundle bundle;
	int category;
	int questionId;
	//拍照上传的图片
	File cameraFile = null;
	//团队的logo路径
	private String teamLogoPath;
	//发布问题所需的对话框msg的textview
	TextView msgTextView = null;

	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case WRITE_ANSWER:
					DialogUtils.closeProgressDialog();
					Toast.makeText(getApplicationContext(), "发布成功", Toast.LENGTH_SHORT).show();
					// 跳转到问题详细activity
					Intent intent = new Intent(WriteMessage.this,
							QuestionDetailActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(intent);
					WriteMessage.this.finish();
					break;
				case WRITE_QUESTION:
					DialogUtils.closeProgressDialog();
					Toast.makeText(getApplicationContext(), "发布成功", Toast.LENGTH_SHORT).show();
					// 跳转到问题activity
					setResult(RESULT_OK);
					WriteMessage.this.finish();
					break;
				case CREATE_TEAM:
					Toast.makeText(WriteMessage.this, "群组创建成功", Toast.LENGTH_SHORT).show();
					//跳转到teamIndex的activity
					intent = new Intent(WriteMessage.this,
							TeamIndexActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(intent);
					break;
				case HANDLER_SERVER_ERROR:
					Toast.makeText(WriteMessage.this, "服务器异常，请稍后再试", Toast.LENGTH_SHORT).show();
					DialogUtils.closeProgressDialog();
					break;
				case WRITE_QUESTION_FAILURE:
					String error = (String) msg.obj;
					DialogUtils.closeProgressDialog();
					Toast.makeText(WriteMessage.this, error, Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
			}
		}
	};
	private int mMaxItemWith;
	private int mMinItemWith;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.write_message);
		MyApplication.getInstance().addActivity(this);
		bundle = getIntent().getBundleExtra("info");
		category = bundle.getInt("category");
		initView();
		showViewByCategory(category);
		// 获取系统宽度
		WindowManager wManager = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wManager.getDefaultDisplay().getMetrics(outMetrics);
		mMaxItemWith = (int) (outMetrics.widthPixels * 0.7f);
		mMinItemWith = (int) (outMetrics.widthPixels * 0.15f);
	}

	public void initView()
	{
		// 初始化顶部菜单
		ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				WriteMessage.this.finish();
			}
		});
		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvTitleRight = (TextView) findViewById(R.id.tv_title_right);
		tvTitleRight.setVisibility(View.VISIBLE);
		// 发布菜单点击事件
		tvTitleRight.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (etMessage.getText().toString().trim().equals(""))
				{
					Toast.makeText(WriteMessage.this, "输入内容不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				if (category == WRITE_ANSWER)
				{
					if (!NetUtils.isConnected(WriteMessage.this))
					{
						Toast.makeText(WriteMessage.this, "請檢查你的網絡是否正常", Toast.LENGTH_SHORT)
								.show();
						return;
					}
					answerService.postAnswer(
							handler,
							etMessage.getText().toString(),
							questionId + "",
							UserServiceImpl.getCurrentUserId(WriteMessage.this));
					DialogUtils.showProgressDialog("提示", "回复发表中，请骚等", WriteMessage.this);
				} else if (category == CREATE_TEAM)//点击创建团队
				{
					createTeam();
				} else if (category == WRITE_QUESTION)
				{
					if (etTitle.getText().toString().trim().equals("")
							|| etTitle.length() > 30)
					{
						Toast.makeText(WriteMessage.this, "标题输入不合法", Toast.LENGTH_SHORT)
								.show();
						return;
					}
					postQuestion();
				}
			}
		});
		// 标题和上传图片、语音栏，默认隐藏
		llTitle = (LinearLayout) findViewById(R.id.ll_question_title);
		llUploadImage = (LinearLayout) findViewById(R.id.ll_upload_image);
		rlPlayVoice = (RelativeLayout) findViewById(R.id.rl_play_voice_ui);
		ivRecorderAnim = (ImageView) rlPlayVoice.findViewById(R.id.iv_recorder_anim);
		flVoiceWrap = (FrameLayout) rlPlayVoice.findViewById(R.id.fl_voice_wrap);
		tvRecorderTime = (TextView) rlPlayVoice.findViewById(R.id.tv_recorder_time);//语音文件显示时间长度的文本
		etMessage = (EditText) findViewById(R.id.et_message);//内容输入框
		llOperation = (LinearLayout) findViewById(R.id.ll_operation);
		ivDelete = (ImageView) findViewById(R.id.iv_delete);//删除按钮
		llTitle = (LinearLayout) findViewById(R.id.ll_question_title);
		etTitle = (EditText) findViewById(R.id.et_question_title);
		ivAddIcon = (ImageView) findViewById(R.id.iv_add_icon);
		btnAddVoice = (RecorderButton) findViewById(R.id.btn_addVoice);
		ivAddOwnPhoto = (ImageView) findViewById(R.id.iv_add_own_photo);
		ivDelete.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				etMessage.setText("");
			}
		});
		//通过类型来完成特定的初始化
		if (category == WRITE_QUESTION)
		{
			onWantToWriteQuestion();
		} else if (category == WRITE_ANSWER)
		{
			questionId = bundle.getInt("questionId");
		} else if (category == WRITE_MOOD)
		{
		} else if (category == CREATE_TEAM)
		{
			onWantToCreateTeam();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == CREATE_TEAM)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				Bundle bundle = data.getExtras();
				teamLogoPath = bundle.getStringArray("image")[0];
				ImageView imageView = (ImageView) llUploadImage.getChildAt(0);
				Bitmap bitmap = BitmapFactory.decodeFile(teamLogoPath);
				imageView.setImageBitmap(bitmap);
			}
		} else if (requestCode == WRITE_QUESTION)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				Bundle bundle = data.getExtras();
				List<String> temp = Arrays.asList(bundle.getStringArray("image"));
				int tempSize = temp.size();
				for (int i = 0; i < tempSize; i++)
				{
					imagePaths.add(temp.get(i));
					if (imagePaths.size() > 2)
					{
						break;
					}
				}
//				imagePaths = new ArrayList<>(imagePaths);
				for (int i = 0; i < imagePaths.size(); i++)
				{
					// 将图片设置进上传图片栏的image里面
					MyImageLoader.getInstance().loadImage(imagePaths.get(i),
							(ImageView) llUploadImage.getChildAt(i));
				}
			}
		} else if (requestCode == REQUEST_CODE_CAPTURE_CAMERA)
		{
			if (resultCode == RESULT_OK)
			{
				String path = cameraFile.getAbsolutePath();
//				Bitmap bitmap;
//				bitmap = BitmapFactory.decodeFile(path);
				L.e("size:" + imagePaths.size());
				// 将图片设置进上传图片栏的image里面
				ImageView imageView = (ImageView) llUploadImage.getChildAt(imagePaths
						.size());
				MyImageLoader.getInstance().loadImage(path,
						imageView);
				L.e(path);
				imagePaths.add(path);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 录音完成后调用
	 */
	public void onRecordComplete(float seconds, final String path)
	{
		voicePath = path;
		//动态设置语音框的长度
		ViewGroup.LayoutParams lp = flVoiceWrap.getLayoutParams();
		lp.width = (int) (mMinItemWith + mMaxItemWith / 120f * seconds);
		flVoiceWrap.setLayoutParams(lp);
		//设置语音框提示语音长度的文本
		tvRecorderTime.setText((Math.round(seconds) + "\""));
		//禁止添加语音
		btnAddVoice.setEnabled(false);
		//显示语音栏
		rlPlayVoice.setVisibility(View.VISIBLE);
		//单击语音图片播放声音
		rlPlayVoice.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//设置语音文件的背景图片并开始播放背景动画
				ivRecorderAnim.setBackgroundResource(R.drawable.play_recorder_anim);
				AnimationDrawable drawable = (AnimationDrawable) ivRecorderAnim
						.getBackground();
				drawable.start();
				MediaManager.playSound(path,
						new MediaPlayer.OnCompletionListener()
						{
							@Override
							public void onCompletion(MediaPlayer mp)
							{
								ivRecorderAnim.setBackgroundResource(R.drawable.adj);
							}
						});
			}
		});
	}

	/**
	 * 通过拍照添加图片
	 */
	public void addImgByTakePhoto()
	{
		if (imagePaths != null && imagePaths.size() > 2)// 上传图片已满
		{
			Toast.makeText(WriteMessage.this, "图片附件已到上限", Toast.LENGTH_SHORT).show();
			return;
		}
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED))
		{
			cameraFile = new File(FileUtils.createImageFile());
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile));
			startActivityForResult(intent, REQUEST_CODE_CAPTURE_CAMERA);
		} else
		{
			Toast.makeText(getApplicationContext(), "请确认已经插入SD卡",
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 当类型为创建团队的一系列初始化操作
	 */
	private void onWantToCreateTeam()
	{
		// 添加图片的单击事件
		ivAddIcon.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startLoadPictureActivityByCategory(LoadPictureAcitivity.SET_TEAM_LOGO,
						CREATE_TEAM);
			}
		});
	}

	/**
	 * 当类型为发表问题的一系列初始化
	 */
	public void onWantToWriteQuestion()
	{
		// 添加图片的单击事件
		ivAddIcon.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (imagePaths.size() > 2)
				{
					Toast.makeText(WriteMessage.this, "图片数量已达上限", Toast.LENGTH_SHORT).show();
					return;
				}
				startLoadPictureActivityByCategory(LoadPictureAcitivity.SET_CONTENT,
						WRITE_QUESTION);
			}
		});
		/**
		 * 添加语音(只能添加一个语音)
		 */
		btnAddVoice.setAudioRecordFinishListener(new RecorderButton.OnAudioRecordFinishListener()
		{
			@Override
			public void finish(float seconds, final String path)
			{
				onRecordComplete(seconds, path);
			}
		});
		/**
		 * 拍照上传
		 */
		ivAddOwnPhoto.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				addImgByTakePhoto();
			}
		});
	}

	/**
	 * 发表一个问题
	 */
	public void postQuestion()
	{
		if (!NetUtils.isConnected(this))
		{
			Toast.makeText(this, "請檢查你的網絡是否正常", Toast.LENGTH_SHORT).show();
			return;
		}
		CustomDialog dialog = null;
		final CustomDialog.Builder builder = new CustomDialog.Builder(
				WriteMessage.this);
		final String point = UserServiceImpl.getCurrentUserStringInfo(this,
				"point");
		builder.setMessage(Html.fromHtml("输入悬赏的金额数<br>剩余:<font color=\"blue\">" + point +
				"</font>积分"));
		final EditText etRewardAmount = new EditText(
				getApplicationContext());
		etRewardAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
		etRewardAmount.setTextColor(Color.BLACK);
		builder.setContentView(etRewardAmount);
		builder.setPositiveButton("确定",
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						String rewardAmount = etRewardAmount.getText()
								.toString();
						if (rewardAmount == null
								|| "".equals(rewardAmount))
						{
							Toast.makeText(WriteMessage.this,
									"悬赏金额不能为空", Toast.LENGTH_SHORT).show();
						} else if (Integer.parseInt(rewardAmount) > 10000)
						{
							Toast.makeText(WriteMessage.this,
									"你确定你这么豪？", Toast.LENGTH_SHORT).show();
						} else if (Integer.parseInt(rewardAmount) > Integer.parseInt(point))
						{
							builder.setMessage("哈哈");
							Toast.makeText(WriteMessage.this,
									"别钱还想装逼？", Toast.LENGTH_SHORT).show();
						} else
						{
							questionService.postQuestion(
									handler,
									etMessage.getText().toString(),
									UserServiceImpl.getCurrentUserId(WriteMessage.this),
									rewardAmount,
									etTitle
											.getText().toString(),
									imagePaths, voicePath);
							DialogUtils.showProgressDialog("提示", "问题发布中，请耐心等待o(∩_∩)o",
									WriteMessage.this);

						}
					}
				});
		dialog = builder.create();
		msgTextView = builder.getMsgTextView();
		dialog.show();
	}

	/**
	 * 通过category打开选择图片的activity
	 */
	public void startLoadPictureActivityByCategory(int LoadPictureCategory, int
			writeMessageCategory)
	{
		Intent intent = new Intent(WriteMessage.this,
				LoadPictureAcitivity.class);
		intent.putExtra("category", LoadPictureCategory);
		startActivityForResult(intent, writeMessageCategory);
	}

	/**
	 * 根据类型显示或者隐藏view
	 * 1：写回答
	 * 2：写问题：显示内容标题，快捷操作栏，图片语音栏
	 * 3：写心情
	 * 4：创建团队：显示内容标题，显示快捷操作栏，图片栏，隐藏语音按钮
	 */
	public void showViewByCategory(int category)
	{
		switch (category)
		{
			case WRITE_ANSWER:
				tvTitle.setText("回复问题");
				tvTitleRight.setText("回复");
				etMessage.setHint("写出你的回答");
				break;
			case WRITE_MOOD:
				tvTitle.setText("发表心情");
				etMessage.setHint("写出你的心情");
				tvTitleRight.setText("发表");
				break;
			case WRITE_QUESTION:
				llTitle.setVisibility(View.VISIBLE);
				llOperation.setVisibility(View.VISIBLE);
				llUploadImage.setVisibility(View.VISIBLE);
				tvTitleRight.setText("发表");
				tvTitle.setText("提出问题");
				etMessage.setHint("写出你的内容");
				break;
			case CREATE_TEAM:
				llTitle.setVisibility(View.VISIBLE);
				etTitle.setHint("你的团队名字");
				llOperation.setVisibility(View.VISIBLE);
				btnAddVoice.setVisibility(View.GONE);
				llUploadImage.setVisibility(View.VISIBLE);
				tvTitle.setText("创建团队");
				tvTitleRight.setText("创建");
				etMessage.setHint("写出你的团队宣言");
				break;
		}
	}

	/**
	 * 创建团队
	 */
	public void createTeam()
	{
		if (!NetUtils.isConnected(this))
		{
			Toast.makeText(this, "網絡異常", Toast.LENGTH_SHORT).show();
			return;
		}
		String teamName = etTitle.getText().toString();
		String teamDeclaration = etMessage.getText().toString();
		if (teamName.length() > 14)
		{
			Toast.makeText(this, "团队名过长", Toast.LENGTH_SHORT).show();
			return;
		}
		if (teamDeclaration.length() > 100)
		{
			Toast.makeText(this, "团队宣言过长", Toast.LENGTH_SHORT).show();
			return;
		}
		teamService.createTeam(teamName, teamDeclaration, teamLogoPath, handler, UserServiceImpl
				.getCurrentUserId
						(this));
	}


}
