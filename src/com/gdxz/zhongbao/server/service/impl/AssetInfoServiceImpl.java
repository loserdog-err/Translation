package com.gdxz.zhongbao.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdxz.zhongbao.server.DAO.impl.DAOSupportImpl;
import com.gdxz.zhongbao.server.domain.AssetInfo;
import com.gdxz.zhongbao.server.domain.User;
import com.gdxz.zhongbao.server.service.AssetInfoService;
import com.gdxz.zhongbao.server.service.UserService;

@Service("assetInfoService")
public class AssetInfoServiceImpl extends DAOSupportImpl<AssetInfo> implements AssetInfoService
{
	@Autowired
	UserService userService;

	/**
	 * 根据用户信息得到用户的资产信息
	 */
	public AssetInfo getAssetInfo(int userId)
	{
		String hql = "from AssetInfo where userId=?";
		AssetInfo assetInfo = queryInUniqueResult(hql, new Object[] { userId });
		if (assetInfo == null)
		{
			assetInfo = new AssetInfo();
			User user = userService.getById(userId);
			assetInfo.setUser(user);
			save(assetInfo);
		}
		return assetInfo;

	}

}
