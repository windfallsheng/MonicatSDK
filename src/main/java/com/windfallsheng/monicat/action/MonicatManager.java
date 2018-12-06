package com.windfallsheng.monicat.action;

import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.windfallsheng.monicat.base.Configuration;
import com.windfallsheng.monicat.command.Constants;
import com.windfallsheng.monicat.command.UploadStrategy;
import com.windfallsheng.monicat.listener.BatchDataChangeListener;
import com.windfallsheng.monicat.listener.SwitchEventObserver;
import com.windfallsheng.monicat.listener.UploadDataObserver;
import com.windfallsheng.monicat.model.ActivityLifecycle;
import com.windfallsheng.monicat.model.Properties;
import com.windfallsheng.monicat.model.SwitchEvent;
import com.windfallsheng.monicat.net.BaseCallBack;
import com.windfallsheng.monicat.net.BaseOkHttpClient;
import com.windfallsheng.monicat.service.TimedService;
import com.windfallsheng.monicat.utils.LogUtils;
import com.windfallsheng.monicat.utils.TimeUtils;
import com.windfallsheng.monicat.utils.TimedTaskUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;


/**
 * CreateDate: 2018/4/9.
 * <p>
 * Author: lzsheng
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
 * 本类的{@link #onBatchDataChanged(int)}方法，在这里会累计所有类型的数据的数量，并且判断是否达到批量上限值，
 * 之后再进行上传数据的操作。
 * <p>
 * Version:
 */
public class MonicatManager implements SwitchEventObserver, BatchDataChangeListener {

    private static volatile MonicatManager instance = null;
    private Configuration mConfig;
    private Context mContext;
    private ScheduledExecutorService mUploadExecutorService;
    private List<UploadDataObserver> mUploadDataObservers = new ArrayList<UploadDataObserver>();
    /**
     * 所有类型的数据已缓存的总数总和
     */
    private int mBatchCount;
    private SessionStatisticsManager mSessionStatisticsManager;
    private SwitchEventManager mSwitchEventManager;
    private PageStatisticsManager mPageStatisticsManager;
    private EventStatisticsManager mEventStatisticsManager;
    /**
     * app 运行状态：1为刚启动打开应用，0为应用正在运行中(包括在前后台的情况)
     */
    public int app_status;
    /**
     * isForeground
     */
    public boolean isForeground;

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

