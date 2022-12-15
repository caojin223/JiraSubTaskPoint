package cn.devops.jira.util;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author cao.jin
 */
public class TimeTools {

    /**
     * 获得当前时间戳
     *
     * @return
     */
    public static Long now() {
        return System.currentTimeMillis();
    }

    public static Date getCurrentDate() {
        return new Date();
    }

    public static void Sleeper(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public static void SleeperThrowException(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    /**
     * 返回当前时间字符串: "yyyy-MM-dd HH:mm:ss"
     */
    public static String currentDatetimeStr() {
        return TimeTools.timestamp2Date(null, null);
    }

    public static String timestamp2Date(String seconds, String format) {
        Long timestamp;
        if (seconds == null || seconds.isEmpty() || "null".equals(seconds)) {
            timestamp = TimeTools.now();
        } else {
            timestamp = Long.valueOf(seconds);
        }
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(timestamp));
    }

    public static String timestamp2Date(long timestamp, String format) {
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(timestamp));
    }

    /**
     * 日期格式字符串转换成时间戳(带毫秒)
     *
     * @param date_str 字符串日期 1970-05-01 10:20:30
     * @param format   如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String date2TimestampStr(String date_str, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return String.valueOf(sdf.parse(date_str).getTime());   // 定位到秒时getTime()/1000
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String date2TimestampStr(String date_str) {
        try {
            String format = "yyyy-MM-dd hh:mm:ss";
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return String.valueOf(sdf.parse(date_str).getTime());   // 定位到秒时getTime()/1000
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Timestamp currentDatetime() {
        Date date = new Date();
        String nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        Timestamp newdate = Timestamp.valueOf(nowTime);
        return newdate;
    }

    public static Timestamp str2Timestamp(String date) {
        return Timestamp.valueOf(date);
    }

    /**
     * 取得当前时间戳（精确到毫秒）
     *
     * @return
     */
    public static String currentTimeStampStr() {
        long time = System.currentTimeMillis();
        String ts = String.valueOf(time);
        return ts;
    }

    public static String getWeek(String seconds) {
        Long timestamp;
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            timestamp = TimeTools.now();
        } else {
            timestamp = Long.valueOf(seconds);
        }
        String format = "%Y%u";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(timestamp));
    }

    public static String getFormatTime() {
        return getFormatTime(null);
    }

    public static String getFormatTime(Long timestamp) {
        if (timestamp == null) {
            timestamp = now();
        }
        Instant instant = Instant.ofEpochMilli(timestamp);
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return ftf.format(localDateTime);
    }

    /**
     * 毫秒转化为时分秒.
     */
    public static String mills2durationStr(long ms) {
        if (ms <= 0) {
            return "0ms";
        }

        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;
        int dd = hh * 24;

        long day = ms / dd;
        long hour = (ms - day * dd) / hh;
        long minute = (ms - day * dd - hour * hh) / mi;
        long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        StringBuilder sb = new StringBuilder();
        if (day > 0) {
            sb.append(day).append("d ");
        }
        if (hour > 0) {
            sb.append(hour).append("h ");
        }
        if (minute > 0) {
            sb.append(minute).append("m ");
        }
        if (second > 0) {
            sb.append(second).append("s ");
        }
        if (milliSecond > 0) {
            sb.append(milliSecond).append("ms");
        }
        return sb.toString();
    }
}