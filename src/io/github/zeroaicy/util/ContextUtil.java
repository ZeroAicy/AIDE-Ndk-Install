package io.github.zeroaicy.util;

import android.app.*;
import android.content.*;
import android.os.*;
import io.github.zeroaicy.util.reflect.*;
import java.util.*;

public class ContextUtil{


	public static final String TAG = "ContextUtil";
	/**
	 * Context对象
	 */
	private static Context CONTEXT_INSTANCE;

	private static ReflectPie activityThreadReflect;
	public static ReflectPie getActivityThread(){
		if ( activityThreadReflect != null && activityThreadReflect.get() != null ){
			return activityThreadReflect;
		}
		return ReflectPie.onClass("android.app.ActivityThread").call("currentActivityThread");
	}

	public static Application getApplication(){
		try{
			if ( ContextUtil.CONTEXT_INSTANCE instanceof Application ){
				return (Application)ContextUtil.CONTEXT_INSTANCE;
			}else{
				Application application = getActivityThread().call("getApplication").get();
				ContextUtil.CONTEXT_INSTANCE = application;
				return application;
			}
		}
		catch (Throwable e){
			Log.e(TAG, "getApplication()", e);
			return null;
		}
	}
	/**
	 * 取得Context对象
	 * PS:必须在主线程调用
	 * 两种方法，极大力度保证返回值不为null
	 * 1.android.app.ActivityThread.currentActivityThread()getApplication()
	 * 2.反射构造(兜底)
	 * @return Context
	 */
	public static Context getContext(){
		if ( ContextUtil.CONTEXT_INSTANCE == null || !(ContextUtil.CONTEXT_INSTANCE instanceof Application) ){
			Application application = getApplication();
			if ( application != null ){
				ContextUtil.CONTEXT_INSTANCE = application;
			}
		}
		if ( ContextUtil.CONTEXT_INSTANCE == null ){
			ContextUtil.CONTEXT_INSTANCE = createAppContext();
		}
		return CONTEXT_INSTANCE;
	}

	/**
	 * @return app包名
	 */
	public static String getPackageName(){
		Context context = getContext();
		if ( context != null ){
			String packageName = context.getPackageName();
			if ( packageName != null ){
				return packageName;
			}
		}
		String processName = getProcessName();
		if ( processName.length() > 1 ){
			int lastIndexOf = processName.indexOf(':');
			if ( lastIndexOf > 0 ){
				return processName.substring(0, lastIndexOf);
			}
		}
		return processName;
	}
	/**
	 * @return 当前进程[进程名]
	 */
	public static String getProcessName(){
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ){
			return Application.getProcessName();
		}
		try{
			return getActivityThread().call("getProcessName").get();
		}
		catch (Throwable e){
			return ":";
		}
	}
	
	public static final boolean isMainProcess = ContextUtil.getPackageName().equals(ContextUtil.getProcessName());
	public static boolean isMainProcess(){
		
		return isMainProcess;
	}
	
	//构造出的Context
	private static Context createAppContext;

	public static Context createAppContext(){
		//复用
        try{
			return createAppContext(true);
		}
		catch (Throwable e){
			Log.e(TAG, "createAppContext()", e);
			return ContextUtil.createAppContext;
		}
    }

	private static Context createAppContext(boolean isReuse) throws Exception{
		if ( isReuse && ContextUtil.createAppContext != null ) return ContextUtil.createAppContext;

        // 反射获取 ActivityThread 的 currentActivityThread 获取 mainThread
		ReflectPie ReflectActivityThread = ReflectPie.onClass("android.app.ActivityThread");
		ReflectActivityThread = ReflectActivityThread.call("currentActivityThread");

		// 反射获取 mainThread 实例中的 mBoundApplication 字段
        ReflectPie ReflectBoundApplication = ReflectActivityThread.field("mBoundApplication");
		// 获取 mBoundApplication 的 packageInfo 变量
        if ( ReflectBoundApplication.get() == null ) throw new NullPointerException("mBoundApplicationObj 反射值空");
        Object packageInfoObj = ReflectBoundApplication.get("info");

		// 反射调用 ContextImpl.createAppContext(ActivityThread mainThread, LoadedApk packageInfo)
        if ( ReflectActivityThread.get() == null ) throw new NullPointerException("mainThreadObj 反射值空");
        if ( packageInfoObj == null ) throw new NullPointerException("packageInfoObj 反射值空");

		ReflectPie ReflectContextImpl = ReflectPie.onClass("android.app.ContextImpl");
		return ContextUtil.createAppContext = ReflectContextImpl.call("createAppContext", ReflectActivityThread.get(), packageInfoObj).get();

    }

	public static Activity getActivity(){
        try{
			Thread.sleep(210);
			Map<Object, Object> activities = getActivityThread().get("mActivities");
			if ( activities.size() < 1 ){   
				return null;
			} 
			for ( Object activityRecord : activities.values() ){
				ReflectPie activityRecordReflect = ReflectPie.on(activityRecord);
				if ( !(Boolean)activityRecordReflect.get("paused") ){
					Activity activity = activityRecordReflect.get("activity");
					return activity;
				} 
			}
		}
		catch (Exception e){ 
			e.printStackTrace();
		}
		return null;
	}
}

