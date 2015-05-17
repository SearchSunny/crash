package com.example.crash;

import android.app.Application;
/**
 * 用来管理应用程序的全局状态。在应用程序启动时Application会首先创建
 * 自定义加强版的Application中注册未捕获异常处理器
 * @author miaowei
 *
 */
public class CrashApplication extends Application{

	@Override
	public void onCreate() {
		super.onCreate();
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
	}
}
