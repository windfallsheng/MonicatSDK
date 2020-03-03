package com.windfallsheng.monicat.base;

/**
 * CreateDate: 2018/4/20
 * <p>
 * @author : lzsheng
 * <p>
 * Description: 上报数据的策略
 * INSTANT      实时发送，app每产生一条消息都会发送到服务器。
 * TIMED_TASK   定时上报
 * BATCH        批量发送，默认当消息数量达到30条时发送一次。
 * PERIOD       间隔一段时间发送，每隔一段时间一次性发送到服务器。
 * ONLY_WIFI    只在wifi状态下发送，非wifi情况缓存到本地。
 * APP_LAUNCH   只在启动时发送，本次产生的所有数据在下次启动时发送。
 * DEVELOPER    开发者模式，只在app调用某个方法时发送，否则缓存消息到本地。
 * <p>
 * Version:
 */
public enum UploadStrategy {
    /**
     * INSTANT      实时发送，app每产生一条消息都会发送到服务器。
     * TIMED_TASK   定时上报
     * BATCH        批量发送，默认当消息数量达到30条时发送一次。
     * PERIOD       间隔一段时间发送，每隔一段时间一次性发送到服务器。
     * ONLY_WIFI    只在wifi状态下发送，非wifi情况缓存到本地。
     * APP_LAUNCH   只在启动时发送，本次产生的所有数据在下次启动时发送。
     * DEVELOPER    开发者模式，只在app调用某个方法时发送，否则缓存消息到本地。
     */
    INSTANT, APP_LAUNCH, TIMED_TASK, BATCH, PERIOD, ONLY_WIFI, ONCE_A_DAY, DEVELOPER
}
