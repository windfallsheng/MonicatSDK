package com.windfallsheng.monicat.util;

/**
 * Created by lzsheng on 2018/5/10.
 * <p>
 * debugEnable = true 则输入项目中的日志信息，false则不输出。
 */
public class LogUtils {

    private static boolean debugEnable;

    public static void init(boolean debugEnable) {
        LogUtils.debugEnable = debugEnable;
    }

    public static void i(String tag, String msg) {
        if (debugEnable)
            android.util.Log.i(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (debugEnable)
            android.util.Log.e(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (debugEnable) {
            android.util.Log.d(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (debugEnable)
            android.util.Log.v(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (debugEnable)
            android.util.Log.w(tag, msg);
    }
}
