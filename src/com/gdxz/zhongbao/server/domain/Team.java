package com.gdxz.zhongbao.server.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jms.Topic;

public class Team
{
	private Long id;// 由Jmessage生成的GroupId生成
	private String name;// 团队名称
	private Date buildTime;// 创办时间
	private String declaration; // 团队宣言
	private int score;// 团队分数，决定团队排名的因素
	private String logo;//团队logo
	public String getLogo()
	{
		return logo;
	}

	public void setLogo(String logo)
	{
		this.logo = logo;
	}

	private int rank;// 团队排名 select name,score,(select count(*) from t_text
						// where a.score<=score) as pm from t_text as a;

	// 一个队伍有多个成员，一个成员只能有一个队伍
//	private Set<User> members = new HashSet<User>();// 队伍的成员

	// 一个队伍只能有一个队长
//	private User captain;// 队长
//

	public static String getTeamImageBasePath()
	{
		return "image/team/";
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public int getScore()
	{
		return score;
	}

	public void setScore(int score)
	{
		this.score = score;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Date getBuildTime()
	{
		return buildTime;
	}

	public void setBuildTime(Date buildTime)
	{
		this.buildTime = buildTime;
	}

	public String getDeclaration()
	{
		return declaration;
	}

	public void setDeclaration(String declaration)
	{
		this.declaration = declaration;
	}

	public int getRank()
	{
		return rank;
	}

	public void setRank(int rank)
	{
		this.rank = rank;
	}



}
