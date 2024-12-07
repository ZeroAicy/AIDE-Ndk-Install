package io.github.zeroaicy.util.crash;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import android.os.*;
import android.widget.TextView;
import android.text.TextWatcher;
import io.github.zeroaicy.util.ContextUtil;
import android.content.Intent;
import android.app.Activity;
import android.content.Context;
import android.app.Application;

/**
 * Created by mfcx on 2018/10/22.
 */
public class CrashApphandler extends CrashAppLog{
	public static final String CrashActivityKey = "crash_log";

    public static CrashApphandler mCrashApphandler = null;

	public Handler MainHandler = new Handler(Looper.getMainLooper());

    private CrashApphandler(){}

    public static CrashApphandler getInstance(){
        if ( mCrashApphandler == null )
            mCrashApphandler = new CrashApphandler();
        return mCrashApphandler;
    }

	public void onCreated(){
		if ( this.onCrashListener == null && this.mContext != null ){
			setOnCrashListener(new OnCrashListener(){
					@Override
					public void onCrash(String crashInfo){
						if(CrashActivity.getErrorLog() != null){
							return; 
						}
						Context context = CrashApphandler.this.mContext;
						if ( !(context instanceof Application) ){
							context = CrashApplication.mCrashApplication;
						}
						if ( context == null ){
							context = ContextUtil.getContext();
						}

						Intent intent = new Intent(context, io.github.zeroaicy.util.crash.CrashActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra(CrashApphandler.CrashActivityKey, crashInfo);
						//启动活动
						context.startActivity(intent);
						//退出活动，因为退出了所以启动活动前仍会走Application
						//如果错误点在Application则会重复启动
						System.exit(-1);
					}
				});
		}
	}
    @Override
    public void sendCrashLogToServer(File folder, File file){
		Log.e("*********", "文件夹:" + folder.getAbsolutePath() + " - " + file.getAbsolutePath() + "");
    }
	@Override
	public void init(Context context){
		super.init(context);
	}
}

