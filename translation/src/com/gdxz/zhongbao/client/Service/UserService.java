package com.gdxz.zhongbao.client.Service;

import android.content.Context;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.gdxz.zhongbao.client.domain.User;

import java.util.Map;

public interface UserService
{

	void login(User user, Context context, boolean haveVerifyCode, Handler handler);

	boolean validateUsername(String username);

	boolean validatePassword(String password);

	boolean validateVerifyCode(String verifyCode);

	boolean validateRePassword(String password, String rePassword);

	boolean validateMobilePhone(String mobilePhone);

	boolean validateEmail(String email);

	boolean validateRealName(String realName);

	void setVerifyCodeImage(ImageView imageView);

	void regist(final User user, final Handler handler);

	boolean updateUserInfo(Map<String, String> userInfo,
	                       final Handler handler, TextView tvErrorInfo);

	void getUserInfo(String userId, Handler handler, boolean isDetail);

	void getUserHead(String userId, ImageView ivHead);

	void uploadHead(String userId, String path, ImageView ivHead);


	void checkUsernameExistsAsync(String username, Handler handler);

	boolean checkUsernameExistsSync(String username);

	void getTeam(String currentUserId, Handler handler);

	void getUserList(Handler handler);

	void inviteToAnswer(Integer id,int questionId);
}
