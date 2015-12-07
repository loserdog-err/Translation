package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.gdxz.zhongbao.client.Service.UserService;
import com.gdxz.zhongbao.client.Service.impl.UserServiceImpl;
import com.gdxz.zhongbao.client.common.CommonAdapter;
import com.gdxz.zhongbao.client.common.MyApplication;
import com.gdxz.zhongbao.client.common.ViewHolder;
import com.gdxz.zhongbao.client.domain.User;
import com.gdxz.zhongbao.client.utils.DateUtils;
import com.gdxz.zhongbao.client.utils.DialogUtils;
import com.gdxz.zhongbao.client.utils.StringUtils;
import com.gdxz.zhongbao.client.view.customView.CustomDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户详细信息设置界面
 *
 * @author chenantao
 */
public class PersonalDetailInfoSettingActivity extends BaseActivity
{
	// handler的一系列常量
	// public static final int HIDE_PROGRESS_DIALOG = 0;
	public static final int SET_USER_DETAIL_INFO = 1;
	public static final int UPDATE_USER_DETAIL_INFO_SUCCESS = 2;
	public static final int UPDATE_USER_DETAIL_INFO_ERROR = 3;
	// 选项在listview中的位置
	public static final int POSITION_REAL_NAME = 0;
	public static final int POSITION_GENDER = 1;
	public static final int POSITION_AGE = 2;
	public static final int POSITION_BIRTHDAY = 3;
	public static final int POSITION_EMAIL = 4;
	public static final int POSITION_MOBILE_PHONE = 5;
	public static final int POSITION_MOOD = 6;
	public static final int POSITION_DESCRIPTION = 7;

	// 初始化数据
	List<String> tvItemTitles;
	// private String[] tvItemTitles = { "真实姓名", "性别", "年龄", "生日", "邮箱", "手机号",
	// "心情",
	// "个人描述" };
	List<String> etItemInfos;
	// { "陈岸涛", "男", "21", "1994-07-07",
	// "2035611205@qq.com", "18819433192", "最近心情有点烦", "谦虚和谐有爱" };

	private List<Map<String, String>> items;
	private Map<String, String> item;

	// listview
	private ListView listview;

	// 完成按钮textview
	TextView tvRightTitle;
	// 后退按钮
	ImageView ivBack;

	// service组件
	UserService userService = new UserServiceImpl();

	// 服务端返回的user信息
	private User userInfo;

	// 显示错误信息的textview
	TextView tvErrorInfo;

	// item事件处理所需要的对象
	private Calendar calendar;
	private DatePickerDialog dateDialog;
	LayoutInflater inflater;

	// 两个list，在初始化listview时进行赋值，用于比较用户是否修改了信息
	List<String> oldList = new ArrayList<>();// 未修改的list对象
	List<String> newList = new ArrayList<>();// 修改后的list对象

	// 布局根结点，用于控制显示或隐藏布局
	ScrollView svWrapper;

