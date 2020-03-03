package com.windfallsheng.monicat.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.windfallsheng.monicat.common.MonicatConstants;
import com.windfallsheng.monicat.db.sqlite.StatisticsSQLiteHelper;
import com.windfallsheng.monicat.model.SessionInfoEntity;
import com.windfallsheng.monicat.model.Param;
import com.windfallsheng.monicat.model.ParamMap;
import com.windfallsheng.monicat.util.LogUtils;
import com.windfallsheng.monicat.util.SQLUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * CreateDate: 2018/04/16.
 * <p>
 * Author: lzsheng
 * <p>
 * Description: 应用启动数据本地数据库中各个表的CRUD操作
 * <p>
 * Version:
 */
public class SessionInfoDaoImpl implements IBaseDao<SessionInfoEntity> {

    private volatile static SessionInfoDaoImpl instance = null;
    private static StatisticsSQLiteHelper sStatisticsSQLiteHelper;

    private SessionInfoDaoImpl() {
    }

    public static IBaseDao getInstance(Context context) {
        sStatisticsSQLiteHelper = StatisticsSQLiteHelper.getInstance(context);
        if (instance == null) {
            synchronized (SessionInfoDaoImpl.class) {
                if (instance == null) {
                    instance = new SessionInfoDaoImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public int save(SessionInfoEntity sessionInfo) {
        SQLiteDatabase db = sStatisticsSQLiteHelper.getDb();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(StatisticsSQLiteHelper.COLUMN_DEVICE_INFO_ID, sessionInfo.getDeviceInfoId());
            values.put(StatisticsSQLiteHelper.COLUMN_TRIGGERING_TIME, sessionInfo.getTriggeringTime());
            values.put(StatisticsSQLiteHelper.COLUMN_SESSION_TYPE, sessionInfo.getSessionType());
            values.put(StatisticsSQLiteHelper.COLUMN_UPLOADE_STATUS, sessionInfo.getUploadeStatus());
            LogUtils.d(MonicatConstants.SDK_NAME, "AppStartupDaoImpl_save_values==" + values.toString());
            db.insert(StatisticsSQLiteHelper.TABLE_APP_SESSION, "id", values);
            StringBuffer sbSQL = new StringBuffer();
            sbSQL.append("SELECT LAST_INSERT_ROWID() From ").append(StatisticsSQLiteHelper.TABLE_APP_SESSION);
//            sbSQL.append("SELECT MAX(").append(StatisticsSQLiteHelper.COLUMN_SESSION_ID).append(") FROM ")
//                    .append(StatisticsSQLiteHelper.TABLE_APP_SESSION);
            String sql = sbSQL.toString();
            LogUtils.d(MonicatConstants.SDK_NAME, "StartupNumDbHelper_last_insert_rowid()_sql==" + sql);
            Cursor cursor = db.rawQuery(sql, null);
            int primaryKeyId = 0;
            if (cursor.moveToFirst()) {
                primaryKeyId = cursor.getInt(0);
                LogUtils.d(MonicatConstants.SDK_NAME, "AppStartupDaoImpl_save_last_insert_rowid==" + primaryKeyId);
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
     * @param conditions 查询的条件，没有条件查询的需求时可传null，
     *                   在DAO的实现层中会有条件语句拼接的实现
     */
    @Override
    public List<SessionInfoEntity> queryAllByMap(ParamMap conditions) {
        List<SessionInfoEntity> startupNumEntities = new ArrayList<>();
        SQLiteDatabase db = sStatisticsSQLiteHelper.getDb();
        StringBuffer sbSQL = new StringBuffer();
        try {
            sbSQL.append("SELECT * FROM ").append(StatisticsSQLiteHelper.TABLE_APP_SESSION)
                    .append(" WHERE 1 = 1 ");
//            sbSQL.append("SELECT ").append(StatisticsSQLiteHelper.COLUMN_SESSION_ID).append(", ").append(StatisticsSQLiteHelper.COLUMN_DEVICE_INFO_ID)
//                    .append(", ").append(StatisticsSQLiteHelper.COLUMN_STARTUP_TIME).append(", ").append(StatisticsSQLiteHelper.COLUMN_SESSION_TYPE)
//                    .append(", ").append(StatisticsSQLiteHelper.COLUMN_UPLOADE_STATUS).append(" FROM ").append(StatisticsSQLiteHelper.TABLE_APP_SESSION)
//                    .append(" WHERE 1 = 1 ");
            if (conditions != null) { // 拼接条件进行查询的语句
                String spliceSQL = SQLUtils.spliceSQL(conditions);
                sbSQL.append(spliceSQL);
            }
            String sql = sbSQL.toString();
            // sbSQL = SELECT startup_id, device_info_id, startup_time, startup_type, has_uploaded FROM app_startup
            // WHERE 1 = 1  AND startup_type = 2 OR startup_type = 0 OR startup_type = 1 AND has_uploaded = 0 ORDER BY startup_id DESC
            LogUtils.d(MonicatConstants.SDK_NAME, "AppStartupDaoImpl-->queryAllByMap()_sql==" + sql);
            Cursor cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                int index = 0;
                SessionInfoEntity startupNumEntity = new SessionInfoEntity();
                startupNumEntity.setSessionId(cursor.getInt(index++));
                startupNumEntity.setDeviceInfoId(cursor.getInt(index++));
                startupNumEntity.setTriggeringTime(cursor.getLong(index++));
                startupNumEntity.setSessionType(cursor.getInt(index++));
                startupNumEntity.setUploadeStatus(cursor.getInt(index++));
                startupNumEntities.add(startupNumEntity);
            }
            if (startupNumEntities.size() > 0) {
                return startupNumEntities;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sStatisticsSQLiteHelper.closeSQLiteDatabase();
        }
        return null;
    }

    /**
     * 查询某条数据的上一条或者下一条数据，可根据条件进行查询
     *
     * @param id
     * @param conditions 查询的条件，没有条件查询的需求时可传null
     * @param lastOrNext 标识符，判断是获取上一条还是下一条数据的依据
     * @return
     */
    @Override
    public SessionInfoEntity queryAdjacentData(Serializable id, ParamMap conditions, int lastOrNext) {
//        1.select * from table_a where id = (select id from table_a where id < {$id} order by id desc limit 1);
//        2.select * from table_a where id = (select id from table_a where id > {$id} order by id asc limit 1);
//
//        1.select * from table_a where id = (select max(id) from table_a where id < {$id});
//        2.select * from table_a where id = (select min(id) from table_a where id > {$id});
        SessionInfoEntity startupNumEntity = null;
        SQLiteDatabase db = sStatisticsSQLiteHelper.getDb();
        StringBuffer sbSQL = new StringBuffer();
        try {
            sbSQL.append("SELECT ").append(StatisticsSQLiteHelper.COLUMN_SESSION_ID).append(", ").append(StatisticsSQLiteHelper.COLUMN_DEVICE_INFO_ID)
                    .append(", ").append(StatisticsSQLiteHelper.COLUMN_TRIGGERING_TIME)
                    .append(", ").append(StatisticsSQLiteHelper.COLUMN_SESSION_TYPE).append(", ").append(StatisticsSQLiteHelper.COLUMN_UPLOADE_STATUS)
                    .append(" FROM ").append(StatisticsSQLiteHelper.TABLE_APP_SESSION).append(" WHERE ")
                    .append(StatisticsSQLiteHelper.COLUMN_SESSION_ID).append(" = ( SELECT ");
            if (lastOrNext == MonicatConstants.QUERY_DATA_LAST) {
                sbSQL.append(" min( ");
            } else if (lastOrNext == MonicatConstants.QUERY_DATA_NEXT) {
                sbSQL.append(" max( ");
            }
            sbSQL.append(StatisticsSQLiteHelper.COLUMN_SESSION_ID).append(" ) FROM ").append(StatisticsSQLiteHelper.TABLE_APP_SESSION)
                    .append(" WHERE 1 = 1 ");
            if (conditions != null) { // 拼接条件进行查询的语句
                String spliceSQL = SQLUtils.spliceSQL(conditions);
                sbSQL.append(spliceSQL);
            }
            if ((int) id > 0) {
                sbSQL.append(" AND ").append(StatisticsSQLiteHelper.COLUMN_SESSION_ID);
                if (lastOrNext == MonicatConstants.QUERY_DATA_LAST) {
                    sbSQL.append(" > ");
                } else if (lastOrNext == MonicatConstants.QUERY_DATA_NEXT) {
                    sbSQL.append(" < ");
                }
                sbSQL.append(id).append(" )");
            }
            // sbSQL = SELECT startup_id, device_info_id, startup_time, startup_type, has_uploaded FROM app_startup WHERE startup_id =
            // ( SELECT  max(startup_id) FROM app_startup WHERE 1 = 1  AND device_id_type = 1 AND startup_id < 12);
            String sql = sbSQL.toString();
            LogUtils.d(MonicatConstants.SDK_NAME, "AppStartupDaoImpl-->queryAdjacentData()_sql==" + sql);
            Cursor cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                int index = 0;
                startupNumEntity = new SessionInfoEntity();
                startupNumEntity.setSessionId(cursor.getInt(index++));
                startupNumEntity.setDeviceInfoId(cursor.getInt(index++));
                startupNumEntity.setTriggeringTime(cursor.getLong(index++));
                startupNumEntity.setSessionType(cursor.getInt(index++));
                startupNumEntity.setUploadeStatus(cursor.getInt(index++));
            }
        } catch (Exception e) {
            LogUtils.d(MonicatConstants.SDK_NAME, "AppStartupDaoImpl-->queryAdjacentData()_e == " + e.toString());
            e.printStackTrace();
        } finally {
            sStatisticsSQLiteHelper.closeSQLiteDatabase();
        }
        return startupNumEntity;
    }

    @Override
    public SessionInfoEntity queryById(Serializable id) {
        SessionInfoEntity startupNumEntity = null;
        SQLiteDatabase db = sStatisticsSQLiteHelper.getDb();
        StringBuffer sbSQL = new StringBuffer();
        try {
            sbSQL.append("SELECT ").append(StatisticsSQLiteHelper.COLUMN_SESSION_ID).append(", ").append(StatisticsSQLiteHelper.COLUMN_DEVICE_INFO_ID)
                    .append(", ").append(StatisticsSQLiteHelper.COLUMN_TRIGGERING_TIME).append(", ").append(StatisticsSQLiteHelper.COLUMN_SESSION_TYPE)
                    .append(", ").append(StatisticsSQLiteHelper.COLUMN_UPLOADE_STATUS).append(" FROM ").append(StatisticsSQLiteHelper.TABLE_APP_SESSION)
                    .append(" WHERE ").append(StatisticsSQLiteHelper.COLUMN_SESSION_ID).append(" = ").append(id);
            String sql = sbSQL.toString();
//            LogUtils.d(Constants.SDK_NAME, "StartupNumDbHelper_queryById()_sql==" + sql);
            Cursor cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                int index = 0;
                startupNumEntity = new SessionInfoEntity();
                startupNumEntity.setSessionId(cursor.getInt(index++));
                startupNumEntity.setDeviceInfoId(cursor.getInt(index++));
                startupNumEntity.setTriggeringTime(cursor.getLong(index++));
                startupNumEntity.setSessionType(cursor.getInt(index++));
                startupNumEntity.setUploadeStatus(cursor.getInt(index++));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sStatisticsSQLiteHelper.closeSQLiteDatabase();
        }
        return startupNumEntity;
    }

    @Override
    public int queryCountByMap(ParamMap conditions) {
        int count = 0;
        SQLiteDatabase db = sStatisticsSQLiteHelper.getDb();
        StringBuffer sbSQL = new StringBuffer();
        try {
            sbSQL.append("SELECT  COUNT(*) FROM ").append(StatisticsSQLiteHelper.TABLE_APP_SESSION).append(" WHERE 1 = 1 ");
            if (conditions != null) { // 拼接条件进行查询的语句
                String spliceSQL = SQLUtils.spliceSQL(conditions);
                sbSQL.append(spliceSQL);
            }
            String sql = sbSQL.toString();
            LogUtils.d(MonicatConstants.SDK_NAME, "AppStartupDaoImpl-->queryCountByMap()_sql==" + sql);
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

    /**
     * 修改,可根据条件进行
     *
     * @param conditions 查询的条件，
     *                   在DAO的实现层中会有条件语句拼接的实现
     */
    @Override
    public void updateByMap(ParamMap conditions) {
        SQLiteDatabase db = sStatisticsSQLiteHelper.getDb();
        db.beginTransaction();
        try {
            if (conditions != null) {
                StringBuffer sbSQL = new StringBuffer();
                sbSQL.append("UPDATE ").append(StatisticsSQLiteHelper.TABLE_APP_SESSION).append(" SET ");
                if (conditions != null) { // 拼接sql语句
                    String andSQL = SQLUtils.spliceUpdateParamMap(conditions);
                    sbSQL.append(andSQL);
                }
                sbSQL.append(" WHERE 1 = 1 ");
                if (conditions != null) { // 拼接条件进行查询的语句
                    String spliceSQL = SQLUtils.spliceSQL(conditions);
                    sbSQL.append(spliceSQL);
                }
                String sql = sbSQL.toString();
                // sbSQL = UPDATE app_startup SET has_uploaded = 2, user_id = '255' WHERE startup_id in( 1, 2, 3 );
                LogUtils.d(MonicatConstants.SDK_NAME, "AppStartupDaoImpl-->updateByMap()_sql==" + sql);
                db.execSQL(sql);
                db.setTransactionSuccessful();
            } else {
                LogUtils.d(MonicatConstants.SDK_NAME, "AppStartupDaoImpl-->_updateByMap()参数有误");
            }
        } catch (Exception e) {
            Log.d(MonicatConstants.SDK_NAME, "AppStartupDaoImpl-->updateByMap()_e == " + e.toString());
            e.printStackTrace();
        } finally {
            db.endTransaction();
            sStatisticsSQLiteHelper.closeSQLiteDatabase();
        }
//        SQLiteDatabase db = getDb();
//        db.beginTransaction();
//        int primaryKeyId = 0;
//        if (conditions != null) {
//            try {
//                Map<String, List<Object>> andInMap = conditions.getAndInMap();
//                List<Param> params = conditions.getUpdateList();
//                ContentValues values = new ContentValues();
//                StringBuffer sbWhereClause = new StringBuffer();
//                String[] whereArgs = new String[params.size()];
//                for (int i = 0; i < params.size(); i++) {
//                    Param param = params.get(i);
//                    //
//                    String key = param.getKey();
//                    if (i > 0) {
//                        sbWhereClause.append(" AND ");
//                    }
//                    sbWhereClause.append(key).append(" = ?");
//                    //
//                    Object obj = param.getObj();
//                    if (obj instanceof String) {
//                        values.put(key, (String) obj);
//                    } else if (obj instanceof Integer) {
//                        values.put(key, (Integer) obj);
//                    } else if (obj instanceof Long) {
//                        values.put(key, (Long) obj);
//                    }
//                    whereArgs[i] = values + "";
//                }
//                String whereClause =  sbWhereClause.toString();
//                primaryKeyId = db.update(StatisticsSQLiteHelper.TABLE_APP_SESSION, values, whereClause, whereArgs);
//                db.setTransactionSuccessful();
//                LogUtils.d(Constants.SDK_NAME, "AppStartupDaoImpl-->updateByMap()_primaryKeyId == " + primaryKeyId);
//            } catch (Exception e) {
//                Log.d(Constants.SDK_NAME, "AppStartupDaoImpl-->updateByMap()_e == " + e.toString());
//                e.printStackTrace();
//            } finally {
//                db.endTransaction();
//                closeSQLiteDatabase();
//            }
//        } else {
//            LogUtils.d(Constants.SDK_NAME, "AppStartupDaoImpl-->_updateByMap()参数有误");
//        }
//        return primaryKeyId;
    }

    public int updateByMap(ParamMap conditions, int o) {
        SQLiteDatabase db = sStatisticsSQLiteHelper.getDb();
        db.beginTransaction();
        int primaryKeyId = 0;
        if (conditions != null) {
            try {
                List<Param> params = conditions.getUpdateList();
                ContentValues values = new ContentValues();
                StringBuffer sbWhereClause = new StringBuffer();
                String[] whereArgs = new String[params.size()];
                for (int i = 0; i < params.size(); i++) {
                    Param param = params.get(i);
                    //
                    String key = param.getKey();
                    if (i > 0) {
                        sbWhereClause.append(" AND ");
                    }
                    sbWhereClause.append(key).append(" = ?");
                    //
                    Object obj = param.getObj();
                    if (obj instanceof String) {
                        values.put(key, (String) obj);
                    } else if (obj instanceof Integer) {
                        values.put(key, (Integer) obj);
                    } else if (obj instanceof Long) {
                        values.put(key, (Long) obj);
                    }
                    whereArgs[i] = values + "";
                }
                String whereClause = sbWhereClause.toString();
                primaryKeyId = db.update(StatisticsSQLiteHelper.TABLE_APP_SESSION, values, whereClause, whereArgs);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.d(MonicatConstants.SDK_NAME, "AppStartupDaoImpl-->updateByMap()_e == " + e.toString());
                e.printStackTrace();
            } finally {
                db.endTransaction();
                sStatisticsSQLiteHelper.closeSQLiteDatabase();
            }
        } else {
            LogUtils.d(MonicatConstants.SDK_NAME, "AppStartupDaoImpl-->_updateByMap()参数有误");
        }
        return primaryKeyId;
    }

    @Override
    public void update(SessionInfoEntity entity) {

    }

    @Override
    public void deleteById(Serializable id) {

    }

    /**
     * 删除所有,可根据条件进行删除
     *
     * @param conditions 查询的条件，没有条件查询的需求时可传null，
     *                   在DAO的实现层中会有条件语句拼接的实现
     */
    @Override
    public void deleteByMap(ParamMap conditions) {
        SQLiteDatabase db = sStatisticsSQLiteHelper.getDb();
        db.beginTransaction();
        StringBuffer sbSQL = new StringBuffer();
        try {
            sbSQL.append("DELETE FROM ").append(StatisticsSQLiteHelper.TABLE_APP_SESSION)
                    .append(" WHERE 1 = 1 ");
            if (conditions != null) { // 拼接条件进行查询的语句
                String andSQL = SQLUtils.spliceQueryParamMap(conditions);
                sbSQL.append(andSQL);
            }
            String sql = sbSQL.toString();
            // sbSQL = DELETE FROM app_startup WHERE 1 = 1  AND startup_type = 2 OR startup_type = 0
            // OR startup_type = 1
            LogUtils.d(MonicatConstants.SDK_NAME, "AppStartupDaoImpl-->deleteByMap()_sql==" + sql);
            db.execSQL(sql);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            sStatisticsSQLiteHelper.closeSQLiteDatabase();
        }
    }

}
