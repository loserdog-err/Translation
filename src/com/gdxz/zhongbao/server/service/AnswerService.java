package com.gdxz.zhongbao.server.service;

import org.springframework.dao.support.DaoSupport;
import org.springframework.stereotype.Service;

import com.gdxz.zhongbao.server.DAO.DAOSupport;
import com.gdxz.zhongbao.server.domain.Answer;

public interface AnswerService extends DAOSupport<Answer>
{

	boolean postAnswer(int questionId, String answerContent,int userId);

	void addPraiseCount(int answerId);

	void addDespiseCount(int answerId);

	void setBestAnswer(int answerId);

}
