package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.gdxz.zhongbao.client.Service.OrderService;
import com.gdxz.zhongbao.client.Service.impl.OrderServiceImpl;
import com.gdxz.zhongbao.client.Service.impl.UserServiceImpl;
import com.gdxz.zhongbao.client.common.CommonAdapter;
import com.gdxz.zhongbao.client.common.ViewHolder;
import com.gdxz.zhongbao.client.domain.Orders;
import com.gdxz.zhongbao.client.utils.DateUtils;
import com.gdxz.zhongbao.client.utils.DensityUtils;
import com.gdxz.zhongbao.client.utils.DialogUtils;

import java.util.List;

/**
 * Created by Chean_antao on 2015/8/12.
 * 流水账的activity
 */
public class GeneralJournalActivity extends Activity
{
	public static final int MSG_LOAD_ORDER_COMPLETE = 0;
	public static final int MSG_LOAD_ORDER_ERROR = 1;

	private List<Orders> mDatas;
	private CommonAdapter adapter;
	private SwipeMenuListView mListView;

	//title
	private ImageView ivBack;
	private TextView tvTitle;

	private OrderService orderService = new OrderServiceImpl();

	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case MSG_LOAD_ORDER_COMPLETE:
					mDatas = (List<Orders>) msg.obj;
					initListView();
					DialogUtils.closeProgressDialog();
					break;
				case MSG_LOAD_ORDER_ERROR:
					Toast.makeText(GeneralJournalActivity.this, "啊哦，加载不到数据", Toast.LENGTH_SHORT)
							.show();
					DialogUtils.closeProgressDialog();
					break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.general_journal_ui);
		initView();
		orderService.getOrderByUserId(UserServiceImpl.getCurrentUserId(this), handler);
		DialogUtils.showDefaultDialog(this);
	}

	private void initView()
	{
		mListView = (SwipeMenuListView) findViewById(R.id.listView);
		ivBack = (ImageView) findViewById(R.id.iv_back);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvTitle.setText("流水明细");
		ivBack.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				GeneralJournalActivity.this.finish();
			}
		});
	}

	private void initListView()
	{
		mListView.setAdapter(adapter = new CommonAdapter<Orders>(this, mDatas, R.layout
				.general_journal_order_item)
		{
			@Override
			public void convert(ViewHolder helper, Orders item, int position)
			{
				helper.setText(R.id.tv_order_id, Html.fromHtml("订单号:" + "<font color='blue'>" +
						item.getOrderId() + "</font>"));
				helper.setText(R.id.tv_price, Html.fromHtml("<font color='orange'>￥</font> " +
						item.getPrice()));
				helper.setText(R.id.tv_order_time, DateUtils.date2string(item.getOrderTime(),
						"yyyy-MM-dd HH:mm:ss"));
				helper.setText(R.id.tv_good_name, "商品名:" + item.getGoodName());

			}
		});
		// step 1. create a MenuCreator
		SwipeMenuCreator creator = new SwipeMenuCreator()
		{

			@Override
			public void create(SwipeMenu menu)
			{
				// create "delete" item
				SwipeMenuItem deleteItem = new SwipeMenuItem(
						getApplicationContext());
				// set item background
				deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
						0x3F, 0x25)));
				// set item width
				deleteItem.setWidth(DensityUtils.dp2px(GeneralJournalActivity.this, 90));
				// set a icon
				deleteItem.setIcon(R.drawable.ic_delete);
				// add to menu
				menu.addMenuItem(deleteItem);
			}
		};
		// set creator
		mListView.setMenuCreator(creator);
		// step 2. listener item click event
		mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(int position, SwipeMenu menu, int index)
			{
				switch (index)
				{
					case 0://删除
						deleteOrder(mDatas.get(position));
						break;
				}
				return false;
			}
		});
	}

	private void deleteOrder(final Orders orders)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("确定删除该订单吗").setNegativeButton("取消", new DialogInterface
				.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		}).setPositiveButton("确定", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				orderService.deleteOrderById(orders.getId());
				dialog.dismiss();
				//更新listview
				mDatas.remove(orders);
				adapter.notifyDataSetChanged();
			}
		});
		builder.create().show();
	}


}
