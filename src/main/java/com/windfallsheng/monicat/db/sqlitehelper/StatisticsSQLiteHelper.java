package com.windfallsheng.monicat.db.sqlitehelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.windfallsheng.monicat.command.Constants;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * CreateDate: 2018/04/16.
 * <p>
 * Author: lzsheng
 * <p>
 * Description: 用户行为统计的数据库，保证StatisticsSQLiteHelper类是单例的；完成数据库各表的创建；
 * 实现数据库的升级、降级及数据迁移等主要功能
 * <p>
 * Version:
 */
public class StatisticsSQLiteHelper extends SQLiteOpenHelper {

    private volatile static StatisticsSQLiteHelper instance = null;
    //AtomicInteger是一个线程安全的类，可以通过它来计数，无论什么线程AtomicInteger值+1后都会改变
    private AtomicInteger mOpenCounter = new AtomicInteger();
    private SQLiteDatabase db;

    public static StatisticsSQLiteHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (StatisticsSQLiteHelper.class) {
                if (instance == null) {
                    instance = new StatisticsSQLiteHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public SQLiteDatabase getDb() {
        if (mOpenCounter.incrementAndGet() == 1) {
            db = this.getWritableDatabase();
        }
        return db;
    }

    public void closeSQLiteDatabase() {
        if (db != null) {
            if (mOpenCounter.decrementAndGet() == 0) {
                db.close();
            }
        }
        this.close();
    }

    private static final int DB_VERSION = 1;
    public static final String DB_NAME = "user_behavior_statistics.db";
    /**
     * 启动次数本地数据库表
     */
    public static final String TABLE_APP_STARTUP = "app_startup";
    public static final String COLUMN_STARTUP_ID = "startup_id";
    public static final String COLUMN_STARTUP_TIME = "startup_time";
    public static final String COLUMN_STARTUP_TYPE = "startup_type";
    public static final String COLUMN_HAS_UPLOADED = "has_uploaded";
    /**
     * 页面停留时间本地数据库表
     */
    public static final String TABLE_PAGE_STAY_TIME = "page_stay_time";
    public static final String COLUMN_PAGE_STAY_TIME_ID = "page_stay_time_id";
    public static final String COLUMN_CREAT_TIME = "creat_time";
    public static final String COLUMN_DESTORY_TIME = "destory_time";
    public static final String COLUMN_PAGE_NAME = "page_name";
    /**
     * 设备信息存储的数据库表
     */
    public static final String TABLE_DEVICE_INFO = "device_info";
    public static final String COLUMN_DEVICE_INFO_ID = "device_info_id";
    public static final String COLUMN_DEVICE_UNIQUE_ID = "device_unique_id";
    public static final String COLUMN_DEVICE_ID_TYPE = "device_id_type";

    private StatisticsSQLiteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /**
         * device_info_id    设备信息相关的数据在本地数据库的存储的ID
         * startup_id        启动时间
         * startup_type      启动方式，0为应用启动期间的前后台切换，1为打开应用，2为退出应用
         * has_uploaded      是否已上传到后台，0为未上传，1为已上传，2为上传失败
         *
         */
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_APP_STARTUP
                + "(" + COLUMN_STARTUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_DEVICE_INFO_ID + " INTEGER, "
                + COLUMN_STARTUP_TIME + " INTEGER, " + COLUMN_STARTUP_TYPE + " INTEGER, "
                + COLUMN_HAS_UPLOADED + " INTEGER)");
        /**
         * user_id          用户ID
         * creat_time       页面打开时间
         * destory_time     页面关闭时间
         * page_name        页面名称
         *
         */
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PAGE_STAY_TIME
                + "( " + COLUMN_PAGE_STAY_TIME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_DEVICE_UNIQUE_ID
                + " TEXT, " + COLUMN_CREAT_TIME + " INTEGER, " + COLUMN_DESTORY_TIME + " INTEGER, "
                + COLUMN_PAGE_NAME + " TEXT)");
        /**
         * device_info_id    设备信息相关的数据在本地数据库的存储的ID
         * device_unique_id  设备唯一ID
         * device_id_type    设备ID类型，1为DeviceId,2为AndroidId，3为Serial，4为IMEI码，5为Mac地址，6为UUID
         *
         */
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DEVICE_INFO
                + "( " + COLUMN_DEVICE_INFO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_DEVICE_UNIQUE_ID
                + " TEXT, " + COLUMN_DEVICE_ID_TYPE + " INTEGER)");
    }

    /**
     * 数据库升级及数据迁移的操作，根据业务需要完善
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        try {
            for (int j = oldVersion; j <= newVersion; j++) {
                switch (j) {
                    case 2:
                        //1. 将表名改为临时表
                        String sqlRename = "ALTER TABLE "
                                + TABLE_APP_STARTUP + " RENAME to " + TABLE_APP_STARTUP + "_temp";
                        db.execSQL(sqlRename);

                        //2. 创建新表
                        String sqlCreatNew = "CREATE TABLE IF NOT EXISTS " + TABLE_APP_STARTUP
                                + "(" + COLUMN_STARTUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_DEVICE_INFO_ID + " INTEGER, "
                                + COLUMN_STARTUP_TIME + " INTEGER, " + COLUMN_STARTUP_TYPE + " INTEGER, "
                                + COLUMN_HAS_UPLOADED + " INTEGER, added_column INTEGER)";
                        db.execSQL(sqlCreatNew);

                        //3. 导入数据　
                        String sqlInsertInto = "INSERT INTO " + TABLE_APP_STARTUP + " SELECT *, '' FROM " + TABLE_APP_STARTUP + "_temp";
                        db.execSQL(sqlInsertInto);
                        // 或者
                        // db.execSQL("INSERT INTO " + TABLE_APP_STARTUP + "()SELECT * , '' FROM " + TABLE_APP_STARTUP+ "_temp");
                        //  注意 双引号”” 是用来补充原来不存在的数据的

                        //4. 删除临时表　　
                        String sqlDropTemp = "DROP TABLE IF EXISTS " + TABLE_APP_STARTUP + "_temp";
                        db.execSQL(sqlDropTemp);
                        break;

                    case 3:

                        break;
                    default:
                        break;
                }
            }
            db.setTransactionSuccessful();
//            Cursor c = db.rawQuery("SELECT * FROM " + TABLE_APP_STARTUP + " WHERE 0", null);
//            try {
//                String[] columnNames = c.getColumnNames();
//                for (int i = 0; i < columnNames.length; i++) {
//                    Log.d(Constants.SDK_NAME, "Monicat:onUpgrade()_columnNames==" + columnNames[i]);
//                }
//            } finally {
//                c.close();
//            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(Constants.SDK_NAME, "Monicat:SQLiteDatabase upgrade failed.");
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 数据库降级及数据迁移的操作，根据业务需要完善
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        try {
            for (int j = oldVersion; j >= newVersion; j--) {
                switch (j) {
                    case 1:
                        String sqlRename = "ALTER TABLE "
                                + TABLE_APP_STARTUP + " RENAME to " + TABLE_APP_STARTUP + "_temp";
                        //1. 将表名改为临时表
                        db.execSQL(sqlRename);
                        String sqlCreatNew = "CREATE TABLE IF NOT EXISTS " + TABLE_APP_STARTUP
                                + "(" + COLUMN_STARTUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_DEVICE_INFO_ID + " INTEGER, "
                                + COLUMN_STARTUP_TIME + " INTEGER, " + COLUMN_STARTUP_TYPE + " INTEGER, "
                                + COLUMN_HAS_UPLOADED + " INTEGER)";
                        //2. 创建新表
                        db.execSQL(sqlCreatNew);
                        String sqlInsertInto = "INSERT INTO " + TABLE_APP_STARTUP + " SELECT " + COLUMN_STARTUP_ID + ", " + COLUMN_DEVICE_INFO_ID + ", "
                                + COLUMN_STARTUP_TIME + ", " + COLUMN_STARTUP_TYPE + ", " + COLUMN_HAS_UPLOADED
                                + " FROM " + TABLE_APP_STARTUP + "_temp";
                        //3. 导入数据　
                        db.execSQL(sqlInsertInto);
                        // 或者
                        // db.execSQL("INSERT INTO " + TABLE_APP_STARTUP + "()SELECT * , '' FROM " + TABLE_APP_STARTUP + "_temp");
                        //  注意 双引号”” 是用来补充原来不存在的数据的
                        String sqlDropTemp = "DROP TABLE IF EXISTS " + TABLE_APP_STARTUP + "_temp";
                        //4. 删除临时表　　
                        db.execSQL(sqlDropTemp);
                        break;

                    case 2:

                        break;
                    default:
                        break;
                }
            }
            db.setTransactionSuccessful();
//            Cursor c = db.rawQuery("SELECT * FROM " + TABLE_APP_STARTUP + " WHERE 0", null);
//            try {
//                String[] columnNames = c.getColumnNames();
//                for (int i = 0; i < columnNames.length; i++) {
//                    Log.d(Constants.SDK_NAME, "Monicat:onDowngrade()_columnNames==" + columnNames[i]);
//                }
//            } finally {
//                c.close();
//            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(Constants.SDK_NAME, "Monicat:SQLiteDatabase downgrade failed.");
        } finally {
            db.endTransaction();
        }
    }

}
