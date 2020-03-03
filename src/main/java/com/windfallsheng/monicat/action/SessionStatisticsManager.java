package com.windfallsheng.monicat.action;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.google.gson.Gson;
import com.windfallsheng.monicat.common.MonicatConstants;
import com.windfallsheng.monicat.db.service.DeviceInfoService;
import com.windfallsheng.monicat.db.service.SessionStatisticsService;
import com.windfallsheng.monicat.db.sqlite.StatisticsSQLiteHelper;
import com.windfallsheng.monicat.model.SessionInfoEntity;
import com.windfallsheng.monicat.model.BatchInfo;
import com.windfallsheng.monicat.model.DeviceInfo;
import com.windfallsheng.monicat.model.ParamMap;
import com.windfallsheng.monicat.model.ResponseEntity;
import com.windfallsheng.monicat.net.BaseCallBack;
import com.windfallsheng.monicat.net.BaseOkHttpClient;
import com.windfallsheng.monicat.util.LogUtils;
import com.windfallsheng.monicat.base.ShraredPrefManager;
import com.windfallsheng.monicat.util.TimeUtils;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;


/**
 * CreateDate: 2018/4/9.
 * <p>
 *
 * @author lzsheng
 * <p>
 * Description: 会话统计:用于统计启动次数，启动时间和退出时间等
 * <p>
 * 以下3种情况下，会视为用户打开一次新的会话：
 * <p>
 * 1) 应用第一次启动，或者应用进程在后台被杀掉之后启动
 * <p>
 * 2) 应用退到后台或锁屏超过X之后再次回到前台
 * <p>
 * X秒通过{@link MonicatConfig.Builder#setSessionTimoutMillis(int)} 设置，默认为30000ms，即30秒
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
class SessionStatisticsManager extends BaseStatisticsManager {

    private final String TAG = "SessionStatisticsManager";

    private long mSessionTimoutMillis;
    private int mActivityCount;
    private long mForegroundTime;
    private long mBackgroundTime;
    private boolean isForeground;

    /**
     * 在构造方法里注册Activity生命周期回调；
     *
     * @param application
     */
    public SessionStatisticsManager(Application application) {
        registerActivityLifecycleCallbacks(application);
    }

    /**
     * 处理APP启动的情况；
     */
    public void handleAppLaunch() {
        LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:handleAppLaunch#isForeground=" + isForeground +
                ", mForegroundTime=" + TimeUtils.timeLongToDefaultDateStr(mForegroundTime) +
                ", mBackgroundTime=" + TimeUtils.timeLongToDefaultDateStr(mBackgroundTime));
        SessionInfoEntity sessionInfo = new SessionInfoEntity();
        sessionInfo.setUploadeStatus(MonicatConstants.UPLOADABLE);
        sessionInfo.setTriggeringTime(System.currentTimeMillis());
        sessionInfo.setSessionType(MonicatConstants.APP_LAUNCH);
        int primaryKeyId = SessionStatisticsService.getInstance(MonicatManager.getInstance().getContext()).save(sessionInfo);
        LogUtils.i(MonicatConstants.SDK_NAME, TAG + ":::method:handleAppLaunch#Add session succeed!");

        // TODO: 2020/2/4 查询上一次退到后台记录 修改为退出状态，
        if (mBackgroundTime <= 0) {
            // 如果正常的启动应用时，要修改上一次的启动记录为退出，因为非正常退出时，上一条的记录无法标识为退出应用，需要在这里现修改一次
            ParamMap conditionQuery = new ParamMap()// 添加数据库查询条件
                    .setAndMap(StatisticsSQLiteHelper.COLUMN_SESSION_TYPE, MonicatConstants.APP_BACKGROUND);
            ParamMap conditionUpdate = new ParamMap()// 添加数据库查询条件
                    .setUpdateMap(StatisticsSQLiteHelper.COLUMN_SESSION_TYPE, MonicatConstants.APP_EXIT);
            SessionStatisticsService.getInstance(MonicatManager.getInstance().getContext())
                    .updataAdjacentData(primaryKeyId, conditionUpdate, conditionQuery, MonicatConstants.QUERY_DATA_NEXT);
            // 删除不必要的数据,比如已经上传过的数据、后台状态时存储的数据
            ParamMap paramMap = new ParamMap()// 添加数据库查询条件
                    .setOrMap(StatisticsSQLiteHelper.COLUMN_UPLOADE_STATUS, MonicatConstants.UPLOADED)
                    .setOrMap(StatisticsSQLiteHelper.COLUMN_SESSION_TYPE, MonicatConstants.APP_BACKGROUND);
            SessionStatisticsService.getInstance(MonicatManager.getInstance().getContext()).deleteByMap(paramMap);
        }
    }

    private void registerActivityLifecycleCallbacks(Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//                String activityName = activity.getClass().getName();
//                LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:onActivityCreated#activityName==" + activityName);
            }

            @Override
            public void onActivityStarted(Activity activity) {
//                String activityName = activity.getClass().getName();
//                LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:onActivityStarted#activityName==" + activityName);
            }

            @Override
            public void onActivityResumed(Activity activity) {
                mActivityCount++;
                String activityName = activity.getClass().getName();
                LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:onActivityResumed#mActivityCount=" + mActivityCount +
                        ", activityName=" + activityName);
                if (mActivityCount > 0) {
                    // 此时表明应用在前台
                    mForegroundTime = System.currentTimeMillis();
                    isForeground = true;
//            foregroundtTime = TimecalibrationManager.getInstance().getCurrentServerTime();
                    handleAppforeground();
                }
                LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:onActivityResumed#isForeground=" + isForeground +
                        ", mForegroundTime=" + TimeUtils.timeLongToDefaultDateStr(mForegroundTime));
            }

            @Override
            public void onActivityPaused(Activity activity) {
                mActivityCount--;
                String activityName = activity.getClass().getName();
                LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:onActivityPaused#mActivityCount=" + mActivityCount + ", activityName=" + activityName);
                if (mActivityCount == 0) {
                    // 此时表明应用在后台
                    mBackgroundTime = System.currentTimeMillis();
                    isForeground = false;
//            mBackgroundTime = TimecalibrationManager.getInstance().getCurrentServerTime();
                    handleAppBackground();
                }
                LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:onActivityPaused#isForeground=" + isForeground +
                        ", mBackgroundTime=" + TimeUtils.timeLongToDefaultDateStr(mBackgroundTime));
            }

            @Override
            public void onActivityStopped(Activity activity) {
//                String activityName = activity.getClass().getName();
//                LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:onActivityStopped#activityName==" + activityName);
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
//                String activityName = activity.getClass().getName();
//                LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:onActivitySaveInstanceState#activityName==" + activityName);
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
//                String activityName = activity.getClass().getName();
//                LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:onActivityDestroyed#activityName==" + activityName);
            }
        });
    }

    private void handleAppforeground() {
        LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:handleAppforeground#mActivityCount=" + mActivityCount +
                ", isForeground=" + isForeground + ", mForegroundTime=" +
                // 0 = 1970-01-01 08:00:00
                TimeUtils.timeLongToDefaultDateStr(mForegroundTime) + ", mBackgroundTime=" + TimeUtils.timeLongToDefaultDateStr(mBackgroundTime));
        if (mSessionTimoutMillis == 0) {
            mSessionTimoutMillis = MonicatManager.getInstance().getMonicatConfig().sessionTimoutMillis;
        }
        // 应用切换到后台时activityCount=0，切换到前台时activityCount=1;
        // 如果只判断(mForegroundTime - mBackgroundTime)，是不准确的，如果另外打开新Activity后，它的backgroundTime也是0，如果这时把这个Activity退出，
        // 也会满足这个判断条件，导致启动次数次数有误，Activity刚启动应用时backgroundTime为0
        if (mActivityCount == 1 && mBackgroundTime > 0 && (mForegroundTime - mBackgroundTime) >= mSessionTimoutMillis) {
            LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:handleAppforeground#mSessionTimoutMillis great than or equal to[ " + mSessionTimoutMillis + " ]milliseconds.\n");
            SessionInfoEntity sessionInfo = new SessionInfoEntity();
            sessionInfo.setUploadeStatus(MonicatConstants.UPLOADABLE);
            sessionInfo.setTriggeringTime(mForegroundTime);
            sessionInfo.setSessionType(MonicatConstants.APP_RESTART);
            int primaryKeyId = SessionStatisticsService.getInstance(MonicatManager.getInstance().getContext()).save(sessionInfo);
            LogUtils.i(MonicatConstants.SDK_NAME, TAG + ":::method:handleAppforeground#Add session succeed!");
            if (primaryKeyId > 0) {
                LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:handleAppforeground#" +
                        "Foreground data inserted into database successfully.");
                sessionInfo.setSessionId(primaryKeyId); // 注入本地数据库存储的主键ID，在某些功能需求上需要传递给后台，来判断本条是否上传传成功
                //
                handleStatisticsByStrategy();
            } else {
                LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:handleAppforeground#" +
                        "Foreground data insert database failed.");
            }
        }
    }

    private void handleAppBackground() {
        // 应用切换到后台时activityCount=0，切换到前台时activityCount=1;
        // 切换到后台的数据也在存储，在需要的时候使用，当前的使用是为了在非正常退出后，再次进来时把上次的后台状态改为退出应用状态
        if (mActivityCount == 0) {
            SessionInfoEntity sessionInfo = new SessionInfoEntity();
            sessionInfo.setUploadeStatus(MonicatConstants.UPLOADABLE);
            sessionInfo.setTriggeringTime(mForegroundTime);
            sessionInfo.setSessionType(MonicatConstants.APP_BACKGROUND);
            int primaryKeyId = SessionStatisticsService.getInstance(MonicatManager.getInstance().getContext()).save(sessionInfo);
            if (primaryKeyId > 0) {
                LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:handleAppBackground#" +
                        "Background data inserted into database successfully.");
            } else {
                LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:handleAppBackground#" +
                        "Background data insert database failed.");
            }
        }
    }

    @Override
    public void uploadData() {
        LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:uploadData");
        uploadCacheData();
    }

    @Override
    int queryCacheTotalCount() {
        ParamMap paramMap = new ParamMap()// 添加数据库查询条件
                .setOrMap(StatisticsSQLiteHelper.COLUMN_SESSION_TYPE, MonicatConstants.APP_EXIT)
                .setOrMap(StatisticsSQLiteHelper.COLUMN_SESSION_TYPE, MonicatConstants.APP_LAUNCH)
                .setOrMap(StatisticsSQLiteHelper.COLUMN_SESSION_TYPE, MonicatConstants.APP_RESTART)
                .setAndMap(StatisticsSQLiteHelper.COLUMN_SESSION_TYPE, MonicatConstants.UPLOADABLE)
                .setOrderDescMap(StatisticsSQLiteHelper.COLUMN_SESSION_ID);
        int totalCount = SessionStatisticsService.getInstance(MonicatManager.getInstance().getContext())
                .queryCountByMap(paramMap);
        return totalCount;
    }

    @Override
    BatchInfo newBatchInfo(int count) {
        return new BatchInfo(SessionStatisticsManager.class.getName(), count);
    }

    /**
     * 上传启动数据到服务器
     */
    @Override
    void uploadCacheData() {
        // TODO: 2020/2/6 查询对应表中的数据
        int versionCode = ShraredPrefManager.getInstance(MonicatManager.getInstance().getContext()).getAppVersionCode();
        ParamMap paramMap = new ParamMap()// 添加数据库查询条件
                .setOrderAscMap(StatisticsSQLiteHelper.COLUMN_DEVICE_INFO_ID);
        List<DeviceInfo> deviceInfos = DeviceInfoService.getInstance(MonicatManager.getInstance().getContext()).queryAllByMap(paramMap);
        String deviceUniqueId = "";
        if (deviceInfos != null && deviceInfos.size() > 0) {
            deviceUniqueId = deviceInfos.get(0).getDeviceUniqueId();
        }
        String url = MonicatConstants.SERVER_HOST + MonicatConstants.SESSION_STATISTICS;
        LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:uploadCacheData#url=" + url);

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
                                 LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:uploadCacheData#onSuccess=" + o.toString());
                                 //{"rt":0,"rtInfo":"正确","data":null}
                                 Gson gson = new Gson();
                                 ResponseEntity responseEntity = gson.fromJson(o.toString(), ResponseEntity.class);
                                 if (responseEntity.getRt() == 0) {
                                     LogUtils.d(MonicatConstants.SDK_NAME, "SessionStatisticsManager-->uploadSessionInfos()_Upload APP startup infos success! \nSessionStatisticsManager-->responseEntity.msg=" + responseEntity.getMsg());
                                     // TODO: 2018/5/7 修改本地数据库中缓存数据的上传状态，改为已上传 Constants.HAS_UPLOADED

                                 } else {

                                 }
                             }

                             @Override
                             public void onError(int code) {
                                 LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:uploadCacheData#onError=" + code);
                             }

                             @Override
                             public void onFailure(Call call, IOException e) {
                                 LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:uploadCacheData#onFailure=" + e.toString());
                             }
                         }
                );

//        Request request = new Request.Builder().url(MonicatManager.getInstance().getMonicatConfig().mUrl).build();
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
