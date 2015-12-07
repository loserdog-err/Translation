package com.gdxz.zhongbao.server.service.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletOutputStream;

import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import com.gdxz.zhongbao.server.DAO.impl.DAOSupportImpl;
import com.gdxz.zhongbao.server.domain.Team;
import com.gdxz.zhongbao.server.domain.User;
import com.gdxz.zhongbao.server.service.UserService;
import com.gdxz.zhongbao.server.servlet.UserServlet;
import com.gdxz.zhongbao.server.utils.DateUtils;
import com.gdxz.zhongbao.server.utils.FileUtils;
import com.gdxz.zhongbao.server.utils.PropertiesUtils;
import com.gdxz.zhongbao.server.utils.VerifyCode;

@Service("userService")
public class UserServiceImpl extends DAOSupportImpl<User> implements UserService
{

	// -----------------------------------登录相关-------------------------------------------------
	/**
	 * 登录验证： 1：先做非业务逻辑的用户名以及密码输入校验
	 * 2：判断用户密码错误次数是否大于等于，若成立，则校验验证码。若验证码错误，返回错误信息
	 * 3：校验用户名以及密码，若正确，返回用户，若错误，判断数据库是否有此用户名，更新密码错误次数字段。
	 * 4：如果校验以及查询全部通过
	 * ，则返回新的User对象，若有一个不通过，则返回原来的loginUser，里面存放着errors信息
	 */
	public User login(User loginUser, int passwordErrorCount)
	{
		List<String> errors = loginUser.getErrors();
		if (validatePassword(loginUser.getPassword()) && validateUsername(loginUser.getUsername()))
		{
			if (passwordErrorCount >= UserServlet.PWD_ERROR_COUNT_LIMIT)
			{
				boolean validateVerifyCode = validateVerifyCode(loginUser.getVerifyCode(),
						loginUser.getRightVerifyCode());
				if (!validateVerifyCode)
				{
					errors.add("验证码错误");
					return loginUser;
				}
			}
			String hql = "from User where username=? and password=?";
			List<User> list = query(hql,
					new Object[] { loginUser.getUsername(), loginUser.getPassword() });
			if (list != null && list.size() > 0)// 登录成功
			{
				User user = list.get(0);
				user.setPasswordErrorCount(0);
				update(user);
				// 返回查询到的用户
				return user;
			}
			// 用户名或者密码错误，判断数据库是否有这个用户名，如果有，记录密码错误次数
			else
			{
				hql = "from User where username=?";
				list = query(hql, new Object[] { loginUser.getUsername() });
				if (list != null && list.size() > 0)
				{
					User user = list.get(0);
					int count = loginUser.getPasswordErrorCount();
					count++;
					loginUser.setPasswordErrorCount(count);
					user.setPasswordErrorCount(count);
					update(user);
				}
				errors.add("用户名或密码错误");
			}
		}
		else
		{
			errors.add("用户名或密码格式错误");
		}
		return loginUser;

	}

