package com.windfallsheng.monicat.action;

import com.windfallsheng.monicat.command.Constants;
import com.windfallsheng.monicat.command.UploadStrategy;
import com.windfallsheng.monicat.listener.BatchDataChangeListener;
import com.windfallsheng.monicat.listener.UploadDataObserver;
import com.windfallsheng.monicat.model.EventInfoEntity;
import com.windfallsheng.monicat.model.Properties;
import com.windfallsheng.monicat.net.BaseCallBack;
import com.windfallsheng.monicat.net.BaseOkHttpClient;
import com.windfallsheng.monicat.utils.LogUtils;

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
public class EventStatisticsManager implements UploadDataObserver {

    private BatchDataChangeListener mBatchDataChangeListener;
    private boolean hasInitFinished;       // 标识查询数据库数据总和是否执行完成

    public void setBatchDataChangeListener(BatchDataChangeListener batchDataChangeListener) {
        mBatchDataChangeListener = batchDataChangeListener;
    }

    /**
     * 初始化一些数据；
     * 查询本地数据库中的数据总和；
     */
    public void initBatchData() {
        UploadStrategy uploadStrategy = MonicatManager.getInstance().getConfig().uploadStrategy;
        if (uploadStrategy == UploadStrategy.BATCH) {
            hasInitFinished = false;
            int batchCount = 0;
            // batchCount = 数据库查询的count值
            notifyBatchDataChanged(batchCount);
            hasInitFinished = true;
        }
    }

    @Override
    public void startUploadData() {
        LogUtils.d(Constants.SDK_NAME, "EventStatisticsManager-->startUploadData()");
        // TODO: 2018/5/9 查询对应表中的数据
        uploadEventInfos();
    }

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
            LogUtils.d(Constants.SDK_NAME, "EventStatisticsManager-->saveEventInfo()_eventInfoEntity==" + eventInfoEntity);

//      TODO: 2018/5/9 保存到本地数据库中
            // …………eventInfoEntity的properties属性可直接转为json串存入数据库
        }

        UploadStrategy uploadStrategy = MonicatManager.getInstance().getConfig().uploadStrategy;
        if (uploadStrategy == UploadStrategy.BATCH) {
            if (hasInitFinished) { // 当初始化执行完成之后，再执行新增数据的逻辑
                // 新增一条数据成功时，传递参数值为1;
                notifyBatchDataChanged(1);
            }
        } else if (uploadStrategy == UploadStrategy.INSTANT) {
            LogUtils.d(Constants.SDK_NAME, "EventStatisticsManager-->saveEventInfo()_INSTANT");
            uploadEventInfos();
        }
    }

    /**
     * 回调 {@link MonicatManager#onBatchDataChanged(int)}方法
     *
     * @param batchCount
     * @see MonicatManager#onBatchDataChanged
     */
    private void notifyBatchDataChanged(int batchCount) {
        if (batchCount > 0 && mBatchDataChangeListener != null) {
            mBatchDataChangeListener.onBatchDataChanged(batchCount);
        }
        LogUtils.d(Constants.SDK_NAME, "EventStatisticsManager-->notifyBatchDataChanged()_batchCount==" + batchCount);
    }

    /**
     * 上传启动数据到服务器
     */
    private void uploadEventInfos() {
        String url = Constants.SERVER_HOST;
        LogUtils.d(Constants.SDK_NAME, "EventStatisticsManager-->uploadEventInfos()_url=" + url);
        BaseOkHttpClient.newBuilder()
                .addParam("key", "value")
                .isJsonParam(false)
                .post()
                .url(url)
                .build()
                .enqueue(new BaseCallBack() {
                             @Override
                             public void onSuccess(Object o) {
                                 LogUtils.d(Constants.SDK_NAME, "EventStatisticsManager-->uploadEventInfos()_onSuccess()=" /*+ o.toString()*/);
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
                                 LogUtils.d(Constants.SDK_NAME, "EventStatisticsManager-->uploadEventInfos()_onError()=" + code);
                             }

                             @Override
                             public void onFailure(Call call, IOException e) {
                                 LogUtils.d(Constants.SDK_NAME, "EventStatisticsManager-->uploadEventInfos()_onFailure()=" + e.toString());
                             }
                         }
                );
    }

}
