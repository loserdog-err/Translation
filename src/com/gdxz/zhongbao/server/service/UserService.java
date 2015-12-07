package com.gdxz.zhongbao.server.service;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;

import net.sf.json.JSONObject;

import com.gdxz.zhongbao.server.DAO.DAOSupport;
import com.gdxz.zhongbao.server.domain.Team;
import com.gdxz.zhongbao.server.domain.User;

public interface UserService extends DAOSupport<User>
{

	public User login(User loginUser, int passwordErrorCount);
	
	public User regist(User registUser);

	public String getVerifyCode(OutputStream outputStream);

	public boolean validateUsername(String username);

	public boolean validatePassword(String password);

	public boolean validateVerifyCode(String verifyCode, String rightVerifyCode);

	public boolean validateRePassword(String password, String rePassword);

	public boolean validateMobilePhone(String mobilePhone);

	public boolean validateEmail(String email);

	public User getUserInfo(User user,boolean isDetail);

	public boolean updateUserInfo(JSONObject jsonObject);

	public int getPasswordErrorCount(String username);

	public boolean checkUsernameExists(String username);

	public Team getTeam(int userId);

	public void uploadHead(Iterator<FileItem> it, int userId);

	public List<User> getUserList();

}
