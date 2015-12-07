package com.gdxz.zhongbao.server.servlet;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.gdxz.zhongbao.server.domain.Question;
import com.gdxz.zhongbao.server.domain.Team;
import com.gdxz.zhongbao.server.domain.User;
import com.gdxz.zhongbao.server.service.QuestionService;
import com.gdxz.zhongbao.server.service.UserDynamicService;
import com.gdxz.zhongbao.server.service.UserService;
import com.gdxz.zhongbao.server.utils.FormBeanUtils;
import com.gdxz.zhongbao.server.utils.JpushClientUtils;
import com.gdxz.zhongbao.server.utils.PropertiesUtils;
import com.gdxz.zhongbao.server.utils.VerifyCode;
import com.google.gson.Gson;

@Controller
@Scope("prototype")
public class UserServlet extends BaseServlet
{

	@Autowired
	private UserService userService;
	@Autowired
	private UserDynamicService userDynamicService;
	@Autowired
	private QuestionService questionService;

	// 返回给客户端的json
	JSONObject jsonObject;

	// 常量，代表登录失败
	public static final int LOGIN_ERROR = 0;
	// 常量，代表登录成功
	public static final int LOGIN_SUCCESS = 1;

	public static final int VERIFYCODE_LENGTH = 4;
	public static final int PWD_ERROR_COUNT_LIMIT = 3;

	/**
	 * 登录
	 * 
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	public void login(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int passwordErrorCount;
		try
		{
			User loginUser = FormBeanUtils.toBean(req.getParameterMap(), User.class);
			passwordErrorCount = userService.getPasswordErrorCount(loginUser.getUsername());
			if (passwordErrorCount >= PWD_ERROR_COUNT_LIMIT)
			{
				String verifyCode = (String) req.getSession().getAttribute("verifyCode");
				loginUser.setRightVerifyCode(verifyCode);
			}
			loginUser.setPasswordErrorCount(passwordErrorCount);
			User user = userService.login(loginUser, passwordErrorCount);
			jsonObject = new JSONObject();
			// 登录信息有误
			if (user.getErrors().size() > 0)
			{
				jsonObject.put("loginIsSuccess", false);
				jsonObject.put("haveVerifyCode",
						loginUser.getPasswordErrorCount() >= PWD_ERROR_COUNT_LIMIT ? true : false);
				jsonObject.put("error", loginUser.getErrors().get(0));
			}
			// 登录成功
			else
			{
				jsonObject.put("userId", user.getId());
				jsonObject.put("username", user.getUsername());
				jsonObject.put("loginIsSuccess", true);
				Integer point = user.getPoint();
				jsonObject.put("point", point == null ? 0 + "" : point + "");
				req.getSession().setAttribute("user", user);
			}
			resp.getWriter().print(jsonObject.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 注册功能
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void regist(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		User registUser = FormBeanUtils.toBean(req.getParameterMap(), User.class);
		User user = userService.regist(registUser);
		jsonObject = new JSONObject();
		// 注册失败
		if (user.getErrors().size() > 0)
		{
			jsonObject.put("registIsSuccess", false);
			jsonObject.put("error", user.getErrors().get(0));
			resp.getWriter().print(jsonObject.toString());
		}
		else
		// 注册成功
		{
			jsonObject.put("userId", user.getId());
			jsonObject.put("registIsSuccess", true);
			resp.getWriter().print(jsonObject.toString());
		}

	}

	/**
	 * 得到用户列表
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void getUserList(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		List<User> userList = userService.getUserList();
		jsonObject = new JSONObject();
		jsonObject.put("isSuccess", true);
		jsonObject.put("userList", new Gson().toJson(userList));
		resp.getWriter().print(jsonObject.toString());
	}

	/**
	 * 邀请用户回答问题
	 */
	public void inviteToAnswer(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int id = Integer.parseInt(req.getParameter("userId"));
		int questionId = Integer.parseInt(req.getParameter("questionId"));
		User user = userService.getById(id);
		Question question = questionService.getById(questionId);
		if (user != null&&question!=null)
		{
			JpushClientUtils.sendPush(JpushClientUtils.buildPushObject_all_alias_alert(
					user.getUsername(), user.getUsername() + " 邀请您回答问题：" + question.getTitle()));
		}
	}

