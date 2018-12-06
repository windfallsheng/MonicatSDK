package com.windfallsheng.monicat.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.windfallsheng.monicat.action.MonicatManager;
import com.windfallsheng.monicat.base.Configuration;

import java.lang.reflect.Field;

/**
 * Created by lzsheng on 2018/5/10.
 * <p>
 * Log原生日志打印的简单实现，根据 {@link Configuration} 配置参数判断是否用户手动打开debug模式；
 * 如果用户没有设置，则获取项目外层app的模式debug or release）模式来选择，
 * isDebug = true 则输入项目中的日志信息，false则不输出。
 */
public class LogUtils {

    private static Boolean isDebug = isDebug();
    // 当 " boolean isDebug = BuildConfig.DEBUG ", module中以这种方式获取模式时，需要这么引入module项目
    //    debugCompile project(path: ':Monicat', configuration: 'debug')
    //    releaseCompile project(path: ':Monicat', configuration: 'release')
//    private static final boolean isDebug = BuildConfig.DEBUG;
    // 自定义BuildConfig字段
//    private static final boolean isDebug = BuildConfig.API_ENV;
//    private static final boolean isDebug = false;

    public static boolean isDebug() {
        Boolean isDebugConfig = MonicatManager.getInstance().getConfig().isDebug;
        if (isDebugConfig != null) {
            return isDebugConfig.booleanValue();
        }
        return isDebug == null ? false : isDebug.booleanValue();
    }

    /**
     * Module得到到外层真正运行App的 BuildConfig
     * 通过反射得到真正执行的 Module 的 BuildConfig
     *
     * @param context
     */
    public static Boolean syncIsDebug(Context context) {
        try {
            PackageInfo packageInfo = AppInfoUtils.getPackageInfo(context);
            if (packageInfo != null) {
                String packageName = packageInfo.packageName;
                Class buildConfig = Class.forName(packageName + ".BuildConfig");
                Field DEBUG = buildConfig.getField("DEBUG");
                DEBUG.setAccessible(true);
                isDebug = DEBUG.getBoolean(null);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return isDebug;
    }

    public static void i(String tag, String msg) {
        if (isDebug)
            android.util.Log.i(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (isDebug)
            android.util.Log.e(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (isDebug) {
            android.util.Log.d(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (isDebug)
            android.util.Log.v(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (isDebug)
            android.util.Log.w(tag, msg);
    }
}
