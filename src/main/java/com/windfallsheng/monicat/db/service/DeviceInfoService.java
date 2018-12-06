package com.windfallsheng.monicat.db.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.windfallsheng.monicat.db.dao.AppStartupDaoImpl;
import com.windfallsheng.monicat.db.dao.DeviceInfoDaoImpl;
import com.windfallsheng.monicat.db.sqlitehelper.StatisticsSQLiteHelper;
import com.windfallsheng.monicat.model.DeviceInfo;
import com.windfallsheng.monicat.model.ParamMap;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CreateDate: 2018/04/16.
 * <p>
 * Author: lzsheng
 * <p>
 * Description: 应用启动数据本地数据库的业务操作类，
 * 调用{@link AppStartupDaoImpl} 中对表的CRUD操作，完成必要的业务逻辑
 * <p>
 * Version:
 */
public class DeviceInfoService implements IBaseService<DeviceInfo> {

    private volatile static IBaseService instance = null;
    private static DeviceInfoDaoImpl sDeviceInfoDaoImpl;
    //AtomicInteger是一个线程安全的类，可以通过它来计数，无论什么线程AtomicInteger值+1后都会改变
    private AtomicInteger mOpenCounter = new AtomicInteger();
    private SQLiteDatabase db;

    private DeviceInfoService() {
    }

    public static IBaseService getInstance(Context context) {
        sDeviceInfoDaoImpl = DeviceInfoDaoImpl.getInstance(context);
        if (instance == null) {
            synchronized (AppStartupDaoImpl.class) {
                if (instance == null) {
                    instance = new DeviceInfoService();
                }
            }
        }
        return instance;
    }

    /**
     * 保存到记录表中
     *
     * @param deviceInfo
     * @return 返回插入的数据的主键ID
     */
    @Override
    public int save(DeviceInfo deviceInfo) {
        if (deviceInfo != null) {
            ParamMap paramMap = new ParamMap();// 添加数据库查询条件
            String deviceUniqueId = deviceInfo.getDeviceUniqueId();
            int deviceIdType = deviceInfo.getDeviceIdType();
            if (deviceUniqueId != null && !"".equals(deviceUniqueId)) {
                paramMap.setAndMap(StatisticsSQLiteHelper.COLUMN_DEVICE_UNIQUE_ID, deviceUniqueId);
            }
            if (deviceIdType > 0) {
                paramMap.setAndMap(StatisticsSQLiteHelper.COLUMN_DEVICE_ID_TYPE, deviceIdType);
            }
            // 查询数据库中是否已经存在此条数据
            List<DeviceInfo> deviceInfos = sDeviceInfoDaoImpl.queryAllByMap(paramMap);
            if (deviceInfos == null) {
                return sDeviceInfoDaoImpl.save(deviceInfo);
            }
        }
        return 0;
    }

    /**
     * 查找所有,可根据条件进行查询
     *
     * @param conditions 查询的条件，没有条件查询的需求时可传null，
     *                   在DAO的实现层中会有条件语句拼接的实现
     */
    @Override
    public List<DeviceInfo> queryAllByMap(ParamMap conditions) {
        return sDeviceInfoDaoImpl.queryAllByMap(conditions);
    }

    @Override
    public int queryCountByMap(ParamMap conditions) {
        return sDeviceInfoDaoImpl.queryCountByMap(conditions);
    }

    @Override
    public void updataAdjacentData(Serializable id, ParamMap conditionUpdate, ParamMap conditionQuery, int lastOrNext) {

    }

    @Override
    public DeviceInfo queryById(Serializable id) {
        return sDeviceInfoDaoImpl.queryById(id);
    }

    @Override
    public void updateByMap(ParamMap conditions) {
        sDeviceInfoDaoImpl.updateByMap(conditions);
    }

    @Override
    public void update(DeviceInfo deviceInfo) {

    }

    @Override
    public void deleteById(Serializable id) {

    }

    @Override
    public void deleteByMap(ParamMap conditions) {

    }
}
