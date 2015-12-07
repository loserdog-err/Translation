package com.gdxz.zhongbao.server.text;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.id.GUIDGenerator;
import org.hibernate.id.UUIDHexGenerator;
import org.junit.Test;
import org.omg.CORBA.UserException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.gdxz.zhongbao.server.domain.Answer;
import com.gdxz.zhongbao.server.domain.AssetInfo;
import com.gdxz.zhongbao.server.domain.Orders;
import com.gdxz.zhongbao.server.domain.Question;
import com.gdxz.zhongbao.server.domain.Team;
import com.gdxz.zhongbao.server.domain.User;
import com.gdxz.zhongbao.server.service.AnswerService;
import com.gdxz.zhongbao.server.service.AssetInfoService;
import com.gdxz.zhongbao.server.service.OrderService;
import com.gdxz.zhongbao.server.service.QuestionService;
import com.gdxz.zhongbao.server.service.TeamService;
import com.gdxz.zhongbao.server.service.UserDynamicService;
import com.gdxz.zhongbao.server.service.UserRemindService;
import com.gdxz.zhongbao.server.service.UserService;
import com.gdxz.zhongbao.server.utils.JpushClientUtils;

public class Text
{
	private static ApplicationContext applicationContext;
	private static UserService userService;
	private static QuestionService questionService;
	public static AnswerService answerService;
	public static UserDynamicService userDynamicService;
	public static TeamService teamService;
	public static UserRemindService userRemindService;
	public static OrderService orderService;
	public static AssetInfoService assetInfoService;
	static
	{
		applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
		userService = (UserService) applicationContext.getBean("userService");
		questionService = (QuestionService) applicationContext.getBean("questionService");
		answerService = (AnswerService) applicationContext.getBean("answerService");
		userDynamicService = (UserDynamicService) applicationContext.getBean("userDynamicService");
		teamService = (TeamService) applicationContext.getBean("teamService");
		userRemindService = (UserRemindService) applicationContext.getBean("userRemindService");
		orderService = (OrderService) applicationContext.getBean("orderService");
		assetInfoService = (AssetInfoService) applicationContext.getBean("assetInfoService");
	}

	@Test
	public void text2()
	{
		int a=1000;
		System.out.println((float)a/100000);
	}
	

	@Test
	public void text()
	{
		User user = userService.getById(1);
		for (int i = 0; i < 20; i++)
		{
			Question question = new Question();
			question.setAuthor(user);
			question.setBrowseCount(250);
			question.setContent("装逼怎么当程序猿，程序猿怎么才能装逼" + i);
			question.setPostTime(new Date());
			question.setTitle("装逼怎么当程序猿?" + i);
			question.setReplyCount((int) (Math.random() * 100));
			question.setRewardAmount(250);
			question.setTodayReplyChange((int) (Math.random() * 100));
			questionService.save(question);
			int random = (int) (Math.random() * 2);
			if (random == 0)
			{
				System.out.println("true");
				question.setIsSolve(true);
				Answer answer = new Answer();
				answer.setAuthor(user);
				answer.setContent("去年买了个表" + i);
				answer.setPostTime(new Date());
				answer.setPraiseCount(100);
				answer.setDespiseCount(250);
				answer.setIsBest(true);
				answer.setQuestion(question);
				answerService.save(answer);
			}
			else
			{
				question.setIsSolve(false);
			}
			for (int j = 0; j < (int) (Math.random() * 15); j++)
			{
				Answer answer = new Answer();
				answer.setAuthor(user);
				answer.setContent("去年买了个表" + j);
				answer.setPostTime(new Date());
				answer.setPraiseCount(100);
				answer.setDespiseCount(250);
				answer.setQuestion(question);
				answerService.save(answer);
			}
			questionService.update(question);

		}
	}

	@Test
	public void add()
	{
		UserService userService = (UserService) applicationContext.getBean("userService");
		User user = new User();
		user.setUsername("xixihehe");
		user.setPassword("20140107");
		user.setMobilePhone("18819433192");
		user.setEmail("yellow@qq.com");
		userService.save(user);
	}

	@Test
	public void delete()
	{
		UserService userService = (UserService) applicationContext.getBean("userService");
		userService.deleteById(1);
	}

	@Test
	public void get()
	{
		UserService userService = (UserService) applicationContext.getBean("userService");
		User user = userService.getById(1);
		System.out.println(user.getUsername());
	}

	@Test
	public void update()
	{
		UserService userService = (UserService) applicationContext.getBean("userService");
		User user = userService.getById(1);
		user.setMood("最近的心情真的非常非常非常的不是很好");
		user.setDescription("男儿当自强");
		userService.update(user);
	}
}
