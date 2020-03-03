package com.windfallsheng.monicat.action;

import com.windfallsheng.monicat.common.MonicatConstants;
import com.windfallsheng.monicat.model.BatchInfo;
import com.windfallsheng.monicat.model.EventInfoEntity;
import com.windfallsheng.monicat.model.Properties;
import com.windfallsheng.monicat.net.BaseCallBack;
import com.windfallsheng.monicat.net.BaseOkHttpClient;
import com.windfallsheng.monicat.util.LogUtils;

import java.io.IOException;

import okhttp3.Call;


/**
 * CreateDate: 2018/4/9.
 * <p>
 * Author: lzsheng
 * <p>
 * Description: 事件统计:包括普通事件、自定义事件统计
 * <p>
 * 可以统计次数：统计指定行为被触发的次数；
 * 也可以统计统计时长：统计两个指定行为之间的消耗时间
 * <p>
 * 本地数据的缓存；
 * <p>
 * 数据的上传；
 * <p>
 * 作为观察者，监听{@link MonicatManager#notifyUploadData()} 发出的通知并上报数据，或者根据上传数据的即时上报策略上传数据；
 * <p>
 * Version:
 */
class EventStatisticsManager extends BaseStatisticsManager {

    /**
     * 保存数据到数据库，并且根据上传策略完成必要的逻辑处理
     *
     * @param className
     * @param eventName
     * @param triggeringTime
     * @param endTime
     * @param properties
     */
    public void saveEventInfo(String className, String eventName, long triggeringTime, long endTime, Properties properties) {
        synchronized (this) {
            EventInfoEntity eventInfoEntity = new EventInfoEntity(className, eventName, triggeringTime,
                    endTime, properties);
            LogUtils.d(MonicatConstants.SDK_NAME, "EventStatisticsManager-->saveEventInfo()_eventInfoEntity==" + eventInfoEntity);

//      TODO: 2018/5/9 保存到本地数据库中
            // …………eventInfoEntity的properties属性可直接转为json串存入数据库
        }

        handleStatisticsByStrategy();

    }

    @Override
    public void uploadData() {
        LogUtils.d(MonicatConstants.SDK_NAME, "EventStatisticsManager-->uploadData()");
        uploadCacheData();
    }


    @Override
    int queryCacheTotalCount() {
        return 0;
    }

    @Override
    BatchInfo newBatchInfo(int count) {
        return new BatchInfo(UserStatisticsManager.class.getName(), count);
    }

    /**
     * 上传启动数据到服务器
     */
    @Override
    void uploadCacheData() {
        // TODO: 2020/2/6 查询对应表中的数据
        String url = MonicatConstants.SERVER_HOST;
        LogUtils.d(MonicatConstants.SDK_NAME, "EventStatisticsManager-->uploadEventInfos()_url=" + url);
        BaseOkHttpClient.newBuilder()
                .addParam("key", "value")
                .isJsonParam(false)
                .post()
                .url(url)
                .build()
                .enqueue(new BaseCallBack() {
                             @Override
                             public void onSuccess(Object o) {
                                 LogUtils.d(MonicatConstants.SDK_NAME, "EventStatisticsManager-->uploadEventInfos()_onSuccess()=" /*+ o.toString()*/);
                                 //{"rt":0,"rtInfo":"正确","data":null}
//                                 Gson gson = new Gson();
//                                 ResponseEntity responseEntity = gson.fromJson(o.toString(), ResponseEntity.class);
//                                 if (responseEntity.getRt() == 0) {
//                                     LogUtils.d(Constants.SDK_NAME, "EventStatisticsManager-->uploadEventInfos()_Upload account infos success! \nSessionStatisticsManager-->responseEntity.msg=" + responseEntity.getMsg());
//                                     // TODO: 2018/5/7 修改本地数据库中缓存数据的上传状态，改为已上传 Constants.HAS_UPLOADED
//
//                                 } else {
//
//                                 }
                             }

                             @Override
                             public void onError(int code) {
                                 LogUtils.d(MonicatConstants.SDK_NAME, "EventStatisticsManager-->uploadEventInfos()_onError()=" + code);
                             }

                             @Override
                             public void onFailure(Call call, IOException e) {
                                 LogUtils.d(MonicatConstants.SDK_NAME, "EventStatisticsManager-->uploadEventInfos()_onFailure()=" + e.toString());
                             }
                         }
                );
    }

}
