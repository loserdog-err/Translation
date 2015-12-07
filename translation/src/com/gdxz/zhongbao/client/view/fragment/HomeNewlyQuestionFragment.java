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

public class HomeNewlyQuestionFragment extends LazyFragment
{
	// 标志位，标志已经初始化完成。
	protected boolean isPrepared = false;
	// 标记当前在第几页
	public int pageNow = 1;


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
								HomeActivity.NEWLY_QUESTION + "",
								HomeActivity.REFRESH_DATA, mPullRefreshListView);
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<ListView> refreshView)// 下拉加载数据
					{
						// pageNow+=1;
						questionService.loadQuestionData(handler, (pageNow += 1)
										+ "", HomeActivity.NEWLY_QUESTION + "",
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
//		((HomeActivity) getActivity()).resetUI();
		if (!isPrepared || !isVisible)
		{
			return;
		}
//		Log.e("TAG", "load");
		if (!isNetConnected()) return;
		pageNow = 1;
		totalItem = 0;
		mPullRefreshListView.setMode(Mode.BOTH);
		mPullRefreshListView.setScrollingWhileRefreshingEnabled(true);
		DialogUtils.showProgressDialog("最新提问", "系统玩命加载中", getActivity());
		questionService.loadQuestionData(handler, pageNow + "",
				HomeActivity.NEWLY_QUESTION + "", HomeActivity.REFRESH_DATA,
				mPullRefreshListView);
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
