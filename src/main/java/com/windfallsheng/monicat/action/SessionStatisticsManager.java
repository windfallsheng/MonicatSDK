package com.windfallsheng.monicat.action;

import com.google.gson.Gson;
import com.windfallsheng.monicat.base.Configuration;
import com.windfallsheng.monicat.command.Constants;
import com.windfallsheng.monicat.command.UploadStrategy;
import com.windfallsheng.monicat.db.service.DeviceInfoService;
import com.windfallsheng.monicat.db.service.SessionStatisticsService;
import com.windfallsheng.monicat.db.sqlitehelper.StatisticsSQLiteHelper;
import com.windfallsheng.monicat.listener.BatchDataChangeListener;
import com.windfallsheng.monicat.listener.SwitchEventObserver;
import com.windfallsheng.monicat.listener.UploadDataObserver;
import com.windfallsheng.monicat.model.AppStartupEntity;
import com.windfallsheng.monicat.model.DeviceInfo;
import com.windfallsheng.monicat.model.ParamMap;
import com.windfallsheng.monicat.model.ResponseEntity;
import com.windfallsheng.monicat.model.SwitchEvent;
import com.windfallsheng.monicat.net.BaseCallBack;
import com.windfallsheng.monicat.net.BaseOkHttpClient;
import com.windfallsheng.monicat.utils.LogUtils;
import com.windfallsheng.monicat.utils.TimeUtils;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;


/**
 * CreateDate: 2018/4/9.
 * <p>
 * Author: lzsheng
 * <p>
 * Description: 会话统计:用于统计启动次数，启动时间和退出时间等
 * <p>
 * 以下3种情况下，会视为用户打开一次新的会话：
 * <p>
 * 1) 应用第一次启动，或者应用进程在后台被杀掉之后启动
 * <p>
 * 2) 应用退到后台或锁屏超过X之后再次回到前台
 * <p>
 * X秒通过{@link Configuration.Builder#setIntervalTime(int)} 设置，默认为30000ms，即30秒
 * <p>
 * 主要功能方法：
 * <p>
 * 作为观察者，监听{@link SwitchEventManager} 应用程序的前后台切换状态；
 * <p>
 * 缓存启动数据到本地数据库，包括启动时间，退出时间，启动（包括前后台切换）记录；
 * <p>
 * 作为观察者，监听{@link MonicatManager#notifyUploadData()} 发出的通知并上报数据，或者根据上传数据的即时上报策略上传数据；
 * <p>
 * Version:
 */
public class SessionStatisticsManager implements SwitchEventObserver, UploadDataObserver {

    // private static SessionStatisticsManager instance = null;
    //    private OkHttpClient mOkHttpClient;
    private boolean hasInitFinished;       // 标识查询数据库数据总和是否执行完成
    private BatchDataChangeListener mBatchDataChangeListener;

    /*private SessionStatisticsManager(){

    }*/

