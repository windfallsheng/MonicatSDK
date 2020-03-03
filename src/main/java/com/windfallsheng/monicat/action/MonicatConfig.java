package com.windfallsheng.monicat.action;

import android.content.Context;

import com.windfallsheng.monicat.base.UploadStrategy;
import com.windfallsheng.monicat.util.LogUtils;
import com.windfallsheng.monicat.util.TimeUtils;

import static com.windfallsheng.monicat.base.UploadStrategy.INSTANT;

/**
 * CreateDate: 2018/3/27
 * <p>
 * Author: lzsheng
 * <p>
 * Description: 配置一些必须和或是按需求而定的参数
 * <p>
 * Version:
 */
public class MonicatConfig {

    final Context context;
    final boolean enableMonicat;
    final boolean enableSessionStatistics;     // 会话统计
    final boolean enablePageStatistics;
    final boolean enableEventStatistics;
    final boolean debugEnable;             // 是否打开debug模式，输入打印日志
    final int sessionTimoutMillis;                // 前后台间隔大于这个时间，算一次启动
    final UploadStrategy uploadStrategy;
    final long triggerTime;                // 定时时间
    final long periodTime;                 // 间隔时间
    final int batchValue;                  // 批量上报的值

    private MonicatConfig(MonicatConfig.Builder builder) {
        this.context = builder.context;
        this.enableMonicat = builder.enableMonicat;
        this.enableSessionStatistics = builder.enableSessionStatistics;
        this.enablePageStatistics = builder.enablePageStatistics;
        this.enableEventStatistics = builder.enableEventStatistics;
        this.sessionTimoutMillis = builder.sessionTimoutMillis;
        this.uploadStrategy = builder.uploadStrategy;
        this.triggerTime = builder.triggerTime;
        this.batchValue = builder.batchValue;
        this.periodTime = builder.periodTime;
        this.debugEnable = builder.debugEnable;
    }

    public static final class Builder {
        private Context context;
        private boolean enableMonicat = true;
        private boolean debugEnable = false;
        private boolean enableSessionStatistics = true;
        private boolean enablePageStatistics = true;
        private boolean enableEventStatistics = true;
        private int sessionTimoutMillis = 30 * 1000;
        private UploadStrategy uploadStrategy = INSTANT;
        private long triggerTime = TimeUtils.getTimeOfDayMillis(8, 30, 0, 0);
        private int batchValue = 50;                // 批量上报的值
        private long periodTime = 30 * 60 * 1000;   // 间隔时间

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        /**
         * 设置是否开启统计功能，默认为true，即开启状态
         *
         * @param enableMonicat
         * @return
         */
        public Builder enableMonicat(boolean enableMonicat) {
            this.enableMonicat = enableMonicat;
            return this;
        }

        /**
         * 设置是否开启统计启动次数功能，默认为true，即开启状态
         *
         * @param onSessionStatistics
         * @return
         */
        public Builder enableSessionStatistics(boolean onSessionStatistics) {
            this.enableSessionStatistics = onSessionStatistics;
            return this;
        }

        /**
         * 设置前后台切换的间隔时间，毫秒级，默认为30秒
         *
         * @param sessionTimoutMillis
         * @return
         */
        public Builder setSessionTimoutMillis(int sessionTimoutMillis) {
            this.sessionTimoutMillis = sessionTimoutMillis;
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
         * 由{@link LogUtils} 的 getBuildConfig() 方法来实现
         *
         * @param debugEnable
         * @return
         */
        public Builder debugEnable(Boolean debugEnable) {
            this.debugEnable = debugEnable;
            return this;
        }

        public MonicatConfig build() {
            return new MonicatConfig(this);
        }
    }
}
