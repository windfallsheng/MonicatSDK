package com.windfallsheng.monicat.model;

import com.windfallsheng.monicat.db.sqlitehelper.StatisticsSQLiteHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lzsheng on 2018/4/16.
 */

public class AppStartupEntity {

    private int startupId;      // 本地数据库存储的ID
    private int deviceInfoId;   // 设备信息在本地数据库存储的ID
    private long startupTime;   // 应用进入前台的时间
    private int startupType;    // 启动方式，0为应用启动期间的前后台切换，1为打开应用，2为退出应用
    private int hasUploaded;    // 数据是否已上传到后台

    public AppStartupEntity() {
        super();
    }

    public int getStartupId() {
        return startupId;
    }

    public void setStartupId(int startupId) {
        this.startupId = startupId;
    }

    public int getDeviceInfoId() {
        return deviceInfoId;
    }

    public void setDeviceInfoId(int deviceInfoId) {
        this.deviceInfoId = deviceInfoId;
    }

    public long getStartupTime() {
        return startupTime;
    }

    public void setStartupTime(long startupTime) {
        this.startupTime = startupTime;
    }

    public int getStartupType() {
        return startupType;
    }

    public void setStartupType(int startupType) {
        this.startupType = startupType;
    }

    public int getHasUploaded() {
        return hasUploaded;
    }

    public void setHasUploaded(int hasUploaded) {
        this.hasUploaded = hasUploaded;
    }

    /**
     * 数据库字段和对象属性的映射关系的Map集合，通过数据库字段名这个KEY，可以获取到对象对应的属性值
     *
     * @return
     */
    public Map<String, Object> getMapping() {
        Map<String, Object> columnAndFeilds = new HashMap<>();
        columnAndFeilds.put(StatisticsSQLiteHelper.COLUMN_STARTUP_ID, getStartupId());
        columnAndFeilds.put(StatisticsSQLiteHelper.COLUMN_DEVICE_INFO_ID, getDeviceInfoId());
        columnAndFeilds.put(StatisticsSQLiteHelper.COLUMN_STARTUP_TIME, getStartupTime());
        columnAndFeilds.put(StatisticsSQLiteHelper.COLUMN_STARTUP_TYPE, getStartupType());
        columnAndFeilds.put(StatisticsSQLiteHelper.COLUMN_HAS_UPLOADED, getHasUploaded());
        return columnAndFeilds;
    }

    @Override
    public String toString() {
        return "AppStartupEntity{" +
                "startupId=" + startupId +
                ", deviceInfoId=" + deviceInfoId +
                ", startupTime=" + startupTime +
                ", startupType=" + startupType +
                ", hasUploaded=" + hasUploaded +
                '}';
    }
}