	// 进度条对话框
	ProgressDialog progressDialog;
	private Handler handler = new Handler()
	{

		public void handleMessage(android.os.Message msg)
		{
			switch (msg.what)
			{
				case SET_USER_DETAIL_INFO:
					userInfo = (User) msg.obj;
					initData();
					initListView();
					hideProgressDialog();
					break;
				case UPDATE_USER_DETAIL_INFO_SUCCESS:
					// 将newlist的值赋值给oldlist
					for (int i = 0; i < newList.size(); i++)
					{
						oldList.set(i, newList.get(i));
					}
					Toast.makeText(PersonalDetailInfoSettingActivity.this, "更新成功", Toast.LENGTH_SHORT)
							.show();
					//重置更新按钮
					tvRightTitle.setEnabled(false);
					break;
				case UPDATE_USER_DETAIL_INFO_ERROR:
					Toast.makeText(PersonalDetailInfoSettingActivity.this, "更新失败", Toast.LENGTH_SHORT);
				default:
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.personal_detail_info_setting_ui);
		MyApplication.getInstance().addActivity(this);
		initActionBar();
		// 进度条
		svWrapper = (ScrollView) findViewById(R.id.sv_wrapper);
		svWrapper.setVisibility(View.INVISIBLE);
		tvErrorInfo = (TextView) findViewById(R.id.tv_errorInfo);
		DialogUtils.showProgressDialog("加载", "数据玩命加载中",this);
		getUserDetailInfo();
	}

	/**
	 * 隐藏进度对话框
	 */
	public void hideProgressDialog()
	{
		DialogUtils.closeProgressDialog();
		svWrapper.setVisibility(View.VISIBLE);
	}

	/**
	 * 初始化数据
	 */
	public void initData()
	{
		tvItemTitles = new ArrayList<>();
		tvItemTitles.add("真实姓名");
		tvItemTitles.add("性别");
		tvItemTitles.add("年龄");
		tvItemTitles.add("生日");
		tvItemTitles.add("邮箱");
		tvItemTitles.add("手机号");
		tvItemTitles.add("心情");
		tvItemTitles.add("个人描述");
		etItemInfos = new ArrayList<>();
//	String realName = userInfo.getRealName();
		etItemInfos.add(StringUtils.nullStringFilter(userInfo.getRealName()));
		Integer gender = userInfo.getGender();
		etItemInfos.add(gender == null ? "" : gender == 0 ? "男" : "女");
		etItemInfos.add(userInfo.getAge() == null ? "" : userInfo.getAge() + "");
		etItemInfos.add(userInfo.getBirthday() == null ? "" : DateUtils.date2string(
				userInfo.getBirthday(), "yyyy-MM-dd"));
		etItemInfos.add(StringUtils.nullStringFilter(userInfo.getEmail()));
		etItemInfos.add(StringUtils.nullStringFilter(userInfo.getMobilePhone()));
		etItemInfos.add(StringUtils.nullStringFilter(userInfo.getMood()));
		etItemInfos.add(StringUtils.nullStringFilter(userInfo.getDescription()));
	}

	/**
	 * 设置用户详细信息
	 */
	public void getUserDetailInfo()
	{
		String userId = UserServiceImpl.getCurrentUserId(this);
		userService.getUserInfo(userId, handler, true);
	}

	/**
	 * 加载actionBar
	 * 1：显示标题
	 * 2:显示完成按钮(不可用状态)
	 */
	public void initActionBar()
	{
		TextView tvTitle = (TextView) findViewById(R.id.tv_title);
		ivBack = (ImageView) findViewById(R.id.iv_back);
		tvRightTitle = (TextView) findViewById(R.id.tv_title_right);
		tvRightTitle.setVisibility(View.VISIBLE);
		tvTitle.setText("详细信息");
		tvRightTitle.setText("完成");
		tvRightTitle.setEnabled(false);
		// 后退事件
		ivBack.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				((Activity) PersonalDetailInfoSettingActivity.this).finish();
			}
		});
		// 完成更新信息的按钮
		tvRightTitle.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// 将更新后的list转换成map对象.map的key需要跟user的字段名对应
				Map<String, String> userInfo = new HashMap<>();
				userInfo.put("realName", newList.get(0));
				userInfo.put("gender", newList.get(1));
				userInfo.put("birthday", newList.get(3));
				userInfo.put("email", newList.get(4));
				userInfo.put("mobilePhone", newList.get(5));
				userInfo.put("mood", newList.get(6));
				userInfo.put("description", newList.get(7));
				userInfo.put("id",
						UserServiceImpl.getCurrentUserId(PersonalDetailInfoSettingActivity.this));
				// 执行输入校验
				userService = new UserServiceImpl();
				userService.updateUserInfo(userInfo, handler, tvErrorInfo);
			}
		});

	}

	/**
	 * 初始化listview
	 */
	public void initListView()
	{
		items = new ArrayList<>();
		listview = (ListView) findViewById(R.id.lv_detail_info_setting);
		for (int i = 0; i < etItemInfos.size(); i++)
		{
			item = new HashMap<>();
			item.put("tvItemTitle", tvItemTitles.get(i));
			item.put("etItemInfo", etItemInfos.get(i));
			items.add(item);
		}
		listview.setAdapter(new CommonAdapter<Map<String, String>>(
				getApplicationContext(), items,
				R.layout.personal_detail_info_setting_item)
		{

			@Override
			public void convert(ViewHolder helper, Map<String, String> item,
			                    final int position)
			{
				final LinearLayout wrapper = (LinearLayout) helper.getConvertView();
				TextView tvItemTitle = helper.getView(R.id.tv_item_title);
				final EditText etItemInfo = helper.getView(R.id.tv_item_info);
				newList.add(item.get("etItemInfo"));
				oldList.add(item.get("etItemInfo"));
				tvItemTitle.setText(item.get("tvItemTitle"));
				etItemInfo.setText(item.get("etItemInfo"));
				etItemInfo.addTextChangedListener(new TextWatcher()
				{
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after)
					{
					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count)
					{
						newList.set(position, s.toString());
						checkIsChange(position);
					}

					@Override
					public void afterTextChanged(Editable s)
					{
					}
				});
				// 给性别选项设置单击事件，打开选择性别对话框
				if (position == POSITION_GENDER)
				{
					final TextView tvItemInfo = et2tv(wrapper, etItemInfo);
					wrapper.setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(View v)
						{
							inflater = LayoutInflater
									.from(PersonalDetailInfoSettingActivity.this);
							View view = inflater.inflate(
									R.layout.select_gender_view, null);
							TextView tvMan = (TextView) view
									.findViewById(R.id.tv_man);
							TextView tvWomen = (TextView) view
									.findViewById(R.id.tv_women);
							CustomDialog.Builder builder = new CustomDialog.Builder(
									PersonalDetailInfoSettingActivity.this);
							builder.setMessage("选择性别").setContentView(view);
							final CustomDialog dialog = builder.create();
							dialog.show();
							// 在对话框中选择了男生
							tvMan.setOnClickListener(new OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									tvItemInfo.setText("男");
									newList.set(position, "男");
									checkIsChange(POSITION_GENDER);
									dialog.hide();
								}
							});
							// 在对话框中选择了女生
							tvWomen.setOnClickListener(new OnClickListener()
							{

								@Override
								public void onClick(View v)
								{
									tvItemInfo.setText("女");
									newList.set(position, "女");
									dialog.hide();
									checkIsChange(POSITION_GENDER);
								}
							});
						}
					});
				}
				if (position == POSITION_AGE)// 禁用编辑年龄
				{
					et2tv(wrapper, etItemInfo);
				}
				// 设置生日选项单击事件
				if (position == POSITION_BIRTHDAY)
				{
					// 把edittext替换为textview
					final TextView tvItemInfo = et2tv(wrapper, etItemInfo);
					// 添加单击事件，弹出日期选择对话框
					wrapper.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							calendar = Calendar.getInstance();
							dateDialog = new DatePickerDialog(
									PersonalDetailInfoSettingActivity.this, null,
									calendar.get(Calendar.YEAR), // 传入年份
									calendar.get(Calendar.MONTH), // 传入月份
									calendar.get(Calendar.DAY_OF_MONTH) // 传入天数
							);
							dateDialog.setButton(DialogInterface.BUTTON_POSITIVE,
									"完成", new DialogInterface.OnClickListener()
									{
										@Override
										public void onClick(DialogInterface dialog,
										                    int which)
										{
											DatePicker datePicker = dateDialog
													.getDatePicker();
											// 将年月日转换为字符串，判断是否为个位数，若为个位数，在前面补上0
											String year = datePicker.getYear() + "";
											String month = datePicker.getMonth() + 1
													+ "";
											String day = datePicker.getDayOfMonth()
													+ "";
											if (month.length() == 1)
											{
												month = "0" + month;
											}
											if (day.length() == 1)
											{
												day = "0" + day;
											}
											String itemInfo = year + "-" + month
													+ "-" + day;
											tvItemInfo.setText(itemInfo);
											newList.set(position, itemInfo);
											checkIsChange(POSITION_BIRTHDAY);
										}
									});
							dateDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
									"取消", new DialogInterface.OnClickListener()
									{
										@Override
										public void onClick(DialogInterface dialog,
										                    int which)
										{
											dialog.cancel();
										}
									});
							dateDialog.show();
						}
					});
				}
				if (position == POSITION_MOOD || position == POSITION_DESCRIPTION)
				{
					etItemInfo.setMinLines(1);
					etItemInfo.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
					etItemInfo.setGravity(Gravity.TOP);
					etItemInfo.setHint("最多50个字");
				}
			}
		});
	}

	/**
	 * 判断listview的item文本是否有发生变化
	 * 如果发生了变化，使tvRightTitle可用
	 * 如未发生变化，禁用tvRightTitle
	 *
	 * @return
	 */
	public void checkIsChange(int position)
	{
		// System.out.println(oldList.size() + "  " + newList.size());
//	for (int i = 0; i < oldList.size(); i++)
//	{
//	  Log.e("TAG","position:"+i+" "+ (oldList.get(i) == null) + " " + (newList.get(i) == null));
		if (!(oldList.get(position).equals(newList.get(position))))
		{
			Log.e("TAG", "change");
			tvRightTitle.setEnabled(true);
			return;
		} else
		{
			tvRightTitle.setEnabled(false);
		}
//	}
	}

	/**
	 * 将editetext替换为textview
	 */
	public TextView et2tv(LinearLayout wrapper, EditText et)
	{
		TextView tvItemInfo = new TextView(getApplicationContext());
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
				LinearLayout.LayoutParams.WRAP_CONTENT, 1.7f);
		lp.setMargins((int) getResources().getDimension(R.dimen.x20), 0, 0, 0);
		tvItemInfo.setTextSize(15);
		tvItemInfo.setTextColor(Color.parseColor("#707070"));
		tvItemInfo.setText(et.getText().toString().trim());
		wrapper.removeView(et);
		wrapper.addView(tvItemInfo);
		tvItemInfo.setLayoutParams(lp);
		return tvItemInfo;
	}

}
