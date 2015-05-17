package com.example.crash;


import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
/**
 * Android中处理崩溃异常
 * @author miaowei
 *
 */
public class MainActivity extends Activity {
	
	private String s;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SdcardUtil.initInstance(this);
		setContentView(R.layout.activity_main);
		
		System.out.println(s.equals("any string"));
	}

}
