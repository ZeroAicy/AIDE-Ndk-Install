package io.github.zeroaicy.util.crash;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;

public class CrashActivity extends Activity{

	private static String log;

	public static String getErrorLog() {
		return log;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState){
		setTheme(android.R.style.Theme_Material_Light);
		setTitle("CrashActivity");
		super.onCreate(savedInstanceState);

		ScrollView mScrollView=new ScrollView(this);
		HorizontalScrollView mHorizontalScrollView = new HorizontalScrollView(this);
		TextView mMessage = new TextView(this);

		mHorizontalScrollView.addView(mMessage);
		mScrollView.addView(mHorizontalScrollView, -1, -1);

		setContentView(mScrollView);

		log = getIntent().getStringExtra(CrashApphandler.CrashActivityKey);
		mMessage.setText(log);
		mMessage.setTextIsSelectable(true);

		int padding = 24;
		mMessage.setPadding(padding, padding, padding, padding);

		mScrollView.setFillViewport(true);
	}

	@Override
	public void onBackPressed(){
		reset();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(android.R.string.copy).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

				@Override
				public boolean onMenuItemClick(MenuItem p1){
					copyText(log);
					return false;
				}
			}).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menu.add("重启").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

				@Override
				public boolean onMenuItemClick(MenuItem p1){
					reset();
					return false;
				}

			}).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		return super.onCreateOptionsMenu(menu);
	}

	private void reset(){
		Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
		intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(-1);
    }

	private void copyText(String msg){
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ){
			final android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			final android.content.ClipData clipData = android.content.ClipData
				.newPlainText(getPackageName(), msg);
			clipboardManager.setPrimaryClip(clipData);
		}else{
			final android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			clipboardManager.setText(msg);
		}
	}

}
