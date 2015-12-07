package com.gdxz.zhongbao.server.utils;

import java.io.File;
import java.util.UUID;

public class FileUtils
{

	public static String lastFileName = "xixi";

	/**
	 * 通过给定的父路径以及文件类型，利用系统当前时间自动生成对应类型的文件
	 * 
	 * @param parentPath
	 *            要生成的文件的父路径
	 * @param fileType
	 *            要生成的文件的类型
	 * @return
	 * @throws InterruptedException 
	 */
	public synchronized static File createFile(String parentPath, String fileType) throws InterruptedException
	{
		File parent = new File(parentPath);
		if (!parent.exists())
		{
			parent.mkdirs();
		}
		long a = System.currentTimeMillis();
		Thread.sleep(1);
		long b = System.currentTimeMillis();
		String fileName = a + b + fileType;
		File saveFile = new File(parent, fileName);
		return saveFile;
	}

	/**
	 * 通过给定的文件名得到文件的类型
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileType(String fileName)
	{
		return fileName.substring(fileName.lastIndexOf("."), fileName.length());
	}

}
