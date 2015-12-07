package com.gdxz.zhongbao.server.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;

import com.gdxz.zhongbao.server.domain.User;
import com.gdxz.zhongbao.server.service.AnswerService;
import com.gdxz.zhongbao.server.service.UserService;

public class AnswerServlet extends BaseServlet
{
	JSONObject jsonObject;
	@Autowired
	AnswerService answerService;
	@Autowired
	UserService userService;

	/**
	 * 发表回复
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void postAnswer(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int questionId = Integer.parseInt(req.getParameter("questionId"));
		String answer = req.getParameter("answer");
		int userId = Integer.parseInt(req.getParameter("userId"));
		boolean isSuccess = answerService.postAnswer(questionId, answer, userId);
		jsonObject = new JSONObject();
		if (isSuccess)
		{
			jsonObject.put("isSuccess", true);
		}
		else
		{
			jsonObject.put("isSuccess", false);
		}
		resp.getWriter().print(jsonObject.toString());
	}

	/**
	 * 得到头像
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void getAnswerHead(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int answerId = Integer.parseInt(req.getParameter("answerId"));
		User user=userService.getById(answerId);
		try
		{
			downLoadFile(User.getUserHeadBasePath(answerId) + userService.getById(answerId).getHead(),
					resp.getOutputStream());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 增加赞的数量
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void addPraiseCount (HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int answerId=Integer.parseInt(req.getParameter("answerId"));
		answerService.addPraiseCount(answerId);
	}
	/**
	 * 增加鄙视的数量
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void addDespiseCount (HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int answerId=Integer.parseInt(req.getParameter("answerId"));
		answerService.addDespiseCount(answerId);
	}

	/**
	 * 设置为最佳答案
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void setBestAnswer (HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int answerId=Integer.parseInt(req.getParameter("answerId"));
		answerService.setBestAnswer(answerId);
	}
}
