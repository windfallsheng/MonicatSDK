package com.windfallsheng.monicat.net;

import android.text.TextUtils;

import com.windfallsheng.monicat.action.TimecalibrationManager;

import java.io.IOException;
import java.util.Date;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.http.HttpDate;

/**
 * CreateDate: 2018/5/3
 * <p>
 * Author: lzsheng
 * <p>
 * Description: 添加了个OkHttp的拦截器，获取服务器的时间，用于校准本地和服务器的时间
 * 在 {@link OkHttpManage} 的 initOkHttp() 方法中添加这个拦截器
 * <p>
 * Version:
 */
public class TimeCalibrationInterceptor implements Interceptor {

    private long mMinResponseTime = Long.MAX_VALUE;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long startTime = System.nanoTime();
//        LogUtils.d(Constants.SDK_NAME, "TimeCalibrationInterceptor-->startTime=" + startTime / 1000000000);
        Response response = chain.proceed(request);
//        LogUtils.d(Constants.SDK_NAME, "TimeCalibrationInterceptor-->responseTime=" + System.nanoTime() / 1000000000);
        long responseTime = System.nanoTime() - startTime;
//        LogUtils.d(Constants.SDK_NAME, "TimeCalibrationInterceptor-->responseTime=" + responseTime);

        Headers headers = response.headers();
        calibration(responseTime, headers);
        return response;
    }

    private void calibration(long responseTime, Headers headers) {
        if (headers == null) {
            return;
        }
        //如果这一次网络请求的 请求和响应 的时间差值小于上一次，则更新本地维护的时间
        if (responseTime >= mMinResponseTime) {
            return;
        }
        String standardTime = headers.get("Date");// standardTime = "Thu, 03 May 2018 07:05:58 GMT"
        if (!TextUtils.isEmpty(standardTime)) {
            Date parse = HttpDate.parse(standardTime);
            if (parse != null) {
                // 客户端请求过程一般大于比收到响应时间耗时，所以没有简单的除2 加上去，而是直接用该时间
                TimecalibrationManager.getInstance().getServerTime(parse.getTime());
                mMinResponseTime = responseTime;
            }
        }
    }
}
