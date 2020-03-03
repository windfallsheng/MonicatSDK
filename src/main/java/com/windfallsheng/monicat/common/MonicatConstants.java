package com.windfallsheng.monicat.common;

/**
 * Created by lzsheng on 2018/4/16.
 */

public class MonicatConstants {


    public static final String SDK_NAME = "Monicat";
    /**
     * 使用SharedPreferences 本地存储的一些信息
     */
    public static final String MONICAT_SHARED_PREF = "monicat_sharedPref";
    /**
     * 服务器
     */
    public static final String SERVER_HOST = "https://www.baidu.com";
    /**
     * 上传会话数据的接口
     */
    public static final String SESSION_STATISTICS = "/";

    //---------------------------分割线-----------------------------
    /**
     * 标识符：标识activity生命周期
     */
    public static final int ON_ACTIVITY_CREATED = 1;
    public static final int ON_ACTIVITY_STARTED = 2;
    public static final int ON_ACTIVITY_RESUMED = 3;
    public static final int ON_ACTIVITY_PAUSED = 4;
    public static final int ON_ACTIVITY_STOPPED = 5;
    public static final int ON_ACTIVITY_SAVEINSTANCESTATE = 6;
    public static final int ON_ACTIVITY_DESTROYED = 7;

    //---------------------------分割线-----------------------------
    /**
     * 标识符：1为页面打开；0为页面关闭
     */
    public static final int PAGE_OPEN = 1;
    /**
     * 标识符：1为页面打开；0为页面关闭
     */
    public static final int PAGE_CLOSE = 0;

    //---------------------------分割线-----------------------------
    /**
     * 获取到的服务器时间
     */
    public static final String LAST_SERVER_TIME = "last_server_time";
    /**
     * 获取到服务器时间时的系统时间
     */
    public static final String LAST_SYSTEM_TIME = "last_system_time";
    /**
     * app应用的versionCode
     */
    public static final String APP_VERSIONCODE = "app_versionCode";
    /**
     * app应用的packageName
     */
    public static final String APP_PACKAGENAME = "app_packageName";

    //---------------------------分割线-----------------------------
    /**
     * 是否已上传到后台，0为已上传，1为未上传，2为上传失败
     */
    public static final int UPLOADED = 0;
    /**
     * 是否已上传到后台，0为已上传，1为未上传，2为上传失败
     */
    public static final int UPLOADABLE = 1;
    /**
     * 是否已上传到后台，0为已上传，1为未上传，2为上传失败
     */
    public static final int UPLOAD_FAILED = 2;

    //---------------------------分割线-----------------------------
    /**
     * 启动方式，0为应用启动期间，前后台切换超时算作的启动；1为安卓系统启动应用；2为退出应用；3为应用在后台；
     */
    public static final int APP_RESTART = 0;
    /**
     * 启动方式，0为应用启动期间，前后台切换超时算作的启动；1为安卓系统启动应用；2为退出应用；3为应用在后台；
     */
    public static final int APP_LAUNCH = 1;
    /**
     * 启动方式，0为应用启动期间，前后台切换超时算作的启动；1为安卓系统启动应用；2为退出应用；3为应用在后台；
     */
    public static final int APP_EXIT = 2;
    /**
     * 启动方式，0为应用启动期间，前后台切换超时算作的启动；1为安卓系统启动应用；2为退出应用；3为应用在后台；
     */
    public static final int APP_BACKGROUND = 3;

    //---------------------------分割线-----------------------------
    /**
     *
     */
    public static final int DATA_TYPE_INITIALIZED = 1;
    /**
     *
     */
    public static final int DATA_TYPE_ADDITIONAL = 0;

    //---------------------------分割线-----------------------------
    /**
     * 查询下一条数据
     */
    public static final int QUERY_DATA_NEXT = 1;
    /**
     * 查询上一条数据
     */
    public static final int QUERY_DATA_LAST = 2;

    //---------------------------分割线-----------------------------

    /**
     * 1 为DeviceId,2 为AndroidId，3 为Serial，4 为IMEI码，5 为Mac地址，6 为UUID
     */
    public static final int DEVICE_ID = 1;
    /**
     * 1 为DeviceId,2 为AndroidId，3 为Serial，4 为IMEI码，5 为Mac地址，6 为UUID
     */
    public static final int ANDROID_ID = 2;
    /**
     * 1 为DeviceId,2 为AndroidId，3 为Serial，4 为IMEI码，5 为Mac地址，6 为UUID
     */
    public static final int SERIAL = 3;
    /**
     * 1 为DeviceId,2 为AndroidId，3 为Serial，4 为IMEI码，5 为Mac地址，6 为UUID
     */
    public static final int IMEI = 4;
    /**
     * 1 为DeviceId,2 为AndroidId，3 为Serial，4 为IMEI码，5 为Mac地址，6 为UUID
     */
    public static final int Mac = 5;
    /**
     * 1 为DeviceId,2 为AndroidId，3 为Serial，4 为IMEI码，5 为Mac地址，6 为UUID
     */
    public static final int DEVICE_UUID = 6;
    /**
     * 条件查询方式的判断标识：1 为AND语句 ,2 为OR语句，3 为AND_LIKE语句，4 为AND_IN语句
     */
    public static final int AND = 1;
    /**
     * 条件查询方式的判断标识：1 为AND语句 ,2 为OR语句，3 为AND_LIKE语句，4 为AND_IN语句
     */
    public static final int OR = 2;
    /**
     * 条件查询方式的判断标识：1 为AND语句 ,2 为OR语句，3 为AND_LIKE语句，4 为AND_IN语句
     */
    public static final int AND_LIKE = 3;
    /**
     * 条件查询方式的判断标识：1 为AND语句 ,2 为OR语句，3 为AND_LIKE语句，4 为AND_IN语句
     */
    public static final int AND_IN = 4;

    /**
     * 条件查询方式的判断标识：1 为AND语句 ,2 为OR语句，3 为AND_LIKE语句，4 为AND_IN语句
     */
    public static final int ORDER_BY_DESC = 5;
    /**
     * 条件查询方式的判断标识：1 为AND语句 ,2 为OR语句，3 为AND_LIKE语句，4 为AND_IN语句
     */
    public static final int ORDER_BY_ASC = 6;

}
