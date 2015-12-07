package com.gdxz.zhongbao.server.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.jpush.api.push.PushClient;

import com.gdxz.zhongbao.server.DAO.impl.DAOSupportImpl;
import com.gdxz.zhongbao.server.domain.Answer;
import com.gdxz.zhongbao.server.domain.Question;
import com.gdxz.zhongbao.server.domain.Team;
import com.gdxz.zhongbao.server.domain.User;
import com.gdxz.zhongbao.server.domain.UserDynamic;
import com.gdxz.zhongbao.server.domain.UserRemind;
import com.gdxz.zhongbao.server.service.AnswerService;
import com.gdxz.zhongbao.server.service.QuestionService;
import com.gdxz.zhongbao.server.service.TeamService;
import com.gdxz.zhongbao.server.service.UserDynamicService;
import com.gdxz.zhongbao.server.service.UserRemindService;
import com.gdxz.zhongbao.server.service.UserService;
import com.gdxz.zhongbao.server.utils.JpushClientUtils;

@Service("answerService")
public class AnswerServiceImpl extends DAOSupportImpl<Answer> implements AnswerService
{

	@Autowired
	UserService userService;
	@Autowired
	QuestionService questionService;
	@Autowired
	UserDynamicService userDynamicService;
	@Autowired
	TeamService teamService;
	@Autowired
	UserRemindService userRemindService;

	/**
	 * 发表回复
	 */
	public boolean postAnswer(int questionId, String answerContent, int userId)
	{
		try
		{
			Question question = questionService.getById(questionId);
			User author = userService.getById(userId);
			Answer answer = new Answer();
			answer.setAuthor(author);
			answer.setContent(answerContent);
			answer.setPostTime(new Date());
			answer.setQuestion(question);
			save(answer);
			// 维护用户动态表
			UserDynamic userDynamic = new UserDynamic();
			userDynamic.setAnswer(answer);
			userDynamic.setType(UserDynamic.DYNAMIC_TYPE_ANSWER);
			userDynamic.setUser(author);
			userDynamic.setUpdateTime(answer.getPostTime());
			userDynamicService.save(userDynamic);
			// 维护用户提醒表(设置提醒类型，关联的问题，以及题主的作者)
			UserRemind userRemind = new UserRemind(UserRemind.TYPE_NEW_ANSWER, answer,
					question.getAuthor(), new Date());
			userRemindService.save(userRemind);
			// 推送给题主有新的回复
			JpushClientUtils.sendPush(JpushClientUtils.buildPushObject_all_alias_alert(question
					.getAuthor().getUsername(), "你的问题:" + question.getTitle() + " 有了新的回复"));
			// 更新答主的正确率
			int answerCount = queryCount("select count(*) from Answer where userId=?",
					new Object[] { userId });// 得到用户回复的总数量
			int bestAnswerCount = queryCount(
					"select count(*) from Answer where userId=? and isBest=?", new Object[] {
							userId, true });// 得到用户最佳答案的数量
			float correctRate = (int) (((float) bestAnswerCount / (float) answerCount) * 100.0f) / 100.0f;
			author.setCorrectRate(correctRate);
			userService.update(author);
			return true;

		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * 点赞
	 */
	public void addPraiseCount(int answerId)
	{
		Answer answer = getById(answerId);
		Integer praiseCount = answer.getPraiseCount();
		answer.setPraiseCount(praiseCount == null ? 1 : praiseCount + 1);
		update(answer);
	}

	/**
	 * 鄙视
	 */
	public void addDespiseCount(int answerId)
	{
		Answer answer = getById(answerId);
		Integer despiseCount = answer.getDespiseCount();
		answer.setDespiseCount(despiseCount == null ? 1 : despiseCount + 1);
		update(answer);
	}

	/**
	 * 设置此答案为最佳答案
	 */
	public void setBestAnswer(int answerId)
	{
		Answer answer = getById(answerId);
		answer.setIsBest(true);
		/**
		 * 维护关联关系 1:设置此答案所归属的问题已解决 2:将问题所对应的悬赏积分奖励给最佳答案的用户
		 * 3:扣除提问者的积分4：用户所属团队得到相应的积分 5：添加消息提醒给 收藏者，回复者，最佳回答者
		 */
		Question question = answer.getQuestion();
		question.setIsSolve(true);
		// 增加回答者积分,推送给回答者,更新回答者答题正确率
		User user = answer.getAuthor();
		user.setPoint(user.getPoint() + question.getRewardAmount());
		// 更新答主的正确率
		int answerCount = queryCount("select count(*) from Answer where userId=?",
				new Object[] { user.getId() });// 得到用户回复的总数量
		int bestAnswerCount = queryCount("select count(*) from Answer where userId=? and isBest=?",
				new Object[] { user.getId(), true });// 得到用户最佳答案的数量
		float correctRate = (int) (((float) bestAnswerCount / (float) answerCount) * 100.0f) / 100.0f;
		user.setCorrectRate(correctRate);
		JpushClientUtils.sendPush(JpushClientUtils.buildPushObject_all_alias_alert(
				user.getUsername(),
				"恭喜您的答案在问题: " + question.getTitle() + " 中被采纳,增加积分: " + question.getRewardAmount()));
		// 扣除提问者积分
		User questioner = question.getAuthor();
		questioner.setPoint(questioner.getPoint() - question.getRewardAmount());
		// 增加回答者所属团队积分
		Team team = answer.getAuthor().getTeam();
		if (team != null)
		{
			System.out.println("team !=null");
			team.setScore(team.getScore() + question.getRewardAmount());
			teamService.update(team);
		}
		// 添加消息提醒给 收藏者，回复者，最佳回答者,同时推送
		List<User> followers = userDynamicService.getFollwersByQuestionId(question.getId());
		List<User> answerers = questionService.getAnswerersByQuestionId(question.getId());
		User bestAnswer = questionService.getBestAnswerer(question.getId());

		for (User follower : followers)// 关注的问题已解决
		{
			UserRemind userRemind = new UserRemind(UserRemind.TYPE_FOLLOW_QUESTION_SOLVE, answer,
					user, new Date());
			userRemindService.save(userRemind);
			JpushClientUtils.sendPush(JpushClientUtils.buildPushObject_all_alias_alert(
					follower.getUsername(), "您关注的问题 " + question.getTitle() + " 已有最佳答案，快去看看吧"));
		}
		for (User answerer : answerers)// 回复的问题已解决
		{
			UserRemind userRemind = new UserRemind(UserRemind.TYPE_ANSWER_QUESTION_SOLVE, answer,
					answerer, new Date());
			userRemindService.save(userRemind);
			JpushClientUtils.sendPush(JpushClientUtils.buildPushObject_all_alias_alert(
					answerer.getUsername(), "您回复过的问题 " + question.getTitle() + " 已有最佳答案，快去看看吧"));
		}
		if (bestAnswer != null)
		{
			UserRemind userRemind = new UserRemind(UserRemind.TYPE_ANSWER_ADOPT, answer,
					bestAnswer, new Date());
			userRemindService.save(userRemind);
		}

		update(answer);
		questionService.update(question);
		userService.update(questioner);
		userService.update(user);
	}
}
