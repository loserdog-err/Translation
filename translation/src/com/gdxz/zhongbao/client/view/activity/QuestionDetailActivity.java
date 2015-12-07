package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gdxz.zhongbao.client.Service.AnswerService;
import com.gdxz.zhongbao.client.Service.QuestionService;
import com.gdxz.zhongbao.client.Service.impl.AnswerServiceimpl;
import com.gdxz.zhongbao.client.Service.impl.QuestionServiceImpl;
import com.gdxz.zhongbao.client.Service.impl.UserServiceImpl;
import com.gdxz.zhongbao.client.common.CommonAdapter;
import com.gdxz.zhongbao.client.common.MyApplication;
import com.gdxz.zhongbao.client.common.ViewHolder;
import com.gdxz.zhongbao.client.domain.Answer;
import com.gdxz.zhongbao.client.domain.Question;
import com.gdxz.zhongbao.client.domain.Team;
import com.gdxz.zhongbao.client.domain.User;
import com.gdxz.zhongbao.client.utils.DatabaseUtils;
import com.gdxz.zhongbao.client.utils.DateUtils;
import com.gdxz.zhongbao.client.utils.DialogUtils;
import com.gdxz.zhongbao.client.utils.FileUtils;
import com.gdxz.zhongbao.client.utils.HttpUtils;
import com.gdxz.zhongbao.client.utils.L;
import com.gdxz.zhongbao.client.utils.MediaManager;
import com.gdxz.zhongbao.client.utils.NetUtils;
import com.gdxz.zhongbao.client.utils.StringUtils;
import com.gdxz.zhongbao.client.view.customView.MyScrollView;
import com.gdxz.zhongbao.client.view.customView.ShareDialog;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * 问题详细信息activity
 * <p/>
 * 这个页面实现有点复杂，需要记录一下：
 * 本页面采用scrollView嵌套pullToRefreshView(本质是一个listview)这个结构
 * 由于scrollView嵌套listview会有冲突，所以需要在代码中动态计算listview的高度
 * <p/>
 * 同时因为scrollView会拦截到listview的上拉加载事件，所以要自定义scrollView监听滑动到底部事件
 * 当scrollView滑动到底部的时候，不拦截move事件，此时move事件将会分发到listview，解决不能上拉加载的问题
 * <p/>
 * 需要一个标识区别是刷新还是加载更多，当标识为刷新时需要初始化listview以及用户信息和最佳答案
 * 当标识为加载更多的时候，遍历从服务器加载过来的数据，add到listview的数据集items上面。
 *
 * @author chenantao
 */