	/**
	 * 用户注销
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void logout(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		req.getSession().removeAttribute("userId");
		jsonObject = new JSONObject();
		jsonObject.put("logoutIsSuccess", true);
		resp.getWriter().print(jsonObject.toString());
	}

	/**
	 * 以流的形式返回验证码图片
	 */
	public void getVerifyCode(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		try
		{
			// System.out.println("client request reach");
			String verifyCode = userService.getVerifyCode(resp.getOutputStream());
			req.getSession().setAttribute("verifyCode", verifyCode);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 以流的形式返回用户头像
	 */
	public void getUserHead(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int userId = Integer.parseInt(req.getParameter("userId"));
		try
		{
			boolean isHeadExist = userService.getById(userId).getHead() == null ? false : true;
			if (isHeadExist)
			{
				String path = PropertiesUtils.getProperty("uploadFileBasePath")
						+ userService.getById(userId).getHead();
				downLoadFile(path, resp.getOutputStream());
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 得到用户中心的信息(需得到两个信息) 1：用户对象 2：用户各种动态的数量
	 */
	public void getUserInfo(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		// System.out.println("come on");
		User user = FormBeanUtils.toBean(req.getParameterMap(), User.class);
		int userId = user.getId();
		user = userService.getUserInfo(user, false);
		jsonObject = new JSONObject();
		if (user == null)
		{
			jsonObject.put("isSuccess", false);
		}
		else
		{
			// 查询各类动态信息的数量
			Map<String, String> userDynamicCount = userDynamicService.getUserDynamicCount(userId);
			Gson gson = new Gson();
			// 将user对象转换为json
			JSONObject jsonUser = JSONObject.fromObject(user);

			jsonObject.put("isSuccess", true);
			jsonObject.put("userInfo", jsonUser);
			jsonObject.put("userDynamicCount", gson.toJson(userDynamicCount));
			// System.out.println(jsonObject.getJSONObject("user").toString());
		}
		resp.getWriter().print(jsonObject.toString());

	}

	/**
	 * 得到详细设置的信息
	 */
	public void getUserDetailInfo(HttpServletRequest req, HttpServletResponse resp)
			throws Exception
	{
		User user = FormBeanUtils.toBean(req.getParameterMap(), User.class);
		user = userService.getUserInfo(user, true);
		jsonObject = new JSONObject();
		if (user == null)
		{
			jsonObject.put("isSuccess", false);
			resp.getWriter().print(jsonObject.toString());
		}
		else
		{
			// 将user对象转换为json
			Gson gson = new Gson();
			String jsonUser = gson.toJson(user);
			jsonObject.put("isSuccess", true);
			jsonObject.put("userInfo", jsonUser);
			resp.getWriter().print(jsonObject.toString());
		}
	}

	/**
	 * 更新用户信息
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void updateUserInfo(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		jsonObject = JSONObject.fromObject(req.getParameter("userInfo"));
		if (userService.updateUserInfo(jsonObject))
		{
			jsonObject = new JSONObject();
			jsonObject.put("isSuccess", true);
		}
		else
		{
			jsonObject = new JSONObject();
			jsonObject.put("isSuccess", false);
		}
		resp.getWriter().print(jsonObject.toString());
	}

	/**
	 * 检查用户名是否存在
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void checkUsernameExists(HttpServletRequest req, HttpServletResponse resp)
			throws Exception
	{
		String username = req.getParameter("username");
		boolean isExists = userService.checkUsernameExists(username);
		jsonObject = new JSONObject();
		jsonObject.put("isExists", isExists);
		resp.getWriter().print(jsonObject.toString());
	}

	/**
	 * 得到用户的团队
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	public void getTeam(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		int userId = Integer.parseInt(req.getParameter("userId"));
		Team team = userService.getTeam(userId);
		jsonObject = new JSONObject();
		if (team != null)
		{
			jsonObject.put("haveTeam", true);
			jsonObject.put("team", new Gson().toJson(team));
		}
		else
		{
			jsonObject.put("haveTeam", false);
		}
		resp.getWriter().print(jsonObject.toString());
	}

}
