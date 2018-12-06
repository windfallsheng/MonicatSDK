# MonicatSDK
移动应用APP数据统计SDK
文档示例：

public class MySupportApplication extends Application {

	@Override
    	public void onCreate() {
        	super.onCreate();
		// ( 1 ) 初始化配置参数
	Configuration config = new Configuration
			// 传入Application的Context实例
              .Builder(this)
		// 设置是否打开启动次数统计功能，默认为true
        	.setOnStartNum(true)
		// 设置前后台切换的间隔时间（单位：毫秒）最大值，默认为30s
		.setIntervalTime(5  *  1000)
                // 设置数据上报策略，默认为INSTANT 即时上报数据
		.setUploadStrategy(UploadStrategy.INSTANT)
		// 设置数据上报策略为定时上报
		//.setUploadStrategy(UploadStrategy.TIMED_TASK)
		// 设置定时上报 时的小时和分钟，小时格式：大于等于0，小于24，
		// 分钟格式：大于等于0，小于60。
                //.setTriggerTime(13,  42)
		// 设置数据上报策略为间隔上报
		//.setUploadStrategy(UploadStrategy.PERIOD)
		// 设置间隔上报 的间隔时间（单位：毫秒），默认为30分钟，最小值为5分钟
                //.setPeriodTime(5  *  60  *  1000)
		// 设置数据上报策略为批量上报
		//.setUploadStrategy(UploadStrategy.BATCH)
		// 设置批量值，默认为50条
		//.setBatchValue(30)
		// 设置开启debug模式，输出打印日志，默认为null
		// 没有设置时，会根据外层项目app的模式（debug or release）模式来选择
		//.setDebug(false)
		// 针对派信接口需要，添加的统计启动次数标识
		.setType(16)
		.build();
        // ( 2 ) 设置配置参数（可以在其它地方再次修改这些参数配置，详见 三、4 说明）。
        MonicatManager.getInstance().init(config);
	// ( 3 ) 打开Monicat的监控功能
	MonicatManager.getInstance().monitor();
	// 配置完成。
    }
}


注意：
1)	如果设置上报策略为TIMED_TASK（定时上报）或者PERIOD（间隔上报）时需要在manifest中注册以下这个服务：

<service android:name="com.windfallsheng.monicat.service.TimedService">
    <intent-filter>
        <action android:name="monicat.service.action.timedservice_timed_upload" />
    </intent-filter>
</service>

4.	信息说明：
统计策略：（ONLY_WIFI功能实现未完成）
INSTANT	即时上报	实时发送，APP每产生一条消息都会发送到服务器。
APP_LAUNCH	启动上报	只在启动时发送，本次产生的所有数据在下次启动时发送。
PERIOD	间隔上报	启动应用后每隔一段时间，一次性发送到服务器。
BATCH	批量发送	默认当消息数量达到50条时发送一次。
TIMED_TASK	定时上报	指定一天中的某个时间一次性上报所有本地缓存中未上报的数据
ONLY_WIFI	WIFI网络下上报	只在WIFI状态下发送，非WIFI情况缓存到本地。
三、	功能说明：
1.	会话统计：
记录应用启动和退出，包括启动次数，启动和退出的时间，可以统计出来每次使用时长等数据
MonicatManager.getInstance().monitor(); 方法调用后，主要由
SessionStatisticsManager类内部来实现。
2.	页面统计：
记录某个页面开闭状态，能统计出某个页面访问的次数、时间等，也能监测出部分用户使用应用时各页面之间的跳转轨迹。（数据库本地缓存未完善）
使用方法1：
	
	MonicatManager.getInstance().registerPage(Context context);
	或者：
	MonicatManager.getInstance().registerPage(Context context, String pageName)
	// 注销对某个页面的开闭状态的记录
	MonicatManager.getInstance().unregisterPage(Context context);

使用方法2：

标记一次页面访问的开始：
MonicatManager.getInstance().trackBeginPage(Context context); 
或者：
MonicatManager.getInstance().trackBeginPage(Context context, String pageName); 

