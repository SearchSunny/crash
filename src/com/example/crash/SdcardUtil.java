package com.example.crash;

import java.io.File;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.support.v4.content.ContextCompat;


/**
 * 工具类，用于获取有效的SD卡路径
 * @author miaowei
 *
 */
public class SdcardUtil {

	private static SdcardUtil mInstance;
	
	private static Context mContext;
	
	private static File[] files = null;
	/**
	 * 自定义广播用于接收SD卡状态改变
	 */
	private BroadcastReceiver mSdcardBroadcastReceiver = new SdcardBroadcastReceiver();
	/**
	 * SDCARD状态改变接口
	 */
	private List<OnSdcardChangedListener> mListenerList = new LinkedList<OnSdcardChangedListener>();
	/**
	 * 广播过滤
	 */
	private static IntentFilter mSdcardIntentFilter = new IntentFilter();
	
	private static List<String> mSdcard2Paths = new LinkedList<String>();
	
	/**
	 * 针对SD卡2 有些情况下会有两个SD卡
	 */
	private static String mSdcard2Path;
	/**
	 * 针对SD卡1
	 */
	private static String mSdcard1Path;
	
	private static final String mVirtualHeader = "/mnt";
	
	/**
	 * 过滤广播
	 */
	static {
		mSdcardIntentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
		mSdcardIntentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		mSdcardIntentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
		mSdcardIntentFilter.addAction(Intent.ACTION_MEDIA_NOFS);
		mSdcardIntentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		mSdcardIntentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
		mSdcardIntentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		mSdcardIntentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
		mSdcardIntentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		mSdcardIntentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		mSdcardIntentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		mSdcardIntentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		mSdcardIntentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
		mSdcardIntentFilter.addDataScheme("file");
	}
	/**
	 * 构造方法
	 * @param context
	 */
	private SdcardUtil(Context context) {
		mContext = context;
	}
	
	/**
	 * 外置
	 * @return
	 */
	public static String getSdcardCollInfoNO() {
		return getSdcardPath()+"/";
	}
	
	
	/**
	 * 
	 * 此方法获取的不一定是外置sd卡，有些三星手机相反
	 */
	protected static String getSdcardPath() {
		return getSdcardPathNoSlash() + "/";
	}
	
	/**
	 * 获取SDCARD路径，一般是/mnt/sdcard/,现在有些手机sdcard路径不是标准的，希望各位通过这种方法获取SDCARD路径
	 * 
	 * @return SDCARD路径
	 */
	protected static String getSdcardPathNoSlash() {
		return mSdcard1Path;

	}
	
	/**
	 * 实例化Sd
	 * @param context
	 */
	public static void initInstance(Context context) {
		mInstance = new SdcardUtil(context);
		mInstance.registerReceiver();
		//当前版本号是否小于1
		if (Build.VERSION.SDK_INT < 1) {
			//获取内置sdcard存储卡路径
			mSdcard1Path = Environment.getExternalStorageDirectory().getAbsolutePath();

			mInstance.initSdcard2Paths();
		} else {
			files = ContextCompat.getExternalFilesDirs(mContext, null);
			if (files != null) {
				if (files.length > 0 && files[0] != null) {
					
					mSdcard1Path = files[0].getAbsolutePath();
				}
				if (files.length > 1 && files[1] != null) {
					mSdcard2Path = files[1].getAbsolutePath();
				}
			}
		}
	}
	
	/**
	 * 注册广播
	 */
	private void registerReceiver() {
		mContext.registerReceiver(mSdcardBroadcastReceiver, mSdcardIntentFilter);
	}
	
    /**
     * 自己写一个广播监听函数
     * @author miaowei
     *
     */
	public class SdcardBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			synchronized (mListenerList) {

