package com.windfallsheng.monicat.base;

import android.content.Context;
import android.support.annotation.NonNull;

import com.windfallsheng.monicat.command.UploadStrategy;
import com.windfallsheng.monicat.utils.LogUtils;
import com.windfallsheng.monicat.utils.TimeUtils;

import static com.windfallsheng.monicat.command.UploadStrategy.INSTANT;

/**
 * CreateDate: 2018/3/27
 * <p>
 * Author: lzsheng
 * <p>
 * Description: 配置一些必须和或是按需求而定的参数
 * <p>
 * Version:
 */
public class Configuration {

    public Context context;
    public boolean onSessionStatistics;     // 会话统计
    public Boolean isDebug;                 // 是否打开debug模式，输入打印日志
    public int intervalTime;                // 前后台间隔大于这个时间，算一次启动
    public UploadStrategy uploadStrategy;
    public long triggerTime;                // 定时时间
    public long periodTime;                 // 间隔时间
    public int batchValue;                  // 批量上报的值

    private Configuration(Configuration.Builder builder) {
        this.context = builder.context;
        this.onSessionStatistics = builder.onSessionStatistics;
        this.intervalTime = builder.intervalTime;
        this.uploadStrategy = builder.uploadStrategy;
        this.triggerTime = builder.triggerTime;
        this.batchValue = builder.batchValue;
        this.periodTime = builder.periodTime;
        this.isDebug = builder.isDebug;
    }

    public static final class Builder {
        private Context context;
        private boolean onSessionStatistics = true;
        private Boolean isDebug = null;
        private int intervalTime = 30 * 1000;
        private UploadStrategy uploadStrategy = INSTANT;
        private long triggerTime = TimeUtils.getTimeOfDayMillis(8, 30, 0, 0);
        private int batchValue = 50;                // 批量上报的值
        private long periodTime = 30 * 60 * 1000;   // 间隔时间

        public Builder(@NonNull Context context) {
            if (context == null) {
                throw new NullPointerException("Monicat: The injected context is null.");
            } else {
                this.context = context.getApplicationContext();
            }
        }

        /**
         * 设置是否开启统计启动次数功能，默认为true，即开启状态
         *
         * @param onSessionStatistics
         * @return
         */
        public Builder setOnStartNum(boolean onSessionStatistics) {
            this.onSessionStatistics = onSessionStatistics;
            return this;
        }

        /**
         * 设置前后台切换的间隔时间，毫秒级，默认为30秒
         *
         * @param intervalTime
         * @return
         */
        public Builder setIntervalTime(int intervalTime) {
            this.intervalTime = intervalTime;
            return this;
        }

        /**
         * 设置上报策略，默认为 {@link UploadStrategy#INSTANT}，即时上报
         *
         * @param uploadStrategy
         * @return
         */
        public Builder setUploadStrategy(UploadStrategy uploadStrategy) {
            this.uploadStrategy = uploadStrategy;
            return this;
        }

        /**
         * 设置定时上报的时间，24小时制， 默认为上午8：30
         * 小时格式：大于等于0，小于24，分钟格式：大于等于0，小于60；
         * 设置格式错误，会按默认值计算
         *
         * @param hour
         * @param minute
         * @return
         */
        public Builder setTriggerTime(int hour, int minute) {
            if (hour >= 0 && hour < 24 && minute >= 0 && minute < 60) {
                this.triggerTime = TimeUtils.getTimeOfDayMillis(hour, minute, 0, 0);
            }
            return this;
        }

        /**
         * 设置间隔时间，毫秒级，默认为30分钟，最小值为5分钟
         *
         * @param periodTime
         * @return
         */
        public Builder setPeriodTime(long periodTime) {
            if (periodTime >= 5 * 60 * 1000) {
                this.periodTime = periodTime;
            }
            return this;
        }

        /**
         * 设置批量上报的值，默认为50条。
         *
         * @param batchValue
         * @return
         */
        public Builder setBatchValue(int batchValue) {
            this.batchValue = batchValue;
            return this;
        }

        /**
         * 设置开启debug模式，输出打印日志，默认为null，
         * 没有设置时，会根据外层项目app的模式（debug or release）模式来选择，
         * 由{@link LogUtils} 的 syncIsDebug() 方法来实现
         *
         * @param isDebug Boolean类型
         * @return
         */
        public Builder setDebug(Boolean isDebug) {
            this.isDebug = isDebug;
            return this;
        }

        public Configuration build() {
            return new Configuration(this);
        }
    }
}
