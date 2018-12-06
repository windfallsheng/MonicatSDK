package com.windfallsheng.monicat.action;

import com.windfallsheng.monicat.command.Constants;
import com.windfallsheng.monicat.command.UploadStrategy;
import com.windfallsheng.monicat.listener.ActivityLifecycleObserver;
import com.windfallsheng.monicat.listener.BatchDataChangeListener;
import com.windfallsheng.monicat.listener.UploadDataObserver;
import com.windfallsheng.monicat.model.ActivityLifecycle;
import com.windfallsheng.monicat.model.PageInfoEntity;
import com.windfallsheng.monicat.net.BaseCallBack;
import com.windfallsheng.monicat.net.BaseOkHttpClient;
import com.windfallsheng.monicat.utils.LogUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;


/**
 * CreateDate: 2018/4/9.
 * <p>
 * Author: lzsheng
 * <p>
 * Description: 页面统计:标记页面访问的开始或者结束；
 * 本地数据的缓存；
 * <p>
 * 数据的上传；
 * <p>
 * 作为观察者，监听{@link SwitchEventManager} 每个activity生命周期变化；
 * 作为观察者，监听{@link MonicatManager#notifyUploadData()} 发出的通知并上报数据，或者根据上传数据的即时上报策略上传数据；
 * <p>
 * Version:
 */
public class PageStatisticsManager implements ActivityLifecycleObserver, UploadDataObserver {

    private Map<String, String> mPageMaps;  // 存放注册了记录页面打开状态的activity的全路径名称
    private boolean hasInitFinished;       // 标识查询数据库数据总和是否执行完成
    private BatchDataChangeListener mBatchDataChangeListener;

    public void setBatchDataChangeListener(BatchDataChangeListener batchDataChangeListener) {
        mBatchDataChangeListener = batchDataChangeListener;
    }

    /**
     * 初始化一些数据；
     * 查询本地数据库中的数据总和
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
    public void activityLifecycleChanged(ActivityLifecycle activityLifecycle) {
        LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->activityLifecycleChanged()_activityLifecycle==" + activityLifecycle);
        if (mPageMaps != null && mPageMaps.size() > 0) { // 说明是注册了页面打开和关闭
            boolean isForeground = activityLifecycle.isForeground();
            String activityName = activityLifecycle.getActivityName();
            if (mPageMaps.containsKey(activityName)) {
                String pageName = mPageMaps.get(activityName);
                int openOrClose = -1;
                int lifeStatus = activityLifecycle.getLifeStatus();
                LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->activityLifecycleChanged()_lifeStatus==" + lifeStatus);
                switch (lifeStatus) {
                    case Constants.ON_ACTIVITY_CREATED:
                        LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->activityLifecycleChanged()_onActivityCreated()");
                        openOrClose = Constants.PAGE_OPEN;
                        break;
                    case Constants.ON_ACTIVITY_STARTED:
                        LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->activityLifecycleChanged()_onActivityStarted()");
                        openOrClose = Constants.PAGE_OPEN;
                        break;
                    case Constants.ON_ACTIVITY_RESUMED:
                        LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->activityLifecycleChanged()_onActivityResumed()");
                        openOrClose = Constants.PAGE_OPEN;
                        break;

                    case Constants.ON_ACTIVITY_PAUSED:
                        LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->activityLifecycleChanged()_onActivityPaused()");
                        openOrClose = Constants.PAGE_CLOSE;
                        break;
                    case Constants.ON_ACTIVITY_STOPPED:
                        LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->activityLifecycleChanged()_onActivityStoped()");
                        openOrClose = Constants.PAGE_CLOSE;
                        break;
                    case Constants.ON_ACTIVITY_SAVEINSTANCESTATE:
                        LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->activityLifecycleChanged()_onActivitySaveInstanceState()");
                        openOrClose = Constants.PAGE_CLOSE;
                        break;
                    case Constants.ON_ACTIVITY_DESTROYED:
                        LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->activityLifecycleChanged()_onActivityDestroyed()");
                        openOrClose = Constants.PAGE_CLOSE;
                        break;
                    default:
                        break;
                }
//                LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->activityLifecycleChanged()_isForeground==" + isForeground);
                LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->activityLifecycleChanged()_activityName==" + activityName);
                LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->activityLifecycleChanged()_pageName==" + pageName);
                LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->activityLifecycleChanged()_openOrClose==" + openOrClose);
                if (openOrClose != -1) {
                    savePageInfo(activityName, pageName, openOrClose);
                }
            }
        }
    }

    @Override
    public void startUploadData() {
        LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->startUploadData()");
        // TODO: 2018/5/9 查询对应表中的数据
        uploadPageInfos();
    }

    /**
     * 添加到记录页面打开状态的activity的全路径名称的集合中
     *
     * @param className
     * @param pageName
     */
    public void addPage(String className, String pageName) {
        if (mPageMaps == null) {
            mPageMaps = new HashMap<>();
        }
        if (!mPageMaps.containsKey(className)) {
            mPageMaps.put(className, pageName);
        }
        LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->addPage()_mPageMaps==" + mPageMaps.toString());
    }

