package io.github.zeroaicy.util.crash;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;
import io.github.zeroaicy.util.ContextUtil;
import io.github.zeroaicy.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import android.app.Application;

/**
 * Created by mfcx on 2018/10/22.
 */

//基于线程的错误日志
public abstract class CrashAppLog implements Thread.UncaughtExceptionHandler{

    private static final String TAG = "CrashAppLog";

    /**
     * 简单日期格式
     */
    private final static SimpleDateFormat formate = new SimpleDateFormat("MM-dd-HH-mm-ss");;


    /**
     * 允许最大日志文件的数量
     */
    private int LIMIT_LOG_COUNT = 10;

    /**
     * 保存异常日志信息集合
     */
    private final LinkedHashMap<String, String> crashAppLog = new LinkedHashMap<String, String>();

    /**
     * 默认放在内存卡的根目录路径
     */
    private String CAHCE_CRASH_LOG = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.Log";
    /**
     * 系统默认的异常类
     */
    private Thread.UncaughtExceptionHandler mUncaughtExceptionHandler;

	public void setOnCrashListener(OnCrashListener onCrashListener){
		this.onCrashListener = onCrashListener;
	}
    /**
     * 发送一场日志文件到服务器
     * @param folder 文件路径
     * @param file 文件
     */
    public abstract void sendCrashLogToServer(File folder, File file);

    public interface OnCrashListener{
        public void onCrash(String crashInfo);
    }
    /**
     * context
     */
    protected Context mContext;
	public OnCrashListener onCrashListener;
	//无参构造
	public void init(){
		init(null);
	}

	public void init(Context context){
		try{
			if ( context == null ){
				//
				context = ContextUtil.getContext();
			}

			if ( context instanceof Application ){
				//直接保存Application
				this.mContext = context;
			}else if ( context != null ){
				this.mContext = context;
			}

			/**
			 * 获取系统默认的异常处理类
			 */
			Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

			//防止重复设置
			/**
			 * 使用当前的类为异常处理类
			 */
			if ( defaultUncaughtExceptionHandler != this ){
				//如果不是自己则设置[系统默认的异常处理类]
				this.mUncaughtExceptionHandler = defaultUncaughtExceptionHandler;

				Thread.setDefaultUncaughtExceptionHandler(this);
			}
			Thread currentThread = Thread.currentThread();
			if ( currentThread.getUncaughtExceptionHandler() != this ){
				currentThread.setUncaughtExceptionHandler(this);
			}

		}
		catch (Throwable e){
			Log.e(TAG, "init - " + e.getMessage());
		}
	}

	/**
     * 获取应用信息
     * @param mContext
     */
    private void collectAppInfo(Context mContext){

        try{
            if ( this.mContext == null ){
                return ;
			}
            PackageManager packageManager = mContext.getPackageManager();

            if ( packageManager != null ){
				this.mContext.getApplicationInfo();
				//当前app信息
                PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
                if ( packageInfo != null ){

                    String versionName = packageInfo.versionName;
                    String versionCode = "" + packageInfo.versionCode;
                    String packName = packageInfo.packageName;

                    crashAppLog.put("versionName", versionName);
                    crashAppLog.put("versionCode", versionCode);
                    crashAppLog.put("packName", packName);

                }
            }

        }
		catch (Throwable e){
            Log.e(TAG, "collectAppInfo - ", e);
        }
    }

	/**
	 * 收集设备信息
	 */
	public static void collectDeviceInfo(Map<String, String> crashAppLog){
		/**
		 * 获取手机型号，系统版本，以及SDK版本
		 */
		try{
			crashAppLog.put("手机型号:", android.os.Build.MODEL);
			crashAppLog.put("系统版本", "" + android.os.Build.VERSION.SDK);
			crashAppLog.put("Android版本", android.os.Build.VERSION.RELEASE);

			Field[] fields = Build.class.getFields();

			if ( fields != null && fields.length > 0 ){
				for ( Field field : fields ){
					if ( field != null ){
						field.setAccessible(true);
						String valueString;
						Object fieldValue = field.get(null);
						if ( fieldValue instanceof String[] ){
							StringBuilder sb = new StringBuilder();
							String[] StringArray = (String[])fieldValue;
							for ( String temp: StringArray ){
								sb.append(temp).append(", ");
							}
							valueString = sb.toString();
						}else{
							valueString = String.valueOf(fieldValue);
						}
						crashAppLog.put(field.getName(), valueString);
					}
				}
			}

		}
		catch (Throwable e){
			Log.e(TAG, "collectDeviceInfo", e);
		}
	}

