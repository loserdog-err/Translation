package com.gdxz.zhongbao.client.domain;

import com.lidroid.xutils.db.annotation.Finder;
import com.lidroid.xutils.db.annotation.NoAutoIncrement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class User implements Serializable
{
	//一些存进sp中的字段常量
	public static final String SP_HEAD = "head";//头像路径


	//需存进数据库的
	@NoAutoIncrement
	private Integer id;
	private String username;
	private String password;
	private String mobilePhone;
	private boolean activity; // 用户是否被激活，1代表已激活，0代表未激活。
	private String email;
	private String address;
	private String description;// 个人说明
	private Integer point; // 用户所得积分
	private Integer gender; // 0代表男性，1代表女性
	private String mood; // 心情
	private String head; // 用户头像
	private String realName;
	private Date birthday;
	private Integer age;
	private float correctRate;

	public float getCorrectRate()
	{
		return correctRate;
	}

	public void setCorrectRate(float correctRate)
	{
		this.correctRate = correctRate;
	}

	public Team getTeam()
	{
		return team;
	}

	public void setTeam(Team team)
	{
		this.team = team;
	}

	private Team team;//所属团队

	//xutils需要的字段
	@Finder(valueColumn = "id", targetColumn = "userId")
	public List<Question> questions;
	@Finder(valueColumn = "id", targetColumn = "userId")
	public List<Answer> answers;

	// 业务逻辑相关
	private String verifyCode;
	private String rePassword;
	private List<String> errors = new ArrayList<String>();
	private String rightVerifyCode;


	public User()
	{
	}

	public User(String username, String description, String mood)
	{
		super();
		this.username = username;
		this.description = description;
		this.mood = mood;
	}

	/**
	 * 得到用户头像的根路径
	 *
	 * @param userId
	 * @return
	 */
	public static String getUserHeadBasePath(String userId)
	{
		return "image/" + userId + "/head/";
	}

	public Integer getAge()
	{
		return age;
	}

	public void setAge(Integer age)
	{
		this.age = age;
	}

	public String getRealName()
	{
		return realName;
	}

	public void setRealName(String realName)
	{
		this.realName = realName;
	}

	public Date getBirthday()
	{
		return birthday;
	}

	public void setBirthday(Date birthday)
	{
		this.birthday = birthday;
		if (birthday != null)
		{
			long day = (new Date().getTime() - birthday.getTime())
					/ (1000 * 60 * 60 * 24);
			setAge((int) (day / 365));
		}


	}

	public String getRePassowrd()
	{
		return rePassword;
	}

	public void setRePassword(String rePassowrd)
	{
		this.rePassword = rePassowrd;
	}

	public String getVerifyCode()
	{
		return verifyCode;
	}

	public void setVerifyCode(String verifyCode)
	{
		this.verifyCode = verifyCode;
	}

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getRePassword()
	{
		return rePassword;
	}

	public String getMobilePhone()
	{
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone)
	{
		this.mobilePhone = mobilePhone;
	}

	public boolean isActivity()
	{
		return activity;
	}

	public void setActivity(boolean activity)
	{
		this.activity = activity;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public Integer getPoint()
	{
		return point;
	}

	public void setPoint(Integer point)
	{
		this.point = point;
	}

	public Integer getGender()
	{
		return gender;
	}

	public void setGender(Integer gender)
	{
		this.gender = gender;
	}

	public String getMood()
	{
		return mood;
	}

	public void setMood(String mood)
	{
		this.mood = mood;
	}

	public String getHead()
	{
		return head;
	}

	public void setHead(String head)
	{
		this.head = head;
	}

	public List<String> getErrors()
	{
		return errors;
	}

	public void setErrors(List<String> errors)
	{
		this.errors = errors;
	}

	public String getRightVerifyCode()
	{
		return rightVerifyCode;
	}

	public void setRightVerifyCode(String rightVerifyCode)
	{
		this.rightVerifyCode = rightVerifyCode;
	}
	// private UserDynamic userDynamic;// 与历史动态一对一的映射关系
	// private List<Answer> answerList = new ArrayList<Answer>(); // 与问题一对多的映射关系
	// private List<Question> questionList = new ArrayList<Question>();//
	// 与回答一对多的映射关系
	// private List<Team> teamList = new ArrayList<Team>(); // 与团队多对多的映射关系
	// private List<Topic> topicList = new ArrayList<Topic>();// 与帖子一对多的映射关系
	// private List<Reply> replyList = new ArrayList<Reply>();// 与帖子回复一对多的映射关系
}
