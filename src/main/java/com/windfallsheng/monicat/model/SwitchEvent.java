package com.windfallsheng.monicat.model;

/**
 * Created by lzsheng on 2018/4/16.
 */

public class SwitchEvent {

    private String activityName;        // activity的全路径名称
    private int activityCount;
    private long foregroundtTime;
    private long backgroundTime;
    private boolean isForeground;

    public SwitchEvent(String activityName, int activityCount, long foregroundtTime, long backgroundTime, boolean isForeground) {
        this.activityName = activityName;
        this.activityCount = activityCount;
        this.foregroundtTime = foregroundtTime;
        this.backgroundTime = backgroundTime;
        this.isForeground = isForeground;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public int getActivityCount() {
        return activityCount;
    }

    public void setActivityCount(int activityCount) {
        this.activityCount = activityCount;
    }

    public long getForegroundtTime() {
        return foregroundtTime;
    }

    public void setForegroundtTime(long foregroundtTime) {
        this.foregroundtTime = foregroundtTime;
    }

    public long getBackgroundTime() {
        return backgroundTime;
    }

    public void setBackgroundTime(long backgroundTime) {
        this.backgroundTime = backgroundTime;
    }

    public boolean isForeground() {
        return isForeground;
    }

    public void setForeground(boolean foreground) {
        isForeground = foreground;
    }

    @Override
    public String toString() {
        return "SwitchEvent{" +
                "activityName='" + activityName + '\'' +
                ", activityCount=" + activityCount +
                ", foregroundtTime=" + foregroundtTime +
                ", backgroundTime=" + backgroundTime +
                ", isForeground=" + isForeground +
                '}';
    }
}