	/**
	 * 此类是当应用出现异常的时候执行该方法
	 * @param thread
	 * @param throwable
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable throwable){
		try{
			if ( !hanlderException(throwable) && mUncaughtExceptionHandler != null ){
				/**
				 * 如果此异常不处理则由系统自己处理
				 */
				this.mUncaughtExceptionHandler.uncaughtException(thread, throwable);
				//退出
				System.exit(-1);
			}else{
				//退出
				System.exit(-1);
			}
		}
		catch (Throwable e){
			Log.e(TAG, "uncaughtThrowable - " + e.getMessage());
		}
	}

	/**
	 * 用户处理异常日志
	 * @param throwable
	 * @return
	 */
	private boolean hanlderException(Throwable throwable){
		try{
			if ( throwable == null ){
				return false;
			}
			//收集应用信息
			collectAppInfo(this.mContext);
			//收集设备信息
			collectDeviceInfo(crashAppLog);

			/**
			 * 将日志写入文件
			 */
			writerCrashLogToFile(throwable);
			/**
			 * 限制日子志文件的数量
			 */
			limitAppLogCount(LIMIT_LOG_COUNT);

			//结束进程
		}
		catch (Throwable e){
			Log.e(TAG, "hanlderThrowable - " + e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * 最大文件数量
	 * @param limitLogCount
	 */
	private void limitAppLogCount(int limitLogCount){

		try{

			File file = new File(CAHCE_CRASH_LOG);
			if ( !file.exists() ){
				file.mkdirs();
			}

			if ( file != null && file.isDirectory() ){

				File[] files = file.listFiles(new CrashLogFliter());

				if ( files != null && files.length > 0 ){

					Arrays.sort(files, comparator);

					if ( files.length > LIMIT_LOG_COUNT ){

						for ( int i = 0 ; i < files.length - LIMIT_LOG_COUNT ;i++ ){

							files[i].delete();
						}
					}

				}
			}

		}
		catch (Throwable e){
			Log.e(TAG, "limitAppLogCount - " + e.getMessage());
		}
	}


	/**
	 * 日志文件按日志修改时间排序
	 */
	private Comparator<File> comparator = new Comparator<File>() {
		@Override
		public int compare(File l, File r){

			if ( l.lastModified() > r.lastModified() )
				return 1;
			if ( l.lastModified() < r.lastModified() )
				return -1;

			return 0;
		}
	};

	/**
	 * 过滤.log的文件
	 */
	public class CrashLogFliter implements FileFilter{
		@Override
		public boolean accept(File file){
			if ( file.getName().endsWith(".log") )
				return true;
			return false;
		}
    }



    /**
     * 写入文件中
     * @param ex
     */
    private void writerCrashLogToFile(Throwable ex){

        try{

            StringBuffer buffer = new StringBuffer();

            if ( crashAppLog != null && crashAppLog.size() > 0 ){
                for ( Map.Entry<String, String> entry : crashAppLog.entrySet() ){
                    buffer.append(entry.getKey() + ":" + entry.getValue() + "\n");
                }
            }
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();

            while ( cause != null ){
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }

            printWriter.flush();
            printWriter.close();

            String result = writer.toString();

            buffer.append("Exception:+\n");

            buffer.append(result);

            String s = buffer.toString();
			writerToFile(s);

			if ( onCrashListener != null ){
				onCrashListener.onCrash(s);
			}

        }
		catch (Throwable e){
            Log.e(TAG, "writerCrashLogToFile - " + e.getMessage());
        }
    }

	private static int curtTimer = 0;

    protected void writerToFile_Test(final String s){
		this.writerToFile(s);
	}

	/**
	 * 写入文件
	 */
    private void writerToFile(final String s){
        try{
			/**
             * 创建日志文件名称
             */
			curtTimer++;
            String timer = formate.format(new Date());

            String fileName ="crash-" + timer + "-" + curtTimer + ".log";
            /**
             * 创建文件夹
             */
            File folder = new File(CAHCE_CRASH_LOG);

            if ( !folder.exists() )
                folder.mkdirs();

            /**
             * 创建日志文件
             */
            File file = new File(folder.getAbsolutePath() + File.separator + fileName);

            if ( !file.exists() )
                file.createNewFile();

            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(s);
            bufferedWriter.flush();
            bufferedWriter.close();

            sendCrashLogToServer(folder, file);

        }
		catch (Throwable e){
            Log.e(TAG, "writerToFile - " + e.getMessage());
        }
    }



    public int getLIMIT_LOG_COUNT(){
        return LIMIT_LOG_COUNT;
    }

    public void setLIMIT_LOG_COUNT(int LIMIT_LOG_COUNT){
        this.LIMIT_LOG_COUNT = LIMIT_LOG_COUNT;
    }

    public String getCAHCE_CRASH_LOG(){
        return CAHCE_CRASH_LOG;
    }

    public void setCAHCE_CRASH_LOG(String CAHCE_CRASH_LOG){
        this.CAHCE_CRASH_LOG = CAHCE_CRASH_LOG;
    }
}

