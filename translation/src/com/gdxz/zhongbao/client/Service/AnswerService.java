package com.gdxz.zhongbao.client.Service;

import android.os.Handler;
import android.widget.ImageView;


public interface AnswerService
{
	public void postAnswer(final Handler handler, final String answer,
			final String questionId, String userId);
	public void getAnswerHead(final ImageView answerHead, final String path);

	void addPraiseOrDespiseCount(String answerId, String methodName);

	void setBestAnswer(String answerId);
}
