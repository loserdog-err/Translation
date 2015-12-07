package com.gdxz.zhongbao.server.DAO.impl;

import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gdxz.zhongbao.server.DAO.DAOSupport;
import com.gdxz.zhongbao.server.domain.Answer;

@Transactional
public class DAOSupportImpl<T> implements DAOSupport<T>
{
	@Resource
	private SessionFactory sessionFactory;

	@Resource
	private HibernateTemplate hibernateTemplate;

	private Class<T> clazz;

	public DAOSupportImpl()
	{
		ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
		this.clazz = (Class<T>) pt.getActualTypeArguments()[0];
		// System.out.println(clazz);
	}

	/**
	 * 保存数据
	 */
	public void save(T t)
	{

		hibernateTemplate.save(t);
	}

	/**
	 * 根据Id获得对象
	 */

	public T getById(Number id)
	{
		return (T) hibernateTemplate.get(clazz, id);
	}

	/**
	 * 执行hql语句获得list
	 */
	public List query(String hql, Object[] parameters)
	{
		List list = null;
		try
		{
			if (parameters != null && parameters.length > 0)
			{
				list = hibernateTemplate.find(hql, parameters);
			}
			else
			{
				list = hibernateTemplate.find(hql);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return list;
	}

	/**
	 * 通过id删除对象
	 */
	public void deleteById(Number id)
	{
		T t = (T) hibernateTemplate.get(clazz, id);
		hibernateTemplate.delete(t);
	}

	/**
	 * 通过id数组得到对象
	 */
	public List getByIds(final String hql, final Object[] ids)
	{
		List list = null;
		try
		{
			list = hibernateTemplate.executeFind(new HibernateCallback()
			{

				public Object doInHibernate(Session session) throws HibernateException,
						SQLException
				{
					Query query = session.createQuery(hql);
					if (ids != null && !("".equals(ids)))
					{
						query.setParameterList("ids", ids);
					}
					return query.list();

				}
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return list;

	}

	/**
	 * 单一结果查询
	 */
	public T queryInUniqueResult(final String hql, final Object[] params)
	{

		Object result = hibernateTemplate.execute(new HibernateCallback()
		{

			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Query query = session.createQuery(hql);
				if (params != null && params.length > 0)
				{
					for (int i = 0; i < params.length; i++)
					{
						query.setParameter(i, params[i]);
					}
				}
				return query.uniqueResult();
			}
		});
		return (T) result;
	}

	/**
	 * 查询记录数
	 */
	public int queryCount(String hql, final Object[] params)
	{
		Number number=(Number) queryInUniqueResult(hql, params);
		if(number!=null)
		{
			return number.intValue();
		}
		else
		{
			return 0;
		}
	}

	/**
	 * 更新对象
	 */
	public void update(T t)
	{

		hibernateTemplate.update(t);
	}

	/**
	 * 分页查询
	 */
	public List<T> getByPage(final int pageNow, final int pageSize, final String hql,
			final Object[] parameters)
	{
		List recordList = hibernateTemplate.executeFind(new HibernateCallback()
		{

			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{

				Query query = session.createQuery(hql);
				if (parameters != null && !("".equals(parameters)))
				{
					for (int i = 0; i < parameters.length; i++)
					{
						query.setParameter(i, parameters[i]);
					}
				}
				query.setFirstResult((pageNow - 1) * (pageSize)).setMaxResults(pageSize);
				List list = query.list();
				return list;
			}
		});

		return recordList;

	}

	// 执行hql语句
	public void executeHql(final String hql, final Object[] parameters)
	{
		if (parameters != null && !("".equals(parameters)))
		{
			hibernateTemplate.execute(new HibernateCallback()
			{

				public Object doInHibernate(Session session) throws HibernateException,
						SQLException
				{
					Query query = session.createQuery(hql);
					for (int i = 0; i < parameters.length; i++)
					{
						query.setParameter(i, parameters[i]);
					}
					query.executeUpdate();
					return null;
				}
			});
		}
	}

}
