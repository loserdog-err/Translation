package com.gdxz.zhongbao.server.servlet;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.gdxz.zhongbao.server.domain.Question;
import com.gdxz.zhongbao.server.service.QuestionService;
import com.gdxz.zhongbao.server.service.TeamService;
import com.gdxz.zhongbao.server.service.UserService;
import com.gdxz.zhongbao.server.utils.PropertiesUtils;

@Controller
@Scope("prototype")
public class UploadFileServlet extends HttpServlet
{
	File tempPathFile;
	File saveFile;// 保存上传文件
	public static final String CATEGORY_UPLOAD_HEAD = "uploadHead";
	public static final String CATEGORY_WRITE_QUESTION = "writeQuestion";
	public static final String CATEGORY_UPLOAD_TEAM_LOGO = "uploadTeamLogo";
	@Autowired
	private UserService userService;
	@Autowired
	private QuestionService questionService;
	@Autowired
	private TeamService teamService;

	public void init() throws ServletException
	{
//		tempPathFile = new File("/home/kaiduan/temp");
		tempPathFile = new File(PropertiesUtils.getProperty("uploadFileBasePath") + "temp/");
		if (!tempPathFile.exists())
		{
			tempPathFile.mkdirs();
		}
		WebApplicationContext ctx = WebApplicationContextUtils
				.getWebApplicationContext(getServletContext());
		AutowireCapableBeanFactory factory = ctx.getAutowireCapableBeanFactory();
		factory.autowireBean(this);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		req.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=UTF-8");
		Map<String, String> params = new HashMap<String, String>();// 请求参数
		JSONObject json = new JSONObject();
		try
		{
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold(4096); // 设置缓冲区大小，这里是4kb
			factory.setRepository(tempPathFile);// 设置缓冲区目录
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setSizeMax(4194304); // 设置最大文件尺寸，这里是4MB

			String header = req.getHeader("content-type");
			List<FileItem> items = upload.parseRequest(req);// 得到所有的文件
			Iterator<FileItem> it = items.iterator();
			// 先遍历list找出字符串请求参数
			for (FileItem fileItem : items)
			{
				if (fileItem.isFormField())
				{
					params.put(fileItem.getFieldName(), fileItem.getString());
				}
			}
			if (CATEGORY_UPLOAD_HEAD.equals(params.get("category")))
			{
				int userId = Integer.parseInt(params.get("userId"));
				userService.uploadHead(it, userId);
			}
			else if (CATEGORY_WRITE_QUESTION.equals(params.get("category")))// 发表发表问题
			{
				int userId = Integer.parseInt(params.get("userId"));
				String rewardAmount = params.get("rewardAmount");
				String title = params.get("title");
				String questionContent = params.get("questionContent");
				Question question = new Question();
				question.setRewardAmount(Integer.parseInt(rewardAmount));
				question.setTitle(title);
				question.setContent(questionContent);
				String error = questionService.postQuestion(it, userId, question);
				if (error != null && !"".equals(error))
				{
					json.put("isSuccess", false);
					json.put("error", error);
				}
				else
				{
					json.put("isSuccess", true);
				}
			}
			else if (CATEGORY_UPLOAD_TEAM_LOGO.equals(params.get("category")))
			{
				Long groupId = Long.parseLong(params.get("groupId"));
				teamService.uploadTeamLogo(groupId, it);
				json.put("isSuccess", "true");
			}
			resp.getWriter().print(json.toString());
		}
		catch (Exception e)
		{
			// 可以跳转出错页面
			e.printStackTrace();
		}
	}
}
