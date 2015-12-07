package com.gdxz.zhongbao.client.utils;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * 发送请求的工具类
 *
 * @author chenantao
 */

public class HttpUtils
{
	public static HttpClient httpClient = new DefaultHttpClient();
	// 常量，服务器的根路径
	public static final String BASE_URL = "http://192.168.188.100:8080/Translation/";
	public static final String BASE_FILE_PATH = "http://192.168.188.100:8080/file/";//文件服务器的根路径
//	public static final String BASE_URL = "http://kaiduan.ecs33.tomcats.pw/Translation/";
//	public static final String BASE_FILE_PATH = "http://kaiduan.ecs33.tomcats.pw/file/";

	/**
	 * 发送get请求
	 *
	 * @param url 请求的url
	 * @return 服务器返回的结果
	 * @throws Exception
	 */
	public static String getRequest(final String url) throws Exception
	{
		FutureTask<String> task = new FutureTask<>(new Callable<String>()
		{

			@Override
			public String call() throws Exception
			{
				HttpGet get = new HttpGet(url);
				HttpResponse httpResponse = httpClient.execute(get);
				// 200代表成功返回响应
				if (httpResponse.getStatusLine().getStatusCode() == 200)
				{
					String result = EntityUtils.toString(httpResponse.getEntity());
					return result;
				}
				return null;
			}
		});
		new Thread(task).start();
		return task.get();
	}

	/**
	 * 发送post请求
	 *
	 * @param url       请求的url
	 * @param rawParams 请求参数
	 * @return 服务器的响应数据
	 * @throws Exception
	 */
	public static String postRequest(final String url,
	                                 final Map<String, String> rawParams) throws Exception
	{
		FutureTask<String> task = new FutureTask<>(new Callable<String>()
		{

			@Override
			public String call() throws Exception
			{
				HttpPost post = new HttpPost(url);
				// 封装请求数据
				List<NameValuePair> parameters = new ArrayList<>();
				for (String key : rawParams.keySet())
				{
					parameters.add(new BasicNameValuePair(key, rawParams.get(key)));
				}
				// 设置请求数据
				post.setEntity(new UrlEncodedFormEntity(parameters, "utf-8"));
				// 发送post请求
				HttpResponse httpResponse = httpClient.execute(post);
				if (httpResponse.getStatusLine().getStatusCode() == 200)
				{
					String result = EntityUtils.toString(httpResponse.getEntity());
					return result;
				}
				return null;
			}
		});
		new Thread(task).start();
		return task.get();
	}

	/**
	 * 获得资源输入流
	 *
	 * @param url
	 * @param rawParams
	 * @return
	 */
	public static InputStream getResourceInputStream(String url,
	                                                 Map<String, String> rawParams)
	{
		HttpPost post = new HttpPost(url);
		try
		{
			List<NameValuePair> parameters = new ArrayList<>();
			for (String key : rawParams.keySet())
			{
				parameters.add(new BasicNameValuePair(key, rawParams.get(key)));
			}
			// 设置请求数据
			post.setEntity(new UrlEncodedFormEntity(parameters, "utf-8"));
			HttpResponse response = httpClient.execute(post);
			if (response.getStatusLine().getStatusCode() == 200)
			{
				return response.getEntity().getContent();
			} else
			{
				return null;
			}

		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 多文件上传，含文本
	 */
	public static boolean upLoadFile(String url, List<File> files,
	                                 Map<String, String> rawParams)
	{
		PostMethod filePost = null;
		try
		{
			Part[] parts = new Part[files.size() + rawParams.size()];
			filePost = new PostMethod(url);
			int count = 0;
			for (String key : rawParams.keySet())
			{
				parts[count++] = new StringPart(key, rawParams.get(key), "utf-8");
			}
			// 添加文件
			for (int i = count; i < files.size() + count; i++)
			{
				L.e("add file");
				File file = files.get(i - count);
				parts[i] = new FilePart(file.getName(), file);
			}
			filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost
					.getParams()));
			org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient
					.HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(3000);
			int status = client.executeMethod(filePost);
			if (status == HttpStatus.SC_OK)
			{
				L.e("success");
				return true;
			} else
			{
				L.e("error");
				return false;
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
			return false;
		} finally
		{
			filePost.releaseConnection();

		}
	}
}
