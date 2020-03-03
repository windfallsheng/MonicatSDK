package com.windfallsheng.monicat.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.windfallsheng.monicat.action.MonicatManager;
import com.windfallsheng.monicat.common.MonicatConstants;

import java.lang.reflect.Field;

/**
 * Created by lzsheng on 2018/4/9.
 */

public class AppInfoUtils {

    public static PackageInfo getPackageInfo(Context context) throws PackageManager.NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
//        int versionCode = packageInfo.versionCode;
//        String packageName = packageInfo.packageName;
        return packageInfo;
    }

    public static PackageInfo getPackageName(Context context) throws PackageManager.NameNotFoundException {
        String packageName = "";

        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageInfo(context);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            packageName = packageInfo.packageName;
        }
        return packageInfo;
    }

    public static int getVersionCode(Context context) throws PackageManager.NameNotFoundException {
        int versionCode = 0;

        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageInfo(context);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = (int) packageInfo.getLongVersionCode();
            } else {
                versionCode = packageInfo.versionCode;
            }
        }
        return versionCode;
    }

    /**
     * Module得到到外层真正运行App的 BuildConfig
     * 通过反射得到真正执行的 Module 的 BuildConfig
     *
     * @param context
     */
    public static Class getBuildConfig(Context context) {
        Class buildConfig = null;
        try {
            PackageInfo packageInfo = AppInfoUtils.getPackageInfo(context);
            if (packageInfo != null) {
                String packageName = packageInfo.packageName;
                buildConfig = Class.forName(packageName + ".BuildConfig");
                Field DEBUG = buildConfig.getField("DEBUG");
                DEBUG.setAccessible(true);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return buildConfig;
    }

    public static Class isDebug(Context context) throws NoSuchFieldException {
        Class buildConfig = getBuildConfig(context);
        if (buildConfig != null) {
            buildConfig.getField("");
        }

        return buildConfig;
    }

}