public class QuestionDetailActivity extends Activity implements OnClickListener,
		PlatformActionListener
{
	//存进sp里面的信息的key常量
	public static final String SP_PRAISE_OR_DESPISE_ANSWER_ID = "praiseOrDespiseAnswerId";
	// 一些常量
	public static final int SHARE_SINA_WEIBO_SUCCESS = 1;
	public static final int SHARE_WECHAT_SUCCESS = 2;
	public static final int SHARE_WECHAT_MOMENT_SUCCESS = 3;
	public static final int SHARE_QQ_SUCCESS = 4;
	public static final int SHARE_CANCEL = 5;
	public static final int SHARE_FAILURE = 6;

	public final static int TEXT_DATA_LOAD_COMPLETE = 0x10;// 代表文字信息加载完成
	public final static int VOICE_DATA_LOAD_COMPLETE = 0x20;// 代表音频文件加载完成
	public final static int SERVER_ERROR = 0x30;//服务器错误
	public static final int SET_BEST_ANSWER_SUCCESS = 0x40;
	public static final int FOLLOW_QUESTION_SUCCESS = 0x50;

	public int loadAnswerType = 0;
	public static final int TYPE_REFRESH_DATA = 0;
	public static final int TYPE_ADD_MORE_DATA = 1;


	public int pageNow = 1;//用户回答的分页显示，标识当前为回答的第几页，下标从1开始

	QuestionService questionService = new QuestionServiceImpl();
	AnswerService answerService = new AnswerServiceimpl();
	String questionId;//当前问题的id
	String questionerId;//提问者的id
	Question currentQuestion;

	// actionbar
	TextView tvTitle;
	ImageView ivBack;
	ImageView ivRightTitle;

	MyScrollView svWrapper;

	// ---------------------------------------------题主部分----------------------------------
	// 题主信息以及悬赏部分
	ImageView ivQuestionerHead;// 题主头像
	TextView tvQuestionerName;// 题主姓名
	TextView tvQuestionerGender;// 题主性别
	TextView tvRewardAmount;// 悬赏金额
	ImageView ivRewardAmount;//金钱图标
	ImageView ivSolve;//已解决图标
	// 问题部分
	TextView tvQuestionTitle;
	TextView tvQuestionContent;
	TextView tvPostTime;
	// 快捷操作栏部分
	TextView tvFollow;// 关注
	ImageView ivFollow;//关注textview左边的图标
	TextView tvAnswerQuestioner;// 回答题主
	User questioner;

	// ---------------------------------------------最佳答案部分----------------------------------
	private Answer bestAnswer;//最佳答案的条目
	LinearLayout wrapperBestAnswer;// 包裹最佳答案栏的linearlayout
	ImageView ivBestAnswerHead;// 最佳答案的头像
	TextView tvBestAnswerName;// 最佳答案的用户名
	TextView tvBestAnswerContent;
	TextView tvBestAnswerPostTime;// 最佳答案的发布时间
	TextView tvBestAnswerPraise;// 赞
	TextView tvBestAnswerDespise;// 鄙视
	TextView tvBestAnswerTeam;//最佳答案所属的团队
	// ---------------------------------------------回答部分----------------------------------

	PullToRefreshListView pullToRefreshListView;
	CommonAdapter<Answer> listViewAdapter;
	List<Answer> items;
	List<String> answerHead;// 回答者的头像，以路径的形式存储
	List<String> answerNames;
	List<String> answerContents;
	List<String> answerPostTimes;
	List<String> answerPraise;
	List<String> answerDespise;
	List<String> answerIds;
	boolean hadBestAnswer;// 标识是否拥有最佳答案
	String voicePath;

	// 从服务端返回来的数据
	Map<String, Object> data;
	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case SHARE_SINA_WEIBO_SUCCESS:
					Toast.makeText(getApplicationContext(), "微博分享成功", Toast.LENGTH_LONG).show();
					break;
				case SHARE_WECHAT_SUCCESS:
					Toast.makeText(getApplicationContext(), "微信分享成功", Toast.LENGTH_LONG).show();
					break;
				case SHARE_WECHAT_MOMENT_SUCCESS:
					Toast.makeText(getApplicationContext(), "朋友圈分享成功", Toast.LENGTH_LONG).show();
					break;
				case SHARE_QQ_SUCCESS:
					Toast.makeText(getApplicationContext(), "QQ分享成功", Toast.LENGTH_LONG).show();
					break;
				case SHARE_CANCEL:
					Toast.makeText(getApplicationContext(), "取消分享", Toast.LENGTH_LONG).show();
					break;
				case SHARE_FAILURE:
					Toast.makeText(getApplicationContext(), "分享失败啊" + msg.obj, Toast.LENGTH_LONG)
							.show();
					break;
				case TEXT_DATA_LOAD_COMPLETE:
					pullToRefreshListView.onRefreshComplete();
					data = (Map<String, Object>) msg.obj;
					List<Answer> answers = (List<Answer>) data.get("answers");
					currentQuestion = (Question) data.get("question");
					DbUtils dbUtils = DatabaseUtils.getDbUtils(QuestionDetailActivity.this);
					try
					{
						dbUtils.saveOrUpdate(currentQuestion);
						dbUtils.saveOrUpdate(currentQuestion.getAuthor());
						for (Answer answer : answers)
						{
							User author = answer.getAuthor();
							dbUtils.saveOrUpdate(author);
							dbUtils.saveOrUpdate(answer);
						}

					} catch (DbException e)
					{
						e.printStackTrace();
					}
					DialogUtils.closeProgressDialog();
					parseData();
					break;
				case SERVER_ERROR:
					Toast.makeText(QuestionDetailActivity.this, "服务器错误", Toast.LENGTH_SHORT)
							.show();
					break;
				case VOICE_DATA_LOAD_COMPLETE:
					voicePath = (String) msg.obj;
					break;
				case SET_BEST_ANSWER_SUCCESS:
					Toast.makeText(QuestionDetailActivity.this, "设置最佳答案成功", Toast.LENGTH_SHORT)
							.show();
					break;
				case FOLLOW_QUESTION_SUCCESS:
					Toast.makeText(QuestionDetailActivity.this, "关注问题成功", Toast.LENGTH_SHORT)
							.show();
					setFollowDisable();
					break;
				default:
					break;
			}
			super.handleMessage(msg);
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.home_question_detail_ui);
		MyApplication.getInstance().addActivity(this);
		ShareSDK.initSDK(this);
		questionId = getIntent().getStringExtra("questionId");
		DialogUtils.showProgressDialog("问题详情", "精彩内容马上呈现", this);
		initView();
		svWrapper = (MyScrollView) findViewById(R.id.sv_wrapper);
