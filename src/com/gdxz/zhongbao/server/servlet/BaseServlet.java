package com.gdxz.zhongbao.server.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class BaseServlet extends HttpServlet
{
	private Method method;

	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException
	 *             if an error occurs
	 */
	public void init() throws ServletException
	{
		super.init();
		WebApplicationContext ctx = WebApplicationContextUtils
				.getWebApplicationContext(getServletContext());
		AutowireCapableBeanFactory factory = ctx.getAutowireCapableBeanFactory();
		factory.autowireBean(this);
	}

	/**
	 * 通过反射机制确定调用Servlet哪个方法
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		req.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=UTF-8");

		// 获得方法名
		String methodName = req.getParameter("method");
		try
		{
			method = getClass().getMethod(methodName,
					new Class[] { HttpServletRequest.class, HttpServletResponse.class });
			// 调用方法
			method.invoke(this, req, resp);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void downLoadFile(String path, OutputStream os)
	{
		InputStream is = null;
		try
		{
			is = new FileInputStream(new File(path));
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = (is.read(buffer, 0, 1024))) != -1)
			{
				os.write(buffer, 0, len);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			try
			{
				if (is != null)
				{
					is.close();
				}
			}
			catch (Exception e2)
			{
				e.printStackTrace();
			}
		}

	}
}
