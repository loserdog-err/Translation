package com.gdxz.zhongbao.client.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.gdxz.zhongbao.client.utils.DialogUtils;
import com.gdxz.zhongbao.client.utils.NetUtils;
import com.gdxz.zhongbao.client.view.activity.HomeActivity;
import com.gdxz.zhongbao.client.view.activity.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

/**
 * 今日热榜fragment
 *
 * @author chenantao
 */
public class HomeTodayHotRankingFragment extends LazyFragment
{
	// 标记当前在第几页
	public int pageNow = 1;

	// 标志位，标志已经初始化完成。
	private boolean isPrepared;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// 初始化数据
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		inflater = getActivity().getLayoutInflater();
		View wrapper = inflater.inflate(R.layout.home_question_ui, container, false);
		mPullRefreshListView = (PullToRefreshListView) wrapper
				.findViewById(R.id.lv_item);
		mPullRefreshListView.setMode(Mode.BOTH);
		mPullRefreshListView.setScrollingWhileRefreshingEnabled(true);
		// 监听列表被刷新时事件.
		mPullRefreshListView
				.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>()
				{

					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<ListView> refreshView)// 下拉刷新
					{
						pageNow = 1;
						questionService.loadQuestionData(handler, pageNow + "",
								HomeActivity.TODAY_HOT_RANKING + "",
								HomeActivity.REFRESH_DATA, mPullRefreshListView);
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<ListView> refreshView)// 下拉加载数据
					{
						// pageNow+=1;
						questionService.loadQuestionData(handler, (pageNow += 1)
										+ "", HomeActivity.TODAY_HOT_RANKING + "",
								HomeActivity.ADD_MORE_DATA, mPullRefreshListView);
					}

				});
		if (!NetUtils.isConnected(getActivity()))//如果网络未连接，禁用加载
		{
			mPullRefreshListView.setMode(Mode.DISABLED);
		}
		isPrepared = true;
		lazyLoad();
		//
		return wrapper;
	}


	@Override
	public void lazyLoad()
	{
		if (!isPrepared || !isVisible)
		{
			return;
		}
		if(!isNetConnected())return;
		pageNow = 1;
		mPullRefreshListView.setMode(Mode.BOTH);
		mPullRefreshListView.setScrollingWhileRefreshingEnabled(true);
		if (NetUtils.isConnected(getActivity()))
		{
			DialogUtils.showProgressDialog("今日热榜","数据玩命加载中",getActivity());
			questionService.loadQuestionData(handler, pageNow + "",
					HomeActivity.TODAY_HOT_RANKING + "", HomeActivity.REFRESH_DATA,
					mPullRefreshListView);
		} else
		{
			mPullRefreshListView.onRefreshComplete();
			handler.sendEmptyMessage(HomeActivity.NETWORK_NOT_CONNECT);
		}


	}

	@Override
	protected void onInvisible()
	{
	}

	@Override
	public void onDestroyView()
	{
		isPrepared = false;
		super.onDestroyView();
	}

}