	/**
	 * 从一个图片输入流读取数据发送到服务端输出流
	 */
	public String getVerifyCode(OutputStream outputStream)
	{
		InputStream inputStream = null;
		VerifyCode code = new VerifyCode();
		try
		{
			BufferedImage bufferedImage = code.getImage();
			inputStream = code.getImageStream(bufferedImage);
			byte[] buffer = new byte[1024];
			int length = 0;
			while ((length = inputStream.read(buffer)) != -1)
			{
				outputStream.write(buffer, 0, length);
			}
			return code.getText();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			try
			{
				if (inputStream != null)
				{
					inputStream.close();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	// -----------------------------------注册相关-------------------------------------------------
	/**
	 * 注册功能： 1：先做不带业务逻辑的输入校验 2：检验用户名是否存在，若存在，返回错误信息
	 * 3：检验邮箱是否存在，若存在，返回错误信息 4：插入数据
	 */
	public User regist(User registUser)
	{
		String username = registUser.getUsername();
		String password = registUser.getPassword();
		String rePassword = registUser.getRePassword();
		String mobilePhone = registUser.getMobilePhone();
		String email = registUser.getEmail();
		List<String> errors = registUser.getErrors();
		// 校验通过
		if (validateUsername(username) && validatePassword(rePassword)
				&& validateRePassword(password, rePassword) && validateEmail(email)
				&& validateMobilePhone(mobilePhone + ""))
		{
			// 查询用户名是否存在
			if (checkUsernameExists(username))
			{

				errors.add("用户名已存在");
				return registUser;
			}
			// 查询邮箱是否已存在
			String hql = "from User where email=?";
			List<User> list = query(hql, new Object[] { email });
			if (list != null && list.size() > 0)
			{
				errors.add("邮箱已存在");
				return registUser;
			}
			// 信息通过，将数据存储进数据库
			User user = new User();
			user.setUsername(username);
			user.setPassword(password);
			user.setMobilePhone(mobilePhone);
			user.setEmail(email);
			user.setPoint(1000);
			save(user);
			return user;
		}
		else
		{
			errors.add("数据有误");
			return registUser;
		}
	}

	// -----------------------------------校验相关-------------------------------------------------

	/**
	 * 校验用户名
	 */
	public boolean validateUsername(String username)
	{
		if ("".equals(username) || null == username)
		{
			// errors.put("usernameError", "用户名不能为空");
			return false;
		}
		else
		{
			if (username.length() > 10 || username.length() < 2)
			{
				// errors.put("usernameError",
				// "用户名长度必须在6到16之间");
				return false;
			}
		}
		return true;
	}

	/**
	 * 校验验证码
	 */
	public boolean validateVerifyCode(String verifyCode, String rightVerifyCode)
	{
		if ("".equals(verifyCode) || null == verifyCode)
		{
			// errors.put("verifyCodeError", "密码不能为空");
			return false;
		}
		else
		{
			if (verifyCode.length() != UserServlet.VERIFYCODE_LENGTH)
			{
				// errors.put("verifyCodeError",
				// "用户名长度必须在6到14之间");
				return false;
			}
			else
			{
				// 验证验证码是否正确
				if (rightVerifyCode.equalsIgnoreCase(verifyCode))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		}
	}

	/**
	 * 校验密码
	 */
	public boolean validatePassword(String password)
	{
		if ("".equals(password) || null == password)
		{
			// errors.put("passwordError", "密码不能为空");
			return false;
		}
		else
		{
			if (password.length() > 14 || password.length() < 6)
			{
				// errors.put("passwordError",
				// "用户名长度必须在6到14之间");
				return false;
			}
		}
		return true;
	}

	/**
	 * 校验重复密码
	 */
	public boolean validateRePassword(String password, String rePassword)
	{
		if ("".equals(rePassword) || null == rePassword)
		{
			// errors.put("passwordError", "密码不能为空");
			return false;
		}
		else if (rePassword.length() > 14 || rePassword.length() < 6)
		{
			return false;
		}
		else if (!(password.equals(rePassword)))
		{
			return false;
		}
		return true;
	}

	/**
	 * 校验手机号
	 */
	public boolean validateMobilePhone(String mobilePhone)
	{
		if ("".equals(mobilePhone) || null == mobilePhone)
		{
			// errors.put("passwordError", "密码不能为空");
			return false;
		}
		else if (!(mobilePhone.matches("1{1}\\d{10}")))// 正则表达式，匹配以1开头的11位正数
		{
			return false;
		}
		return true;
	}

	/**
	 * 校验邮箱
	 */
	public boolean validateEmail(String email)
	{
		if ("".equals(email) || null == email)
		{
			// errors.put("passwordError", "密码不能为空");
			return false;
		}
		else if (!(email
				.matches("^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$")))// 正则表达式，匹配邮箱格式
		{
			return false;
		}
		return true;
	}

	// -----------------------------------个人信息相关-------------------------------------------------
	/**
	 * 得到个人中心的用户信息
	 */
	public User getUserInfo(User user, boolean isDetail)
	{
		String hql = "";
		if (isDetail)
		{
			hql = "from User where id=?";
		}
		else
		{
			hql = "select new User(username,description,mood,head,point) from User where id=?";

		}
		List<User> list = query(hql, new Object[] { user.getId() });
		if (list != null && list.size() > 0)
		{
			return list.get(0);
		}
		return null;
	}

	/**
	 * 更新用户信息
	 */
	public boolean updateUserInfo(JSONObject jsonObject)
	{

		String realName = jsonObject.getString("realName");
		String email = jsonObject.getString("email");
		String mobilePhone = jsonObject.getString("mobilePhone");
		String mood = jsonObject.getString("mood");
		String description = jsonObject.getString("description");
		int userId = Integer.parseInt(jsonObject.getString("id"));
		int gender = jsonObject.getString("gender").equals("男") ? 0 : 1;
		Date birthday = DateUtils.string2date(jsonObject.getString("birthday"), "yyyy-MM-dd");

		// 查询数据库，得到user对象
		User user = getById(userId);
		if (user != null)
		{
			// 更新信息
			user.setRealName(realName);
			user.setEmail(email);
			user.setMobilePhone(mobilePhone);
			user.setMood(mood);
			user.setDescription(description);
			user.setGender(gender);
			user.setBirthday(birthday);
			update(user);
			return true;
		}

		return false;
	}

	/**
	 * 检测用户是否由于密码输错次数超过三次而产生验证码
	 */
	public int getPasswordErrorCount(String username)
	{
		String hql = "select passwordErrorCount from User where username=?";
		List<Integer> list = query(hql, new Object[] { username });
		if (list != null && list.size() > 0)
		{
			Integer passwordErrorCount = list.get(0);
			return passwordErrorCount == null ? 0 : passwordErrorCount.intValue();
		}
		else
		{
			return 0;
		}
	}

	/**
	 * 检查用户名是否存在
	 */
	public boolean checkUsernameExists(String username)
	{
		String hql = "from User where username=?";
		User user = (User) queryInUniqueResult(hql, new Object[] { username });
		if (user != null)
		{
			return true;
		}
		return false;
	}

	public Team getTeam(int userId)
	{
		User user = getById(userId);
		Team team = user.getTeam();
		return team;
	}

	/**
	 * 上传用户头像
	 */
	public void uploadHead(Iterator<FileItem> it, int userId)
	{
		User user = getById(userId);
		while (it.hasNext())
		{
			FileItem fileItem = (FileItem) it.next();
			if (fileItem.isFormField())
			{
				continue;
			}
			else
			{
				String fileName = fileItem.getName();
				String fileType = FileUtils.getFileType(fileName);
				if (fileName != null)
				{
					File saveFile;
					try
					{
						saveFile = FileUtils.createFile(
								PropertiesUtils.getProperty("uploadFileBasePath")
										+ User.getUserHeadBasePath(userId), fileType);
						fileItem.write(saveFile);
						user.setHead(User.getUserHeadBasePath(userId) + saveFile.getName());
						update(user);
					}
					catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * 得到用户列表
	 */
	public List<User> getUserList()
	{
		String hql = "from User";
		List<User> userList = query(hql, null);
		return userList;
	}

}
