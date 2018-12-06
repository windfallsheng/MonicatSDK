package com.windfallsheng.monicat.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.windfallsheng.monicat.command.Constants;
import com.windfallsheng.monicat.model.DeviceInfo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by lzsheng on 2018/4/9.
 * <p>
 * todo 获取设置唯一标识的模块，借鉴的方法，等完善逻辑
 */

public class SystemUtils {

    private static String filePath = File.separator + "UTips" + File.separator + "UUID";

    public static DeviceInfo getDeviceInfo(Context context) {
        String deviceId = "";
        try {
            deviceId = getDeviceId(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (deviceId != null && !"".equals(deviceId)) {
            deviceId = deviceId.replace(":", "");
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setDeviceUniqueId(deviceId);
            deviceInfo.setDeviceIdType(Constants.DEVICE_ID);
            return deviceInfo;
        }
        try {
            deviceId = getAndroidId(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (deviceId != null && !"".equals(deviceId)) {
            deviceId = deviceId.replace(":", "");
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setDeviceUniqueId(deviceId);
            deviceInfo.setDeviceIdType(Constants.ANDROID_ID);
            return deviceInfo;
        }
        try {
            deviceId = getIMIEStatus(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (deviceId != null && !"".equals(deviceId)) {
            deviceId = deviceId.replace(":", "");
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setDeviceUniqueId(deviceId);
            deviceInfo.setDeviceIdType(Constants.IMEI);
            return deviceInfo;
        }
        try {
            deviceId = getMACAddress(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (deviceId != null && !"".equals(deviceId)) {
            deviceId = deviceId.replace(":", "");
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setDeviceUniqueId(deviceId);
            deviceInfo.setDeviceIdType(Constants.Mac);
            return deviceInfo;
        }
        String fileRootPath = getPath(context) + filePath;
        String uuid = FileUtils.readFile(fileRootPath);
        if (uuid != null || !"".equals(uuid)) {
            deviceId = UUID.randomUUID().toString();
            deviceId = deviceId.replace(":", "");
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setDeviceUniqueId(deviceId);
            deviceInfo.setDeviceIdType(Constants.DEVICE_UUID);
            return deviceInfo;
        }
        return null;
    }

    // IMEI码
    private static String getIMIEStatus(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        return deviceId;
    }

    public static String getDeviceId(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId().toString();
    }

    // Mac地址
    public static String getMACAddress(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wm.getConnectionInfo().getMacAddress();
    }

    public static String getSerialNumber() {
        return Build.SERIAL;
    }

    // Android Id
    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    private static void saveUUID(Context context, String UUID) {
        String ExternalSdCardPath = getExternalSdCardPath() + filePath;
        FileUtils.writeFile(ExternalSdCardPath, UUID);
        String InnerPath = context.getFilesDir().getAbsolutePath() + filePath;
        FileUtils.writeFile(InnerPath, UUID);
    }

    public static String getPath(Context context) {
        //首先判断是否有外部存储卡，如没有判断是否有内部存储卡，如没有，继续读取应用程序所在存储
        String phonePicsPath = getExternalSdCardPath();
        if (phonePicsPath == null) {
            phonePicsPath = context.getFilesDir().getAbsolutePath();
        }
        return phonePicsPath;
    }

    /**
     * 获取扩展SD卡存储目录
     * <p/>
     * 如果有外接的SD卡，并且已挂载，则返回这个外置SD卡目录
     * 否则：返回内置SD卡目录
     *
     * @return
     */
    public static String getExternalSdCardPath() {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File sdCardFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            return sdCardFile.getAbsolutePath();
        }

        String path = null;

        File sdCardFile = null;

        ArrayList<String> devMountList = getDevMountList();

        for (String devMount : devMountList) {
            File file = new File(devMount);

            if (file.isDirectory() && file.canWrite()) {
                path = file.getAbsolutePath();

                String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
                File testWritable = new File(path, "test_" + timeStamp);

                if (testWritable.mkdirs()) {
                    testWritable.delete();
                } else {
                    path = null;
                }
            }
        }

        if (path != null) {
            sdCardFile = new File(path);
            return sdCardFile.getAbsolutePath();
        }

        return null;
    }

    /**
     * 遍历 "system/etc/vold.fstab” 文件，获取全部的Android的挂载点信息
     *
     * @return
     */
    private static ArrayList<String> getDevMountList() {
        String[] toSearch = FileUtils.readFile("/system/etc/vold.fstab").split(" ");
        ArrayList<String> out = new ArrayList<>();
        for (int i = 0; i < toSearch.length; i++) {
            if (toSearch[i].contains("dev_mount")) {
                if (new File(toSearch[i + 2]).exists()) {
                    out.add(toSearch[i + 2]);
                }
            }
        }
        return out;
    }

}