    public Configuration getConfig() {
        return mConfig;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * 初始化配置参数
     *
     * @param config
     */
    public void init(@NonNull Configuration config) {
        if (config == null) {
            throw new NullPointerException("Monicat: The injected 'Configuration' instance is null.");
        }
        this.mConfig = config;
        this.mContext = config.context;
    }

    @Override
    public void switchEventChanged(SwitchEvent switchEvent) {
        isForeground = switchEvent.isForeground();
        int activityCount = switchEvent.getActivityCount();
        long foregroundTime = switchEvent.getForegroundtTime();
        long backgroundTime = switchEvent.getBackgroundTime();
        if (activityCount == 1 && backgroundTime <= 0) {// 启动应用
            app_status = Constants.APP_STARTUP;
            UploadStrategy uploadStrategy = mConfig.uploadStrategy;
            if (uploadStrategy == UploadStrategy.APP_LAUNCH) {
                // TODO: 2018/5/2 如果是在这个类里集中上传所有表的数据时，可以先查询各表的数据，再请求
                // TODO: 2018/5/4  另一种情况中各个相关的类去处理各自的数据，这时可以通知观察者各自上传数据
                notifyUploadData();
            }
        } else {
            app_status = Constants.APP_RUNNING;
        }
        LogUtils.d(Constants.SDK_NAME, "MonicatManager-->switchEventChanged()_mBatchCount==" + mBatchCount);
    }

    /**
     * 根据上报策略等配置参数处理相关业务逻辑
     */
    public void monitor() {
        if (mConfig == null) {
            throw new NullPointerException("Monicat: The injected 'Configuration' instance is null.");
        }
        /**
         * 1.获取必要的配置参数
         */
        Boolean isDebugConfig = mConfig.isDebug;
        boolean onSessionStatistics = mConfig.onSessionStatistics;
        UploadStrategy uploadStrategy = mConfig.uploadStrategy;
        /**
         * 2.完成一些初始化逻辑
         */
        if (isDebugConfig == null) {// 没有设置isDebug值时，会根据外层项目app的模式（debug or release）模式来选择
            Boolean isDebug = LogUtils.syncIsDebug(mContext);
            Log.d(Constants.SDK_NAME, "Monicat: isDebug==" + isDebug.booleanValue());
        } else {
            Log.d(Constants.SDK_NAME, "Monicat: isDebugConfig==" + isDebugConfig.booleanValue());
        }
        TimecalibrationManager.getInstance().getCurrentServerTime();
        if (mSessionStatisticsManager == null) {
            mSessionStatisticsManager = new SessionStatisticsManager();
        }
        if (mSwitchEventManager == null) {
            mSwitchEventManager = new SwitchEventManager();
        }
        if (mPageStatisticsManager == null) {
            mPageStatisticsManager = new PageStatisticsManager();
        }
        if (mEventStatisticsManager == null) {
            mEventStatisticsManager = new EventStatisticsManager();
        }
        /**
         * 3.根据配置信息，完善内部必要的功能设置和逻辑关系处理
         */
        // 3.1 如果传入了这个context参数，必须要在这里注册这个回调事件，以实现对应用前后台切换的监听和对Activity生命周期的监听处理
        if (mContext != null && mContext instanceof Application) {
            ((Application) mContext).registerActivityLifecycleCallbacks(mSwitchEventManager);
        } else {
            if (mContext == null) {
                throw new NullPointerException("Monicat: The injected context is null.");
            } else {
                throw new IllegalArgumentException("Monicat: The injected context is not an instance of the application," +
                        " and therefore the ActivityLifecycleCallbacks is not registered.");
            }
        }
        // 3.2 是否开始会话统计的功能，若开启了此功能，需要加入观察者，接收前后台切换的监听事件通知，同时还要加入上报数据通知的观察者中
        if (onSessionStatistics) {
            /**
             * 有需要监听前后台变化，或有其它依据于此的业务逻辑处理的，需要添加到这个观察者里
             * 使用{@link SwitchEventManager}的监听
             */
            mSwitchEventManager.addSwitchEventObserver(mSessionStatisticsManager);
            /**
             * 添加到这个观察者里，以便在需要上报数据时能被通知到
             */
            addUploadDataObserver(mSessionStatisticsManager);
        } else {
            /**
             *  移除{@link SwitchEventManager} 中这个观察者的监听
             *  移除通知上报数据的这个观察者的监听
             */
            mSwitchEventManager.removeSwitchEventObserver(mSessionStatisticsManager);
            removeUploadDataObserver(mSessionStatisticsManager);
        }
        /**
         * 如果还有其它的数据类型要统计，也要添加到这个观察者里，以便在需要上报数据时能被通知到
         */
        addUploadDataObserver(mPageStatisticsManager);
        addUploadDataObserver(mEventStatisticsManager);
//        addUploadDataObserver(***Observer);
        // 3.3 判断上报策略，进行相关逻辑处理
        switch (uploadStrategy) {
            case APP_LAUNCH:
                /** 如果是  {@link UploadStrategy.APP_LAUNCH} 的上报策略，需要本类监听这个前后台的切换事件，
                 * 之后在观察者中重写的 switchEventChanged()方法里判断是否是刚打开应用，并且在这个监听事件里处理上报数据的逻辑
                 * 使用{@link SwitchEventManager}的监听
                 */
                mSwitchEventManager.addSwitchEventObserver(MonicatManager.getInstance());
                break;
            case TIMED_TASK:// 定时上报的策略
                long timeMillis = mConfig.triggerTime;
                String triggerTime = TimeUtils.timeLongToDateStr(timeMillis, "");
                LogUtils.d(Constants.SDK_NAME, "MonicatManager-->monitor()_TIMED_TASK_timeMillis=="
                        + timeMillis + " triggerTime==" + triggerTime);
                /**
                 * 通过定时时钟实现定时任务，在定时时钟中开启服务 {@link TimedService}，到指定时间时，再通知各观察者上传数据
                 */
                TimedTaskUtils.startTimedTask(MonicatManager.getInstance().getContext(), timeMillis, AlarmManager.INTERVAL_DAY,
                        TimedService.class, TimedService.ACTION_TIMEDSERVICE_TIMED_UPLOAD);
                break;
            case PERIOD:// 间隔时间上报的策略
                //用ScheduledExecutorService去实现间隔时间上报数据
                long periodTime = mConfig.periodTime;
                LogUtils.d(Constants.SDK_NAME, "MonicatManager-->monitor()_periodTime==" + periodTime);
                if (mUploadExecutorService == null) {
                    mUploadExecutorService = Executors.newSingleThreadScheduledExecutor();
                }
                mUploadExecutorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        LogUtils.d(Constants.SDK_NAME, "SessionStatisticsManager-->monitor()_mUploadExecutorService_run()" + System.currentTimeMillis());
                        // TODO: 2018/5/2 如果是在这个类里集中上传所有表的数据时，可以先查询各表的数据，再请求
                        // TODO: 2018/5/4  另一种情况中各个相关的类去处理各自的数据，这时可以通知观察者各自上传数据
                        notifyUploadData();
                    }
                }, 0, periodTime, TimeUnit.MILLISECONDS);
                break;
            case BATCH:// 批量上报的策略
                // 首先注入回调接口的实例
                if (onSessionStatistics) {
                    mSessionStatisticsManager.setBatchDataChangeListener(this);
                }
                mEventStatisticsManager.setBatchDataChangeListener(this);
                mPageStatisticsManager.setBatchDataChangeListener(this);
                // 初始化各个类型数据当前未上传数量的总和
                if (onSessionStatistics) {
                    mSessionStatisticsManager.initBatchData();
                }
                mEventStatisticsManager.initBatchData();
                mPageStatisticsManager.initBatchData();
                break;
            default:
                break;
        }
    }

    /**
     * 各类型的数据在初始化(通过查询本地数据库)各自未上传的数据总和时，回调此方法，本类会累计入批量值的总和，
     * 并且之后再有数据变化，比如新增数据后，可以通过接口回调这个方法告知本类，本类会累计这个批量值，
     * 在本类累计的{@link MonicatManager#mBatchCount}值，已达到批量上报策略的设定值时就通知各类型数据上传数据，
     * 并且在通知之前再初始化本类的{@link MonicatManager#mBatchCount}值
     *
     * @param batchCount 新增一条数据成功时，传递的参数值为1;初始化数据总和时，传递的参数是这个总和的值
     */
    @Override
    public void onBatchDataChanged(int batchCount) {
        UploadStrategy uploadStrategy = mConfig.uploadStrategy;
        if (uploadStrategy == UploadStrategy.BATCH) {// 这个判断可以去掉，因为在各个统计数据的类中，会对上报策略进行判断，再调用onDataChangeListener()方法
            // 数据大于设定值时，就批量上传
            int batchValue = MonicatManager.getInstance().getConfig().batchValue;
            synchronized (MonicatManager.class) {
                mBatchCount += batchCount;
                LogUtils.d(Constants.SDK_NAME, "MonicatManager-->onBatchDataChanged()_mBatchCount==" + mBatchCount);
                if (mBatchCount >= batchValue) {
                    // TODO: 2018/5/2 如果是在这个类里集中上传所有表的数据时，可以先查询各表的数据，再请求
                    // TODO: 2018/5/4  另一种情况中各个相关的类去处理各自的数据，这时可以通知观察者各自上传数据
                    /**
                     * 先初始化{@link MonicatManager#mBatchCount}值，
                     * 再调用{@link MonicatManager#notifyUploadData()}方法，因为各个数据类型在被通知上报数据并且各自成功后，
                     * 会再次回调{@link MonicatManager#onBatchDataChanged(int)}方法
                     */
                    mBatchCount = 0;
                    notifyUploadData();// 再通知各个数据类各自上传数据
                }
            }
        }
    }

    //忽略的方法
