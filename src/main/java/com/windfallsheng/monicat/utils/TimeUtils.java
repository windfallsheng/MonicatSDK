package com.windfallsheng.monicat.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by lzsheng on 2018/4/26.
 */

public class TimeUtils {

    /**
     * 日期格式字符串转换成时间戳
     *
     * @param dateStr    字符串日期
     * @param timeFormat 如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String dateStrToTimeStampStr(String dateStr, String timeFormat) {
        return String.valueOf(dateStrToTimeStamp(dateStr, timeFormat));
    }

    /**
     * 日期格式字符串转换成时间戳
     *
     * @param dateStr    字符串日期
     * @param timeFormat 如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static long dateStrToTimeStamp(String dateStr, String timeFormat) {
        if ("".equals(timeFormat) || timeFormat == null) {
            timeFormat = "yyyy-MM-dd HH:mm:ss";
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);
            return sdf.parse(dateStr).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * @param hour        几点
     * @param minute      几分
     * @param second
     * @param millisecond
     * @return
     */
    public static long getTimeOfDayMillis(int hour, int minute, int second, int millisecond) {
        //得到日历实例，主要是为了下面的获取时间
        Calendar calendar = Calendar.getInstance();
        //是设置日历的时间，主要是让日历的年月日和当前同步
        calendar.setTimeInMillis(System.currentTimeMillis());
        // 这里时区需要设置一下，不然可能个别手机会有8个小时的时间差
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        //设置在几点提醒
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        //设置在几分提醒
        calendar.set(Calendar.MINUTE, minute);
        //
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        return calendar.getTimeInMillis();// **点**分的毫秒值
    }

    /**
     * 将long类型的时间戳转化为日期字符串
     *
     * @param timeMillis 如：1497595115014
     * @return String 如：2017-06-16 14:38:35
     */
    public static String timeLongToDateStr(long timeMillis, String timeFormat) {
        return timeStrToDateStr(String.valueOf(timeMillis), timeFormat);
    }

    /**
     * 将GMT类型的时间戳转化为时间戳
     *
     * @param timeGTMStr 如：Thu, 03 May 2018 07:05:58 GMT
     * @return long
     */
    public static long timeGTMToDateLong(String timeGTMStr, String timeFormatGTM) {
        long time = 0;
        if ("".equals(timeFormatGTM) || timeFormatGTM == null) {
            timeFormatGTM = "EEE, d MMM yyyy HH:mm:ss 'GMT'";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(timeFormatGTM, Locale.ENGLISH);
        Date date = null;
        try {
            date = sdf.parse(timeGTMStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        time = date.getTime();
        return time;
    }

    /**
     * 将GMT类型的时间戳转化为日期字符串
     *
     * @param timeGTMStr 如：Thu, 03 May 2018 07:05:58 GMT
     * @return String 如：2017-06-16 14:38:35
     */
    public static String timeGTMToDateStr(String timeGTMStr, String timeFormatGTM, String timeFormat) {
        long timeMillis = timeGTMToDateLong(timeGTMStr, timeFormatGTM);
        return timeLongToDateStr(timeMillis, timeFormat);
    }

    /**
     * 将字符串类型的时间戳转化为日期字符串
     *
     * @param timeMillis 如：1497595115014
     * @return String 如：2017-06-16 14:38:35
     */
    public static String timeStrToDateStr(String timeMillis, String timeFormat) {
        if ("".equals(timeFormat) || timeFormat == null) {
            timeFormat = "yyyy-MM-dd HH:mm:ss";
        }
        Date date = new Date(Long.parseLong(timeMillis));
        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);//目标格式
        String dateString = sdf.format(date);
        return dateString;
    }

    /**
     * 取得当前时间戳（精确到毫秒）
     *
     * @return
     */
    public static String getTimeStampStr() {
        long time = System.currentTimeMillis();
        String t = String.valueOf(time);
        return t;
    }

    /**
     * 取得当前时间戳（精确到毫秒）
     *
     * @return
     */
    public static long getTimeStamp() {
        long time = System.currentTimeMillis();
        return time;
    }

}
