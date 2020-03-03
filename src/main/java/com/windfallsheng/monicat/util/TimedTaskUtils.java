package com.windfallsheng.monicat.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.windfallsheng.monicat.action.MonicatManager;

/**
 * CreateDate: 2018/4/9.
 * <p>
 * Author: lzsheng
 * <p>
 * Description: 定时提醒 Tools
 */
public class TimedTaskUtils {

    /**
     * @param context
     * @param
     * @param cls
     * @param action
     */
    public static void startTimedTask(Context context, long triggerAtTime, long intervalMillis, Class<?> cls, String action) {
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, cls);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        long triggerAtTime = SystemClock.elapsedRealtime();
//        Log.d(Constants.SDK_NAME, "TimedTaskUtils-->startTimedTask()_triggerAtTime=" + TimeUtils.timeLongToDateStr(triggerAtTime, ""));
        final int sdkVersion = MonicatManager.getInstance().getContext().getApplicationInfo().targetSdkVersion;
//        Log.d(Constants.SDK_NAME, "TimedTaskUtils-->startTimedTask()_sdkVersion==" + sdkVersion);
        if (sdkVersion < Build.VERSION_CODES.KITKAT) {//API>19之后，android平台为了省电机制，setRepeating()已经不再准确
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime,
                    intervalMillis, pendingIntent);
        } else {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAtTime,
                    intervalMillis, pendingIntent);
        }
    }

    /**
     * @param context
     * @param cls
     * @param action
     */
    public static void stopTimedTask(Context context, Class<?> cls, String action) {
        AlarmManager manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, cls);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.cancel(pendingIntent);
    }
}
