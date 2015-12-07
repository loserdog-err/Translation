package com.gdxz.zhongbao.client.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gdxz.zhongbao.client.common.CommonAdapter;
import com.gdxz.zhongbao.client.common.ViewHolder;
import com.gdxz.zhongbao.client.domain.Question;
import com.gdxz.zhongbao.client.domain.UserRemind;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chean_antao on 2015/8/11.
 * 用户提醒activity
 */
public class UserRemindActivity extends Activity
{
	List<UserRemind> userRemindList;

	//title
	private ImageView mIvBack;
	private TextView mTvTitle;

	private ListView mListView;
	private CommonAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.personal_setting_dynamic_ui);
		userRemindList = (List<UserRemind>) getIntent().getSerializableExtra("userRemindList");
		if (userRemindList == null)
		{
			userRemindList = new ArrayList<>();
		}
		initView();
	}

	private void initView()
	{
		mIvBack = (ImageView) findViewById(R.id.iv_back);
		mIvBack.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				UserRemindActivity.this.finish();
			}
		});
		mTvTitle = (TextView) findViewById(R.id.tv_title);
		mTvTitle.setText("提醒");
		mListView = (ListView) findViewById(R.id.lv_dynamic);
		initListView();
	}

	private void initListView()
	{
		mListView.setAdapter(mAdapter = new CommonAdapter<UserRemind>(this, userRemindList, R
				.layout.personal_setting_dynamic_item)
		{
			@Override
			public void convert(ViewHolder helper, UserRemind item, int position)
			{
				String type = "";
				switch (item.getType())
				{
					case UserRemind.TYPE_ANSWER_ADOPT:
						type = "您的回答被采纳";
						break;
					case UserRemind.TYPE_ANSWER_QUESTION_SOLVE:
						type = "您回复的问题已解决";
						break;
					case UserRemind.TYPE_FOLLOW_QUESTION_SOLVE:
						type = "您关注的问题已解决";
						break;
					case UserRemind.TYPE_NEW_ANSWER:
						type = "您的问题有了新的回复";
						break;
				}
				Question question = item.getAnswer().getQuestion();
				helper.setText(R.id.tv_type, type);
				helper.setText(R.id.tv_title, question.getTitle());
				helper.setText(R.id.tv_content, item.getAnswer().getContent());
			}
		});
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Question question = userRemindList.get(position).getAnswer().getQuestion();
				Intent intent = new Intent(UserRemindActivity.this, QuestionDetailActivity.class);
				intent.putExtra("questionId", question.getId() + "");
				startActivity(intent);
			}
		});
	}
}
