package com.gdxz.zhongbao.server.service;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;

import com.gdxz.zhongbao.server.DAO.DAOSupport;
import com.gdxz.zhongbao.server.domain.Question;
import com.gdxz.zhongbao.server.domain.User;

public interface QuestionService extends DAOSupport<Question>
{

	List<Question> loadQuestionData(int pageNow, int category);


	void addBrowseCount(String questionId);

	void addReplyCount(String questionId);

	void followQuestion(int userId, int questionId);

	boolean hadFollow(int userId, int questionId);

	String postQuestion(Iterator<FileItem> it, int userId, Question question) throws Exception;


	List<User> getAnswerersByQuestionId(Integer id);


	User getBestAnswerer(Integer id);


}
