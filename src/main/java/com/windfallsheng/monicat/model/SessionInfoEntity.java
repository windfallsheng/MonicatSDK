package com.windfallsheng.monicat.model;

import com.windfallsheng.monicat.db.sqlite.StatisticsSQLiteHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Created by lzsheng on 2018/4/16.
 */
public class SessionInfoEntity {

    /**
     * 本地数据库存储的ID；
     */
    private int sessionId;
    /**
     * 设备信息在本地数据库存储的ID；
     */
    private int deviceInfoId;
    /**
     * 触发时间；
     */
    private long triggeringTime;
    /**
     * 启动方式，0为应用启动期间，前后台切换超时算作的启动；1为安卓系统启动应用；2为退出应用；3为应用在后台；
     */
    private int sessionType;
    /**
     * 数据是否已上传到后台；
     */
    private transient int uploadeStatus;

    public SessionInfoEntity() {
        super();
    }

    public SessionInfoEntity(int sessionId, int deviceInfoId, long launchTime,
                             @SessionType int sessionType, @UploadeStatus int uploadeStatus) {
        this.sessionId = sessionId;
        this.deviceInfoId = deviceInfoId;
        this.triggeringTime = launchTime;
        this.sessionType = sessionType;
        this.uploadeStatus = uploadeStatus;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getDeviceInfoId() {
        return deviceInfoId;
    }

    public void setDeviceInfoId(int deviceInfoId) {
        this.deviceInfoId = deviceInfoId;
    }

    public long getTriggeringTime() {
        return triggeringTime;
    }

    public void setTriggeringTime(long triggeringTime) {
        this.triggeringTime = triggeringTime;
    }

    public int getSessionType() {
        return sessionType;
    }

    public void setSessionType(@SessionType int sessionType) {
        this.sessionType = sessionType;
    }

    public int getUploadeStatus() {
        return uploadeStatus;
    }

    public void setUploadeStatus(@UploadeStatus int uploadeStatus) {
        this.uploadeStatus = uploadeStatus;
    }

    /**
     * 数据库字段和对象属性的映射关系的Map集合，通过数据库字段名这个KEY，可以获取到对象对应的属性值
     *
     * @return
     */
    public Map<String, Object> getMapping() {
        Map<String, Object> columnAndFeilds = new HashMap<>();
        columnAndFeilds.put(StatisticsSQLiteHelper.COLUMN_SESSION_ID, getSessionId());
        columnAndFeilds.put(StatisticsSQLiteHelper.COLUMN_DEVICE_INFO_ID, getDeviceInfoId());
        columnAndFeilds.put(StatisticsSQLiteHelper.COLUMN_TRIGGERING_TIME, getTriggeringTime());
        columnAndFeilds.put(StatisticsSQLiteHelper.COLUMN_SESSION_TYPE, getSessionType());
        columnAndFeilds.put(StatisticsSQLiteHelper.COLUMN_UPLOADE_STATUS, getUploadeStatus());
        return columnAndFeilds;
    }

    @Override
    public String toString() {
        return "AppStartupEntity{" +
                "sessionId=" + sessionId +
                ", deviceInfoId=" + deviceInfoId +
                ", triggeringTime=" + triggeringTime +
                ", sessionType=" + sessionType +
                ", uploadeStatus=" + uploadeStatus +
                '}';
    }
}
