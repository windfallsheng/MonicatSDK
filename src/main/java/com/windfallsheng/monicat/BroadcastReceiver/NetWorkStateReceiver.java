package com.windfallsheng.monicat.BroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.windfallsheng.monicat.command.Constants;
import com.windfallsheng.monicat.utils.LogUtils;

/**
 * Created by lzsheng on 2018/5/7.
 */

public class NetWorkStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Constants.SDK_NAME, "NetWorkStateReceiver-->onReceive()_网络状态发生变化");
        //检测API是不是小于23，因为到了API23之后getNetworkInfo(int networkType)方法被弃用
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            LogUtils.d(Constants.SDK_NAME, "NetWorkStateReceiver-->onReceive()_API level 小于23");
            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //获取ConnectivityManager对象对应的NetworkInfo对象
            // 获取WIFI连接的信息
            NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            //获取移动数据连接的信息
            NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                LogUtils.d(Constants.SDK_NAME, "NetWorkStateReceiver-->onReceive()_WIFI已连接,移动数据已连接");
                Toast.makeText(context, "WIFI已连接,移动数据已连接", Toast.LENGTH_SHORT).show();
            } else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
                LogUtils.d(Constants.SDK_NAME, "NetWorkStateReceiver-->onReceive()_WIFI已连接,移动数据已断开");
                Toast.makeText(context, "WIFI已连接,移动数据已断开", Toast.LENGTH_SHORT).show();
            } else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                LogUtils.d(Constants.SDK_NAME, "NetWorkStateReceiver-->onReceive()_WIFI已断开,移动数据已连接");
                Toast.makeText(context, "WIFI已断开,移动数据已连接", Toast.LENGTH_SHORT).show();
            } else {
                LogUtils.d(Constants.SDK_NAME, "NetWorkStateReceiver-->onReceive()_WIFI已断开,移动数据已断开");
                Toast.makeText(context, "WIFI已断开,移动数据已断开", Toast.LENGTH_SHORT).show();
            }
            //API大于23时使用下面的方式进行网络监听
        } else {
            LogUtils.d(Constants.SDK_NAME, "NetWorkStateReceiver-->onReceive()_API level 大于23");
            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //获取所有网络连接的信息
            Network[] networks = connMgr.getAllNetworks();
            //用于存放网络连接信息
            StringBuilder sb = new StringBuilder();
            //通过循环将网络信息逐个取出来
            for (int i = 0; i < networks.length; i++) {
                //获取ConnectivityManager对象对应的NetworkInfo对象
                NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                sb.append(networkInfo.getTypeName() + " connect is " + networkInfo.isConnected());
            }
            LogUtils.d(Constants.SDK_NAME, "NetWorkStateReceiver-->onReceive()_NetworkInfo=" + sb.toString());
            Toast.makeText(context, sb.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}

//<receiver android:name=".NetWorkStateReceiver">
//<intent-filter>
//检测网络变化的acton
// <action android:name="android.net.conn.CONNECTIVITY_CHANGE"/>
// <category android:name="android.intent.category.DEFAULT" />
// </intent-filter>
// </receiver>

//        NetWorkStateReceiver netWorkStateReceiver;
//        if (netWorkStateReceiver == null) {
//             netWorkStateReceiver = new NetWorkStateReceiver();
//        }
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//        registerReceiver(netWorkStateReceiver, filter);
//
//        unregisterReceiver(netWorkStateReceiver);









