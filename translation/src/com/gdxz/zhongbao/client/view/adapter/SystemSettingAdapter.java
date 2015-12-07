package com.gdxz.zhongbao.client.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gdxz.zhongbao.client.domain.SystemConfigItem;
import com.gdxz.zhongbao.client.view.activity.R;

import java.util.List;

import me.xiaopan.switchbutton.SwitchButton;

/**
 * Created by Chean_antao on 2015/8/9.
 */
public class SystemSettingAdapter extends BaseAdapter
{
	Context mContext;

	private List<SystemConfigItem> mDatas;

	@Override
	public int getCount()
	{
		return mDatas.size();
	}

	public SystemSettingAdapter(Context context, List<SystemConfigItem> datas)
	{
		mContext = context;
		mDatas = datas;
	}


	@Override
	public int getViewTypeCount()
	{
		return 2;
	}

	@Override
	public int getItemViewType(int position)
	{
		if (position < 4) return 0;
		return 1;
	}

	@Override
	public Object getItem(int position)
	{
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final ViewHolder holder;
		final SystemConfigItem configItem = mDatas.get(position);
		if (convertView == null)
		{
			holder = new ViewHolder();
			LayoutInflater inflater = LayoutInflater.from(mContext);
			if (position < 4)//
			{
				convertView = inflater.inflate(R.layout.lv_item_2_tv, null);
				holder.tvConfigName = (TextView) convertView.findViewById(R.id.tv_config_name);
				holder.tvConfigValue = (TextView) convertView.findViewById(R.id.tv_config_value);
			} else
			{
				convertView = inflater.inflate(R.layout.lv_item_switch, null);
				holder.switchButton = (SwitchButton) convertView.findViewById(R.id.switch_button);

			}
			convertView.setTag(holder);
		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		if (position < 4)
		{
			holder.tvConfigName.setText(configItem.getConfigName());
			holder.tvConfigValue.setText((String) configItem.getConfigValue());
		} else
		{
			holder.switchButton.setText(configItem.getConfigName());
			boolean isChecked = (boolean) configItem.getConfigValue();
			holder.switchButton.setChecked(isChecked);
		}
		return convertView;
	}


	public class ViewHolder
	{
		public TextView tvConfigName;
		public TextView tvConfigValue;
		public SwitchButton switchButton;
	}
}
