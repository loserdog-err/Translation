package com.gdxz.zhongbao.client.Service;

import android.os.Handler;
import android.widget.ImageView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.List;

public interface QuestionService
{
	public void loadQuestionData(final Handler handler, final String pageNow,
			final String category, final int action,
			PullToRefreshListView pullToRefreshListView);

	public void loadQuestionDetailData(final Handler handler,
			final String questionId, final String pageNow,
			final PullToRefreshListView pullToRefreshListView);

	public void postQuestion(final Handler handler, final String questionContent,
			final String userId, final String rewardAmount, String title,
			List<String> paths, String voicePath);

	public void loadQuestionImage(String path, ImageView imageView);

	public void loadVoiceFile(Handler handler, String questionVoicePath);

	void addBrowseOrReplyCount(String questionId,String methodName);

	void followQuestion(String currentUserId, int questionId,Handler handler);
}
