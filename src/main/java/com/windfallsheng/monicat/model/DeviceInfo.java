package com.windfallsheng.monicat.model;

/**
 * Created by lzsheng on 2018/4/23.
 */

public class DeviceInfo {

    private int device_info_id;     // 本地数据库存储的ID
    private String deviceUniqueId;  // 设备的唯一标识
    private int deviceIdType;       // 设备标识的类型，1 为DeviceId,2 为AndroidId，3 为Serial，4 为IMEI码，5 为Mac地址，6 为UUID

    public int getDevice_info_id() {
        return device_info_id;
    }

    public void setDevice_info_id(int device_info_id) {
        this.device_info_id = device_info_id;
    }

    public String getDeviceUniqueId() {
        return deviceUniqueId;
    }

    public void setDeviceUniqueId(String deviceUniqueId) {
        this.deviceUniqueId = deviceUniqueId;
    }

    public int getDeviceIdType() {
        return deviceIdType;
    }

    public void setDeviceIdType(int deviceIdType) {
        this.deviceIdType = deviceIdType;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "device_info_id=" + device_info_id +
                ", deviceUniqueId='" + deviceUniqueId + '\'' +
                ", deviceIdType=" + deviceIdType +
                '}';
    }
}
