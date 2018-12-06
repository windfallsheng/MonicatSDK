package com.windfallsheng.monicat.db.service;

import android.content.Context;

import com.windfallsheng.monicat.action.MonicatManager;
import com.windfallsheng.monicat.command.Constants;
import com.windfallsheng.monicat.db.dao.AppStartupDaoImpl;
import com.windfallsheng.monicat.db.dao.DeviceInfoDaoImpl;
import com.windfallsheng.monicat.db.dao.IBaseDao;
import com.windfallsheng.monicat.db.sqlitehelper.StatisticsSQLiteHelper;
import com.windfallsheng.monicat.model.AppStartupEntity;
import com.windfallsheng.monicat.model.DeviceInfo;
import com.windfallsheng.monicat.model.Param;
import com.windfallsheng.monicat.model.ParamMap;
import com.windfallsheng.monicat.utils.LogUtils;
import com.windfallsheng.monicat.utils.SystemUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * CreateDate: 2018/04/16.
 * <p>
 * Author: lzsheng
 * <p>
 * Description: 会话数据(包括应用启动数据)，设备信息数据的本地数据库的业务操作类
 * 调用{@link AppStartupDaoImpl } {@link DeviceInfoDaoImpl }对表的CRUD操作，完成必要的业务逻辑
 * <p>
 * Version:
 */
public class SessionStatisticsService implements IBaseService<AppStartupEntity> {

    private static IBaseService sDeviceInfoService;
    private static IBaseDao sAppStartupDaoImpl;
    private volatile static SessionStatisticsService instance = null;

    private SessionStatisticsService() {
    }

    public static SessionStatisticsService getInstance(Context context) {
        sAppStartupDaoImpl = AppStartupDaoImpl.getInstance(context);
        sDeviceInfoService = DeviceInfoService.getInstance(context);
        if (instance == null) {
            synchronized (AppStartupDaoImpl.class) {
                if (instance == null) {
                    instance = new SessionStatisticsService();
                }
            }
        }
        return instance;
    }

    /**
     * 保存到记录表中
     *
     * @param startupNumEntity
     * @return 返回插入的数据的主键ID
     */
    @Override
    public int save(AppStartupEntity startupNumEntity) {
        ParamMap paramMap = new ParamMap()// 添加数据库查询条件
                .setOrderAscMap(StatisticsSQLiteHelper.COLUMN_DEVICE_INFO_ID);
        List<DeviceInfo> deviceInfos = sDeviceInfoService.queryAllByMap(paramMap);
        int deviceInfoId = 0;
        if (deviceInfos != null) { // 判断设备信息在本地库中有没有存储信息，若有则取出，没有则重新获取并存到数据库中
            deviceInfoId = deviceInfos.get(0).getDevice_info_id();
        } else {
            DeviceInfo deviceInfo = SystemUtils.getDeviceInfo(MonicatManager.getInstance().getContext());
            deviceInfoId = sDeviceInfoService.save(deviceInfo);
        }
        if (deviceInfoId > 0) {
            startupNumEntity.setDeviceInfoId(deviceInfoId);
            return sAppStartupDaoImpl.save(startupNumEntity);
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
    public List<AppStartupEntity> queryAllByMap(ParamMap conditions) {
        return sAppStartupDaoImpl.queryAllByMap(conditions);
    }

    @Override
    public int queryCountByMap(ParamMap conditions) {
        return sAppStartupDaoImpl.queryCountByMap(conditions);
    }

    /**
     * 修改某条数据的上一条或者下一条数据
     *
     * @param id
     * @param conditionUpdate ParamMap集合的Key为要修改的数据库字段名，Value为要修改的值
     * @param conditionQuery  查询的条件，没有条件查询的需求时可传null
     * @param lastOrNext      标识符，判断是获取上一条还是下一条数据的依据
     * @return
     */
    @Override
    public void updataAdjacentData(Serializable id, ParamMap conditionUpdate, ParamMap conditionQuery, int lastOrNext) {
        AppStartupEntity appStartupEntity = (AppStartupEntity) sAppStartupDaoImpl.queryAdjacentData(id, conditionQuery, lastOrNext);
        if (appStartupEntity != null) {
            LogUtils.d(Constants.SDK_NAME, "SessionStatisticsService-->updataAdjacentData()_appStartupEntity==" + appStartupEntity);
            int num = 0;// 用来标识是不是需要修改操作
            List<Param> params = conditionUpdate.getUpdateList();
            for (Param param : params) {// 判断数据库的字段值和要修改的值是不是相同，将不相同的值修改
                String key = param.getKey();
                Object value = param.getObj();
                Map<String, Object> mapping = appStartupEntity.getMapping();
//                Log.d(Constants.SDK_NAME, "SessionStatisticsService-->updataAdjacentData()_mapping==" + mapping);
                Object fieldObj = mapping.get(key); // 得到实体对象对应的属性值
                LogUtils.d(Constants.SDK_NAME, "SessionStatisticsService-->updataAdjacentData()_fieldObj==" + fieldObj.toString());
                if (value instanceof String) { // todo 对数据类型的判断需要完善
                    if (value.equals((String) fieldObj)) { // 判断从数据库的对象字段值和要修改的值是不是相同
                        params.remove(param); // 数据库的字段值和要修改的值是相同的，不需要再次修改，所以需要从集合中移除，因为可能的情况是，有些字段需要修改，有些字段不需要修改，所以要移除
                    } else {
                        num++;
                    }
                } else {
                    if (fieldObj == value) {
                        params.remove(param); // 数据库的字段值和要修改的值是相同的，不需要再次修改，所以从集合中移除
                    } else {
                        num++;
                    }
                }
            }
            if (num > 0) {// num > 0 说明有需要修改操作的字段
                LogUtils.d(Constants.SDK_NAME, "SessionStatisticsService-->updataAdjacentData()_appStartupEntity.getStartupId()==null" + appStartupEntity.getStartupId());
                conditionUpdate.setAndInMap(StatisticsSQLiteHelper.COLUMN_STARTUP_ID, appStartupEntity.getStartupId());
                sAppStartupDaoImpl.updateByMap(conditionUpdate);
            }
        } else {
            LogUtils.d(Constants.SDK_NAME, "SessionStatisticsService-->updataAdjacentData()_appStartupEntity==null");
        }
    }

    /**
     * 删除所有,可根据条件进行删除
     *
     * @param conditions 查询的条件，没有条件查询的需求时可传null，
     *                   在DAO的实现层中会有条件语句拼接的实现
     */
    @Override
    public void deleteByMap(ParamMap conditions) {
        sAppStartupDaoImpl.deleteByMap(conditions);
    }

    @Override
    public AppStartupEntity queryById(Serializable id) {
        return (AppStartupEntity) sAppStartupDaoImpl.queryById(id);
    }

    /**
     * 修改,可根据条件进行
     *
     * @param conditions 查询的条件，
     *                   在DAO的实现层中会有条件语句拼接的实现
     */
    @Override
    public void updateByMap(ParamMap conditions) {
        sAppStartupDaoImpl.updateByMap(conditions);
    }

    @Override
    public void update(AppStartupEntity entity) {

    }

    @Override
    public void deleteById(Serializable id) {

    }

}
