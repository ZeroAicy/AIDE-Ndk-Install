package io.github.zeroaicy.util;
import android.app.*;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;

public class NotificationUtil{
	
	public static Notification zero(){
		println(getNotification(null, Service.class));
		println(getNotification(null, Build.class));
		
		return null;
	}

	private static void println(Notification notification){
		// TODO: Implement this method
	}
	public static Notification getNotification(Context context, Class clazz){
		String channelId = null;
        // 8.0 以上需要特殊处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel(context, clazz.getName(), "ForegroundService");
        } else {
            channelId = "";
        }
        
		return new Notification.Builder(context, channelId).build();
	}
	/**
     * 创建通知通道
     * @param channelId
     * @param channelName
     * @return
     */
    private static String createNotificationChannel(Context context, String channelId, String channelName){
        NotificationChannel chan = new NotificationChannel(channelId,
														   channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }
	
}
