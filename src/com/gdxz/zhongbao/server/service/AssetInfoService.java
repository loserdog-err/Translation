package com.gdxz.zhongbao.server.service;

import org.springframework.dao.support.DaoSupport;

import com.gdxz.zhongbao.server.DAO.DAOSupport;
import com.gdxz.zhongbao.server.domain.AssetInfo;

public interface AssetInfoService extends DAOSupport<AssetInfo>
{

	AssetInfo getAssetInfo(int userId);

}
