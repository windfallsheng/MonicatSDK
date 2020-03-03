package com.windfallsheng.monicat.action;

import com.windfallsheng.monicat.base.UploadStrategy;
import com.windfallsheng.monicat.common.MonicatConstants;
import com.windfallsheng.monicat.listener.BatchDataChangeListener;
import com.windfallsheng.monicat.listener.UploadDataObserver;
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
 * Description: 用户数据统计；
 * <p>
 * Version:
 */
class UserStatisticsManager extends BaseStatisticsManager {

    public void userRegister(String userAccount, long triggeringTime, Properties properties) {

//      TODO: 2018/5/9 保存到本地数据库中
        // …………eventInfoEntity的properties属性可直接转为json串存入数据库

        handleStatisticsByStrategy();
    }

    public void userLogin(String userAccount, long triggeringTime, Properties properties) {

//      TODO: 2018/5/9 保存到本地数据库中
        // …………eventInfoEntity的properties属性可直接转为json串存入数据库

        handleStatisticsByStrategy();
    }

    public void userLogout(String userAccount, long triggeringTime, Properties properties) {

//      TODO: 2018/5/9 保存到本地数据库中
        // …………eventInfoEntity的properties属性可直接转为json串存入数据库

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
