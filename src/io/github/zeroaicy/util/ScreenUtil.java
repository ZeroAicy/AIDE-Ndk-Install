package io.github.zeroaicy.util;

import android.app.*;
import android.content.*;
import android.os.*;
import android.provider.*;
import android.view.*;
import java.util.*;

public class ScreenUtil {
	//隐藏系统栏(导航、状态栏)
	public static void hideSystemBars(Activity activity) {
		Window window = activity.getWindow();
		if( window == null ){
			return;
		}
		View decorView = window.getDecorView();
		if( decorView == null ){
			return;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				//systemBars(状态栏&导航栏)
				WindowInsetsController controller = window.getInsetsController();
				controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.systemBars() | WindowInsets.Type.navigationBars());
			}
			else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				// 全屏显示，隐藏状态栏和导航栏，拉出状态栏和导航栏显示一会儿后消失。
				window.getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
			}
			else {
				// 全屏显示，隐藏状态栏
				decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
			}
		}
	}

	/*
     * 获取系统亮度
     */
    public static int getBrightness(Context context) {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
    }
}
