package com.gdxz.zhongbao.server.service;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;

import com.gdxz.zhongbao.server.DAO.DAOSupport;
import com.gdxz.zhongbao.server.domain.Team;

public interface TeamService extends DAOSupport<Team>
{

	void createTeam(Team team,int userId);

	List<Team> getTeamList(int category, String condition);

	Team getTeamById(long teamId);

	void addGroupMember(long teamId, int userId);

	void disagreeJoin(long teamId, int userId);

	boolean exitGroup(int userId, long teamId,String newOwner);

	void uploadTeamLogo(Long groupId, Iterator<FileItem> it) throws Exception;


}
