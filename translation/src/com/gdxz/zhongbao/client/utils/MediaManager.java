package com.gdxz.zhongbao.client.utils;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by chenantao on 2015/7/10.
 */
public class MediaManager
{
  private static MediaPlayer mMediaPlayer;
  private static boolean isPause;

  public static void playSound(String path, MediaPlayer.OnCompletionListener listener)
  {
	if (mMediaPlayer == null)
	{
	  mMediaPlayer = new MediaPlayer();

	} else
	{
	  mMediaPlayer.reset();
	}
	try
	{
	  //报错监听
	  mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener()
	  {
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra)
		{
		  mMediaPlayer.reset();
		  return false;
		}
	  });
	  mMediaPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
	  mMediaPlayer.setOnCompletionListener(listener);
	  mMediaPlayer.setDataSource(path);
	  mMediaPlayer.prepare();
	  mMediaPlayer.start();
	} catch (IOException e)
	{
	  e.printStackTrace();
	}

  }

  //停止函数
  public static void pause()
  {
	if (mMediaPlayer != null && mMediaPlayer.isPlaying())
	{
	  mMediaPlayer.pause();
	  isPause = true;
	}
  }

  //继续
  public static void resume()
  {
	if (mMediaPlayer != null && isPause)
	{
	  mMediaPlayer.start();
	  isPause = false;
	}
  }


  public static void release()
  {
	if (mMediaPlayer != null)
	{
	  mMediaPlayer.release();
	  mMediaPlayer = null;
	}
  }

}
