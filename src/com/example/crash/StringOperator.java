package com.example.crash;

public class StringOperator {

	/**
	 * 判断是否为NULL或者空
	 * @param str 判断字符串
	 * @return 是则为NULL或者空
	 */
	public static boolean isNullOrEmpty(String str)
	{
		if(null == str || "".equals(str))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * 判断是否为NULL或者空或者空格
	 * @param str 判断字符串
	 * @return 是则为NULL或者空或者空格
	 */
	public static boolean isNullOrEmptyOrSpace(String str)
	{
		if(null == str || "".equals(str.trim()))
		{
			return true;
		}
		return false;
	}
}
