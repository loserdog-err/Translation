package com.gdxz.zhongbao.client.utils;

import android.graphics.Bitmap;
import android.os.Environment;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

/**
 * Created by chenantao on 2015/6/29.
 */
public class FileUtils
{
	private static File imageCacheDir;//图片缓存的目录
	private static File voiceCacheDir;//音频缓存的目录
	private static final String VOICE_PATH = Environment.getExternalStorageDirectory()
			.getPath() + "/cat_voice";//音频文件存储路径
	private static final String IMAGE_PATH = Environment.getExternalStorageDirectory()
			.getPath() + "/cat_image"; //图片地址缓存路径

	static
	{
		imageCacheDir = new File(IMAGE_PATH);
		if (!imageCacheDir.exists())
		{
			imageCacheDir.mkdirs();
		}
		voiceCacheDir = new File(VOICE_PATH);
		if (!voiceCacheDir.exists())
		{
			voiceCacheDir.mkdirs();
		}

	}

	/**
	 * 将bitmap文件保存到sd卡
	 */
	public static String saveBitmapInSdCard(Bitmap bitmap, String fileName)
	{
		FileOutputStream fos = null;
		File file = null;
		String path;
		try
		{
			file = new File(createFile(IMAGE_PATH, fileName));
			fos = new FileOutputStream(file);
			if (fos != null)
			{
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
				fos.close();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return file.getAbsolutePath();
	}

	public static String createFile(String parent, String fileName)
	{
		File dir = new File(parent);
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		File file = new File(dir, fileName);
		return file.getAbsolutePath();
	}

	/**
	 * 在默认的语音路径下创建一个语音文件
	 *
	 * @return
	 */
	public static String createVoiceFile()
	{
		return createFile(VOICE_PATH, UUID.randomUUID() + ".amr");
	}

	/**
	 * 在默认的图片目录下创建一个图片文件
	 */
	public static String createImageFile()
	{
		return createFile(IMAGE_PATH, System.currentTimeMillis() + ".jpg");
	}

	/**
	 * 得到imageloader加载图片时默认使用的参数
	 *
	 * @return
	 */
	public static DisplayImageOptions getDefaultImageOptions()
	{
		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.build();
		return options;
	}

	/**
	 * 得到图片缓存目录
	 *
	 * @return
	 */
	public static File getImageCacheDir()
	{
		if (imageCacheDir != null && imageCacheDir.exists())
		{
			return imageCacheDir;
		} else
		{
			imageCacheDir = new File(IMAGE_PATH);
			if (!imageCacheDir.exists())
			{
				imageCacheDir.mkdirs();
			}
			return imageCacheDir;
		}
	}

	/**
	 * 得到音频缓存目录
	 *
	 * @return
	 */
	public static File getVoiceCacheDir()
	{
		if (voiceCacheDir != null && voiceCacheDir.exists())
		{
			return voiceCacheDir;
		} else
		{
			voiceCacheDir = new File(VOICE_PATH);
			if (!voiceCacheDir.exists())
			{
				voiceCacheDir.mkdirs();
			}
			return voiceCacheDir;
		}
	}

	/**
	 * 递归删除目录下的所有文件及子目录下所有文件
	 *
	 * @param dir 将要删除的文件目录
	 * @return boolean Returns "true" if all deletions were successful.
	 * If a deletion fails, the method stops attempting to
	 * delete and returns "false".
	 */
	public static boolean deleteDir(File dir)
	{
		if (dir.isDirectory())
		{
			String[] children = dir.list();
			//递归删除目录中的子目录下
			for (int i = 0; i < children.length; i++)
			{
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success)
				{
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}

	/**
	 * 得到文件的大小
	 * @param f
	 * @return
	 * @throws Exception
	 */
	public static long getFileSize(File f)
	{
		long size = 0;
		File files[] = f.listFiles();
		for (int i = 0; i < files.length; i++)
		{
			if (files[i].isDirectory())
			{
				size = size + getFileSize(files[i]);
			}
			else
			{
				size = size + files[i].length();
			}
		}
		return size;
	}

}
