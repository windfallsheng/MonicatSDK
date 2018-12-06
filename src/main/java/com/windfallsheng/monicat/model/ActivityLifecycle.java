package com.windfallsheng.monicat.model;

/**
 * Created by lzsheng on 2018/4/16.
 */

public class ActivityLifecycle {

    private String activityName;        // activity的全路径名称
    private int lifeStatus;             // 生命周期执行到的方法标识
    private boolean isForeground;

    public ActivityLifecycle(String activityName, int lifeStatus, boolean isForeground) {
        this.activityName = activityName;
        this.lifeStatus = lifeStatus;
        this.isForeground = isForeground;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public int getLifeStatus() {
        return lifeStatus;
    }

    public void setLifeStatus(int lifeStatus) {
        this.lifeStatus = lifeStatus;
    }

    public boolean isForeground() {
        return isForeground;
    }

    public void setForeground(boolean foreground) {
        isForeground = foreground;
    }

    @Override
    public String toString() {
        return "ActivityLifecycle{" +
                "activityName='" + activityName + '\'' +
                ", lifeStatus=" + lifeStatus +
                ", isForeground=" + isForeground +
                '}';
    }
}
