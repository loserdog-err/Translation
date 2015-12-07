package com.gdxz.zhongbao.client.view.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gdxz.zhongbao.client.Service.AssetInfoService;
import com.gdxz.zhongbao.client.Service.impl.AssetInfoServiceImpl;
import com.gdxz.zhongbao.client.Service.impl.UserServiceImpl;
import com.gdxz.zhongbao.client.domain.AssetInfo;
import com.gdxz.zhongbao.client.utils.DateUtils;
import com.gdxz.zhongbao.client.utils.DialogUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;

/**
 * Created by Chean_antao on 2015/8/12.
 * 资产分析的activity
 */
public class AssetsAnalysisActivity extends ActionBarActivity
{

	private ColumnChartView chart;
	private List<String> rawValue;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_column_chart);
		chart = (ColumnChartView) findViewById(R.id.chart);
		if (savedInstanceState == null)
		{
			getSupportFragmentManager().beginTransaction().add(R.id.container, new
					PlaceholderFragment()).commit();
		}
	}

	/**
	 * A fragment containing a column chart.
	 */
	public static class PlaceholderFragment extends Fragment
	{

		private static final int DEFAULT_DATA = 0;
		private static final int SUBCOLUMNS_DATA = 1;
		private static final int STACKED_DATA = 2;
		private static final int NEGATIVE_SUBCOLUMNS_DATA = 3;
		private static final int NEGATIVE_STACKED_DATA = 4;

		private ColumnChartView chart;
		private ColumnChartData data;
		private boolean hasAxes = true;
		private boolean hasAxesNames = true;
		private boolean hasLabels = false;
		private boolean hasLabelForSelected = false;
		private int dataType = DEFAULT_DATA;

		public static final int MSG_LOAD_ASSET_INFO_COMPLETE = 0;
		public static final int MSG_LOAD_ASSET_INFO_FALIURE = 1;
		//业务组件
		AssetInfoService assetInfoService = new AssetInfoServiceImpl();
		//资产信息对象
		AssetInfo assetInfo;
		//三个textview(今日消费，本月消费，总消费)
		TextView tvTodayPay;
		TextView tvMonthPay;
		TextView tvTotalPay;
		//title
		ImageView ivBack;
		TextView tvTitle;

		public PlaceholderFragment()
		{
		}

		private Handler handler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
					case MSG_LOAD_ASSET_INFO_COMPLETE:
						assetInfo = (AssetInfo) msg.obj;
						generateDefaultData();
						DialogUtils.closeProgressDialog();
						break;
					case MSG_LOAD_ASSET_INFO_FALIURE:
						Toast.makeText(getActivity(), "啊哦，加载不到数据", Toast.LENGTH_SHORT).show();
						DialogUtils.closeProgressDialog();
				}
				super.handleMessage(msg);
			}
		};

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
				savedInstanceState)
		{
			setHasOptionsMenu(true);
			View rootView = inflater.inflate(R.layout.fragment_column_chart, container, false);
			ivBack = (ImageView) rootView.findViewById(R.id.iv_back);
			tvTitle = (TextView) rootView.findViewById(R.id.tv_title);
			ivBack.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					getActivity().finish();
				}
			});
			tvTitle.setText("资产分析");
			chart = (ColumnChartView) rootView.findViewById(R.id.chart);
			tvTodayPay = (TextView) rootView.findViewById(R.id.tv_today_pay);
			tvMonthPay = (TextView) rootView.findViewById(R.id.tv_month_pay);
			tvTotalPay = (TextView) rootView.findViewById(R.id.tv_total_pay);
			chart.setOnValueTouchListener(new ValueTouchListener());
			assetInfoService.getAssetInfo(UserServiceImpl.getCurrentUserId(getActivity()),
					handler);
			DialogUtils.showDefaultDialog(getActivity());
			return rootView;
		}


		private void generateDefaultData()
		{
			int numSubcolumns = 1;
			int numColumns = 7;
			// Column can have many subcolumns, here by default I use 1 subcolumn in each of 8
			// columns.
			float[] rowDatas = new float[]{assetInfo.getSixAgoPay(), assetInfo.getFiveAgoPay(),
					assetInfo.getFourAgoPay(), assetInfo.getThreeAgoPay(), assetInfo
					.getTwoAgoPay(), assetInfo.getOneAgoPay(),
					assetInfo.getTodayPay()};
			List<Column> columns = new ArrayList<Column>();
			List<SubcolumnValue> values;
			List<AxisValue> axisValues = new ArrayList<AxisValue>();
			for (int i = 0; i < numColumns; ++i)
			{
				values = new ArrayList<SubcolumnValue>();
				for (int j = 0; j < numSubcolumns; ++j)
				{
					values.add(new SubcolumnValue(rowDatas[i], ChartUtils
							.pickColor()));
				}
				axisValues.add(new AxisValue(i).setLabel(generateRowValue(i)));
				Column column = new Column(values);
				column.setHasLabels(hasLabels);
				column.setHasLabelsOnlyForSelected(hasLabelForSelected);
				columns.add(column);
			}
			data = new ColumnChartData(columns);
			if (hasAxes)
			{
				Axis axisX = new Axis(axisValues).setHasLines(true);
				Axis axisY = new Axis().setHasLines(true);
				if (hasAxesNames)
				{
					axisX.setName("一周消费");
					axisY.setName("RMB(元)");
				}
				data.setAxisXBottom(axisX);
				data.setAxisYLeft(axisY);
				data.setBaseValue(0);
				data.setStacked(true);
			} else
			{
				data.setAxisXBottom(null);
				data.setAxisYLeft(null);
			}
			chart.setColumnChartData(data);
			//初始化textview信息(今日消费，本月消费，总消费)
			tvTodayPay.setText(Html.fromHtml("<font color='" + ChartUtils.pickColor() +
					"'>今日消费:</font>" + assetInfo.getTodayPay() + ""));
			tvMonthPay.setText(Html.fromHtml("<font color='" + ChartUtils.pickColor() +
					"'>本月消费:</font>" + assetInfo.getMonthPay() + ""));
			tvTotalPay.setText(Html.fromHtml("<font color='" + ChartUtils.pickColor() +
					"'>历史消费:</font>" + assetInfo.getTotalPay() + ""));

		}

		public String generateRowValue(int position)
		{
			Date date = new Date(new Date().getTime() - 24 * (6 - position) * 60 * 60 * 1000);
			return DateUtils.date2string(date, "MM-dd");
		}

		private class ValueTouchListener implements ColumnChartOnValueSelectListener
		{

			@Override
			public void onValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value)
			{
				Toast.makeText(getActivity(), "Selected: " + value.getValue(), Toast.LENGTH_SHORT)
						.show();
			}

			@Override
			public void onValueDeselected()
			{
				// TODO Auto-generated method stub
			}

		}

	}
}
