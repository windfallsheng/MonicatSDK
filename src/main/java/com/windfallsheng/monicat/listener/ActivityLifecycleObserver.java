package com.windfallsheng.monicat.listener;

import com.windfallsheng.monicat.model.ActivityLifecycle;


/**
 * CreateDate: 2018/4/9
 * <p>
 * Author: lzsheng
 * <p>
 * Description: ActivityLifecycle监听
 * <p>
 * Version:
 */
public interface ActivityLifecycleObserver {

    /**
     * Activity生命周期变化
     *
     * @param activityLifecycle
     */
    void activityLifecycleChanged(ActivityLifecycle activityLifecycle);

}