//    /**
//     * 各类型的数据在初始化查询各自未上传的数据总和，并且之后再有新增时，可以通过回调这个方法告知本类，
//     * 在本类中计算已缓存的所有类型的数据总数，达到批量上报策略的设定值时就上传数据。
//     *
//     * @param count 新增一条数据成功时，传递参数值为1;初始化数据总和时，传递的参数是这个总和的值
//     */
//    protected synchronized void onDataChangeListener(int count) {
//        mBatchCount += count;
//        UploadStrategy uploadStrategy = mConfig.uploadStrategy;
//        if (uploadStrategy == BATCH) {// 这个判断可以去掉，因为在各个统计数据的类中，会对上报策略进行判断，再调用onDataChangeListener()方法
//            // 数据大于设定值时，就批量上传
//            int batchValue = MonicatManager.getInstance().getConfig().batchValue;
//            if (mBatchCount >= batchValue) {
//                // TODO: 2018/5/2 如果是在这个类里集中上传所有表的数据时，可以先查询各表的数据，再请求
//                // TODO: 2018/5/4  另一种情况中各个相关的类去处理各自的数据，这时可以通知观察者各自上传数据
//                mBatchCount = 0;// 在调用notifyUploadData()方法发出通知之前先重新初始化
//                notifyUploadData();// 再通知各个各功能区上传数据
//            }
//        }
//        LogUtils.d(Constants.SDK_NAME, "MonicatManager-->onDataChangeListener()_mBatchCount==" + mBatchCount);
//    }

    /**
     * 注册当前页面，可以自动记录目标页面的开闭状态
     *
     * @param context 页面的设备上下文
     */
    public void registerPage(@NonNull Context context) {
        if (context == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'registerPage(Context context)' method is null or empty.");
        }
        String className = context.getClass().getName();
        /**
         * 保存到记录页面打开状态的activity的全路径名称的集合中，以便在{@link PageStatisticsManager}
         * 中重写的监听生命周期的方法 {@link PageStatisticsManager#activityLifecycleChanged(ActivityLifecycle)}
         * 里可以过滤出来哪些activity是需要记录的
         */
        mPageStatisticsManager.addPage(className, "");
        // 添加到对activity生命周期监听的观察者中，如果观察者集合当中已经存在此实例的观察者，则不会再次添加
        mSwitchEventManager.addActivityLifecycleObserver(mPageStatisticsManager);
    }

    /**
     * 注册当前页面，可以自动记录目标页面的开闭状态
     *
     * @param context 页面的设备上下文
     */
    public void registerPage(@NonNull Context context, String pageName) {
        if (context == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'registerPage(Context context)' method is null or empty.");
        }
        String className = context.getClass().getName();
        /**
         * 保存到记录页面打开状态的activity的全路径名称的集合中，以便在{@link PageStatisticsManager}
         * 中重写的监听生命周期的方法 {@link PageStatisticsManager#activityLifecycleChanged(ActivityLifecycle)}
         * 里可以过滤出来哪些activity是需要记录的
         */
        mPageStatisticsManager.addPage(className, pageName);
        // 添加到对activity生命周期监听的观察者中，如果观察者集合当中已经存在此实例的观察者，则不会再次添加
        mSwitchEventManager.addActivityLifecycleObserver(mPageStatisticsManager);
    }

    /**
     * 注销当前页面，可以取消对目标页面的开闭状态的记录，建议在activity的onStop()方法里执行
     *
     * @param context 页面的设备上下文
     */
    public void unregisterPage(@NonNull Context context) {
        if (context == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'registerPage(Context context)' method is null or empty.");
        }
        String className = context.getClass().getName();
        mPageStatisticsManager.removePage(className);
        Map<String, String> pageMaps = mPageStatisticsManager.getPageMaps();
        if (pageMaps == null || (pageMaps != null && pageMaps.size() == 0)) {
            // 集合中没有数据，说明已经没有页面需要记录开闭状态，所以就可以移除这个观察者
            mSwitchEventManager.removeActivityLifecycleObserver(mPageStatisticsManager);
        }
    }

    /**
     * 标记一次页面访问的开始
     *
     * @param context 页面的设备上下文
     */
    public void trackBeginPage(@NonNull Context context) {
        if (context == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'trackbeginpage(Context context)' method is null or empty.");
        }
        String className = context.getClass().getName();
        mPageStatisticsManager.savePageInfo(className, "", Constants.PAGE_OPEN);
    }

    /**
     * 标记一次页面访问的开始
     *
     * @param context  页面的设备上下文
     * @param pageName 自定义页面名称
     */
    public void trackBeginPage(@NonNull Context context, @NonNull String pageName) {
        if (pageName == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'trackbeginpage(Context context, String pageName)' method is null or empty.");
        }
        String className = context.getClass().getName();
        mPageStatisticsManager.savePageInfo(className, pageName, Constants.PAGE_OPEN);
    }

    /**
     * 标记一次页面访问的结束
     *
     * @param context 页面的设备上下文
     */
    public void trackEndPage(@NonNull Context context) {
        if (context == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'trackEndPage(Context context)' method is null or empty.");
        }
        String className = context.getClass().getName();
        mPageStatisticsManager.savePageInfo(className, "", Constants.PAGE_CLOSE);
    }

    /**
     * 标记一次页面访问的结束
     *
     * @param context  页面的设备上下文
     * @param pageName 自定义页面名称
     */
    public void trackEndPage(@NonNull Context context, @NonNull String pageName) {
        if (context == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'trackEndPage(Context context, String pageName)' method is null or empty.");
        }
        String className = context.getClass().getName();
        mPageStatisticsManager.savePageInfo(className, pageName, Constants.PAGE_CLOSE);
    }

    /**
     * 标记一次普通事件的开始
     *
     * @param context   页面的设备上下文
     * @param eventName 事件名称
     */
    public void trackBeginEvent(@NonNull Context context, @NonNull String eventName) {
        if (context == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'trackbeginpage(Context context)' method is null or empty.");
        }
        String className = context.getClass().getName();
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
        if (context == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'trackEndPage(Context context)' method is null or empty.");
        }
        String className = context.getClass().getName();
        mEventStatisticsManager.saveEventInfo(className, eventName, 0,
                TimecalibrationManager.getInstance().getCurrentServerTime(), null);
    }

    /**
     * 标记一次自定义事件的开始
     *
     * @param context    页面的设备上下文
     * @param eventName  事件名称
     * @param properties 自定义事件Key-Value参数
     */
    public void trackCustomBeginEvent(@NonNull Context context, @NonNull String eventName, @NonNull Properties properties) {
        if (context == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'trackbeginpage(Context context, String pageName)' method is null or empty.");
        }
        String className = context.getClass().getName();
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
        if (context == null) {
            throw new NullPointerException("Monicat: The 'context' parameter passed " +
                    "in the 'trackEndPage(Context context, String pageName)' method is null or empty.");
        }
        String className = context.getClass().getName();
        mEventStatisticsManager.saveEventInfo(className, eventName, 0,
                TimecalibrationManager.getInstance().getCurrentServerTime(), properties);
    }

    /**
     * 添加进行数据上传操作的观察者
     */
    protected void addUploadDataObserver(UploadDataObserver uploadDataObserver) {
        if (uploadDataObserver == null) {
            throw new NullPointerException("Monicat: The uploadDataObserver parameter passed in is null.");
        }
        synchronized (this) {
            if (mUploadDataObservers != null && !mUploadDataObservers.contains(uploadDataObserver)) {
                mUploadDataObservers.add(uploadDataObserver);
            }
        }
    }

    /**
     * 删除进行数据上传操作的观察者
     */
    protected void removeUploadDataObserver(UploadDataObserver uploadDataObserver) {
        synchronized (this) {
            if (mUploadDataObservers != null && mUploadDataObservers.contains(uploadDataObserver)) {
                mUploadDataObservers.remove(uploadDataObserver);
            }
        }
    }

    /**
     * 删除所有进行数据上传操作的观察者
     */
    protected void removeAllUploadDataObservers() {
        synchronized (this) {
            if (mUploadDataObservers != null && mUploadDataObservers.size() > 0) {
                for (UploadDataObserver uploadDataObserver : mUploadDataObservers) {
                    mUploadDataObservers.remove(uploadDataObserver);
                }
            }
        }
    }

    /**
     * 通知所有的观察者，可以开始上传数据
     */
    public void notifyUploadData() {
        synchronized (this) {
            if (mUploadDataObservers != null && mUploadDataObservers.size() > 0) {
                for (UploadDataObserver observer : mUploadDataObservers) {
                    LogUtils.d(Constants.SDK_NAME, "MonicatManager-->notifyUploadData()_"
                            + observer.getClass().getSimpleName());
                    observer.startUploadData();
                }
            }
        }
    }

    /**
     * 如果采用统一在本类中上传所有数据时，使用这个方法
     */
    private void queryDataBase() {
        // todo 先从数据库各表中查出数据

        boolean onSessionStatistics = MonicatManager.getInstance().getConfig().onSessionStatistics;
        if (onSessionStatistics) {
            // todo  判断是否需要查出启动数据
        }
    }

    /**
     * 上传启动数据到服务器，如果采用统一在本类中上传所有数据时，使用这个方法
     */
    private void uploadDataToServer() {
        String url = Constants.SERVER_HOST + Constants.SESSION_STATISTICS;
        BaseOkHttpClient.newBuilder()
                .addParam("key1", "value1")
                .addParam("key2", "value2")
                .isJsonParam(false)
                .post()
                .url(url)
                .build()
                .enqueue(new BaseCallBack() {
                    @Override
                    public void onSuccess(Object o) {
                        LogUtils.d(Constants.SDK_NAME, "MonicatManager-->uploadDataToServer()_onSuccess()=" /*+ o.toString()*/);
                    }

                    @Override
                    public void onError(int code) {
                        LogUtils.d(Constants.SDK_NAME, "MonicatManager-->uploadDataToServer()_onError()=" + code);
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        LogUtils.d(Constants.SDK_NAME, "MonicatManager-->uploadDataToServer()_onFailure()=" + e.toString());
                    }
                });
    }

}
