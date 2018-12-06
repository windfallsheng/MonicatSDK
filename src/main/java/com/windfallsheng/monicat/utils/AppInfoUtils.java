package com.windfallsheng.monicat.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

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

}
