package com.windfallsheng.monicat.base;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.windfallsheng.monicat.action.MonicatManager;
import com.windfallsheng.monicat.common.MonicatConstants;
import com.windfallsheng.monicat.util.AppInfoUtils;
import com.windfallsheng.monicat.util.SharedPrefUtil;

import static android.R.attr.versionCode;

/**
 * CreateDate: 2018/04/16.
 * <p>
 * Author: lzsheng
 * <p>
 * Description: app应用信息等数据在SharedPreferences中存储，读取的操作
 * <p>
 * 对于本项目保证只存在一个SharedPreferences文件( {@link MonicatConstants#MONICAT_SHARED_PREF} )就可以了
 * <p>
 * 调用{@link SharedPrefUtil} 中对数据的CRUD操作，完成必要的业务逻辑
 * <p>
 * 其它的一些和获取app应用包名和版本号类似的方法也可以在这里扩展
 * <p>
 * Version:
 */
public class ShraredPrefManager {

    private volatile static ShraredPrefManager instance = null;

    private ShraredPrefManager() {
    }

    public static ShraredPrefManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ShraredPrefManager.class) {
                if (instance == null) {
                    instance = new ShraredPrefManager();
                }
            }
        }
        return instance;
    }

    /**
     * 获取app应用的VersionCode，如果SharedPreferences文件中有存储，则从中取出，没有则通过api获取，并且存入SharedPreferences文件
     *
     * @return
     */
    public int getAppVersionCode() {
        boolean hasInitAppInfos = SharedPrefUtil.init(MonicatManager.getInstance().getContext(), MonicatConstants.MONICAT_SHARED_PREF)
                .contains(MonicatConstants.APP_VERSIONCODE);
        int versionCode = 0;
        if (hasInitAppInfos) {
            versionCode = SharedPrefUtil.init(MonicatManager.getInstance().getContext(), MonicatConstants.MONICAT_SHARED_PREF)
                    .getInt(MonicatConstants.APP_VERSIONCODE);
            return versionCode;
        } else {
            PackageInfo packageInfo = null;
            try {
                packageInfo = AppInfoUtils.getPackageInfo(MonicatManager.getInstance().getContext());
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (packageInfo != null) {
                versionCode = packageInfo.versionCode;
                SharedPrefUtil.init(MonicatManager.getInstance().getContext(), MonicatConstants.MONICAT_SHARED_PREF)
                        .putInt(MonicatConstants.APP_VERSIONCODE, versionCode);
            }
        }
        return versionCode;
    }

    /**
     * 获取app应用的PackageName，如果SharedPreferences文件中有存储，则从中取出，没有则通过api获取，并且存入SharedPreferences文件
     *
     * @return
     */
    public String getAppPackageName() {
        boolean hasInitAppInfos = SharedPrefUtil.init(MonicatManager.getInstance().getContext(), MonicatConstants.MONICAT_SHARED_PREF)
                .contains(MonicatConstants.APP_PACKAGENAME);
        String packageName = "";
        if (hasInitAppInfos) {
            packageName = SharedPrefUtil.init(MonicatManager.getInstance().getContext(), MonicatConstants.MONICAT_SHARED_PREF)
                    .getString(MonicatConstants.APP_PACKAGENAME);
            return packageName;
        } else {
            PackageInfo packageInfo = null;
            try {
                packageInfo = AppInfoUtils.getPackageInfo(MonicatManager.getInstance().getContext());
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (packageInfo != null) {
                packageName = packageInfo.packageName;
                SharedPrefUtil.init(MonicatManager.getInstance().getContext(), MonicatConstants.MONICAT_SHARED_PREF)
                        .putInt(MonicatConstants.APP_PACKAGENAME, versionCode);
            }
        }
        return packageName;
    }

}