//		svWrapper.setVisibility(View.INVISIBLE);
		if (NetUtils.isConnected(this))
		{
			pullToRefreshListView.setMode(Mode.PULL_FROM_END);
		} else
		{
			pullToRefreshListView.setMode(Mode.DISABLED);
		}
		pullToRefreshListView.setScrollingWhileRefreshingEnabled(true);
	}


	/**
	 * 解析从服务端返回来的数据
	 */
	public void parseData()
	{
		List<Answer> answers = (List<Answer>) (data.get("answers"));
		if (loadAnswerType == TYPE_REFRESH_DATA)
		{
			// 设置题主的数据
			initQuestioner();
			// Set<Answer> answers = question.getAnswers();
			items = answers;
			initListView();
			svWrapper.post(new Runnable()
			{
				@Override
				public void run()
				{
					svWrapper.scrollTo(0, 0);
				}
			});

		} else//加载更多答案
		{
			L.e("add more");
			for (int i = 0; i < answers.size(); i++)
			{
				items.add(answers.get(i));
			}
			listViewAdapter.notifyDataSetChanged();
			setListViewHeightBasedOnChildren(pullToRefreshListView.getRefreshableView());
			svWrapper.isBottom = false;
		}
	}

	/**
	 * 初始化各view
	 */
	public void initView()
	{
		// actionbar
		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvTitle.setText("问题详情");
		ivRightTitle = (ImageView) findViewById(R.id.iv_title_right);
		ivRightTitle.setVisibility(View.VISIBLE);
		ivRightTitle.setOnClickListener(this);
		ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(this);
		// 题主部分
		ivQuestionerHead = (ImageView) findViewById(R.id.iv_questioner_head);
		tvQuestionerName = (TextView) findViewById(R.id.tv_questioner_name);
		tvQuestionerGender = (TextView) findViewById(R.id.tv_questioner_gender);
		tvRewardAmount = (TextView) findViewById(R.id.tv_reward_amount);
		ivRewardAmount = (ImageView) findViewById(R.id.iv_reward_amount);
		ivSolve = (ImageView) findViewById(R.id.iv_solve);
		tvQuestionTitle = (TextView) findViewById(R.id.tv_question_title);
		tvQuestionContent = (TextView) findViewById(R.id.tv_question_content);
		tvPostTime = (TextView) findViewById(R.id.tv_question_postTime);
		ivFollow = (ImageView) findViewById(R.id.iv_follow);
		tvFollow = (TextView) findViewById(R.id.tv_follow);
		tvAnswerQuestioner = (TextView) findViewById(R.id.tv_answer_questioner);
		// 最佳答案部分
		wrapperBestAnswer = (LinearLayout) findViewById(R.id.ll_best_answer);
		ivBestAnswerHead = (ImageView) findViewById(R.id.iv_best_answer_head);
		tvBestAnswerName = (TextView) findViewById(R.id.tv_best_answer_name);
		tvBestAnswerContent = (TextView) findViewById(R.id.tv_best_answer_content);
		tvBestAnswerPostTime = (TextView) findViewById(R.id.tv_best_answer_postTime);
		tvBestAnswerPraise = (TextView) findViewById(R.id.tv_best_answer_praise);
		tvBestAnswerDespise = (TextView) findViewById(R.id.tv_best_answer_despise);
		tvBestAnswerTeam = (TextView) findViewById(R.id.tv_best_answer_team);
		// 默认隐藏最佳答案且不占位置
		wrapperBestAnswer.setVisibility(View.GONE);
		// 回答部分
		pullToRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_answer);
		//判斷是否有網絡，如果有，則從服務器加載，如果沒有，則從緩存加載
		if (NetUtils.isConnected(this))
		{
			//加载服务器端问题的数据
			questionService.loadQuestionDetailData(handler, questionId, pageNow + "",
					pullToRefreshListView);
			pageNow++;
		} else
		{
			DbUtils dbUtils = DatabaseUtils.getDbUtils(this);
			try
			{
				Question question = dbUtils.findById(Question.class, questionId);
				if (question == null)//沒有緩存過本問題
				{
					Toast.makeText(QuestionDetailActivity.this, "沒緩存過本問題", Toast.LENGTH_SHORT)
							.show();
					QuestionDetailActivity.this.finish();
				} else
				{
					List<Answer> answers = question.getAnswers();
					data = new HashMap<>();
					data.put("question", question);
					data.put("answers", answers);
					data.put("hadFollow", true);
					parseData();
					L.e("answers size:" + answers.size());
					DialogUtils.closeProgressDialog();
				}

			} catch (DbException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 初始化题主部分
	 */
	public void initQuestioner()
	{
		final Question question = (Question) data.get("question");
		questioner = question.getAuthor();
		questionerId = questioner.getId() + "";
		// 回复的单击事件
		tvAnswerQuestioner.setOnClickListener(this);
		//如果当前用户是题主，则将收藏替换为邀请回答
		String currentUserId = UserServiceImpl.getCurrentUserId(this);
		if (!"".equals(currentUserId) && currentUserId.equals(questionerId))
		{
			tvFollow.setText("邀请回答");
			ivFollow.setImageResource(android.R.drawable.ic_menu_crop);
		} else
		{
			tvFollow.setText("收藏");
			ivFollow.setImageResource(android.R.drawable.star_big_on);
			boolean hadFollow = (boolean) data.get("hadFollow");
			L.e("had follow:" + hadFollow);
			if (hadFollow)
			{
				setFollowDisable();
			}
		}
		tvFollow.setOnClickListener(this);
		tvQuestionerName.setText(question.getAuthor().getUsername());
		Integer gender = question.getAuthor().getGender();
		tvQuestionerGender.setText(gender == null ? "" : (gender == 0 ? "男" : "女"));
		if (question.getIsSolve())
		{
			tvRewardAmount.setVisibility(View.GONE);
			ivRewardAmount.setVisibility(View.GONE);
			ivSolve.setVisibility(View.VISIBLE);
		} else
		{
			tvRewardAmount.setVisibility(View.VISIBLE);
			ivRewardAmount.setVisibility(View.VISIBLE);
			ivSolve.setVisibility(View.GONE);
			tvRewardAmount.setText(question.getRewardAmount() + "");
		}
		/**
		 * 问题内容得处理一下
		 * 1：如果有诸如/文件名.png/的图片格式，则利用SpannableString使用默认图片覆盖掉
		 * 2：使用imageLoder异步加载图片，附加到默认图片上去
		 */
		tvQuestionContent.setText(parseContent(question.getContent(), question.getAuthor().getId()
				+ ""));
		tvQuestionTitle.setText(question.getTitle());
		tvPostTime.setText(question.getPostTime() == null ? "" : DateUtils.date2string(question
						.getPostTime(),
				"yyyy-MM-dd"));
		//加载题主的头像
		if (questioner.getHead() != null)
		{
			DisplayImageOptions options = FileUtils.getDefaultImageOptions();
			ImageLoader.getInstance().displayImage(HttpUtils.BASE_FILE_PATH + questioner.getHead(),
					ivQuestionerHead, options);
		}

	}

	/**
	 * 初始化最佳答案部分
	 */
	public void initBestAnswer(final Answer answer)
	{
		hadBestAnswer = true;
		// 显示最佳答案的界面
		wrapperBestAnswer.setVisibility(View.VISIBLE);
		tvBestAnswerName.setText(answer.getAuthor().getUsername());
		tvBestAnswerContent.setText(answer.getContent());
		tvBestAnswerPostTime.setText(DateUtils.date2string(answer.getPostTime(), "yyyy-MM-dd"));
		tvBestAnswerPraise.setText(StringUtils.nullIntegerFilter(answer.getPraiseCount()));
		tvBestAnswerDespise.setText(StringUtils.nullIntegerFilter(answer.getDespiseCount()));
		//从sp中取出数据，校验当前用户是否赞过或者鄙视过该回答
		Set<String> set = UserServiceImpl.getCurrentUserSetInfo(this,
				SP_PRAISE_OR_DESPISE_ANSWER_ID);
		for (String str : set)
		{
			if (str.equals(answer.getId() + ""))
			{
				setBestAnswerPraiseAndDespiseDisable();
				break;
			}
		}
		tvBestAnswerPraise.setOnClickListener(new OnPraiseAndDespiseListener(tvBestAnswerPraise,
				answer.getId() + "",
				AnswerServiceimpl.METHOD_NAME_ADD_PRAISE_COUNT));
		tvBestAnswerDespise.setOnClickListener(new OnPraiseAndDespiseListener(tvBestAnswerDespise,
				answer.getId() +
						"", AnswerServiceimpl.METHOD_NAME_ADD_DESPISE_COUNT));
		Team team = answer.getAuthor().getTeam();
		if (team != null)
		{
			String teamName = team.getName();
			tvBestAnswerTeam.setText(Html.fromHtml("来自 <font color=\"blue\">" + teamName +
					"</font>"));
		}


	}

	/**
	 * 初始化listview(普通回答)
	 */
	public void initListView()
	{
		pullToRefreshListView.setAdapter(listViewAdapter = new CommonAdapter<Answer>
				(this, items, R.layout.home_question_detail_item)
		{

			@Override
			public void convert(ViewHolder helper, final Answer item, final int
					position)
			{
				if (item.getIsBest())
				{
					initBestAnswer(item);
					items.remove(position);
					return;
				}
				TextView tvAnswerName = helper.getView(R.id.tv_answer_name);
				TextView tvAnswerContent = helper.getView(R.id.tv_answer_content);
				TextView tvAnswerPostTime = helper.getView(R.id.tv_answer_postTime);
				TextView tvAnswerPraise = helper.getView(R.id.tv_answer_praise);
				TextView tvAnswerDespise = helper.getView(R.id.tv_answer_despise);
				TextView tvAnswerTeam = helper.getView(R.id.tv_answer_team);
				ImageView ivHead = helper.getView(R.id.iv_answer_head);
				//
				String answerHead = item.getAuthor().getHead();
				String content = StringUtils.nullStringFilter(item.getContent());
				String authorName = StringUtils.nullStringFilter(item.getAuthor().getUsername());
				String date = DateUtils.date2string(item.getPostTime());
				String praiseCount = StringUtils.nullIntegerFilter(item.getPraiseCount());
				String despiseCount = StringUtils.nullIntegerFilter(item.getDespiseCount());
				Team team = item.getAuthor().getTeam();
				if (team != null)
				{
					String teamName = team.getName();
					tvAnswerTeam.setText(Html.fromHtml("来自 <font color=\"blue\">" + teamName +
							"</font>"));
				}
				//
				tvAnswerContent.setText(content);
				tvAnswerName.setText(authorName);
				tvAnswerPostTime.setText(date);
				tvAnswerPraise.setText(praiseCount);
				tvAnswerDespise.setText(despiseCount);
				Button button = helper.getView(R.id.btn_adopt);//采纳最佳答案的按钮
				final String answerId = item.getId() + "";
				//如果浏览者与题主是同一个，显示采纳最佳答案的按钮
				if (questionerId.equals(UserServiceImpl.getCurrentUserId(QuestionDetailActivity
						.this)))
				{
					button.setVisibility(View.VISIBLE);
					//如果已经有最佳答案，禁用按钮
					if (hadBestAnswer)
					{
						button.setEnabled(false);
						button.setTextColor(Color.GRAY);
					}
					//设置最佳答案的按钮的单击事件
					button.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							if (NetUtils.isConnected(QuestionDetailActivity.this))
								showConfirmDialog(answerId, position);//显示确认对话框
						}
					});
				}
				// 异步请求设置回答者的头像
				if (answerHead != null)
				{
					if (NetUtils.isConnected(QuestionDetailActivity.this))
						answerService.getAnswerHead(ivHead, answerHead);

				}
				//从sp中取出数据，校验当前用户是否给该回答赞过或者回复过
				Set<String> set = UserServiceImpl.getCurrentUserSetInfo(QuestionDetailActivity
								.this,
						SP_PRAISE_OR_DESPISE_ANSWER_ID);
				for (String str : set)
				{
					if (str.equals(answerId))
					{
						tvAnswerDespise.setEnabled(false);
						tvAnswerDespise.setTextColor(Color.GRAY);
						tvAnswerPraise.setEnabled(false);
						tvAnswerPraise.setTextColor(Color.GRAY);
					}
				}
				//为回答点赞和鄙视
				tvAnswerPraise.setOnClickListener(new OnPraiseAndDespiseListener(answerId,
						AnswerServiceimpl
								.METHOD_NAME_ADD_PRAISE_COUNT, OnPraiseAndDespiseListener
						.KEY_ANSWER_PRAISE, item,
						tvAnswerPraise, tvAnswerDespise));
				tvAnswerDespise.setOnClickListener(new OnPraiseAndDespiseListener(answerId,
						AnswerServiceimpl
								.METHOD_NAME_ADD_DESPISE_COUNT, OnPraiseAndDespiseListener
						.KEY_ANSWER_DESPISE, item,
						tvAnswerPraise, tvAnswerDespise));

			}
		});
		pullToRefreshListView.setOnRefreshListener(new PullToRefreshBase
				.OnRefreshListener2<ListView>()
		{
			@Override
			public void onPullDownToRefresh
					(PullToRefreshBase<ListView>
							 refreshView)
			{
			}

			@Override
			public void onPullUpToRefresh //下拉加载更多
			(PullToRefreshBase<ListView>
					 refreshView)
			{
				questionService.loadQuestionDetailData(handler, questionId, pageNow + "",
						pullToRefreshListView);
				pageNow++;
				loadAnswerType = TYPE_ADD_MORE_DATA;
			}
		});
		setListViewHeightBasedOnChildren(pullToRefreshListView.getRefreshableView());
	}


	/**
	 * 动态设置ListView的高度
	 *
	 * @param listView
	 */
	public void setListViewHeightBasedOnChildren(ListView listView)
	{
		if (listView == null) return;
		ListAdapter listViewAdapter = listView.getAdapter();
		if (listViewAdapter == null)
		{
			return;
		}
		int totalHeight = 0;
//		L.e("count:" + listViewAdapter.getCount());
		for (int i = 0; i < listViewAdapter.getCount(); i++)
		{
			View listItem = listViewAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}
		ViewGroup.LayoutParams params = pullToRefreshListView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listViewAdapter.getCount() -
				1));
		pullToRefreshListView.setLayoutParams(params);
	}

	/**
	 * 其他activity跳回到此activity
	 */
	@Override
	protected void onNewIntent(Intent intent)
	{
		pageNow = 1;
		// 重新向服务器发送请求，更新数据
		DialogUtils.showDefaultDialog(this);
		//向服务器发送添加回复数的请求
		questionService.addBrowseOrReplyCount(questionId, QuestionServiceImpl
				.METHOD_NAME_ADD_REPLY_COUNT);
		questionService.loadQuestionDetailData(handler, questionId, "1", pullToRefreshListView);
	}

	/**
	 * 利用正则表达式解析文本中是否有图片占位符
	 * 如果有，删除占位符并加载图片
	 *
	 * @param content
	 */
	public String parseContent(String content, String userId)
	{
		Pattern pattern = Pattern.compile("/{1}(\\d+.(jpg|png|amr))/{1}");
		Matcher matcher = pattern.matcher(content.toString());
		List<String> paths = new ArrayList<>();
		while (matcher.find())
		{
			String path = Question.getQuestionImagePath(userId) + matcher.group(1);
			L.e("find path:" + path);
			paths.add(path);
			// 移除占位符
			content = content.replace("/" + matcher.group(1) + "/", "");
		}
		if (NetUtils.isConnected(this))
		{
			if (paths.size() > 0)// 找到文件的占位符
			{
				int imageCount = 0;//图片栏图片的数量
				LinearLayout llQuestionImage = (LinearLayout) findViewById(R.id.ll_question_image);
				llQuestionImage.setVisibility(View.VISIBLE);
				for (int i = 0; i < paths.size(); i++)
				{
					if (paths.get(i).endsWith(".amr"))// 如果为语音文件
					{
						RelativeLayout rlPlayVoice = (RelativeLayout) findViewById(R.id
								.rl_play_voice_ui);
						rlPlayVoice.setVisibility(View.VISIBLE);
						TextView tvRecordTime = (TextView) rlPlayVoice.findViewById(R.id
								.tv_recorder_time);
						tvRecordTime.setVisibility(View.GONE);
						final ImageView ivRecorderAnim = (ImageView) rlPlayVoice.findViewById(R.id
								.iv_recorder_anim);
						ivRecorderAnim.setBackgroundResource(R.drawable.adj);
						/**
						 * 声音控件的单击事件，单击播放声音
						 */
						rlPlayVoice.setOnClickListener(new OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								if (voicePath != null && !("".equals(voicePath)))
								{
									//播放语音
									ivRecorderAnim.setBackgroundResource(R.drawable
											.play_recorder_anim);
									AnimationDrawable drawable = (AnimationDrawable) ivRecorderAnim
											.getBackground();
									drawable.start();
									MediaManager.playSound(voicePath, new MediaPlayer
											.OnCompletionListener()
									{
										@Override
										public void onCompletion(MediaPlayer mp)
										{
											ivRecorderAnim.setBackgroundResource(R.drawable.adj);
										}
									});
								}
							}
						});
						String newPath = paths.get(i).replace("image", "voice");
						questionService.loadVoiceFile(handler, newPath);
					} else
					// 图片文件
					{
						ImageView imageView = (ImageView) llQuestionImage.getChildAt(imageCount);
						Log.e("TAG", "imageView:" + imageView);
						questionService.loadQuestionImage(paths.get(i), imageView);
						imageCount++;
					}
				}
			}
		}
		return content;
	}


	/**
	 * 显示设置最佳答案的确认对话框
	 *
	 * @param
	 * @return
	 */
	private void showConfirmDialog(final String answerId, final int location)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(QuestionDetailActivity.this);
		builder.setMessage("确认设置此答案为最佳答案吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				/**
				 * 向服务器发送请求，设置此答案为最佳答案,同时需要维护关联关系
				 * 1:设置此问题已解决
				 * 2:奖励用户悬赏的积分
				 */
				answerService.setBestAnswer(answerId);
				bestAnswer = ((List<Answer>) data.get("answers")).get(location);
				initBestAnswer(bestAnswer);
				//更新界面(将此答案从listview中移除，显示在最佳答案区域)
				items.remove(location);
				listViewAdapter.notifyDataSetChanged();
				hadBestAnswer = true;
				Toast.makeText(QuestionDetailActivity.this, "采纳成功", Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	/**
	 * 使最佳答案的赞和鄙视都失效
	 */
	public void setBestAnswerPraiseAndDespiseDisable()
	{
		tvBestAnswerPraise.setEnabled(false);
		tvBestAnswerPraise.setTextColor(Color.GRAY);
		tvBestAnswerDespise.setEnabled(false);
		tvBestAnswerDespise.setTextColor(Color.GRAY);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.iv_back:
				((Activity) (QuestionDetailActivity.this)).finish();
				break;
			case R.id.iv_title_right://分享
				//打开分享对话框
				openShareDialog();
				break;
			case R.id.tv_answer_questioner://回复按钮
				Intent intent = new Intent(QuestionDetailActivity.this, WriteMessage.class);
				Bundle bundle = new Bundle();
				bundle.putInt("category", WriteMessage.WRITE_ANSWER);
				bundle.putInt("questionId", Integer.parseInt(questionId));
				intent.putExtra("info", bundle);
				startActivity(intent);
				break;
			case R.id.tv_follow:
				String currentUserId = UserServiceImpl.getCurrentUserId(this);
				if (!"".equals(currentUserId) && currentUserId.equals(questionerId))//当前用户为题主
				{
					L.e("user rank activity");
					intent = new Intent(QuestionDetailActivity.this, UserRankActivity.class);
					intent.putExtra("questionId", questionId);
					startActivity(intent);
				} else
				{
					L.e("follow");
					L.e("currentUser:" + UserServiceImpl.getCurrentUserId(this));
					questionService.followQuestion(UserServiceImpl.getCurrentUserId
							(this), Integer.parseInt(questionId), handler);
				}
				break;
		}
	}


	/**
	 * 赞和鄙视的单击事件监听器
	 */
	class OnPraiseAndDespiseListener implements OnClickListener
	{

		public static final String KEY_ANSWER_PRAISE = "answerPraise";
		public static final String KEY_ANSWER_DESPISE = "answerDespise";
		String answerId = "";
		String methodName = "";
		String key = "";
		Answer answer = null;
		TextView bestAnswerView = null;//最佳答案的view
		TextView normalAnswerPraise = null;//普通回复的赞按钮
		TextView normalAnswerDespise = null;//普通回复的鄙视按钮

		public OnPraiseAndDespiseListener(String answerId, String methodName, String key,
		                                  Answer answer,
		                                  TextView normalAnswerPraise, TextView
				                                  normalAnswerDespise)
		{
			this.answerId = answerId;
			this.methodName = methodName;
			this.key = key;
			this.answer = answer;
			this.normalAnswerPraise = normalAnswerPraise;
			this.normalAnswerDespise = normalAnswerDespise;
		}

		public OnPraiseAndDespiseListener(TextView view, String answerId, String methodName)
		{
			this.bestAnswerView = view;
			this.answerId = answerId;
			this.methodName = methodName;
		}

		@Override
		public void onClick(View v)
		{
			if (!NetUtils.isConnected(QuestionDetailActivity.this)) return;
			//向服务器发出鄙视的请求并且更新ui
			answerService.addPraiseOrDespiseCount(answerId, methodName);
			if (answer != null)//代表普通回复的鄙视或者赞的点击事件
			{
				if (key.equals(KEY_ANSWER_PRAISE))
				{
					answer.setPraiseCount(Integer.parseInt(StringUtils.nullIntegerFilter(answer
							.getPraiseCount())) + 1);
				} else
				{
					answer.setDespiseCount(Integer.parseInt(StringUtils.nullIntegerFilter(answer
							.getDespiseCount())) + 1);
				}
				listViewAdapter.notifyDataSetChanged();
				//使赞和鄙视都失效
				normalAnswerPraise.setEnabled(false);
				normalAnswerDespise.setEnabled(false);
			}
			if (bestAnswerView != null)//代表是最佳答案的赞和鄙视的点击事件
			{
				bestAnswerView.setText(Integer.parseInt(bestAnswerView.getText().toString()) + 1 +
						"");
				//使赞和鄙视都失效
				setBestAnswerPraiseAndDespiseDisable();
			}
			//将赞过或者鄙视过的answerId存进sp里
			Set<String> set = UserServiceImpl.getCurrentUserSetInfo(QuestionDetailActivity.this,
					SP_PRAISE_OR_DESPISE_ANSWER_ID);
			set.add(answerId);
			UserServiceImpl.setCurrentUserSetInfo(QuestionDetailActivity.this,
					SP_PRAISE_OR_DESPISE_ANSWER_ID, set);
		}
	}

	/**
	 * 使关注按钮失效
	 */
	public void setFollowDisable()
	{
		tvFollow.setEnabled(false);
		tvFollow.setTextColor(Color.GRAY);
	}
	//------------------------------------分享部分--------------------------------------------------


	/**
	 * 打开分享对话框
	 */
	public void openShareDialog()
	{
		final ShareDialog shareDialog = new ShareDialog(this);
		shareDialog.setCancelButtonOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				shareDialog.dismiss();

			}
		});
		shareDialog.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
			                        int arg2, long arg3)
			{
				HashMap<String, Object> item = (HashMap<String, Object>) arg0.getItemAtPosition
						(arg2);
				if (item.get("ItemText").equals("微博"))
				{
					//2、设置分享内容
					ShareParams sp = new ShareParams();
					sp.setText("朋友们我在 帮我译 遇到一个问题了:" + currentQuestion.getTitle() + ",快来帮我解决吧");
					//分享文本
					sp.setImageUrl("http://kaiduan.ecs04.tomcats.pw/Translation/app_logo.png");
					//网络图片rul
					//3、非常重要：获取平台对象
					Platform sinaWeibo = ShareSDK.getPlatform(SinaWeibo.NAME);
					sinaWeibo.setPlatformActionListener(QuestionDetailActivity.this); // 设置分享事件回调
					// 执行分享
					sinaWeibo.share(sp);

				} else if (item.get("ItemText").equals("微信好友"))
				{
					L.e("微信好友");
					//2、设置分享内容
					ShareParams sp = new ShareParams();
					sp.setShareType(Platform.SHARE_WEBPAGE);//非常重要：一定要设置分享属性
					sp.setTitle("帮我译");  //分享标题
					sp.setText("朋友们我在 帮我译 遇到一个问题了:" + currentQuestion.getTitle() + ",快来帮我解决吧");
					//分享文本
					sp.setImageUrl("http://kaiduan.ecs04.tomcats.pw/Translation/app_logo.png");
					//网络图片rul
					sp.setUrl("http://sharesdk.cn");   //网友点进链接后，可以看到分享的详情
					//3、非常重要：获取平台对象
					Platform wechat = ShareSDK.getPlatform(Wechat.NAME);
					wechat.setPlatformActionListener(QuestionDetailActivity.this); // 设置分享事件回调
					// 执行分享
					wechat.share(sp);


				} else if (item.get("ItemText").equals("朋友圈"))
				{
					L.e("朋友圈");
					//2、设置分享内容
					ShareParams sp = new ShareParams();
					sp.setShareType(Platform.SHARE_WEBPAGE); //非常重要：一定要设置分享属性
					sp.setTitle("帮我译");  //分享标题
					sp.setText("朋友们我在 帮我译 遇到一个问题了: +" + currentQuestion.getTitle() + " ,快来帮我解决吧");
					//分享文本
					sp.setImageUrl("http://kaiduan.ecs04.tomcats.pw/Translation/app_logo.png");
					//网络图片rul
					sp.setUrl("http://sharesdk.cn");   //网友点进链接后，可以看到分享的详情
					//3、非常重要：获取平台对象
					Platform wechatMoments = ShareSDK.getPlatform(WechatMoments.NAME);
					wechatMoments.setPlatformActionListener(QuestionDetailActivity.this); //
					// 设置分享事件回调
					// 执行分享
					wechatMoments.share(sp);

				} else if (item.get("ItemText").equals("QQ"))
				{
					//2、设置分享内容
					ShareParams sp = new ShareParams();
					sp.setTitle("帮我译");
					sp.setText("朋友们我在 帮我译 遇到一个问题了:" + currentQuestion.getTitle() + ",快来帮我解决吧");
					sp.setImageUrl("http://kaiduan.ecs04.tomcats.pw/Translation/app_logo.png");
					//网络图片rul
					sp.setTitleUrl("http://www.baidu.com");  //网友点进链接后，可以看到分享的详情
					//3、非常重要：获取平台对象
					Platform qq = ShareSDK.getPlatform(QQ.NAME);
					qq.setPlatformActionListener(QuestionDetailActivity.this); // 设置分享事件回调
					// 执行分享
					qq.share(sp);

				}
				shareDialog.dismiss();


			}
		});
	}

	@Override
	public void onCancel(Platform arg0, int arg1)
	{//回调的地方是子线程，进行UI操作要用handle处理
		handler.sendEmptyMessage(SHARE_CANCEL);

	}

	@Override
	public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2)
	{//回调的地方是子线程，进行UI操作要用handle处理
		if (arg0.getName().equals(SinaWeibo.NAME))
		{// 判断成功的平台是不是新浪微博
			handler.sendEmptyMessage(SHARE_SINA_WEIBO_SUCCESS);
		} else if (arg0.getName().equals(Wechat.NAME))
		{
			handler.sendEmptyMessage(SHARE_WECHAT_SUCCESS);
		} else if (arg0.getName().equals(WechatMoments.NAME))
		{
			handler.sendEmptyMessage(SHARE_WECHAT_MOMENT_SUCCESS);
		} else if (arg0.getName().equals(QQ.NAME))
		{
			handler.sendEmptyMessage(SHARE_QQ_SUCCESS);
		}

	}

	@Override
	public void onError(Platform arg0, int arg1, Throwable arg2)
	{//回调的地方是子线程，进行UI操作要用handle处理
		arg2.printStackTrace();
		Message msg = new Message();
		msg.what = SHARE_FAILURE;
		msg.obj = arg2.getMessage();
		handler.sendMessage(msg);
	}


}
