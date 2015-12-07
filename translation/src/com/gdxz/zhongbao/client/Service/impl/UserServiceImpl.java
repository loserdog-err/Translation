package com.gdxz.zhongbao.client.Service.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.gdxz.zhongbao.client.Service.UserService;
import com.gdxz.zhongbao.client.common.MyApplication;
import com.gdxz.zhongbao.client.domain.SystemConfig;
import com.gdxz.zhongbao.client.domain.Team;
import com.gdxz.zhongbao.client.domain.UploadConstant;
import com.gdxz.zhongbao.client.domain.User;
import com.gdxz.zhongbao.client.utils.DatabaseUtils;
import com.gdxz.zhongbao.client.utils.DialogUtils;
import com.gdxz.zhongbao.client.utils.FileUtils;
import com.gdxz.zhongbao.client.utils.HandleResponseCode;
import com.gdxz.zhongbao.client.utils.HttpUtils;
import com.gdxz.zhongbao.client.utils.L;
import com.gdxz.zhongbao.client.utils.SpUtils;
import com.gdxz.zhongbao.client.view.activity.UserRankActivity;
import com.gdxz.zhongbao.client.view.activity.LoginActivity;
import com.gdxz.zhongbao.client.view.activity.PersonalDetailInfoSettingActivity;
import com.gdxz.zhongbao.client.view.activity.PersonalSettingActivity;
import com.gdxz.zhongbao.client.view.activity.RegistActivity;
import com.gdxz.zhongbao.client.view.activity.TeamIndexActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;

/**
 * 业务逻辑层 凡是以validateXXX命名的方法为不带业务逻辑的校验
 *
 * @author chenantao
 */
public class UserServiceImpl implements UserService
{
	// 存放错误信息的list
	private List<String> errors;
	// Map<String, String> errors=new HashMap<>();

	// 服务器返回的json
	JSONObject jsonObject;
	// -------------------------------------登录相关------------------------------------------------------