   /* public static SessionStatisticsManager getInstance() {
        if (instance == null) {
            instance = new SessionStatisticsManager();
        }
        return instance;
    }*/

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
            ParamMap paramMap = new ParamMap()// 添加数据库查询条件
                    .setOrMap(StatisticsSQLiteHelper.COLUMN_STARTUP_TYPE, Constants.APP_EXIT)
                    .setOrMap(StatisticsSQLiteHelper.COLUMN_STARTUP_TYPE, Constants.APP_RUNNING)
                    .setOrMap(StatisticsSQLiteHelper.COLUMN_STARTUP_TYPE, Constants.APP_STARTUP)
                    .setAndMap(StatisticsSQLiteHelper.COLUMN_STARTUP_TYPE, Constants.NOT_UPLOADED)
                    .setOrderDescMap(StatisticsSQLiteHelper.COLUMN_STARTUP_ID);
            int batchCount = SessionStatisticsService.getInstance(MonicatManager.getInstance().getContext())
                    .queryCountByMap(paramMap);
            notifyBatchDataChanged(batchCount);
            hasInitFinished = true;
        }
    }

    @Override
    public void switchEventChanged(SwitchEvent switchEvent) {
//        LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->switchEventChange_switchEvent==" + switchEvent);
//        List<AppStartupEntity> startupNumEntities = SessionStatisticsService.getInstance(MonicatManager.getInstance().getContext())
//                .queryAllByMap(null);
//        LogUtils.d(Constants.SDK_NAME, "Monicat:startupNumEntities==" + startupNumEntities);
        int activityCount = switchEvent.getActivityCount();
        long foregroundTime = switchEvent.getForegroundtTime();
        long backgroundTime = switchEvent.getBackgroundTime();
        boolean isForeground = switchEvent.isForeground();
        LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->switchEventChange()_activityCount==" + activityCount);
        LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->switchEventChange()_isForeground==" + isForeground);
        LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->switchEventChange()_foregroundTime=="
                + TimeUtils.timeLongToDateStr(foregroundTime, ""));
        LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->switchEventChange()_backgroundTime=="
                + TimeUtils.timeLongToDateStr(backgroundTime, "")); // 0 = 1970-01-01 08:00:00
        UploadStrategy uploadStrategy = MonicatManager.getInstance().getConfig().uploadStrategy;
        long timeInterval = MonicatManager.getInstance().getConfig().intervalTime;

        // 应用切换到后台时activityCount=0，切换到前台时activityCount=1;
        // 如果只判断(foregroundTime - backgroundTime)，是不准确的，如果另外打开新Activity后，它的backgroundTime也是0，如果这时把这个Activity退出，
        // 也会满足这个判断条件，导致启动次数次数有误
        if (activityCount == 1 && (foregroundTime - backgroundTime) >= timeInterval) {// 某个Activity刚启动应用时backgroundTime为0
            LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->switchEventChange()_TimeInterval great than or equal to[ " + timeInterval + " ]milliseconds.\n");
            AppStartupEntity startupNumEntity = new AppStartupEntity();
            startupNumEntity.setHasUploaded(Constants.NOT_UPLOADED);
            startupNumEntity.setStartupTime(foregroundTime);
            if (backgroundTime == 0) {// 某个Activity刚启动应用时backgroundTime为0
                startupNumEntity.setStartupType(Constants.APP_STARTUP);
            } else {
                startupNumEntity.setStartupType(Constants.APP_RUNNING);
            }
            int primaryKeyId = SessionStatisticsService.getInstance(MonicatManager.getInstance().getContext()).save(startupNumEntity);
            if (primaryKeyId > 0) {
                LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->switchEventChange()_" +
                        "Foreground data inserted into database successfully.");
                startupNumEntity.setStartupId(primaryKeyId); // 注入本地数据库存储的主键ID，在某些功能需求上需要传递给后台，来判断本条是否上传传成功
                if (backgroundTime <= 0) {
                    // 如果正常的启动应用时，要修改上一次的启动记录为退出，因为非正常退出时，上一条的记录无法标识为退出应用，需要在这里现修改一次
                    ParamMap conditionQuery = new ParamMap()// 添加数据库查询条件
                            .setAndMap(StatisticsSQLiteHelper.COLUMN_STARTUP_TYPE, Constants.APP_BACKGROUND);
                    ParamMap conditionUpdate = new ParamMap()// 添加数据库查询条件
                            .setUpdateMap(StatisticsSQLiteHelper.COLUMN_STARTUP_TYPE, Constants.APP_EXIT);
                    SessionStatisticsService.getInstance(MonicatManager.getInstance().getContext())
                            .updataAdjacentData(primaryKeyId, conditionUpdate, conditionQuery, Constants.QUERY_DATA_NEXT);
                    // 删除不必要的数据,比如已经上传过的数据、后台状态时存储的数据
                    ParamMap paramMap = new ParamMap()// 添加数据库查询条件
                            .setOrMap(StatisticsSQLiteHelper.COLUMN_HAS_UPLOADED, Constants.HAS_UPLOADED)
                            .setOrMap(StatisticsSQLiteHelper.COLUMN_STARTUP_TYPE, Constants.APP_BACKGROUND);
                    SessionStatisticsService.getInstance(MonicatManager.getInstance().getContext()).deleteByMap(paramMap);
                }
                LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->switchEventChange()_" +
                        "hasInitFinished==" + hasInitFinished);
                // 是判断是否是批量上传时，先初始化mBatchCount值，存储成功一条就++，不必每次都从数据库查询
                if (uploadStrategy == UploadStrategy.BATCH && hasInitFinished) {// 当初始化执行完成之后，再执行新增数据的逻辑
                    // 新增一条数据成功时，传递参数值为1;
                    notifyBatchDataChanged(1);
                } else if (uploadStrategy == UploadStrategy.INSTANT) {
                    // todo 直接传startupNumEntity
                    LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->switchEventChange()_INSTANT");
                    uploadSessionInfos();
                }
            } else {
                LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->switchEventChange()_" +
                        "Foreground data insert database failed.");
            }
        } else {// 应用切换到后台时activityCount=0，切换到前台时activityCount=1;
            LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->switchEventChange()_TimeInterval less than[ " + timeInterval + " ]milliseconds.\n");
            // 切换到后台的数据也在存储，在需要的时候使用，当前的使用是为了在非正常退出后，再次进来时把上次的后台状态改为退出应用状态
            if (activityCount == 0) {
                AppStartupEntity startupNumEntity = new AppStartupEntity();
                startupNumEntity.setHasUploaded(Constants.NOT_UPLOADED);
                startupNumEntity.setStartupTime(foregroundTime);
                startupNumEntity.setStartupType(Constants.APP_BACKGROUND);
                int primaryKeyId = SessionStatisticsService.getInstance(MonicatManager.getInstance().getContext()).save(startupNumEntity);
                if (primaryKeyId > 0) {
                    LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->switchEventChange()_" +
                            "Background data inserted into database successfully.");
                } else {
                    LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->switchEventChange()_" +
                            "Background data insert database failed.");
                }
            }
        }
    }

    @Override
    public void startUploadData() {
        LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->startUploadData()");
//        ParamMap paramMap = new ParamMap()// 添加数据库查询条件
//                .setOrMap(StatisticsSQLiteHelper.COLUMN_STARTUP_TYPE, Constants.APP_EXIT)
//                .setOrMap(StatisticsSQLiteHelper.COLUMN_STARTUP_TYPE, Constants.APP_RUNNING)
//                .setOrMap(StatisticsSQLiteHelper.COLUMN_STARTUP_TYPE, Constants.APP_STARTUP)
//                .setAndMap(StatisticsSQLiteHelper.COLUMN_STARTUP_TYPE, Constants.NOT_UPLOADED)
//                .setOrderDescMap(StatisticsSQLiteHelper.COLUMN_STARTUP_ID);
//        List<AppStartupEntity> startupNumEntities = SessionStatisticsService.getInstance(MonicatManager.getInstance().getContext())
//                .queryAllByMap(paramMap);
        uploadSessionInfos();
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
        LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->notifyBatchDataChanged()_batchCount==" + batchCount);
    }

    /**
     * 上传启动数据到服务器
     */
    private void uploadSessionInfos() {
        int versionCode = ShraredPrefManager.getInstance(MonicatManager.getInstance().getContext()).getAppVersionCode();
        ParamMap paramMap = new ParamMap()// 添加数据库查询条件
                .setOrderAscMap(StatisticsSQLiteHelper.COLUMN_DEVICE_INFO_ID);
        List<DeviceInfo> deviceInfos = DeviceInfoService.getInstance(MonicatManager.getInstance().getContext()).queryAllByMap(paramMap);
        String deviceUniqueId = "";
        if (deviceInfos != null && deviceInfos.size() > 0) {
            deviceUniqueId = deviceInfos.get(0).getDeviceUniqueId();
        }
        String url = Constants.SERVER_HOST + Constants.SESSION_STATISTICS;
        LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->uploadSessionInfos()_url=" + url);
        LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->uploadSessionInfos()_deviceUniqueId=" + deviceUniqueId);
        LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->uploadSessionInfos()_versionNumber=" + versionCode);

        BaseOkHttpClient.newBuilder()
                .addParam("userId", deviceUniqueId)
                .addParam("versionNumber", versionCode)
                .isJsonParam(false)
                .post()
                .url(url)
                .build()
                .enqueue(new BaseCallBack() {
                             @Override
                             public void onSuccess(Object o) {
                                 LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->uploadSessionInfos()_onSuccess()=" + o.toString());
                                 //{"rt":0,"rtInfo":"正确","data":null}
                                 Gson gson = new Gson();
                                 ResponseEntity responseEntity = gson.fromJson(o.toString(), ResponseEntity.class);
                                 if (responseEntity.getRt() == 0) {
                                     LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->uploadSessionInfos()_Upload APP startup infos success! \nSessionStatisticsManager-->responseEntity.msg=" + responseEntity.getMsg());
                                     // TODO: 2018/5/7 修改本地数据库中缓存数据的上传状态，改为已上传 Constants.HAS_UPLOADED

                                 } else {

                                 }
                             }

                             @Override
                             public void onError(int code) {
                                 LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->uploadSessionInfos()_onError()=" + code);
                             }

                             @Override
                             public void onFailure(Call call, IOException e) {
                                 LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->uploadSessionInfos()_onFailure()=" + e.toString());
                             }
                         }
                );

//        Request request = new Request.Builder().url(MonicatManager.getInstance().getConfig().mUrl).build();
//        mOkHttpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                LogUtils.d(Constants.SDK_NAME, "uploadAppStartNum()_onFailure=" + e.toString());
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                LogUtils.d(Constants.SDK_NAME, "uploadAppStartNum()_onResponse");
//
//            }
//        });
//        mOkHttpClient = new OkHttpClient.Builder()
//                .connectTimeout(10, TimeUnit.SECONDS)
//                .writeTimeout(10, TimeUnit.SECONDS)
//                .readTimeout(20, TimeUnit.SECONDS)
//                .build();
//        //post方式提交的数据,提交表单方式
//        FormBody formBody = new FormBody.Builder()
//                .add("userId", deviceUniqueId)
//                .add("type", String.valueOf(16))
//                .add("versionNumber", String.valueOf(versionCode))
//                .build();
//        // POST方式请求（提交json方式）MediaType  设置Content-Type 标头中包含的媒体类型值
////        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
////                , json);
//        final Request request = new Request.Builder()
//                .url(url)//请求的url
//                .post(formBody)
//                .build();
//        //创建/Call
//        Call call = mOkHttpClient.newCall(request);
//        //加入队列 异步操作
//        call.enqueue(new Callback() {
//            //请求错误回调方法
//            @Override
//            public void onFailure(Call call, IOException e) {
//                LogUtils.d(Constants.SDK_NAME, "uploadAppStartNum()_onFailure=" + e.toString());
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                LogUtils.d(Constants.SDK_NAME, "uploadAppStartNum()_onResponse()_response.code()" + response.code());
//                if (response.code() == 200) {
//                    LogUtils.d(Constants.SDK_NAME, "uploadAppStartNum()_onResponse" + response.body().string());
//                }
//            }
//        });

    }

}
