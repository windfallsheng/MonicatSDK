package com.windfallsheng.monicat.action;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.windfallsheng.monicat.common.MonicatConstants;
import com.windfallsheng.monicat.listener.ActivityLifecycleObserver;
import com.windfallsheng.monicat.listener.SwitchEventObserver;
import com.windfallsheng.monicat.model.ActivityLifecycle;
import com.windfallsheng.monicat.model.SwitchEvent;
import com.windfallsheng.monicat.util.LogUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * CreateDate: 2018/4/16
 * <p>
 * Author: lzsheng
 * <p>
 * Description: 监听应用前后台的切换，同时作为被观察者，将前后台切换的相关数据通知给各观察者，以便各观察者判断相关数据，进行业务处理
 * <p>
 * 在 {@link MonicatManager#monitor()}的方法中注册
 * (调用{@link Application#registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks)}方法)这个事件，
 * 之后就可以实现监听应用前后台的切换。
 * <p>
 * Version:
 */
class SwitchEventManager implements Application.ActivityLifecycleCallbacks {

    //    private static SwitchEventManager instance = null;
    private int mActivityCount;
    private long foregroundTime;
    private long backgroundTime;
    private boolean isForeground;
    private SessionStatisticsManager mSessionStatisticsManager;
    /**
     * {@link PageStatisticsManager#mCachePages}
     * 即：存放注册了记录页面打开状态的activity的全路径名称
     */
    private Map<String, String> mPageMaps;
    private PageStatisticsManager mPageStatisticsManager;

    private List<SwitchEventObserver> mSwitchEventObservers;
    private List<ActivityLifecycleObserver> mActivityLifecycleObservers;

    public SwitchEventManager() {
        mSwitchEventObservers = new ArrayList<>();
        mActivityLifecycleObservers = new ArrayList<>();
    }

//    public static SwitchEventManager getInstance() {
//        if (instance == null) {
//            instance = new SwitchEventManager();
//            mSwitchEventObservers = new ArrayList<>();
//        }
//        return instance;
//    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        String activityName = activity.getClass().getName();
        LogUtils.d(MonicatConstants.SDK_NAME, "SwitchEventManager-->onActivityCreated()_activityName==" + activityName);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        String activityName = activity.getClass().getName();
        LogUtils.d(MonicatConstants.SDK_NAME, "SwitchEventManager-->onActivityStarted()_activityName==" + activityName);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        mActivityCount++;
        String activityName = activity.getClass().getName();
        LogUtils.d(MonicatConstants.SDK_NAME, "SwitchEventManager-->onActivityResumed()_activityName==" + activityName);
        if (mActivityCount > 0) {// 此时表明应用在前台
            foregroundTime = System.currentTimeMillis();
            isForeground = true;
//            foregroundtTime = TimecalibrationManager.getInstance().getCurrentServerTime();
            SwitchEvent switchEvent = new SwitchEvent(activityName, mActivityCount, foregroundTime, backgroundTime, isForeground);
            notifySwitchEventChanged(switchEvent);
        }
        if (mPageStatisticsManager != null) {
            mPageMaps = mPageStatisticsManager.getCachePages();
        }
        /**
         * 如果当前监听到的activity在{@link PageStatisticsManager} 的{@link mPageMaps}集合中，则发出通知，
         * 这个判断目录只针对{@link PageStatisticsManager}里判断页面开闭状态的需求，如有功能扩展，可去掉此条件限制
         */
        if (mPageMaps != null && mPageMaps.containsKey(activityName)) {
            ActivityLifecycle activityLifecycle = new ActivityLifecycle(activityName, MonicatConstants.ON_ACTIVITY_RESUMED, isForeground);
            notifyActivityLifecycleChanged(activityLifecycle);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        mActivityCount--;
        String activityName = activity.getClass().getName();
        LogUtils.d(MonicatConstants.SDK_NAME, "SwitchEventManager-->onActivityPaused()_activityName==" + activityName);
        if (mActivityCount == 0) {// 此时表明应用在后台
            backgroundTime = System.currentTimeMillis();
            isForeground = false;
//            backgroundTime = TimecalibrationManager.getInstance().getCurrentServerTime();
            SwitchEvent switchEvent = new SwitchEvent(activityName, mActivityCount, foregroundTime, backgroundTime, isForeground);
            notifySwitchEventChanged(switchEvent);
        }
        /**
         * 如果当前监听到的activity在{@link PageStatisticsManager} 的{@link mPageMaps}集合中，则发出通知，
         * 这个判断目录只针对{@link PageStatisticsManager}里判断页面开闭状态的需求，如有功能扩展，可去掉此条件限制
         */
        if (mPageMaps != null && mPageMaps.containsKey(activityName)) {
            ActivityLifecycle activityLifecycle = new ActivityLifecycle(activityName, MonicatConstants.ON_ACTIVITY_PAUSED, isForeground);
            notifyActivityLifecycleChanged(activityLifecycle);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        String activityName = activity.getClass().getName();
        LogUtils.d(MonicatConstants.SDK_NAME, "SwitchEventManager-->onActivityStopped()_activityName==" + activityName);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        String activityName = activity.getClass().getName();
        LogUtils.d(MonicatConstants.SDK_NAME, "SwitchEventManager-->onActivitySaveInstanceState()_activityName==" + activityName);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        String activityName = activity.getClass().getName();
        LogUtils.d(MonicatConstants.SDK_NAME, "SwitchEventManager-->onActivityDestroyed()_activityName==" + activityName);
    }

    /**
     * 添加对应用前后台切换的观察者
     * <p>
     * 如果已经添加过某个观察者的实例，则不会重复添加
     *
     * @param switchEventObserver
     */
    protected void addSwitchEventObserver(SwitchEventObserver switchEventObserver) {
        if (switchEventObserver == null) {
            throw new NullPointerException("Monicat: The switchEventObserver parameter passed in is null.");
        }
        synchronized (this) {
            if (mSwitchEventObservers != null && !mSwitchEventObservers.contains(switchEventObserver)) {
                mSwitchEventObservers.add(switchEventObserver);
            }
        }
    }

    /**
     * 删除对应用前后台切换的观察者
     * <p>
     * 如果集合中存在某个观察者的实例，则移除这个观察者
     *
     * @param observer
     */
    protected void removeSwitchEventObserver(SwitchEventObserver observer) {
        synchronized (this) {
            if (mSwitchEventObservers != null && mSwitchEventObservers.contains(observer)) {
                mSwitchEventObservers.remove(observer);
            }
        }
    }

    /**
     * 删除所有对应用前后台切换的观察者
     */
    protected void removeAllSwitchEventObservers() {
        synchronized (this) {
            if (mSwitchEventObservers != null && mSwitchEventObservers.size() > 0) {
                Iterator<SwitchEventObserver> it = mSwitchEventObservers.iterator();
                while (it.hasNext()) {
                    it.remove();
                }
            }
        }
    }

    /**
     * 通知所有观察者，告知应用前后台切换的状态
     */
    protected void notifySwitchEventChanged(SwitchEvent switchEvent) {
        synchronized (this) {
            if (mSwitchEventObservers != null && mSwitchEventObservers.size() > 0) {
                for (SwitchEventObserver switchEventObserver : mSwitchEventObservers) {
                    switchEventObserver.switchEventChanged(switchEvent);
                }
            }
        }
    }

    /**
     * 添加对activity生命周期监听的观察者
     * <p>
     * 如果已经添加过某个观察者的实例，则不会重复添加
     *
     * @param activityLifecycleObserver
     */
    protected void addActivityLifecycleObserver(ActivityLifecycleObserver activityLifecycleObserver) {
        if (activityLifecycleObserver == null) {
            throw new NullPointerException("Monicat: The activityLifecycleObserver parameter passed in is null.");
        }
        synchronized (this) {
            if (mActivityLifecycleObservers != null && !mActivityLifecycleObservers.contains(activityLifecycleObserver)) {
                mActivityLifecycleObservers.add(activityLifecycleObserver);
            }
            if (activityLifecycleObserver instanceof PageStatisticsManager) {
                mPageStatisticsManager = (PageStatisticsManager) activityLifecycleObserver;
            }
        }
    }

    /**
     * 删除对activity生命周期监听的观察者
     * <p>
     * 如果集合中存在某个观察者的实例，则移除这个观察者
     *
     * @param observer
     */
    protected void removeActivityLifecycleObserver(ActivityLifecycleObserver observer) {
        synchronized (this) {
            if (mActivityLifecycleObservers != null && mActivityLifecycleObservers.contains(observer)) {
                mActivityLifecycleObservers.remove(observer);
            }
        }
    }

    /**
     * 删除所有对activity生命周期监听的观察者
     */
    protected void removeAllActivityLifecycleObservers() {
        synchronized (this) {
            if (mActivityLifecycleObservers != null && mActivityLifecycleObservers.size() > 0) {
                Iterator<ActivityLifecycleObserver> it = mActivityLifecycleObservers.iterator();
                while (it.hasNext()) {
                    it.remove();
                }
            }
        }
    }

    /**
     * 通知监听的观察者，告知activity生命周期状态
     *
     * @param activityLifecycle
     */
    protected void notifyActivityLifecycleChanged(ActivityLifecycle activityLifecycle) {
        synchronized (this) {
            if (mActivityLifecycleObservers != null && mActivityLifecycleObservers.size() > 0) {
                for (ActivityLifecycleObserver lifecycle : mActivityLifecycleObservers) {
                    lifecycle.activityLifecycleChanged(activityLifecycle);
                }
            }
        }
    }
}