	/**
	 * 登录方法
	 * 1：需要把登录信息写进本地sp里
	 * 2：需要设置别名到Jpush上
	 */
	@Override
	public void login(final User user, final Context context, final boolean haveVerifyCode, final
	Handler handler)
	{
		errors = new ArrayList<>();
		new AsyncTask<Void, Void, Boolean>()
		{
			@Override
			protected Boolean doInBackground(Void... params)
			{
				String username = user.getUsername();
				String password = user.getPassword();
				if (validateUsername(username) && validatePassword(password)
						&& (haveVerifyCode == true ? validateVerifyCode(user.getVerifyCode()) :
						true))
				{
					Map<String, String> rawParams = new HashMap<>();
					// 设置请求参数
					rawParams.put("username", username);
					rawParams.put("password", password);
					if (haveVerifyCode)
					{
						String verifyCode = user.getVerifyCode();
						rawParams.put("verifyCode", verifyCode);
					}
					// 设置服务端servlet调用哪个方法
					rawParams.put("method", "login");
					String url = HttpUtils.BASE_URL + "UserServlet";
					// 发送请求
					try
					{
						jsonObject = new JSONObject(HttpUtils.postRequest(url, rawParams));
						// 代表登录成功,把登录信息写进xml文件
						if (jsonObject.getBoolean("loginIsSuccess"))
						{
							user.setId(jsonObject.getInt("userId"));
							user.setUsername(jsonObject.getString("username"));
							user.setPoint(Integer.parseInt(jsonObject.getString("point")));
							writeMsgToSp(context, user);
							//设置别名
							setAliasInJpush(context, user.getUsername());
							return true;
						}
						// 代表登录失败
						else
						{
							final String error = jsonObject.getString("error");
							errors.add(error);
							L.e("error:" + error);
							if (jsonObject.getBoolean("haveVerifyCode"))
							{
								L.e("verify true");
								Message msg = Message.obtain();
								msg.what = LoginActivity.HAS_VERIFYCODE;
								handler.sendMessage(msg);
							}
							return false;
						}
					} catch (Exception e)
					{
						e.printStackTrace();
						errors.add("服务器错误");
						return false;
					}
				} else
				{
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				if (result)
				{
					handler.sendEmptyMessage(LoginActivity.LOGIN_SUCCESS);
				} else
				{
					Message msg = Message.obtain();
					msg.what = LoginActivity.LOGIN_FAILER;
					msg.obj = errors.get(0);
					handler.sendMessage(msg);
				}
				super.onPostExecute(result);
			}

		}.execute();
	}

	// -------------------------------------注册相关------------------------------------------------------
	@Override
	public void regist(final User user, final Handler handler)
	{
		new AsyncTask<Void, Void, Boolean>()
		{

			@Override
			protected Boolean doInBackground(Void... params)
			{
				errors = new ArrayList<>();
				String username = user.getUsername();
				String password = user.getPassword();
				String rePassword = user.getRePassowrd();
				String mobilePhone = user.getMobilePhone();
				String email = user.getEmail();
				if (validateEmail(email) && validateMobilePhone(mobilePhone + "")
						&& validatePassword(password)
						&& validateRePassword(password, rePassword)
						&& validateUsername(username))
				{
					if (!checkUsernameExistsSync(username))//如果用户名不存在
					{
						Map<String, String> rawParams = new HashMap<>();
						// 设置请求参数
						rawParams.put("username", username);
						rawParams.put("password", password);
						rawParams.put("rePassword", rePassword);
						rawParams.put("mobilePhone", mobilePhone + "");
						rawParams.put("email", email);
						// 设置服务端servlet调用哪个方法
						rawParams.put("method", "regist");
						String url = HttpUtils.BASE_URL + "UserServlet";
						// 发送请求
						try
						{
							jsonObject = new JSONObject(HttpUtils.postRequest(url, rawParams));
							// 代表注册成功，把信息写进文件里
							if (jsonObject.getBoolean("registIsSuccess"))
							{
								user.setId(jsonObject.getInt("userId"));
								user.setPoint(1000);
								return true;
							}
							// 代表注册失败
							else
							{
								errors.add(jsonObject.get("error").toString());
								return false;
							}
						} catch (Exception e)
						{
							e.printStackTrace();
							return false;
						}
					} else
					{
						errors.add("用户名已存在");
						return false;
					}

				} else
				// 校验失败
				{
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				if (result)
				{
					final Message msg = Message.obtain();
					JMessageClient.register(user.getUsername(), user.getPassword(),
							new BasicCallback()
							{
								@Override
								public void gotResult(int i, String s)
								{
									if (i == 0)//注册到jmessage成功
									{
										Log.e("TAG", "注册Jmessage帐号成功");
										msg.what = RegistActivity.MSG_REGISTER_SUCCESS;
										msg.obj = user;
										handler.sendMessage(msg);
									} else//注册到jmessage失败
									{
										Log.e("TAG", "注册Jmessage失败：" + i);
										handler.sendEmptyMessage(RegistActivity
												.MSG_REGISTER_FROM_JMESSAGE_ERROR);
									}
								}
							});

				} else
				{
					if (errors != null && errors.size() > 0)
					{
						Message msg = Message.obtain();
						msg.what = RegistActivity.MSG_REGISTER_INFO_ERROR;
						msg.obj = errors;
						handler.sendMessage(msg);
					} else
					{
						handler.sendEmptyMessage(RegistActivity.MSG_REGISTER_INFO_ERROR);
					}
				}
				super.onPostExecute(result);
			}
		}.execute();

	}
	// -------------------------------------校验相关------------------------------------------------------

	/**
	 * 用户更新信息的校验，只需校验真实姓名，手机，邮箱，且当长度大于0时才进行校验
	 *
	 * @param realName
	 * @param mobilePhone
	 * @param email
	 * @return
	 */

	public boolean validateUpdateInfo(String realName, String mobilePhone, String email)
	{
		boolean isSuccess = false;
		if (!realName.equals(""))
		{
			isSuccess = validateRealName(realName);
		}
		if (!mobilePhone.equals(""))
		{
			isSuccess = validateMobilePhone(mobilePhone);
		}
		if (!email.equals(""))
		{
			isSuccess = validateEmail(email);
		}
		return isSuccess;
	}

	/**
	 * 校验验证码
	 */
	@Override
	public boolean validateVerifyCode(String verifyCode)
	{
		if ("".equals(verifyCode) || null == verifyCode)
		{
			// errors.put("verifyCodeError", "密码不能为空");
			errors.add("验证码不能为空");
			return false;
		} else
		{
			if (verifyCode.length() != LoginActivity.VERIFYCODE_LENGTH)
			{
				// errors.put("verifyCodeError", "用户名长度必须在6到14之间");
				errors.add("验证码有误");
				return false;
			}
		}
		return true;
	}

	/**
	 * 校验用户名
	 */
	public boolean validateUsername(String username)
	{
		if ("".equals(username) || null == username)
		{
			// errors.put("usernameError", "用户名不能为空");
			errors.add("用户名不能为空");
			return false;
		} else
		{
			if (username.length() > 10 || username.length() < 5)
			{
				// errors.put("usernameError", "用户名长度必须在6到16之间");
				System.out.println("validate: " + username.length());
				errors.add("用户名长度必须在5到16之间");
				return false;
			}
		}
		return true;
	}

	/**
	 * 校验密码
	 */
	@Override
	public boolean validatePassword(String password)
	{
		if ("".equals(password) || null == password)
		{
			// errors.put("passwordError", "密码不能为空");
			errors.add("密码不能为空");
			return false;
		} else
		{
			if (password.length() > 14 || password.length() < 6)
			{
				// errors.put("passwordError", "用户名长度必须在6到14之间");
				errors.add("密码长度必须在6到14之间");
				return false;
			}
		}
		return true;
	}

	/**
	 * 校验重复密码
	 */
	@Override
	public boolean validateRePassword(String password, String rePassword)
	{
		if ("".equals(rePassword) || null == rePassword)
		{
			// errors.put("passwordError", "密码不能为空");
			errors.add("重复密码不能为空");
			return false;
		} else if (rePassword.length() > 14 || rePassword.length() < 6)
		{
			errors.add("重复密码长度必须在6到14之间");
			return false;
		} else if (!(password.equals(rePassword)))
		{
			errors.add("两次输入的密码不一致");
			return false;
		}
		return true;
	}

	/**
	 * 校验手机号
	 */
	@Override
	public boolean validateMobilePhone(String mobilePhone)
	{
		if ("".equals(mobilePhone) || null == mobilePhone)
		{
			// errors.put("passwordError", "密码不能为空");
			errors.add("手机号不能为空");
			return false;
		} else if (!(mobilePhone.matches("1\\d{10}")))// 正则表达式，匹配以1开头的11位正数
		{
			errors.add("手机号格式不对");
			return false;
		}
		return true;
	}

	/**
	 * 校验邮箱
	 */
	@Override
	public boolean validateEmail(String email)
	{
		if ("".equals(email) || null == email)
		{
			errors.add("邮箱不能为空");
			return false;
		} else if (email.length() > 25)
		{
			errors.add("邮箱格式错误");
			return false;
		} else
		{
			Pattern pattern = Pattern
					.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
			Matcher matcher = pattern.matcher(email);
			if (matcher.matches())
			{
				return true;
			} else
			{
				errors.add("邮箱格式错误");
				return false;
			}
		}
	}

	/**
	 * 设置验证码图片
	 */
	@Override
	public void setVerifyCodeImage(final ImageView imageView)
	{
		new AsyncTask<java.lang.Void, java.lang.Void, Bitmap>()
		{

			@Override
			protected Bitmap doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "UserServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("method", "getVerifyCode");
				InputStream is = HttpUtils.getResourceInputStream(url, rawParams);
				Bitmap bitmap = BitmapFactory.decodeStream(is);
				return bitmap;
			}

			@Override
			protected void onPostExecute(Bitmap result)
			{
				imageView.setImageBitmap(result);
			}
		}.execute();
	}

	/**
	 * 检验真实用户名
	 */
	@Override
	public boolean validateRealName(String realName)
	{
		// TODO Auto-generated method stub
		return false;
	}
	// -------------------------------------用户信息设置相关------------------------------------------------------

	/**
	 * 更新用户信息
	 */
	public boolean updateUserInfo(final Map<String, String> userInfo,
	                              final Handler handler, TextView tvErrorInfo)
	{
		errors = new ArrayList<>();
		String realName = userInfo.get("realName");
		String email = userInfo.get("email");
		String mobilePhone = userInfo.get("mobilePhone");
		String mood = userInfo.get("mood");
		String description = userInfo.get("description");
		// 校验用户输入，由于性别、生日字段为非用户输入项，心情、个人描述项长度大小在editText被限制，所以不用进行校验
		if (validateUpdateInfo(realName, mobilePhone, email))
		{
			new AsyncTask<java.lang.Void, java.lang.Void, Boolean>()
			{

				@Override
				protected Boolean doInBackground(Void... params)
				{
					String url = HttpUtils.BASE_URL + "UserServlet";
					Map<String, String> rawParams = new HashMap<>();
					rawParams.put("method", "updateUserInfo");
					// System.out.println("client data:"
					// + new JSONObject(userInfo).toString());
					rawParams.put("userInfo", new JSONObject(userInfo).toString());
					try
					{
						jsonObject = new JSONObject(HttpUtils.postRequest(url,
								rawParams));
						if (jsonObject.getBoolean("isSuccess"))
						{
							return true;
						}
					} catch (Exception e)
					{
						e.printStackTrace();
						return false;
					}
					return false;
				}

				@Override
				protected void onPostExecute(Boolean result)
				{
					if (result)// 更新用户信息成功
					{
						handler.sendEmptyMessage(PersonalDetailInfoSettingActivity
								.UPDATE_USER_DETAIL_INFO_SUCCESS);
					} else
					{
						handler.sendEmptyMessage(PersonalDetailInfoSettingActivity
								.UPDATE_USER_DETAIL_INFO_ERROR);
					}
				}
			}.execute();
			return true;
		} else
		{
//            Log.e("TAG", errors.get(0));
			tvErrorInfo.setText(errors.get(0));
			handler.sendEmptyMessage(PersonalDetailInfoSettingActivity
					.UPDATE_USER_DETAIL_INFO_ERROR);
			return false;
		}
	}

	/**
	 * 加载用户的详细信息
	 */
	@Override
	public void getUserInfo(final String userId, final Handler handler,
	                        final boolean isDetail)
	{
		new AsyncTask<Void, Void, Object>()
		{

			@Override
			protected Object doInBackground(Void... params)
			{
				String methodName = "";
				String url = HttpUtils.BASE_URL + "UserServlet";
				Map<String, String> rawParams = new HashMap<>();
				if (isDetail)
				{
					methodName = "getUserDetailInfo";
				} else
				{
					methodName = "getUserInfo";
				}
				rawParams.put("method", methodName);
				rawParams.put("id", userId + "");
				try
				{
					jsonObject = new JSONObject(
							HttpUtils.postRequest(url, rawParams));
					boolean isSuccess = jsonObject.getBoolean("isSuccess");
					if (isSuccess)// 成功得到对应user
					{
						Gson gson = new Gson();
						User user = gson.fromJson(
								jsonObject.getJSONObject("userInfo").toString(),
								User.class);
						if (isDetail)
						{
							return user;
						} else
						{
							Map<String, Object> data = new HashMap<>();
							Map<String, String> userDynamicCount = gson.fromJson(jsonObject
									.getJSONObject
											("userDynamicCount").toString(), new
									TypeToken<Map<String,
											Object>>()
									{
									}.getType());
							data.put("userInfo", user);
							data.put("userDynamicCount", userDynamicCount);
							return data;
						}
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

			@Override
			protected void onPostExecute(Object result)
			{
				if (result != null)
				{
					if (isDetail)
					{
						Message msg = Message.obtain();
						msg.what = PersonalDetailInfoSettingActivity.SET_USER_DETAIL_INFO;
						msg.obj = result;
						handler.sendMessage(msg);
					} else
					{
						Message msg = Message.obtain();
						msg.what = PersonalSettingActivity.HANDLER_LOAD_DATA_COMPLETE;
						msg.obj = result;
						handler.sendMessage(msg);
					}
				}

			}

		}.execute();
	}

	/**
	 * 加载用户的头像
	 */
	@Override
	public void getUserHead(final String userId, final ImageView ivHead)
	{
		new AsyncTask<java.lang.Void, java.lang.Void, Bitmap>()
		{

			@Override
			protected Bitmap doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "UserServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("method", "getUserHead");
				rawParams.put("userId", userId);
				InputStream is = HttpUtils.getResourceInputStream(url, rawParams);
				Bitmap bitmap = BitmapFactory.decodeStream(is);
				if (bitmap == null)
				{
					return null;
				}
				if (is != null)
				{
					try
					{
						is.close();
					} catch (Exception e)
					{
						e.printStackTrace();
					}

				}
				return bitmap;
			}

			protected void onPostExecute(Bitmap result)
			{
				if (result != null)
				{
					ivHead.setImageBitmap(result);
					//将用户头像存进sp中
					String path = FileUtils.saveBitmapInSdCard(result, System.currentTimeMillis()
							+ "");
					setCurrentUserStringInfo(ivHead.getContext(), User.SP_HEAD, path);
				}
			}
		}.execute();
	}

	/**
	 * 上传头像
	 */
	@Override
	public void uploadHead(final String userId, final String path, final ImageView head)
	{
		new AsyncTask<java.lang.Void, java.lang.Void, Boolean>()
		{

			@Override
			protected Boolean doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "UploadFileServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("userId", userId);
				rawParams.put("category", UploadConstant.CATEGORY_UPLOAD_HEAD);
				List<File> files = new ArrayList<>();
				File file = new File(path);
				files.add(file);
				if (HttpUtils.upLoadFile(url, files, rawParams))
				{
					return true;
				} else
				{
					return false;
				}
			}

			protected void onPostExecute(Boolean result)
			{
				if (result)
				{
					Bitmap bitmap = BitmapFactory.decodeFile(path);
					head.setImageBitmap(bitmap);
					DialogUtils.closeProgressDialog();
					setCurrentUserStringInfo(head.getContext(), User.SP_HEAD, path);
//                    Log.e("TAG", "上传成功");
				} else
				{
					Log.e("TAG", "上传失败");
				}
			}
		}.execute();
	}

	/**
	 * 同步检查用户名是否存在
	 *
	 * @param username
	 * @return
	 */
	public boolean checkUsernameExistsSync(final String username)
	{
		String url = HttpUtils.BASE_URL + "UserServlet";
		Map<String, String> rawParams = new HashMap<>();
		rawParams.put("method", "checkUsernameExists");
		rawParams.put("username", username);
		try
		{
			JSONObject json = new JSONObject(HttpUtils.postRequest(url, rawParams));
			if (json.getBoolean("isExists"))
			{
				return true;
			} else
			{
				return false;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			return true;
		}
	}

	/**
	 * 异步检查用户名是否已存在
	 *
	 * @param username
	 * @param handler
	 */
	@Override
	public void checkUsernameExistsAsync(final String username, final Handler handler)
	{
		new AsyncTask<Void, Void, Boolean>()
		{

			@Override
			protected Boolean doInBackground(Void... params)
			{
				return checkUsernameExistsSync(username);
			}

			@Override
			protected void onPostExecute(Boolean result)
			{
				if (result)
				{
					handler.sendEmptyMessage(RegistActivity.MSG_USERNAME_EXISTS);
				} else
				{
					handler.sendEmptyMessage(RegistActivity.MSG_USERNAME_NOT_EXISTS);
				}
				super.onPostExecute(result);
			}
		}.execute();
	}

	/**
	 * 设置当前登录用户的id
	 *
	 * @param context
	 * @param userId
	 */
	public static void setCurrentUserId(Context context, String userId)
	{
		SpUtils.setStringProperty(context, "userId", userId);
	}

	/**
	 * 得到当前登录用户的id
	 *
	 * @param context
	 * @return
	 */
	public static String getCurrentUserId(Context context)
	{
		return SpUtils.getStringProperty(context, "userId");
	}

	/**
	 * 设置当前登录用户的字符串信息
	 */
	public static void setCurrentUserStringInfo(Context context, String key, String value)
	{
		//得到当前登录用户的id
		String userId = getCurrentUserId(context);
		SpUtils.setStringProperty(context, key, value, userId);
	}

	/**
	 * 设置当前登录用户的Set集合信息
	 *
	 * @param context
	 * @param key
	 * @param
	 */
	public static void setCurrentUserSetInfo(Context context, String key, Set<String> set)
	{
		//得到当前登录用户的id
		String userId = getCurrentUserId(context);
		SpUtils.setStringSetProperty(context, key, set, userId);
	}

	/**
	 * 得到当前登录用户的字符串信息
	 */
	public static String getCurrentUserStringInfo(Context context, String key)
	{
		//得到当前登录用户的id
		String userId = getCurrentUserId(context);
		return SpUtils.getStringProperty(context, key, userId);
	}

	/**
	 * 得到当前登录用户的字符串集合信息
	 */
	public static Set<String> getCurrentUserSetInfo(Context context, String key)
	{
		//得到当前登录用户的id
		String userId = getCurrentUserId(context);
		return SpUtils.getStringSetProperty(context, key, userId);
	}

	/**
	 * 得到当前用户的团队
	 *
	 * @param currentUserId
	 * @param handler
	 */
	@Override
	public void getTeam(final String currentUserId, final Handler handler)
	{
		new AsyncTask<Void, Void, Team>()
		{
			@Override
			protected Team doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "UserServlet";
				Map<String, String> rawParams = new HashMap<>();
				rawParams.put("method", "getTeam");
				rawParams.put("userId", currentUserId);
				try
				{
					JSONObject json = new JSONObject(HttpUtils.postRequest(url, rawParams));
					if (json.getBoolean("haveTeam"))
					{
						Team team = new Gson().fromJson(json.getJSONObject("team").toString(),
								Team.class);
						return team;
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

			@Override
			protected void onPostExecute(Team team)
			{
				Message msg = Message.obtain();
				msg.what = TeamIndexActivity.MSG_GET_TEAM;
				if (team != null)
				{
					msg.obj = team;
				}
				handler.sendMessage(msg);
				super.onPostExecute(team);
			}
		}.execute();
	}

	/**
	 * 加载服务器的用户列表
	 *
	 * @param handler
	 */
	@Override
	public void getUserList(final Handler handler)
	{
		new AsyncTask<Void, Void, List<User>>()
		{

			@Override
			protected List<User> doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "UserServlet";
				Map<String, String> rawParams = new HashMap<String, String>();
				rawParams.put("method", "getUserList");
				try
				{
					JSONObject json = new JSONObject(HttpUtils.postRequest(url, rawParams));
					if (json.getBoolean("isSuccess"))
					{
						Gson gson = new Gson();
						List<User> userList = gson.fromJson(json.getString("userList"), new
								TypeToken<List<User>>()
						{
						}.getType());
						return userList;
					}
				} catch (Exception e)
				{
					e.printStackTrace();
					return null;
				}
				return null;
			}

			@Override
			protected void onPostExecute(List<User> users)
			{
				if (users != null)
				{
					Message msg = Message.obtain();
					msg.what = UserRankActivity.MSG_DATA_LOAD_COMPLETE;
					msg.obj = users;
					handler.sendMessage(msg);
				}
				super.onPostExecute(users);
			}
		}.execute();
	}

	@Override
	public void inviteToAnswer(final Integer id,final int questionId)
	{
		new AsyncTask<Void, Void, Void>()
		{

			@Override
			protected Void doInBackground(Void... params)
			{
				String url = HttpUtils.BASE_URL + "UserServlet";
				Map<String, String> rawParams = new HashMap<String, String>();
				rawParams.put("method", "inviteToAnswer");
				rawParams.put("userId", id+"");
				rawParams.put("questionId", questionId+"");
				try
				{
					HttpUtils.postRequest(url, rawParams);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				return null;
			}
		}.execute();
	}

	/**
	 * 将登录的一些信息写进sp中
	 */
	public static void writeMsgToSp(Context context, User user)
	{
		if (user.getId() != null)
		{
			UserServiceImpl.setCurrentUserId(context, user.getId() + "");
			UserServiceImpl.setCurrentUserStringInfo(context, "userId", user.getId() + "");
		}
		if (user.getUsername() != null)
		{
			UserServiceImpl.setCurrentUserStringInfo(context, "username", user.getUsername());
		}
		if (user.getPassword() != null)
		{
			UserServiceImpl.setCurrentUserStringInfo(context, "password", user.getPassword());
		}
		if (user.getPoint() != null)
		{
			UserServiceImpl.setCurrentUserStringInfo(context, "point", user.getPoint() + "");
		}
	}

	/**
	 * 在jpush上设置别名
	 */
	public static void setAliasInJpush(final Context context, String alias)
	{
		//在jpush上设置别名
		JPushInterface.setAlias(context.getApplicationContext(), alias, new TagAliasCallback()
		{
			@Override
			public void gotResult(int i, String s, Set<String> set)
			{
				if (i == 0)
				{
					L.e("设置别名成功");
				} else
				{
					HandleResponseCode.onHandle(context, i);
				}
			}
		});
	}

	/**
	 * 登录到jmessage
	 */
	public static void loginInJmessage(String username, String password, final Handler handler)
	{
		JMessageClient.login(username, password, new BasicCallback()
		{
			@Override
			public void gotResult(int i, String s)
			{
				if (i == 0)
				{
					handler.sendEmptyMessage(LoginActivity.LOGIN_JMESSAGE_SUCCESS);
					Log.e("TAG", "Jmessage登录成功");
				} else
				{
					Log.e("TAG", "Jmessage登录失败：" + i);
				}
			}
		});
	}

	public static void loadGlobalValue(Context context)
	{
		DbUtils utils = DatabaseUtils.getDbUtils(context);
		String userId = UserServiceImpl.getCurrentUserId(context);
		if (userId != null && !"".equals(userId))
		{
			int currentUserId = Integer.parseInt(userId);
			SystemConfig config = null;
			try
			{
				config = utils.findFirst(Selector.from(SystemConfig.class).where("id", "=",
						currentUserId));
				if (config != null)
				{
					MyApplication.isAutoLogin = config.isAutoLogin();
					MyApplication.isReceivePush = config.isReceivePush();
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}


}