标记一次页面访问的结束：
MonicatManager.getInstance().trackEndPage(Context context); 
或者：
MonicatManager.getInstance().trackEndPage(Context context, String pageName); 

方法1代码示例：

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		MonicatManager.getInstance().registerPage(this);
	或者：
	MonicatManager.getInstance().registerPage(this, "H5页面");
    }
}

方法2代码示例：

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
	// 标记一次页面访问的开始
	MonicatManager.getInstance().trackBeginPage(this);
	或者：
       MonicatManager.getInstance().trackBeginPage(this, "主界面"
);
    }

    @Override
    protected void onPause() {
        super.onPause();
	// 标记一次页面访问的结束
	MonicatManager.getInstance().trackEndPage(this);
	或者：
MonicatManager.getInstance().trackEndPage(this, "主界面");
	}
}

3.	事件统计：
包括普通事件、自定义事件统计，可以统计次数，即：统计指定行为被触发的次数；
也可统计时长，即，统计两个指定行为之间的消耗时间，以及其它相关数据。（数据库本地缓存未完善）
普通事件方法：

	//标记一次普通事件的开始
	MonicatManager.getInstance().trackBeginEvent(Context context, String eventName);
	//标记一次普通事件的结束
	MonicatManager.getInstance().trackEndEvent(Context context, String eventName);

自定义事件方法：
	// properties Key-Value参数对，key和value都是String类型
	Properties prop = new Properties()
        		.addProperty("name", "value")
        		.addProperty("level", "51");
	//标记一次普通事件的开始
	MonicatManager.getInstance().trackCustomBeginEvent(Context context, String eventName, Properties properties);
	//标记一次普通事件的结束
	MonicatManager.getInstance().trackCustomEndEvent(Context context, String eventName, Properties properties);

代码示例：

mButtonLogin.setOnClickListener(new View.OnClickListener() {
    
@Override
    public void onClick(View view) {
                Properties prop = new Properties()
                		.addProperty("key", "value")
                		.addProperty("level", "51");   
MonicatManager.getInstance().trackCustomBeginEvent(MainActivity.this, "button_click login事件", prop);                                                      
    }
});

4.	Configuration的动态配置问题：
首先保证在项目的Application中，已经配置过Configuration的相关参数，之后如果在其它地方需要修改一些配置参数，可以进行动态的修改，比如：

	  Configuration config = MonicatManager.getInstance().getConfig();
 config.intervalTime = 10 * 1000;
 MonicatManager.getInstance().setConfig(config);

或者(推荐用此方法)按照Application中的配置步骤，再设置一次， 如：

	Configuration config = new Configuration
                .Builder(MonicatManager.getInstance().getContext())
                .setIntervalTime(10 * 1000)
                .build();
        MonicatManager.getInstance().init(config);

7.	扩展的其它方法说明：
1)	调用这个方法可以手动的、一次性上传所有本地缓存数据。

	MonicatManager.getInstance().notifyUploadData();

2)	如果设置了打开启动次数统计功能（默认设置也是 true），如：
	
	Configuration config = new Configuration
                        .Builder(context)
				// 设置是否打开启动次数统计功能，默认为true
        			.setOnStartNum(true)
				// 其它参数配置省略
				……………………
				……………………
                        .build();

那么：

Boolean isForeground = MonicatManager.getInstance().isForeground;
这个方法可以得到时当前app应用是在前台( isForeground = true )还是后台( isForeground = false )；
int appStatus = MonicatManager.getInstance().app_status;
这个方法可以得到时当前app运行状态：1为刚启动打开应用，0为应用正在运行中(包括在前后台的情况)。


特别说明：
有关项目中SQLite数据库缓存数据及其它CRUD相关的功能及网络请求的功能，在本项目中只是粗略实现，未优化，有需要时可以进行修改，替换为其它ORM框架，完善网络请求的代码封装。

see [Blog](https://blog.csdn.net/Silence1515/article/details/84848759)

由于作者水平有限，语言描述及代码实现中难免有纰漏，望各位看官多提宝贵意见！

Hello , World !

感谢所有！
