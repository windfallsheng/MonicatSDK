package com.windfallsheng.monicat.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.windfallsheng.monicat.action.MonicatManager;
import com.windfallsheng.monicat.command.Constants;
import com.windfallsheng.monicat.utils.LogUtils;
import com.windfallsheng.monicat.utils.TimeUtils;

/**
 * Created by lzsheng on 2017/12/7.定时任务的服务
 */

public class TimedService extends Service {

    public static final String ACTION_TIMEDSERVICE_TIMED_UPLOAD = "monicat.service.action.timedservice_timed_upload";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(Constants.SDK_NAME, "TimedService-->onStartCommand()_time==" + TimeUtils.timeLongToDateStr(System.currentTimeMillis(), ""));
        if (intent != null) {
            if (ACTION_TIMEDSERVICE_TIMED_UPLOAD.equals(intent.getAction())) {
                long triggerTime = TimeUtils.getTimeStamp();
//                TimedTaskUtils.startTimedTask(MonicatApplication.getInstance(), triggerTime, 900000L,
//                        TimedService.class, "action.timedservice.timedupload");
                // TODO: 2018/5/2 如果是在这个类里集中上传所有表的数据时，可以先查询各表的数据，再请求
                // TODO: 2018/5/4  另一种情况中各个相关的类去处理各自的数据，这时可以通知观察者各自上传数据
                MonicatManager.getInstance().notifyUploadData();
                LogUtils.d(Constants.SDK_NAME, "TimedService-->onStartCommand()_action==" + intent.getAction());
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(Constants.SDK_NAME, "TimedService-->onDestroy()");
    }

}
