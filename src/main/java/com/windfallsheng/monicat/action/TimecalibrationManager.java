package com.windfallsheng.monicat.action;

import android.os.SystemClock;

import com.windfallsheng.monicat.common.MonicatConstants;
import com.windfallsheng.monicat.net.BaseCallBack;
import com.windfallsheng.monicat.net.BaseOkHttpClient;
import com.windfallsheng.monicat.util.LogUtils;
import com.windfallsheng.monicat.util.TimeUtils;

import java.io.IOException;

import okhttp3.Call;

import static com.windfallsheng.monicat.util.SharedPrefUtil.init;


/**
 * CreateDate: 2018/5/3
 * <p>
 * Author: lzsheng
 * <p>
 * Description: 实现和服务器的时间校准功能，尽可能获取到更精准的服务器时间。
 * SystemClock.elapsedRealtime()开机之后会保持一个时钟(绝对时间）
 * <p>
 * Version:
 */
public class TimecalibrationManager {

    private static volatile TimecalibrationManager instance;
    private long mResponServerTime;       // 服务器时间
    private long mResponSystemTime;       // 得到服务器时间时的系统时间
    private boolean hasGetServerTime;     // 是否已经获取到时服务器时间

    private TimecalibrationManager() {
    }

    public static TimecalibrationManager getInstance() {
        if (instance == null) {
            synchronized (TimecalibrationManager.class) {
                if (instance == null) {
                    instance = new TimecalibrationManager();
                }
            }
        }
        return instance;
    }

    /**
     * 获取当前的服务器时间
     *
     * @return
     */
    public long getCurrentServerTime() {
        long currentServerTime = 0;
        long currentSystemTime = SystemClock.elapsedRealtime();
        synchronized (TimecalibrationManager.class) {
            if (hasGetServerTime) {
                LogUtils.d(MonicatConstants.SDK_NAME, "TimecalibrationManager-->getCurrentServerTime()_hasGetServerTime=true_mResponServerTime==" + TimeUtils.timeLongToDateStr(mResponServerTime, ""));
                currentServerTime = mResponServerTime + (currentSystemTime - mResponSystemTime);
            } else {
                // 这里SharedPreferences已经存储了上一次的时间，可以从中取出
                boolean hasInitServerTime = init(MonicatManager.getInstance().getContext(), MonicatConstants.MONICAT_SHARED_PREF)
                        .contains(MonicatConstants.LAST_SERVER_TIME);
                boolean hasInitSystemTime = init(MonicatManager.getInstance().getContext(), MonicatConstants.MONICAT_SHARED_PREF)
                        .contains(MonicatConstants.LAST_SYSTEM_TIME);
                if (hasInitServerTime && hasInitSystemTime) {
                    mResponServerTime = init(MonicatManager.getInstance().getContext(), MonicatConstants.MONICAT_SHARED_PREF)
                            .getLong(MonicatConstants.LAST_SERVER_TIME);
                    mResponSystemTime = init(MonicatManager.getInstance().getContext(), MonicatConstants.MONICAT_SHARED_PREF)
                            .getLong(MonicatConstants.LAST_SYSTEM_TIME);
                    currentServerTime = mResponServerTime + (currentSystemTime - mResponSystemTime);
                    LogUtils.d(MonicatConstants.SDK_NAME, "TimecalibrationManager-->getCurrentServerTime()_hasGetServerTime=false_mResponServerTime==" + TimeUtils.timeLongToDateStr(mResponServerTime, ""));
                } else {
                    mResponServerTime = System.currentTimeMillis();
                    mResponSystemTime = SystemClock.elapsedRealtime();
                    LogUtils.d(MonicatConstants.SDK_NAME, "TimecalibrationManager-->getCurrentServerTime()_hasGetServerTime=false_mResponServerTime=System.currentTimeMillis()" + TimeUtils.timeLongToDateStr(mResponServerTime, ""));
                    currentServerTime = mResponServerTime + (currentSystemTime - mResponSystemTime);
                    //todo 一直没有获取到服务器时间的情况，可以试着触发获取服务器时间的操作
                    requestServerTime();
                }
            }
        }
        return currentServerTime;
    }

    /**
     * 获取服务器时间，并且初始化获取到服务器时间时的系统时间
     *
     * @param responServerTime
     */
    public void getServerTime(long responServerTime) {
        if (responServerTime > 0) {
            synchronized (TimecalibrationManager.class) {
                mResponSystemTime = SystemClock.elapsedRealtime();
//              String headerDate = response.header("Date"); // headerDate = "Thu, 03 May 2018 07:05:58 GMT"
//              Date parse = HttpDate.parse(headerDate);
//              mResponServerTime = parse.getTime();
                mResponServerTime = responServerTime;
                LogUtils.d(MonicatConstants.SDK_NAME, "TimecalibrationManager-->getServerTime()_mResponServerTime=" + mResponServerTime);
                LogUtils.d(MonicatConstants.SDK_NAME, "TimecalibrationManager-->getServerTime()_mResponSystemTime=" + TimeUtils.timeLongToDateStr(mResponServerTime, ""));
                hasGetServerTime = true;
                init(MonicatManager.getInstance().getContext(), MonicatConstants.MONICAT_SHARED_PREF)
                        .putLong(MonicatConstants.LAST_SERVER_TIME, mResponServerTime);
                init(MonicatManager.getInstance().getContext(), MonicatConstants.MONICAT_SHARED_PREF)
                        .putLong(MonicatConstants.LAST_SYSTEM_TIME, mResponSystemTime);
            }
        }
    }

    /**
     * 请求一下服务器，只是为了获取服务器的时间
     */
    private void requestServerTime() {
        LogUtils.d(MonicatConstants.SDK_NAME, "TimecalibrationManager-->requestServerTime()");
        String url = MonicatConstants.SERVER_HOST + MonicatConstants.SESSION_STATISTICS;
        BaseOkHttpClient.newBuilder()
                .get()
                .url(url)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        LogUtils.d(MonicatConstants.SDK_NAME, "TimecalibrationManager-->requestServerTime()_onSuccess()");
                    }

                    @Override
                    public void onError(int code) {
                        LogUtils.d(MonicatConstants.SDK_NAME, "TimecalibrationManager-->requestServerTime()_onError()=" + code);
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        LogUtils.d(MonicatConstants.SDK_NAME, "TimecalibrationManager-->requestServerTime()_onFailure()=" + e.toString());
                    }
                });
    }

//    public long getServerTime(ResponseInfo<String> responseInfo) {
//        long currentMillisTime = 0;
//        if (responseInfo != null) {
//            PreferenceActivity.Header headers = responseInfo.getFirstHeader("Date");
//            String strServerDate = headers.getValue();
//            if (!TextUtils.isEmpty(strServerDate)) {
//                //Thu, 29 Sep 2016 07:57:42 GMT
//                final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z",
//                        Locale.ENGLISH);
//                TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
//                try {
//                    Date serverDate = simpleDateFormat.parse(strServerDate);
//
//                    long responServerTime = serverDate.getTime();
//                    long responSystemTime = SystemClock.elapsedRealtime();
//                    currentMillisTime = responServerTime
//                            + SystemClock.elapsedRealtime() - responSystemTime;
//                } catch (Exception exception) {
//                    exception.printStackTrace();
//                }
//            }
//            return currentMillisTime;
//        }
//    }
}
