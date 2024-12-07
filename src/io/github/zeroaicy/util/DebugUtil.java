package io.github.zeroaicy.util;
import io.github.zeroaicy.util.crash.CrashApplication;
import android.content.Context;

public class DebugUtil{
	
	
	public static void debug(){
		Log.SetSystemOut(true);
		Log.enable(FileUtil.LogCatPath);
		CrashApplication.CrashInit();
	}

	public static void debug(Context context){
		Log.SetSystemOut(true);
		Log.enable(FileUtil.LogCatPath);
		CrashApplication.CrashInit(context);


	}
	
	public static void notDebug(){
		Log.disable();
	}

}