				for (int index = mListenerList.size() - 1; index > -1; index--) {
					OnSdcardChangedListener listener = mListenerList.get(index);
					if (listener == null) {
						continue;
					}
					listener.onSdcardChanged(SdcardUtil.this);
				}
			}
		}
	};
		
		/**
		 * SDCARD状态改变接口
		 * @author miaowei
		 *
		 */
		public interface OnSdcardChangedListener {
			public void onSdcardChanged(SdcardUtil sender);
		}
		/**
		 * 添加SDCARD改变事件监听
		 * 
		 * @param listener 监听
		 *            
		 */
		public void addListener(OnSdcardChangedListener listener) {
			mListenerList.add(listener);
		}
		
		/**
		 * 移除监听，可以不移除在系统关闭的时候会自动移除所有监听
		 * 
		 * @param listener
		 *            监听
		 * @return 是否移除成功
		 */
		public boolean removeListener(OnSdcardChangedListener listener) {
			boolean isContains = false;
			synchronized (mListenerList) {
				isContains = mListenerList.contains(listener);
				if (isContains) {
					mListenerList.remove(listener);
					isContains = true;
				} else {
					isContains = false;
				}
			}
			if (isContains) {
				return true;
			}

			else {
				return false;
			}
		}
		
		/**
		 * 初始化SD卡路径
		 * 版本不同，SD卡名字也有所不同
		 */
		@SuppressLint("SdCardPath")
		private void initSdcard2Paths() {
			// 3.2及以上SDK识别路径
			mSdcard2Paths = getSdcard2Paths();
			mSdcard2Paths.add("/mnt/emmc");
			mSdcard2Paths.add("/mnt/extsdcard");
			mSdcard2Paths.add("/mnt/ext_sdcard");
			mSdcard2Paths.add("/sdcard-ext");
			mSdcard2Paths.add("/mnt/sdcard-ext");
			mSdcard2Paths.add("/sdcard2");
			mSdcard2Paths.add("/sdcard");
			mSdcard2Paths.add("/mnt/sdcard2");
			mSdcard2Paths.add("/mnt/sdcard");
			mSdcard2Paths.add("/sdcard/sd");
			mSdcard2Paths.add("/sdcard/external");
			mSdcard2Paths.add("/flash");
			mSdcard2Paths.add("/mnt/flash");
			mSdcard2Paths.add("/mnt/sdcard/external_sd");

			mSdcard2Paths.add("/mnt/external1");
			mSdcard2Paths.add("/mnt/sdcard/extra_sd");
			mSdcard2Paths.add("/mnt/sdcard/_ExternalSD");
			mSdcard2Paths.add("/mnt/extrasd_bin");
			// 4.1SDK 识别路径
			mSdcard2Paths.add("/storage/extSdCard");
			mSdcard2Paths.add("/storage/sdcard0");
			mSdcard2Paths.add("/storage/sdcard1");
			initSdcard2();
		}
		
		/**
		 * 获取初始化SD卡2
		 */
		private static void initSdcard2() {
			String sdcard = getSdcardPathNoSlash();
			int count = mSdcard2Paths.size();
			for (int index = 0; index < count; index++) {
				boolean isSame = isSamePath(sdcard, mSdcard2Paths.get(index));
				if (isSame) {
					continue;
				}
				boolean isExsits = isExsitsPath(mSdcard2Paths.get(index));
				if (isExsits && !isSameSdcard(sdcard, mSdcard2Paths.get(index))) {
					mSdcard2Path = mSdcard2Paths.get(index);
					break;
				}
			}
		}
		
		
		/**
		 * 统计数据的文件系统路径 以M单位返回
		 * @param sdcardPath
		 * @return
		 */
		protected static long getSdcardSize(String sdcardPath) {
			long size = 0;
			try {
				StatFs statFs = new StatFs(sdcardPath);
				int blockSize = statFs.getBlockSize();
				int totalBlocks = statFs.getBlockCount();
				size = (long) ((long) totalBlocks * (long) blockSize);
			} catch (Exception e) {
			}
			//return size / 1024 / 1024; 以M单位返回
			return size;
		}
	
		/**
		 * SD卡有效大小 以M单位返回
		 * @param sdcardPath
		 * @return
		 */
		protected static long getSdcardAvailableSize(String sdcardPath) {
			long size = 0;
			StatFs statFs = new StatFs(sdcardPath);
			int blockSize = statFs.getBlockSize();
			int totalBlocks = statFs.getAvailableBlocks();
			size = (long) ((long) totalBlocks * (long) blockSize);
			
			//return size / 1024 / 1024; 以M单位返回
			return size;
			
		}
		
		/**
		 * 比较SD卡名字是否相同
		 * @param path
		 * @param path2
		 * @return
		 */
		private static boolean isSamePath(String path, String path2) {
			// 名称有空则认为一样
			if (StringOperator.isNullOrEmptyOrSpace(path) || StringOperator.isNullOrEmptyOrSpace(path2)) {
				return true;
			}
			// 一样
			if (path.trim().toLowerCase().equals(path2.trim().toLowerCase())) {
				return true;
			}
			// 添加/mnt
			if (path2.trim().toLowerCase().equals((mVirtualHeader + path).trim().toLowerCase())) {
				return true;
			}
			// 添加/mnt
			if (path.trim().toLowerCase().equals((mVirtualHeader + path2).trim().toLowerCase())) {
				return true;
			}

			return false;
		}
		
		/**
		 * 比较两个SDcard,大小是否相同,文件目录长度是否相同
		 * @param sdcard1
		 * @param sdcard2
		 * @return
		 */
		private static boolean isSameSdcard(String sdcard1, String sdcard2) {
			long sdcard1Size = getSdcardSize(sdcard1);
			long sdcard2Size = getSdcardSize(sdcard2);
			if (sdcard1Size != sdcard2Size) {
				return false;
			}
			sdcard1Size = getSdcardAvailableSize(sdcard1);
			sdcard2Size = getSdcardAvailableSize(sdcard2);
			
			if (sdcard1Size != sdcard2Size) {
				return false;
			}

			File f1 = new File(sdcard1);
			File f2 = new File(sdcard2);

			String[] fileList1 = f1.list();
			String[] fileList2 = f2.list();

			// 都是空，则认为是同一个目录
			if (fileList1 == null && fileList2 == null) {
				return true;
			}

			// 有一个为空，则认为是不同目录
			if (fileList1 == null || fileList2 == null) {
				return false;
			}

			// 不一样多的文件，则认为不同目录
			if (fileList1.length != fileList2.length) {
				return false;
			}

			return true;
		}
		/**
		 * SD文件路径是否存在
		 * @param path SD卡文件路径
		 * @return
		 */
		private static boolean isExsitsPath(String path) {
			File f = new File(path);
			if (f.exists() && f.canWrite()) {
				return true;
			}
			return false;
		}
		
		/**
		 * 获取SD卡2的路径，有些情况下一个手机中会出现两个SD卡
		 * @return
		 */
		@SuppressLint({ "InlinedApi", "NewApi", "NewApi" })
		private List<String> getSdcard2Paths() {
			List<String> paths = new LinkedList<String>();
			if (Build.VERSION.SDK_INT < 13) {
				return paths;
			}

			StorageManager sm = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
			try {
				Class<? extends StorageManager> clazz = sm.getClass();
				Method mlist = clazz.getMethod("getVolumeList", (Class[]) null);
				Class<?> cstrvol = Class.forName("android.os.storage.StorageVolume");
				Method mvol = cstrvol.getMethod("getPath", (Class[]) null);
				Object[] objects = (Object[]) mlist.invoke(sm);
				if (objects != null && objects.length > 0) {
					for (Object obj : objects) {
						paths.add((String) mvol.invoke(obj));
					}
				}
			} catch (Exception e) {
			}
			return paths;
		}
		
		
}
