package com.gdxz.zhongbao.server.DAO;

import java.util.List;

import org.hibernate.SessionFactory;


public interface DAOSupport<T>
{
	public void save(T t);
	
	public T getById(Number id);
	
	public void deleteById(Number id);
	
	public void update(T t);
	
	public List<T> query(String hql,Object[]parameters);
	
	public void executeHql(String hql,Object[]parameters);
	
	public List<T> getByIds(String hql,Object[] ids);
	
	public List<T> getByPage(final int pageNow,final int pageSize,final String hql,final Object[] parameters);
	
	public T queryInUniqueResult(final String hql, final Object[] params);
	
//	public User getUserByName(String username);

}
