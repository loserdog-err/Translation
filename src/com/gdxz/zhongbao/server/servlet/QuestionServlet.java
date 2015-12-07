package com.gdxz.zhongbao.server.servlet;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.gdxz.zhongbao.server.domain.Answer;
import com.gdxz.zhongbao.server.domain.Question;
import com.gdxz.zhongbao.server.service.AnswerService;
import com.gdxz.zhongbao.server.service.QuestionService;
import com.gdxz.zhongbao.server.utils.PropertiesUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;
import net.sf.json.util.PropertyFilter;

@Controller
@Scope("prototype")
public class QuestionServlet extends BaseServlet
{
	// 常量
	public static final int NEWLY_QUESTION = 1;
	public static final int TODAY_HOT_RANKING = 2;
	public static final int TOTAL_RANKING = 3;

	public static final int PAGE_SIZE = 10;

	// 返回给客户端的jsonArray
	JSONArray jsonArray;

	// 返回给客户端的json
	JSONObject jsonObject;

	@Autowired
	QuestionService questionService;

	@Autowired
	AnswerService answerService;

	/**
	 * 加载问题数据
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void loadQuestionData(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int pageNow = Integer.parseInt(req.getParameter("pageNow"));
		int category = Integer.parseInt(req.getParameter("category"));
		List<Question> list = questionService.loadQuestionData(pageNow, category);
		if (list != null)// 成功返回数据
		{
			Gson gson = new GsonBuilder().create();
			jsonObject = new JSONObject();
			jsonObject.put("isSuccess", true);
			jsonObject.put("questionData", gson.toJson(list));
//			System.out.println(gson.toJson(list));
		}
		else
		{
			jsonObject = new JSONObject();
			jsonObject.put("isSuccess", false);
		}
		resp.getWriter().print(jsonObject.toString());

	}

	/**
	 * 加载问题详细页面的数据(需查询用户是否已收藏了该问题)
	 */
	public void loadQuestionDetailData(HttpServletRequest req, HttpServletResponse resp)
			throws Exception
	{
		Gson gson = new GsonBuilder().serializeNulls().create();
		jsonObject = new JSONObject();
		int pageNow = Integer.parseInt(req.getParameter("pageNow"));
		int questionId = Integer.parseInt(req.getParameter("questionId"));
		int userId = Integer.parseInt(req.getParameter("userId"));
		Question question = questionService.getById(questionId);
		List<Answer> answers = answerService.getByPage(pageNow, PAGE_SIZE,
				"from Answer where questionId=? order by postTime desc",
				new Object[] { questionId });
		boolean hadFollow = questionService.hadFollow(userId, questionId);
		String questionJson = gson.toJson(question);
		String answersJson = gson.toJson(answers);
		jsonObject.put("isSuccess", true);
		jsonObject.put("question", questionJson);
		jsonObject.put("answers", answersJson);
		jsonObject.put("hadFollow", hadFollow);
		// System.out.println(answersJson);
		resp.getWriter().print(jsonObject.toString());
	}

	/**
	 * 发布一个问题
	 */
	public void postQuestion(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int userId = Integer.parseInt(req.getParameter("userId"));
		String questionContent = req.getParameter("questionContent");
		String title = req.getParameter("title");
		int rewardAmount = Integer.parseInt(req.getParameter("rewardAmount"));
		Question question = new Question();
		question.setContent(questionContent);
		question.setTitle(title);
		question.setRewardAmount(rewardAmount);
		String error = questionService.postQuestion(null, userId, question);
		jsonObject = new JSONObject();
		if (error != null && !"".equals(error))
		{
			// 有错误
			jsonObject.put("isSuccess", false);
			jsonObject.put("error", error);
		}
		else
		{
			jsonObject.put("isSuccess", true);
		}
		resp.getWriter().print(jsonObject.toString());
	}

	/**
	 * 收藏一个问题
	 */
	public void followQuestion(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int userId = Integer.parseInt(req.getParameter("userId"));
		int questionId = Integer.parseInt(req.getParameter("questionId"));
		questionService.followQuestion(userId, questionId);
	}

	/**
	 * 下载音频文件
	 */
	public void getQuestionVoice(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		String path = req.getParameter("path");
		downLoadFile(PropertiesUtils.getProperty("uploadFileBasePath") + path,
				resp.getOutputStream());
	}

	public void addBrowseCount(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		String questionId = req.getParameter("questionId");
		questionService.addBrowseCount(questionId);

	}

	public void addReplyCount(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		String questionId = req.getParameter("questionId");
		questionService.addReplyCount(questionId);

	}

}
