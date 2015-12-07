package com.gdxz.zhongbao.client.view.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gdxz.zhongbao.client.Service.QuestionService;
import com.gdxz.zhongbao.client.Service.impl.QuestionServiceImpl;
import com.gdxz.zhongbao.client.common.CommonAdapter;
import com.gdxz.zhongbao.client.common.ViewHolder;
import com.gdxz.zhongbao.client.domain.Question;
import com.gdxz.zhongbao.client.domain.User;
import com.gdxz.zhongbao.client.utils.DatabaseUtils;
import com.gdxz.zhongbao.client.utils.DialogUtils;
import com.gdxz.zhongbao.client.utils.L;
import com.gdxz.zhongbao.client.utils.NetUtils;
import com.gdxz.zhongbao.client.utils.StringUtils;
import com.gdxz.zhongbao.client.view.activity.HomeActivity;
import com.gdxz.zhongbao.client.view.activity.QuestionDetailActivity;
import com.gdxz.zhongbao.client.view.activity.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class LazyFragment extends Fragment
{
	public static final String QUESTION_CACHE = "questionCache";

	// fragment是否可见
	protected boolean isVisible;
	PullToRefreshListView mPullRefreshListView;

	// 当前显示的总记录数
	int totalItem = 0;
	// item上的各个view

	// 存放view上内容的list
	List<String> questioners;
	List<String> isSolve;
	List<String> titles;
	List<String> contents;
	List<String> browseCount;
	List<String> replyCount;
	// item中的隐藏数据，用于标识该question，从服务器取得问题的详细信息
	List<String> questionIds;

	// 进度条对话框
	ProgressDialog progressDialog;

	// listview的adapter
	CommonAdapter<Map<String, String>> listViewAdapter;

	// listview的item
	List<Map<String, String>> items = new ArrayList<>();

	public QuestionService questionService = new QuestionServiceImpl();

	// 从服务端返回的数据
	List<Question> list;
	// 标识question

	Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case HomeActivity.REFRESH_DATA:
					list = (List<Question>) msg.obj;
					addData(HomeActivity.REFRESH_DATA);
					DialogUtils.closeProgressDialog();
					//加入数据库缓存,判断当前是否能联网，无联网则说明从缓存中取出的数据，并不加入缓存
					if (NetUtils.isConnected(getActivity()))
					{
						DbUtils dbUtils = DatabaseUtils.getDbUtils(getActivity());
						try
						{
							for (Question question : list)
							{
								User author = question.getAuthor();
								dbUtils.saveOrUpdate(author);
								dbUtils.saveOrUpdate(question);
							}
						} catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					break;
				case HomeActivity.ADD_MORE_DATA:
					list = (List<Question>) msg.obj;
					addData(HomeActivity.ADD_MORE_DATA);
					break;
				case HomeActivity.SERVER_ERROR:
					Toast.makeText(getActivity(), "服务器错误", Toast.LENGTH_SHORT).show();
					DialogUtils.closeProgressDialog();
					break;
				case HomeActivity.NETWORK_NOT_CONNECT:
					Toast.makeText(getActivity(), "网络未连接", Toast.LENGTH_SHORT).show();
					DialogUtils.closeProgressDialog();
					break;
				default:
					break;
			}
		}
	};

	/**
	 * 在这里实现Fragment数据的缓加载.
	 *
	 * @param isVisibleToUser
	 */
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		if (getUserVisibleHint())
		{
			isVisible = true;
			onVisible();
		} else
		{
			isVisible = false;
			onInvisible();
		}
	}

	protected void onVisible()
	{
		lazyLoad();
	}

	public abstract void lazyLoad();

	protected abstract void onInvisible();


	/**
	 * 添加数据
	 *
	 * @param currentTask 当前任务
	 *                    刷新（REFRESH_DATA）
	 *                    添加（ADD_MORE_DATA）
	 */
	public void addData(int currentTask)
	{
		if (list != null && list.size() > 0)
		{
			if (currentTask == HomeActivity.REFRESH_DATA)
			{
				totalItem = 0;
				items = new ArrayList<>();
				questioners = new ArrayList<>();
				questionIds = new ArrayList<>();
				isSolve = new ArrayList<>();
				titles = new ArrayList<>();
				contents = new ArrayList<>();
				browseCount = new ArrayList<>();
				replyCount = new ArrayList<>();
				questionIds = new ArrayList<>();
			}
			for (int i = 0; i < list.size(); i++)
			{
				Question question = list.get(i);
				questioners.add(question.getAuthor().getUsername());
				isSolve.add(question.getIsSolve() ? "已解决" :
						question.getRewardAmount() + "");
				String content = question.getContent();
				content = filerString(content);
				questionIds.add(question.getId() + "");
				// 如果内容长度大于25，大于25的部分用...代替
				if (content.length() > 25)
				{
					contents.add(content.substring(0, 25) + "...");
				} else
				{
					contents.add(content);
				}
				titles.add(question.getTitle());
				browseCount.add(StringUtils.nullIntegerFilter(question.getBrowseCount()));
				replyCount.add(StringUtils.nullIntegerFilter(question.getReplyCount()));
			}
			// 这里要判断一下当前任务是刷新还是添加，如果是刷新，开始下标为0，如果是添加，开始下标为总的条目数
			int j = (currentTask == HomeActivity.REFRESH_DATA ? 0 : totalItem);
			for (int i = j; i < list.size() + j; i++)
			{
				Map<String, String> item = new HashMap<>();
				item.put("questioners", questioners.get(i));
				item.put("isSolve", isSolve.get(i));
				item.put("titles", titles.get(i));
				item.put("contents", contents.get(i));
				item.put("browseCount", browseCount.get(i));
				item.put("replyCount", replyCount.get(i));
				item.put("questionIds", questionIds.get(i));
				items.add(item);
			}
			if (currentTask == HomeActivity.REFRESH_DATA)// 如果当前任务为刷新，重新初始化listview
			{
				L.e("init");
				initListView();
			} else
			// 如果当前任务为添加，更新listview
			{
				// System.out.println(items.size());
				listViewAdapter.notifyDataSetChanged();
			}
			totalItem += 10;
		}
	}


	/**
	 * 加载listview
	 */
	public void initListView()
	{
//		actualListView = mPullRefreshListView.getRefreshableView();
		listViewAdapter = new CommonAdapter<Map<String, String>>(
				getActivity(), items, R.layout.home_question_item)
		{
			@Override
			public void convert(ViewHolder helper, final Map<String, String> item,
			                    final int position)
			{
				LinearLayout wrapper = (LinearLayout) helper
						.getConvertView();
				// 得到view
				TextView tvQuestioner = helper.getView(R.id.tv_questioner);
				TextView tvSolve = helper.getView(R.id.tv_solve);
				TextView tvTitle = helper.getView(R.id.tv_title);
				TextView tvContent = helper.getView(R.id.tv_content);
				TextView tvBrowseCount = helper.getView(R.id.tv_browse_count);
				TextView tvReplyCount = helper.getView(R.id.tv_reply_count);
				ImageView ivSolve = helper.getView(R.id.iv_solve_icon);
				ImageView ivHaveSolve = helper.getView(R.id.iv_have_solve);
				// 设置view的文本
				tvQuestioner.setText(item.get("questioners"));
				String isSolve = item.get("isSolve");
				if (isSolve.equals("已解决"))
				{
					tvSolve.setVisibility(View.GONE);
					ivSolve.setVisibility(View.GONE);
					ivHaveSolve.setVisibility(View.VISIBLE);
					ivHaveSolve.setBackgroundResource(R.drawable.have_solve);
				} else
				{
					ivHaveSolve.setVisibility(View.GONE);
					ivSolve.setVisibility(View.VISIBLE);
					tvSolve.setVisibility(View.VISIBLE);
					tvSolve.setText(item.get("isSolve"));
					ivSolve.setBackgroundResource(R.drawable.money);
				}
				tvTitle.setText(item.get("titles"));
				tvContent.setText(item.get("contents"));
				final String browseCount = item.get("browseCount");
				tvBrowseCount.setText(browseCount);
				tvReplyCount.setText(item.get("replyCount"));
				// 得到question的id
				final String questionId = item.get("questionIds");
				// 为每个item添加单击事件，点击进入问题详细页面
				wrapper.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
//						tvBrowseCount.setText("0");tvBrowseCount为全局变量，不可以这样修改值
						//向服务器发送一个浏览了该帖子的请求
						questionService.addBrowseOrReplyCount(questionId, QuestionServiceImpl
								.METHOD_NAME_ADD_BROWSE_COUNT);
						Intent intent = new Intent(getActivity(), QuestionDetailActivity.class);
						intent.putExtra("questionId", questionId);
						startActivity(intent);
						item.put("browseCount", Integer.parseInt(browseCount) + 1 + "");
						listViewAdapter.notifyDataSetChanged();
					}
				});
			}
		};
		mPullRefreshListView
				.setAdapter(listViewAdapter);
	}

	/**
	 * 显示加载对话框并隐藏listview
	 *
	 * @param title
	 */
	public void showProgressDialog(String title)
	{
		mPullRefreshListView.setVisibility(View.INVISIBLE);
		DialogUtils.showProgressDialog(title, "系统玩命加载中", getActivity());
	}

	/**
	 * 隐藏加载对话框并显示listview
	 */
	public void hideProgressDialog()
	{
		mPullRefreshListView.setVisibility(View.VISIBLE);
		DialogUtils.closeProgressDialog();
	}

	public boolean isNetConnected()
	{
		//如果网络未连接，则从缓存取出数据
		if (!NetUtils.isConnected(getActivity()))
		{
			mPullRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
			DbUtils dbUtils = DatabaseUtils.getDbUtils(getActivity());
			try
			{
				list = dbUtils.findAll(Selector.from(Question.class).orderBy("postTime", true)
						.limit(10));
				L.e("get from cache,data size:" + list.size());
			} catch (DbException e)
			{
				e.printStackTrace();
			}
			addData(HomeActivity.REFRESH_DATA);
			return false;
		}
		return true;
	}

	@Override
	public void onResume()
	{
		if (NetUtils.isConnected(getActivity()))
		{
			if (mPullRefreshListView != null)
			{
				mPullRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
			}
		}
		super.onResume();
	}

	public String filerString(String content)
	{
		Pattern pattern = Pattern.compile("/{1}(\\d+.(jpg|png|amr))/{1}");
		Matcher matcher = pattern.matcher(content.toString());
		List<String> paths = new ArrayList<>();
		while (matcher.find())
		{
			// 移除占位符
			content = content.replace("/" + matcher.group(1) + "/", "");
		}
		return content;
	}
}
