package io.github.zeroaicy.util.crash;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import io.github.zeroaicy.util.DebugUtil;
import io.github.zeroaicy.util.FileUtil;
import io.github.zeroaicy.util.crash.CrashApphandler;

public class CrashApplication extends Application{

	static Application mCrashApplication;

	protected static boolean isDebug = true;

	@Override
	public void onCreate(){
		super.onCreate();
		//闪退日志
		mCrashApplication = CrashApplication.this;
		//注册
		CrashApphandler.getInstance().onCreated();
	}

	@Override
	protected void attachBaseContext(Context base){
		if ( isDebug ){
			DebugUtil.debug(base);
		}
		CrashApphandler.getInstance().onCreated();
		super.attachBaseContext(base);
		
	}

    public static void CrashInit(){
        CrashApphandler instance = CrashApphandler.getInstance();
        instance.setCAHCE_CRASH_LOG(FileUtil.CrashLogPath);
		instance.setLIMIT_LOG_COUNT(5);
		instance.init();
    }
    public static void CrashInit(Context base){
        CrashApphandler instance = CrashApphandler.getInstance();
		instance.setCAHCE_CRASH_LOG(FileUtil.CrashLogPath);
		instance.setLIMIT_LOG_COUNT(5);
		instance.init(base);
    }
}

