package com.gdxz.zhongbao.server.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.hibernate.hql.ast.tree.QueryNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdxz.zhongbao.server.DAO.impl.DAOSupportImpl;
import com.gdxz.zhongbao.server.domain.Answer;
import com.gdxz.zhongbao.server.domain.Question;
import com.gdxz.zhongbao.server.domain.User;
import com.gdxz.zhongbao.server.domain.UserDynamic;
import com.gdxz.zhongbao.server.service.AnswerService;
import com.gdxz.zhongbao.server.service.QuestionService;
import com.gdxz.zhongbao.server.service.UserDynamicService;
import com.gdxz.zhongbao.server.service.UserService;
import com.gdxz.zhongbao.server.servlet.QuestionServlet;
import com.gdxz.zhongbao.server.utils.FileUtils;
import com.gdxz.zhongbao.server.utils.PropertiesUtils;

@Service("questionService")
public class QuestionServiceImpl extends DAOSupportImpl<Question> implements QuestionService
{

	@Autowired
	private UserService userService;
	@Autowired
	private UserDynamicService userDynamicService;
	@Autowired
	private AnswerService answerService;

	/**
	 * 根据category和pageNow获得数据 1：pageNow为返回对应为第几页
	 * 2：category确定返回的是什么类型的数据（最新发布，今日热榜，总排行榜）
	 */
	public List<Question> loadQuestionData(int pageNow, int category)
	{
		String hql = "";
		if (category == QuestionServlet.NEWLY_QUESTION)// 最新提问
		{
			hql = "from Question order by postTime desc";
			// hql="select author from Question";
		}
		else if (category == QuestionServlet.TODAY_HOT_RANKING)// 今日热榜
		{
			hql = "from Question order by todayReplyChange desc";
		}
		else if (category == QuestionServlet.TOTAL_RANKING)
		{
			hql = "from Question order by replyCount desc";
		}
		List<Question> list = getByPage(pageNow, QuestionServlet.PAGE_SIZE, hql, null);
		if (list != null)
		{
			return list;
		}
		return null;
	}

	/**
	 * 添加问题的浏览次数
	 */
	public void addBrowseCount(String questionId)
	{
		Question question = getById(Integer.parseInt(questionId));
		Integer browseCount = question.getBrowseCount();
		question.setBrowseCount((browseCount == null ? 0 : browseCount) + 1);
		update(question);

	}

	/**
	 * 添加问题的回复次数
	 */
	public void addReplyCount(String questionId)
	{
		Question question = getById(Integer.parseInt(questionId));
		Integer replyCount = question.getReplyCount();
		question.setReplyCount((replyCount == null ? 0 : replyCount) + 1);
		update(question);

	}

	/**
	 * 收藏一个问题
	 */
	public void followQuestion(int userId, int questionId)
	{
		Question question = getById(questionId);
		User user=userService.getById(userId);
		UserDynamic userDynamic = new UserDynamic();
		userDynamic.setUpdateTime(new Date());
		userDynamic.setQuestion(question);
		userDynamic.setUser(user);
		userDynamic.setType(UserDynamic.DYNAMIC_TYPE_FOLLOW);
		userDynamicService.save(userDynamic);
	}

	/**
	 * 查询用户是否关注了该问题
	 */
	public boolean hadFollow(int userId, int questionId)
	{
		System.out.println(userId + "," + questionId);
		String hql = "select count(*) from UserDynamic where userId=? and questionId=? and type=?";
		int count = queryCount(hql, new Object[] { userId, questionId,
				UserDynamic.DYNAMIC_TYPE_FOLLOW });
		if (count > 0)
		{
			return true;
		}
		return false;
	}

	/**
	 * 发布一个问题
	 */
	public String postQuestion(Iterator<FileItem> it, int userId, Question question)
			throws Exception
	{
		StringBuffer sb = new StringBuffer(question.getContent());
		boolean hasVoiceFile = false;
		boolean hasImageFile = false;
		File saveFile = null;
		List<String> files = new ArrayList<String>();
		User user = userService.getById(userId);
		int point = user.getPoint() == null ? 0 : user.getPoint();
		if (point - question.getRewardAmount() >= 0)
		{
			if (it != null)
			{
				while (it.hasNext())
				{
					FileItem fileItem = (FileItem) it.next();
					if (fileItem.isFormField())
					{
						continue;
					}
					else
					{
						String fileName = fileItem.getName();
						String fileType = fileName.substring(fileName.lastIndexOf("."),
								fileName.length());

						if (".amr".equals(fileType))
						{
							// 有语音文件
							hasVoiceFile = true;
							saveFile = FileUtils.createFile(
									PropertiesUtils.getProperty("uploadFileBasePath")
											+ Question.getQuestionVoiceBasePath(userId), fileType);
						}
						else
						{
							// 有图片文件
							hasImageFile = true;
							saveFile = FileUtils.createFile(
									PropertiesUtils.getProperty("uploadFileBasePath")
											+ Question.getQuestionImageBasePath(userId), fileType);
						}
						fileItem.write(saveFile);
						files.add(saveFile.getName());
					}
				}
				if (hasImageFile || hasVoiceFile)// 如果有图片文件或者，将文件名拼接到content的后面，格式为/文件名/
				{
					for (String fileName : files)
					{
						sb.append("/" + fileName + "/");
					}
				}
				question.setContent(sb.toString());
			}
			question.setAuthor(user);
			question.setPostTime(new Date());
			save(question);
			// 维护userDynamic表
			UserDynamic userDynamic = new UserDynamic();
			userDynamic.setQuestion(question);
			userDynamic.setType(UserDynamic.DYNAMIC_TYPE_ASK);
			userDynamic.setUser(question.getAuthor());
			userDynamic.setUpdateTime(question.getPostTime());
			userDynamicService.save(userDynamic);
			return null;
		}
		else
		{
			return "剩余积分不足";
		}
	}

	/**
	 * 根据id获得该问题所有的用户
	 */
	public List<User> getAnswerersByQuestionId(Integer id)
	{
		String hql = "from Answer where questionId=? ";
		List<Answer> answers = answerService.query(hql, new Object[] { id });
		List<User> answerers = new ArrayList<User>();
		for (Answer answer : answers)
		{
			answerers.add(answer.getAuthor());
		}
		return answerers;
	}

	/**
	 * 根据问题id获得最佳回答的用户
	 */
	public User getBestAnswerer(Integer id)
	{
		String hql = "from Answer where questionId=? ";
		List<Answer> answers = answerService.query(hql, new Object[] { id });
		User bestAnswerer = null;
		for (Answer answer : answers)
		{
			if (answer.getIsBest())
			{
				bestAnswerer = answer.getAuthor();
			}
		}
		return bestAnswerer;
	}

}