    /**
     * 从记录页面打开状态的activity的全路径名称的集合中移除
     *
     * @param className
     */
    public void removePage(String className) {
        if (mPageMaps != null && mPageMaps.containsKey(className)) {
            mPageMaps.remove(className);
        }
    }

    /**
     * 获取记录页面打开状态的activity的全路径名称的集合
     *
     * @return
     */
    public Map<String, String> getPageMaps() {
        return mPageMaps;
    }

    /**
     * 清除记录页面打开状态的activity的全路径名称的集合
     */
    public void clearPage() {
        if (mPageMaps != null && mPageMaps.size() > 0) {
            mPageMaps.clear();
        }
    }

    /**
     * 保存数据到数据库，并且根据上传策略完成必要的逻辑处理
     *
     * @param className
     * @param pageName
     * @param openOrClose
     */
    public void savePageInfo(String className, String pageName, int openOrClose) {
        synchronized (this) {
            PageInfoEntity pageInfoEntity = new PageInfoEntity(className, pageName,
                    TimecalibrationManager.getInstance().getCurrentServerTime(), openOrClose);
            LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->savePageInfos()_pageInfoEntity==" + pageInfoEntity);

//      TODO: 2018/5/9 保存到本地数据库中
            // …………
        }
        UploadStrategy uploadStrategy = MonicatManager.getInstance().getConfig().uploadStrategy;
        if (uploadStrategy == UploadStrategy.BATCH) {
            if (hasInitFinished) { // 当初始化执行完成之后，再执行新增数据的逻辑
                // 新增一条数据成功时，传递参数值为1;
                notifyBatchDataChanged(1);
            }
        } else if (uploadStrategy == UploadStrategy.INSTANT) {
            LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->saveAccountInfos()_INSTANT");
            uploadPageInfos();
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
        LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->notifyBatchDataChanged()_batchCount=" + batchCount);
    }

    /**
     * 上传启动数据到服务器
     */
    private void uploadPageInfos() {
        String url = Constants.SERVER_HOST;
        LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->uploadPageInfos()_url=" + url);
        BaseOkHttpClient.newBuilder()
                .addParam("key", "value")
                .isJsonParam(false)
                .post()
                .url(url)
                .build()
                .enqueue(new BaseCallBack() {
                             @Override
                             public void onSuccess(Object o) {
                                 LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->uploadAccountInfos()_onSuccess()=" /*+ o.toString()*/);
                                 //{"rt":0,"rtInfo":"正确","data":null}
//                                 Gson gson = new Gson();
//                                 ResponseEntity responseEntity = gson.fromJson(o.toString(), ResponseEntity.class);
//                                 if (responseEntity.getRt() == 0) {
//                                     LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->uploadAccountInfos()_Upload account infos success! \nSessionStatisticsManager-->responseEntity.msg=" + responseEntity.getMsg());
//                                     // TODO: 2018/5/7 修改本地数据库中缓存数据的上传状态，改为已上传 Constants.HAS_UPLOADED
//
//                                 } else {
//
//                                 }
                             }

                             @Override
                             public void onError(int code) {
                                 LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->uploadPageInfos()_onError()=" + code);
                             }

                             @Override
                             public void onFailure(Call call, IOException e) {
                                 LogUtils.d(Constants.SDK_NAME, "PageStatisticsManager-->uploadPageInfos()_onFailure()=" + e.toString());
                             }
                         }
                );
    }

}
