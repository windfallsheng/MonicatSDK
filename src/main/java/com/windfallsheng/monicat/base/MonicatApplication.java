package com.windfallsheng.monicat.base;

/**
 * CreateDate: 2018/4/9
 * <p>
 * Author: lzsheng
 * <p>
 * Description: 通过注册ActivityLifecycleCallbacks()，来监听应用前后台切换事件，并且通知各观察者更新状态
 * // TODO: 2018/5/8  这里 registerActivityLifecycleCallbacks()方法注册的匿名内部类可以
 * 使用 {@link SwitchEventManager} 来完成，并且把本类中的观察者功能去掉
 * <p>
 * Version:
 * <p>
 * 使用 {@link MonicatApplication} 的监听和 {@link SwitchEventManager}的监听
 * 是有不同的用法，所以这里去判断返回
 * <p>
 * Returns the application instance
 * <p>
 * Returns the application instance
 * <p>
 * 使用 {@link MonicatApplication} 的监听和 {@link SwitchEventManager}的监听
 * 是有不同的用法，所以这里去判断返回
 */
/*public class MonicatApplication extends Application {

    private static MonicatApplication singleton;
    private int mActivityCount;
    private long foregroundtTime;
    private long backgroundTime;
    private List<SwitchEventObserver> mSwitchEventObservers;

    @Override
    public void onCreate() {
        super.onCreate();
//        Log.d(Constants.SDK_NAME, "MonicatApplication-->onCreate()");
        singleton = this;
        mSwitchEventObservers = new ArrayList<SwitchEventObserver>();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                mActivityCount++;
//                Log.d(Constants.SDK_NAME, "onActivityStarted()_mActivityCount==" + mActivityCount);
//                Log.d(Constants.SDK_NAME, "onActivityStarted()_activity==" + activity.getClass().getSimpleName());
                if (mActivityCount > 0) {// 此时表明应用在前台
//                    foregroundtTime = System.currentTimeMillis();
                    foregroundtTime = TimecalibrationManager.getInstance().getCurrentServerTime();
                    SwitchEvent switchEvent = new SwitchEvent(mActivityCount, foregroundtTime, backgroundTime, true);
                    notifySwitchEventChanged(switchEvent);
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                mActivityCount--;
//                Log.d(Constants.SDK_NAME, "onActivityStopped()_mActivityCount==" + mActivityCount);
//                Log.d(Constants.SDK_NAME, "onActivityStarted()_activity==" + activity.getClass().getSimpleName());
                if (mActivityCount == 0) {// 此时表明应用在后台
//                    backgroundTime = System.currentTimeMillis();
                    backgroundTime = TimecalibrationManager.getInstance().getCurrentServerTime();
                    SwitchEvent switchEvent = new SwitchEvent(mActivityCount, foregroundtTime, backgroundTime, false);
                    notifySwitchEventChanged(switchEvent);
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

    }

    *//**
 * Returns the application instance
 *//*
    public static MonicatApplication getInstance() {
        return singleton;
    }

    *//**
 * Returns the application instance
 *//*
    public static Context getContext() {
        Context context = MonicatManager.getInstance().getMonicatConfig().context;
        *//**
 *  使用 {@link MonicatApplication} 的监听和 {@link SwitchEventManager}的监听
 *  是有不同的用法，所以这里去判断返回
 *//*
        if (context == null) {
            context = MonicatApplication.getInstance();
        }
        return context;
    }

    // 添加观察者
    public void addSwitchEventObserver(SwitchEventObserver switchEventObserver) {
        if (switchEventObserver == null) {
            throw new NullPointerException("switchEventObserver == null");
        }
        synchronized (this) {
            if (mSwitchEventObservers != null && !mSwitchEventObservers.contains(switchEventObserver)) {
                mSwitchEventObservers.add(switchEventObserver);
            }
        }
    }

    // 删除观察者
    public void removeSwitchEventObserver(SwitchEventObserver observer) {
        synchronized (this) {
            if (mSwitchEventObservers != null && mSwitchEventObservers.contains(observer)) {
                mSwitchEventObservers.remove(observer);
            }
        }
    }

    // 删除观察者
    public void removeAllSwitchEventObservers() {
        synchronized (this) {
            if (mSwitchEventObservers != null && mSwitchEventObservers.size() > 0) {
                for (SwitchEventObserver infoObserver : mSwitchEventObservers) {
                    mSwitchEventObservers.remove(infoObserver);
                }
            }
        }
    }

    // 通知观察者
    public void notifySwitchEventChanged(SwitchEvent switchEvent) {
        synchronized (this) {
            if (mSwitchEventObservers != null && mSwitchEventObservers.size() > 0) {
                for (SwitchEventObserver switchEventObserver : mSwitchEventObservers) {
                    switchEventObserver.handleAppStatusChanged(switchEvent);
                }
            }
        }
    }

    public int getActivityCount() {
        return mActivityCount;
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(Constants.SDK_NAME, "MonicatApplication-->onTerminate()");
    }
}*/
