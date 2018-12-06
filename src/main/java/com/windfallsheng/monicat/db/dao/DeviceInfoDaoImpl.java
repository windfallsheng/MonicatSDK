package com.windfallsheng.monicat.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.windfallsheng.monicat.command.Constants;
import com.windfallsheng.monicat.db.sqlitehelper.StatisticsSQLiteHelper;
import com.windfallsheng.monicat.model.DeviceInfo;
import com.windfallsheng.monicat.model.ParamMap;
import com.windfallsheng.monicat.utils.LogUtils;
import com.windfallsheng.monicat.utils.SQLUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * CreateDate: 2018/04/16.
 * <p>
 * Author: lzsheng
 * <p>
 * Description: 设备信息数据本地数据库中各个表的CRUD操作
 * <p>
 * Version:
 */
public class DeviceInfoDaoImpl implements IBaseDao<DeviceInfo> {

    private volatile static DeviceInfoDaoImpl instance = null;
    private static StatisticsSQLiteHelper sStatisticsSQLiteHelper;

    private DeviceInfoDaoImpl() {
    }

    public static DeviceInfoDaoImpl getInstance(Context context) {
        sStatisticsSQLiteHelper = StatisticsSQLiteHelper.getInstance(context);
        if (instance == null) {
            synchronized (DeviceInfoDaoImpl.class) {
                if (instance == null) {
                    instance = new DeviceInfoDaoImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public int save(DeviceInfo deviceInfo) {
        SQLiteDatabase db = sStatisticsSQLiteHelper.getDb();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(StatisticsSQLiteHelper.COLUMN_DEVICE_UNIQUE_ID, deviceInfo.getDeviceUniqueId());
            values.put(StatisticsSQLiteHelper.COLUMN_DEVICE_ID_TYPE, deviceInfo.getDeviceIdType());
            LogUtils.d(Constants.SDK_NAME, "DeviceInfoDaoImpl-->save_values==" + values.toString());
            db.insert(StatisticsSQLiteHelper.TABLE_DEVICE_INFO, "id", values);
            StringBuffer sbSQL = new StringBuffer();
            sbSQL.append("SELECT LAST_INSERT_ROWID() From ").append(StatisticsSQLiteHelper.TABLE_DEVICE_INFO);
//            sbSQL.append("SELECT MAX(").append(StatisticsSQLiteHelper.COLUMN_DEVICE_INFO_ID).append(") FROM ")
//                    .append(StatisticsSQLiteHelper.TABLE_DEVICE_INFO);
            String sql = sbSQL.toString();
            LogUtils.d(Constants.SDK_NAME, "DeviceInfoDaoImpl-->last_insert_rowid()_sql==" + sql);
            Cursor cursor = db.rawQuery(sql, null);
            int primaryKeyId = 0;
            if (cursor.moveToFirst()) {
                primaryKeyId = cursor.getInt(0);
                LogUtils.d(Constants.SDK_NAME, "DeviceInfoDaoImpl-->save_last_insert_rowid==" + primaryKeyId);
            }
            db.setTransactionSuccessful();
            return primaryKeyId;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //结束事务
            //如果程序执行到 endTransaction()之前调用了setTransactionSuccessful() 方法设置事务的标志为成功则提交事务，
            //否则回滚事务。
            db.endTransaction();
            sStatisticsSQLiteHelper.closeSQLiteDatabase();
        }
        return 0;
    }

    /**
     * 查找所有,可根据条件进行查询
     *
     * @param conditions 查询的条件，没有条件查询的需求时可传null，List集合对象Param的Key为要修改的数据库字段名，Value为要修改的值
     *                   特别注意：如果条件里有相同的字段，必须把两个相同字段的条件连续的添加到List集合中，
     *                   在这个实现层中会把有相同的字段的条件语句拼 OR 关键字
     */
    @Override
    public List<DeviceInfo> queryAllByMap(ParamMap conditions) {
        List<DeviceInfo> deviceInfos = new ArrayList<>();
        SQLiteDatabase db = sStatisticsSQLiteHelper.getDb();
        StringBuffer sbSQL = new StringBuffer();
        try {
            sbSQL.append("SELECT * FROM ").append(StatisticsSQLiteHelper.TABLE_DEVICE_INFO).append(" WHERE 1 = 1 ");
//            sbSQL.append("SELECT ").append(StatisticsSQLiteHelper.COLUMN_DEVICE_INFO_ID).append(", ").append(StatisticsSQLiteHelper.COLUMN_DEVICE_UNIQUE_ID)
//                    .append(", ").append(StatisticsSQLiteHelper.COLUMN_DEVICE_ID_TYPE)
//                    .append(" FROM ").append(StatisticsSQLiteHelper.TABLE_DEVICE_INFO)
//                    .append(" WHERE 1 = 1 ");
            if (conditions != null) { // 拼接条件进行查询的语句
                String spliceSQL = SQLUtils.spliceSQL(conditions);
                sbSQL.append(spliceSQL);
            }
            String sql = sbSQL.toString();
            // sbSQL = SELECT device_info_id, device_unique_id, device_id_type
            // WHERE  1 = 1  AND device_unique_id = '8484454118855' AND device_id_type = 6);
            LogUtils.d(Constants.SDK_NAME, "DeviceInfoDaoImpl-->queryAllByMap()_sql==" + sql);
            Cursor cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                int index = 0;
                DeviceInfo deviceInfo = new DeviceInfo();
                deviceInfo.setDevice_info_id(cursor.getInt(index++));
                deviceInfo.setDeviceUniqueId(cursor.getString(index++));
                deviceInfo.setDeviceIdType(cursor.getInt(index++));
                deviceInfos.add(deviceInfo);
            }
            if (deviceInfos.size() > 0) {
                return deviceInfos;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sStatisticsSQLiteHelper.closeSQLiteDatabase();
        }
        return null;
    }

    @Override
    public int queryCountByMap(ParamMap conditions) {
        int count = 0;
        SQLiteDatabase db = sStatisticsSQLiteHelper.getDb();
        StringBuffer sbSQL = new StringBuffer();
        try {
            sbSQL.append("SELECT  COUNT(*) FROM ").append(StatisticsSQLiteHelper.TABLE_DEVICE_INFO).append(" WHERE 1 = 1 ");
            if (conditions != null) { // 拼接条件进行查询的语句
                String spliceSQL = SQLUtils.spliceSQL(conditions);
                sbSQL.append(spliceSQL);
            }
            String sql = sbSQL.toString();
            // sbSQL = SELECT COUNT(*)
            // WHERE  1 = 1  AND device_unique_id = '8484454118855' AND device_id_type = 6);
            LogUtils.d(Constants.SDK_NAME, "DeviceInfoDaoImpl-->queryCountByMap()_sql==" + sql);
            Cursor cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sStatisticsSQLiteHelper.closeSQLiteDatabase();
        }
        return count;
    }

    @Override
    public DeviceInfo queryAdjacentData(Serializable id, ParamMap conditions, int lastOrNext) {

        return null;
    }

    @Override
    public DeviceInfo queryById(Serializable id) {

        return null;
    }

    @Override
    public void updateByMap(ParamMap conditions) {

    }

    @Override
    public void update(DeviceInfo entity) {

    }

    @Override
    public void deleteById(Serializable id) {

    }

    @Override
    public void deleteByMap(ParamMap conditions) {

    }
}
