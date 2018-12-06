package com.windfallsheng.monicat.model;

/**
 * Created by lzsheng on 2018/4/16.
 */

public class EventInfoEntity {

    private int eventInfoId;      // 本地数据库存储的ID
    private String className;    // 所在页面的ID，也即页面所在Activity的全路径
    private String eventName;     // 事件名
    private long triggeringTime; // 事件的触发发生时间
    private long endTime;        // 发事件的结束时间
    private Properties properties;  // 自定义事件Key-Value参数

    public EventInfoEntity(String className, String eventName, long triggeringTime, long endTime) {
        this.className = className;
        this.eventName = eventName;
        this.triggeringTime = triggeringTime;
        this.endTime = endTime;
    }

    public EventInfoEntity(String className, String eventName, long triggeringTime, long endTime, Properties properties) {
        this.className = className;
        this.eventName = eventName;
        this.triggeringTime = triggeringTime;
        this.endTime = endTime;
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "EventInfoEntity{" +
                "eventInfoId=" + eventInfoId +
                ", className='" + className + '\'' +
                ", eventName='" + eventName + '\'' +
                ", triggeringTime=" + triggeringTime +
                ", endTime=" + endTime +
                ", properties=" + properties +
                '}';
    }
}
