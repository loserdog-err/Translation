package com.gdxz.zhongbao.server.utils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public class FormBeanUtils
{

	/**
	 * 一个将请求参数封装成指定类型的formBean的静态方法
	 * 
	 * @param <E>
	 * 
	 * @param parametrs
	 *            请求参数
	 * @param clazz
	 *            要封装成的类型
	 * @return 封装好的bena
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static <T, E> T toBean(Map<String, String[]> parametrs, Class<T> clazz)
	{
		try
		{
			T bean = clazz.newInstance();
			for (String key : parametrs.keySet())
			{
				// 得到方法名
				String methodName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
//				System.out.println("方法名为：" + methodName);
				// 遍历对应的domain中对应的方法，得到set方法参数的类型
				for (Method method : bean.getClass().getMethods())
				{
					// 匹配对应的set方法
					if (methodName.equals(method.getName()))
					{
						Class<E>[] clazzs = (Class<E>[]) method.getParameterTypes();
						for (Class<E> parameterType : clazzs)
						{
							// domain中对应的set方法
							Method setMethod = bean.getClass().getMethod(methodName, parameterType);
							setMethod.invoke(bean,
									stringArray2targetType(parametrs.get(key), parameterType));
							break;
						}
						break;
					}
				}
			}
			return bean;

		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 把字符串转化为特定类型（目前仅支持转换为int,String,double,boolean）
	 * 
	 * @param source
	 *            源字符串数组
	 * @param targetType
	 *            目标类型
	 * @return 转换后的结果
	 */
	public static <E> E stringArray2targetType(String[] source, Class<E> targetType)
	{
		E result = null;
		try
		{
			// 判断set方法的参数是否为数组
			if (targetType.isArray())
			{
				// 如果是字符串数组
				if (targetType.getName().equals(String[].class.getName()))
				{
					result = (E) source;
				}
				// 如果是int型数组
				else if (targetType.getName().equals(int[].class.getName()))
				{
					int[] intArray=new int[source.length];
					for (int i = 0; i < intArray.length; i++)
					{
						intArray[i] = Integer.parseInt(source[i]);
					}
					result = (E) intArray;
				}
				// 如果是double型数组
				else if (targetType.getName().equals(double[].class.getName()))
				{
					double[] doubleArray=new double[source.length];
					for (int i = 0; i < doubleArray.length; i++)
					{
						doubleArray[i] = Double.parseDouble(source[i]);
					}
					result = (E) doubleArray;
				}
				// 如果是boolean型数组
				else if (targetType.getName().equals(boolean[].class.getName()))
				{
					boolean [] booleanArray=new boolean[source.length];
					for (int i = 0; i < booleanArray.length; i++)
					{
						booleanArray[i] = Boolean.parseBoolean(source[i]);
					}
					result = (E) booleanArray;
				}
				else
				{
					new RuntimeException("不支持的目标数组类型");
				}
			}
			// set方法参数为非数组类型
			else
			{

				if (targetType.getName().equals(String.class.getName()))
				{
					result = (E) source[0];
				}
				else if (targetType.getName().equals(Integer.class.getName()))
				{
					result = (E) ((Integer) Integer.parseInt(source[0]));
				}
				else if (targetType.getName().equals(double.class.getName()))
				{
					result = (E) ((Double) Double.parseDouble(source[0]));
				}
				else if (targetType.getName().equals(boolean.class.getName()))
				{
					result = (E) ((Boolean) Boolean.parseBoolean(source[0]));
				}
				else
				{
					throw new RuntimeException("不支持的目标类型");
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
//		System.out.println("转换后的结果为： " + result);

		return result;
	}

}
