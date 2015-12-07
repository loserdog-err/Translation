package com.gdxz.zhongbao.server.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.jpush.api.JPushClient;
import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.Notification;

public class JpushClientUtils
{
	protected static final Logger LOG = LoggerFactory.getLogger(JpushClientUtils.class);

	private static final String appKey = "7ca43d82e3f91e37d66dd275";
	private static final String masterSecret = "230f7dca06925ec4589df499";
	private static JPushClient jpushClient = new JPushClient(masterSecret, appKey);

	/**
	 * 快捷地构建推送对象：所有平台，所有设备，内容为 ALERT 的通知。
	 * 
	 * @param alert
	 * @return
	 */
	public static PushPayload buildPushObject_all_all_alert(String alert)
	{
		return PushPayload.alertAll(alert);
	}

	/**
	 * 构建推送对象：平台是 Android，目标是 tag 为 "tag1" 的设备，内容是 Android 通知 ALERT，并且标题为 TITLE。
	 * 
	 * @param alert
	 * @param title
	 * @return
	 */
	public static PushPayload buildPushObject_android_tag_alertWithTitle(String alert, String title,String tag)
	{
		return PushPayload.newBuilder().setPlatform(Platform.android())
				.setAudience(Audience.tag(tag))
				.setNotification(Notification.android(alert, title, null)).build();

	}

	/**
	 * 构建推送对象：所有平台，推送目标是别名为 alias，通知内容为 alert。
	 * 
	 * @param alias
	 * @param alert
	 * @return
	 */
	public static PushPayload buildPushObject_all_alias_alert(String alias,String alert)
	{
		return PushPayload.newBuilder().setPlatform(Platform.all())
				.setAudience(Audience.alias(alias)).setNotification(Notification.alert(alert))
				.build();
	}

	/**
	 * 推送通知
	 * 
	 * @param payload
	 */
	public static void sendPush(PushPayload payload)
	{
		// For push, all you need do is to build PushPayload object.

		try
		{
			PushResult result = jpushClient.sendPush(payload);
			LOG.info("Got result - " + result);
		}
		catch (APIConnectionException e)
		{
			// Connection error, should retry later
			LOG.error("Connection error, should retry later", e);

		}
		catch (APIRequestException e)
		{
			// Should review the error, and fix the request
			LOG.error("Should review the error, and fix the request", e);
			LOG.info("HTTP Status: " + e.getStatus());
			LOG.info("Error Code: " + e.getErrorCode());
			LOG.info("Error Message: " + e.getErrorMessage());
		}
	}
}
