package com.gdxz.zhongbao.server.service;

import java.util.List;
import java.util.Map;

import com.gdxz.zhongbao.server.DAO.DAOSupport;
import com.gdxz.zhongbao.server.domain.User;
import com.gdxz.zhongbao.server.domain.UserDynamic;

public interface UserDynamicService extends DAOSupport<UserDynamic>
{

	List<UserDynamic> loadDynamicData(int category, int userId);

	Map<String, String> getUserDynamicCount(Integer id);

	String getUserAllDynamicCount(Integer id);

	String getUserAnswerDynamicCount(Integer id);

	String getUserAskDynamicCount(Integer id);

	String getUserFollowDynamicCount(Integer id);

	List<User> getFollwersByQuestionId(Integer id);

}
