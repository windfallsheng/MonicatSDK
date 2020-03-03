package com.windfallsheng.monicat.action;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.windfallsheng.monicat.base.UploadStrategy;
import com.windfallsheng.monicat.common.MonicatConstants;
import com.windfallsheng.monicat.listener.BatchDataChangeListener;
import com.windfallsheng.monicat.listener.UploadDataObserver;
import com.windfallsheng.monicat.model.BatchInfo;
import com.windfallsheng.monicat.model.Properties;
import com.windfallsheng.monicat.service.TimedService;
import com.windfallsheng.monicat.util.LogUtils;
import com.windfallsheng.monicat.util.TimeUtils;
import com.windfallsheng.monicat.util.TimedTaskUtils;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * CreateDate: 2018/4/9.
 * <p>
 *
 * @author lzsheng
 * <p>
 * Description: 根据上报策略等配置参数，进行相应的业务逻辑处理；
 * <p>
 * 注意要在本类的 {@link #monitor()}方法中必须要调用
 * {@link Application#registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks)}注册Activity生命周期监听，
 * 并且传入{@link SwitchEventManager}实例，来实现监听应用的前后台切换事件；
 * <p>
 * 同时本类作为被观察者，在需要上报数据时能通知到各个观察者，
 * 也即各种类型数据的处理类（比如会话类型数据的处理类 {@link SessionStatisticsManager}），在收到通知时可以各自上报自己的数据；
 * <p>
 * {@link UploadStrategy#INSTANT}
 * <p>
 * 如果上报策略是即时上报，则各个类自行上传数据(或者也可以由本类实现统一上传所有数据表的数据，
 * 可在本类的 onDataChangeListener() 方法中加判断条件进行处理)，
 * 其它的上报策略可以由本类集中统一上传所有数据表的数据，或者通知观察者各自上传自己的数据(目前采用这个方法)
 * <p>
 * {@link UploadStrategy#BATCH}
 * <p>
 * 对于批量上传的情况，每个类在初始化本类相关数据或者新增数据成功后，需要调用
 * 本类的{@link #onBatchDataChanged(BatchInfo)}方法，在这里会累计所有类型的数据的数量，并且判断是否达到批量上限值，
 * 之后再进行上传数据的操作。
 * <p>
 * Version:
 */
public class MonicatManager implements BatchDataChangeListener {

    private final String TAG = "MonicatManager";
    private static volatile MonicatManager instance = null;

    private MonicatConfig mMonicatConfig;
    private Context mContext;
    private UploadStrategy mUploadStrategy;
    private boolean mEnableSessionStatistics;
    private boolean mEnablePageStatistics;
    private boolean mEnableEventStatistics;
    private int mBatchValue;
    /**
     * 所有类型的数据已缓存的总数总和
     */
    private int mTotalBatchCount;
    private ScheduledExecutorService mUploadExecutorService;
    private CopyOnWriteArrayList<UploadDataObserver> mUploadDataObservers;
    private SessionStatisticsManager mSessionStatisticsManager;
    private PageStatisticsManager mPageStatisticsManager;
    private EventStatisticsManager mEventStatisticsManager;

    private MonicatManager() {

    }

    public static MonicatManager getInstance() {
        if (instance == null) {
            synchronized (MonicatManager.class) {
                if (instance == null) {
                    instance = new MonicatManager();
                }
            }
        }
        return instance;
    }

    public MonicatConfig getMonicatConfig() {
        return mMonicatConfig;
    }

    public Context getContext() {
        return mContext;
    }

    public UploadStrategy getUploadStrategy() {
        return mUploadStrategy;
    }

    /**
     * 初始化配置参数
     *
     * @param monicatConfig
     */
    public MonicatManager initConfig(@NonNull MonicatConfig monicatConfig) {
        if (monicatConfig == null) {
            throw new NullPointerException("Monicat:monicatConfig == null.");
        }
        this.mMonicatConfig = monicatConfig;
        this.mContext = monicatConfig.context;
        this.mUploadStrategy = monicatConfig.uploadStrategy;
        return instance;
    }

    /**
     * 根据上报策略等配置参数处理相关业务逻辑
     */
    public void monitor() {
        enforce("Application", "monitor");
        if (mMonicatConfig == null) {
            throw new NullPointerException("Monicat:mMonicatConfig == null.");
        }
        //是你当前方法执行堆栈
//        Thread.currentThread().getStackTrace()[1];
        //就是上一级的方法堆栈 以此类推
//        Thread.currentThread().getStackTrace()[2];
        StackTraceElement[] temp = Thread.currentThread().getStackTrace();
        StackTraceElement a = (StackTraceElement) temp[2];
        //这就是调用当前方法的方法名称
        Log.d(MonicatConstants.SDK_NAME, TAG + ":::method:monitor#currentMethodName=" + a.getMethodName());

        // 根据配置信息，完善内部必要的功能设置和逻辑关系处理
        LogUtils.init(mMonicatConfig.debugEnable);
        Boolean enableMonicat = mMonicatConfig.enableMonicat;
        if (!enableMonicat) {
            LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:monitor#return#enableMonicat=" + enableMonicat);
            return;
        }

        initConfigParams();

//        if (isDebugConfig == null) {// 没有设置isDebug值时，会根据外层项目app的模式（debug or release）模式来选择
//            Boolean isDebug = LogUtils.getBuildConfig(mContext);
//            Log.d(Constants.SDK_NAME, "Monicat: debugEnable==" + isDebug.booleanValue());
//        } else {
//            Log.d(Constants.SDK_NAME, "Monicat: isDebugConfig==" + isDebugConfig.booleanValue());
//        }
        TimecalibrationManager.getInstance().getCurrentServerTime();

        handleEnableStatistics();

        handleUploadStrategy();
    }

    private void initConfigParams() {
        mContext = mMonicatConfig.context;
        mUploadStrategy = mMonicatConfig.uploadStrategy;
        mEnableSessionStatistics = mMonicatConfig.enableSessionStatistics;
        mEnablePageStatistics = mMonicatConfig.enablePageStatistics;
        mEnableEventStatistics = mMonicatConfig.enableEventStatistics;
    }

    private void handleEnableStatistics() {
        // 是否开始会话统计的功能，若开启了此功能，要加入上报数据通知的观察者中
        if (mEnableSessionStatistics) {
            if (mContext == null) {
                throw new IllegalArgumentException("Monicat:mContext == null");
            }
            if (!(mContext instanceof Application)) {
                throw new IllegalArgumentException("Monicat:The context instance must be of type application");
            }
            /**
             * 有需要监听前后台变化，或有其它依据于此的业务逻辑处理的，需要添加到这个观察者里
             * 使用{@link SwitchEventManager}的监听
             */
            if (mSessionStatisticsManager == null) {
                mSessionStatisticsManager = new SessionStatisticsManager((Application) mContext);
            }
            //添加到这个观察者里，以便在需要上报数据时能被通知到
            if (mUploadStrategy != UploadStrategy.INSTANT) {
                addUploadDataObserver(mSessionStatisticsManager);
            }
            mSessionStatisticsManager.handleAppLaunch();
        }
        if (mEnablePageStatistics) {
            if (mPageStatisticsManager == null) {
                mPageStatisticsManager = new PageStatisticsManager();
            }
            if (mUploadStrategy != UploadStrategy.INSTANT) {
                addUploadDataObserver(mPageStatisticsManager);
            }
        }
        if (mEnableEventStatistics) {
            if (mEventStatisticsManager == null) {
                mEventStatisticsManager = new EventStatisticsManager();
            }
            if (mUploadStrategy != UploadStrategy.INSTANT) {
                addUploadDataObserver(mEventStatisticsManager);
            }
        }

        // 如果还有其它的数据类型要统计，也要添加到这个观察者里，以便在需要上报数据时能被通知到
//        addUploadDataObserver(***Observer);
    }

    /**
     * 判断上报策略，进行相关逻辑处理
     */
    private void handleUploadStrategy() {
        switch (mUploadStrategy) {
            case APP_LAUNCH:
                notifyUploadData();
                break;
            case TIMED_TASK:
                // 定时上报的策略
                if (mContext == null) {
                    throw new IllegalArgumentException("Monicat: Enabling this feature requires the " +
                            MonicatConfig.class.getSimpleName() + " to set the context parameter by method setContext()");
                }
                long timeMillis = mMonicatConfig.triggerTime;
                String triggerTime = TimeUtils.timeLongToDefaultDateStr(timeMillis);
                LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:monitor#TIMED_TASK#timeMillis="
                        + timeMillis + ", triggerTime=" + triggerTime);
                /**
                 * 通过定时时钟实现定时任务，在定时时钟中开启服务 {@link TimedService}，到指定时间时，再通知各观察者上传数据
                 */
                TimedTaskUtils.startTimedTask(mContext, timeMillis, AlarmManager.INTERVAL_DAY,
                        TimedService.class, TimedService.ACTION_TIMEDSERVICE_TIMED_UPLOAD);
                break;
            case PERIOD:
                // 间隔时间上报的策略，用ScheduledExecutorService去实现间隔时间上报数据
                long periodTime = mMonicatConfig.periodTime;
                LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:monitor#periodTime=" + periodTime);
                if (mUploadExecutorService == null) {
                    mUploadExecutorService = Executors.newSingleThreadScheduledExecutor();
                }
                mUploadExecutorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:monitor#mUploadExecutorService#run#currentTime=" + System.currentTimeMillis());
                        notifyUploadData();
                    }
                }, 0, periodTime, TimeUnit.MILLISECONDS);
                break;
            case BATCH:
                // 批量上报的策略
                handleBatchStrategyService();
                break;
            default:
                break;
        }
    }

    private void handleBatchStrategyService() {
        mBatchValue = mMonicatConfig.batchValue;
        if (mSessionStatisticsManager != null) {
            // 首先注入回调接口的实例
            mSessionStatisticsManager.setBatchDataChangeListener(this);
        }
        if (mPageStatisticsManager != null) {
            mPageStatisticsManager.setBatchDataChangeListener(this);
        }
        if (mEventStatisticsManager != null) {
            mEventStatisticsManager.setBatchDataChangeListener(this);
        }
    }

    /**
     * 各类型的数据在初始化(通过查询本地数据库)各自未上传的数据总和时，回调此方法，本类会累计入批量值的总和，
     * 并且之后再有数据变化，比如新增数据后，可以通过接口回调这个方法告知本类，本类会累计这个批量值，
     * 在本类累计的{@link MonicatManager#mTotalBatchCount}值，已达到批量上报策略的设定值时就通知各类型数据上传数据，
     * 并且在通知之前再初始化本类的{@link MonicatManager#mTotalBatchCount}值
     *
     * @param batchInfo 新增一条数据成功时，传递的参数值为1;初始化数据总和时，传递的参数是这个总和的值
     */
    @Override
    public void onBatchDataChanged(BatchInfo batchInfo) {
        LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:#onBatchDataChanged#batchInfo=" + batchInfo);
        if (batchInfo == null) {
            LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:onBatchDataChanged#return#batchInfo == null");
            return;
        }
        synchronized (MonicatManager.class) {
            mTotalBatchCount += batchInfo.getCount();
            LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:monitor#onBatchDataChanged#mTotalBatchCount=" + mTotalBatchCount);
            // 数据大于设定值时，就上传数据；
            if (mTotalBatchCount >= mBatchValue) {
                // TODO: 2018/5/2 如果是在这个类里集中上传所有表的数据时，可以先查询各表的数据，再请求
                // TODO: 2018/5/4  另一种情况中各个相关的类去处理各自的数据，这时可以通知观察者各自上传数据
                /**
                 * 先初始化{@link MonicatManager#mTotalBatchCount}值，
                 * 再调用{@link MonicatManager#notifyUploadData()}方法，因为各个数据类型在被通知上报数据并且各自成功后，
                 * 会再次回调{@link MonicatManager#onBatchDataChanged(int)}方法
                 */
                notifyUploadData();// 再通知各个数据类各自上传数据
                mTotalBatchCount = 0;
            }
        }
    }

    /**
     * 手动上传所有当前开启统计功能的数据；
     */
    public void uploadmEnableStatisticsData() {
        notifyUploadData();
    }

    /**
     * 添加进行数据上传操作的观察者
     */
    protected void addUploadDataObserver(UploadDataObserver uploadDataObserver) {
        if (uploadDataObserver == null) {
            throw new NullPointerException("Monicat: The uploadDataObserver parameter passed in is null.");
        }
        if (mUploadDataObservers == null) {
            mUploadDataObservers = new CopyOnWriteArrayList<>();
        }
        if (!mUploadDataObservers.contains(uploadDataObserver)) {
            mUploadDataObservers.add(uploadDataObserver);
        }
    }

    /**
     * 删除进行数据上传操作的观察者
     */
    protected void removeUploadDataObserver(UploadDataObserver uploadDataObserver) {
        if (mUploadDataObservers != null && mUploadDataObservers.contains(uploadDataObserver)) {
            mUploadDataObservers.remove(uploadDataObserver);
        }
    }

    /**
     * 删除所有进行数据上传操作的观察者
     */
    protected void removeAllUploadDataObservers() {
        if (mUploadDataObservers != null && mUploadDataObservers.size() > 0) {
            Iterator<UploadDataObserver> it = mUploadDataObservers.iterator();
            while (it.hasNext()) {
                it.remove();
            }
        }
    }

    /**
     * 通知所有的观察者，可以开始上传数据
     */
    private void notifyUploadData() {
        if (mUploadDataObservers != null && mUploadDataObservers.size() > 0) {
            for (UploadDataObserver observer : mUploadDataObservers) {
                LogUtils.d(MonicatConstants.SDK_NAME, TAG + ":::method:notifyUploadData#observer="
                        + observer.getClass().getSimpleName());
                observer.uploadData();
            }
        }
    }

    private void enforce(String className, String methodName) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException(className + " object calls method " + methodName + " in the non-main thread " + Thread.currentThread());
        }
    }

    /**
     * 标记一次页面访问；
     *
     * @param context 页面的设备上下文
     */
    public void trackPage(@NonNull Context context) {
        if (!mEnablePageStatistics) {
            throw new NullPointerException("Monicat: mEnablePageStatistics == false.");
        }
        if (context == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'trackbeginpage(Context context)' method is null or empty.");
        }
        String className = context.getClass().getName();
        enforce(className, "trackPage");
        mPageStatisticsManager.savePageInfo(className, "", MonicatConstants.PAGE_OPEN);
    }

    /**
     * 标记一次页面访问；
     *
     * @param context 页面的设备上下文
     */
    public void trackPage(@NonNull Context context, @NonNull String pageName) {
        if (!mEnablePageStatistics) {
            throw new NullPointerException("Monicat: mEnablePageStatistics == false.");
        }
        if (context == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'trackbeginpage(Context context)' method is null or empty.");
        }
        String className = context.getClass().getName();
        enforce(className, "trackPage");
        mPageStatisticsManager.savePageInfo(className, pageName, MonicatConstants.PAGE_OPEN);
    }

    /**
     * 标记一次页面访问的开始
     *
     * @param context 页面的设备上下文
     */
    public void trackBeginPage(@NonNull Context context) {
        if (!mEnablePageStatistics) {
            throw new NullPointerException("Monicat: mEnablePageStatistics == false.");
        }
        if (context == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'trackbeginpage(Context context)' method is null or empty.");
        }
        String className = context.getClass().getName();
        enforce(className, "trackBeginPage");
        mPageStatisticsManager.savePageInfo(className, "", MonicatConstants.PAGE_OPEN);
    }

    /**
     * 标记一次页面访问的开始
     *
     * @param context  页面的设备上下文
     * @param pageName 自定义页面名称
     */
    public void trackBeginPage(@NonNull Context context, @NonNull String pageName) {
        if (!mEnablePageStatistics) {
            throw new NullPointerException("Monicat: mEnablePageStatistics == false.");
        }
        if (pageName == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'trackbeginpage(Context context, String pageName)' method is null or empty.");
        }
        String className = context.getClass().getName();
        enforce(className, "trackBeginPage");
        // TODO: 2020/2/5 判断是否已注册 
        mPageStatisticsManager.savePageInfo(className, pageName, MonicatConstants.PAGE_OPEN);
    }

    /**
     * 标记一次页面访问的结束
     *
     * @param context 页面的设备上下文
     */
    public void trackEndPage(@NonNull Context context) {
        if (!mEnablePageStatistics) {
            throw new NullPointerException("Monicat: mEnablePageStatistics == false.");
        }
        if (context == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'trackEndPage(Context context)' method is null or empty.");
        }
        String className = context.getClass().getName();
        enforce(className, "trackEndPage");
        mPageStatisticsManager.savePageInfo(className, "", MonicatConstants.PAGE_CLOSE);
    }

    /**
     * 标记一次页面访问的结束
     *
     * @param context  页面的设备上下文
     * @param pageName 自定义页面名称
     */
    public void trackEndPage(@NonNull Context context, @NonNull String pageName) {
        if (!mEnablePageStatistics) {
            throw new NullPointerException("Monicat: mEnablePageStatistics == false.");
        }
        if (context == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'trackEndPage(Context context, String pageName)' method is null or empty.");
        }
        String className = context.getClass().getName();
        enforce(className, "trackEndPage");
        mPageStatisticsManager.savePageInfo(className, pageName, MonicatConstants.PAGE_CLOSE);
    }

    /**
     * 标记一次普通事件的开始
     *
     * @param context   页面的设备上下文
     * @param eventName 事件名称
     */
    public void trackBeginEvent(@NonNull Context context, @NonNull String eventName) {
        if (!mEnableEventStatistics) {
            throw new NullPointerException("Monicat: mEnableEventStatistics == false.");
        }
        if (context == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'trackbeginpage(Context context)' method is null or empty.");
        }
        String className = context.getClass().getName();
        enforce(className, "trackBeginEvent");
        mEventStatisticsManager.saveEventInfo(className, eventName,
                TimecalibrationManager.getInstance().getCurrentServerTime(), 0, null);
    }

    /**
     * 标记一次普通事件的结束
     *
     * @param context   页面的设备上下文
     * @param eventName 事件名称
     */
    public void trackEndEvent(@NonNull Context context, @NonNull String eventName) {
        if (!mEnableEventStatistics) {
            throw new NullPointerException("Monicat: mEnableEventStatistics == false.");
        }
        if (context == null) {
            throw new NullPointerException("Monicat: The parameter context for" +
                    " the method trackEndPage method is null.");
        }
        if (TextUtils.isEmpty(eventName)) {
            throw new NullPointerException("Monicat: The parameter eventName for" +
                    " the method trackEndPage is empty.");
        }
        String className = context.getClass().getName();
        enforce(className, "trackEndEvent");
        mEventStatisticsManager.saveEventInfo(className, eventName, 0,
                TimecalibrationManager.getInstance().getCurrentServerTime(), null);
    }

    /**
     * 标记一次自定义事件的次数;
     *
     * @param context    页面的设备上下文
     * @param eventName  事件名称
     * @param properties 自定义事件Key-Value参数
     */
    public void trackCustomEvent(@NonNull Context context, @NonNull String eventName, @NonNull Properties properties) {
        if (!mEnableEventStatistics) {
            throw new NullPointerException("Monicat: mEnableEventStatistics == false.");
        }
        if (context == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'trackEndPage(Context context, String pageName)' method is null or empty.");
        }
        String className = context.getClass().getName();
        enforce(className, "trackCustomEndEvent");
        mEventStatisticsManager.saveEventInfo(className, eventName, System.currentTimeMillis(), 0, properties);
    }

    /**
     * 标记一次自定义事件的开始
     *
     * @param context    页面的设备上下文
     * @param eventName  事件名称
     * @param properties 自定义事件Key-Value参数
     */
    public void trackCustomBeginEvent(@NonNull Context context, @NonNull String eventName, @NonNull Properties properties) {
        if (!mEnableEventStatistics) {
            throw new NullPointerException("Monicat: mEnableEventStatistics == false.");
        }
        if (context == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'trackbeginpage(Context context, String pageName)' method is null or empty.");
        }
        String className = context.getClass().getName();
        enforce(className, "trackCustomBeginEvent");
        mEventStatisticsManager.saveEventInfo(className, eventName, TimecalibrationManager.getInstance().getCurrentServerTime(),
                0, properties);
    }

    /**
     * 标记一次自定义事件的结束
     *
     * @param context    页面的设备上下文
     * @param eventName  事件名称
     * @param properties 自定义事件Key-Value参数
     */
    public void trackCustomEndEvent(@NonNull Context context, @NonNull String eventName, @NonNull Properties properties) {
        if (!mEnableEventStatistics) {
            throw new NullPointerException("Monicat: mEnableEventStatistics == false.");
        }
        if (context == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'trackEndPage(Context context, String pageName)' method is null or empty.");
        }
        String className = context.getClass().getName();
        enforce(className, "trackCustomEndEvent");
        mEventStatisticsManager.saveEventInfo(className, eventName, 0,
                TimecalibrationManager.getInstance().getCurrentServerTime(), properties);
    }

}
