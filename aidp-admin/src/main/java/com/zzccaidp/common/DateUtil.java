package com.zzccaidp.common;


import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.zzccaidp.common.DateCommon.DATETIME_PATTERN_DEFAULT;

/**
 * 日期工具类
 *
 * @author
 * @since
 */
public class DateUtil {

    private static final String ZONE_ID = "Asia/Shanghai";

    private final static TimeZone TIME_ZONE = TimeZone.getTimeZone(ZONE_ID);

    /**
     * 得到按指定格式的系统当前时间
     *
     * @param dateFormat 日期格式 @nullAble default:yyyy-MM-dd HH:mm:ss
     * @return 格式化的日期字符串
     */
    public static String getSysDate(String dateFormat) {
        if (StringUtils.isBlank(dateFormat)) {
            dateFormat = DateCommon.DATETIME_PATTERN_DEFAULT;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        LocalDateTime localDateTime = LocalDateTime.now();
        return localDateTime.format(formatter);
    }

    /**
     * 得到按指定格式的系统当前时间
     *
     * @return 格式化的日期字符串
     */
    public static String getSysDate() {
        return getSysDate(DateCommon.DATETIME_PATTERN_DEFAULT);
    }

    public static String longToString(Long dateTime, String dateFormat) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(dateTime),
                ZoneId.systemDefault()
        );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return localDateTime.format(formatter);
    }

    public static String longToStringUTC(Long dateTime, String dateFormat) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(dateTime),
                ZoneOffset.UTC
        );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return localDateTime.format(formatter);
    }


    public static String iso8601ToString(String dateTime, String dateFormat) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return localDateTime.format(formatter);
    }

    /**
     * 获取当前时间多久以前的时间。
     *
     * @param size       时间大小
     * @param type       类型，多少天或者多少年
     * @param dateFormat 格式
     * @return
     */
    public static String getBeforeTime(long size, String type, String dateFormat) {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime before;
        switch (type) {
            case "y":
                before = localDateTime.minusYears(size);
                break;
            case "M":
                before = localDateTime.minusMonths(size);
                break;
            case "d":
                before = localDateTime.minusDays(size);
                break;
            case "h":
                before = localDateTime.minusHours(size);
                break;
            case "m":
                before = localDateTime.minusMinutes(size);
                break;
            case "s":
                before = localDateTime.minusSeconds(size);
                break;
            default:
                before = localDateTime.minusYears(size);
                break;
        }

        if (StringUtils.isBlank(dateFormat)) {
            dateFormat = DateCommon.DATETIME_PATTERN_DEFAULT;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return before.format(formatter);

    }

    /**
     * 得到按指定格式的时间
     *
     * @param date       日期
     * @param dateFormat 格式 @nullAble default:yyyy-MM-dd HH:mm:ss
     * @return 格式化的日期字符串
     */
    public static String getDateString(Date date, String dateFormat) {
        if (ObjectUtils.isEmpty(date)) {
            return StringUtils.EMPTY;
        }

        LocalDateTime localDateTime = dateToLocalDateTime(date);
        if (ObjectUtils.isEmpty(localDateTime)) {
            return StringUtils.EMPTY;
        }
        return localDateTime.format(DateTimeFormatter.ofPattern(dateFormat));
    }

    /**
     * 得到默认格式的时间 yyyy-MM-dd HH:mm:ss
     *
     * @param date 日期
     * @return 格式化的日期字符串
     */
    public static String getDateString(Date date) {
        return getDateString(date, DateCommon.DATETIME_PATTERN_DEFAULT);
    }


    /**
     * Date 转 LocalDateTime
     *
     * @param date Date
     * @return LocalDateTime
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        if (ObjectUtils.isEmpty(date)) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 获取时间戳
     *
     * @return 时间戳
     */
    public static Long getTimeStamp() {
        return Instant.now().toEpochMilli();
    }


    /**
     * 时间戳转指定格式的字符串
     *
     * @param dateFormat 日期格式 @nullAble default:yyyy-MM-dd HH:mm:ss
     * @param timeStamp  时间戳
     * @return 格式化的日期字符串
     */
    public static String getDateStringByTimestamp(long timeStamp, String dateFormat) {
        if (StringUtils.isBlank(dateFormat)) {
            dateFormat = DateCommon.DATETIME_PATTERN_DEFAULT;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeStamp), ZoneId.systemDefault());
        return localDateTime.format(formatter);
    }


    /**
     * 判断时间格式字符串是否在当前时间之前
     *
     * @param timeStr    时间字符串 @NotBlack
     * @param dateFormat 格式化 @NotBlack
     * @return boolean
     */
    public static Boolean isBeforeNow(String timeStr, String dateFormat) {
        return parse(timeStr, dateFormat).isBefore(LocalDateTime.now());
    }

    /**
     * 判断时间格式字符串是否在当前时间之前
     *
     * @param timeStr 时间字符串 @NotBlack yyyy-MM-dd HH:mm:ss
     * @return boolean
     */
    public static Boolean isBeforeNow(String timeStr) {
        return parse(timeStr, DateCommon.DATETIME_PATTERN_DEFAULT).isBefore(LocalDateTime.now());
    }

    /**
     * 判断时间格式字符串是否在当前时间之后
     *
     * @param timeStr    时间字符串 @NotBlack
     * @param dateFormat 格式化 @NotBlack
     * @return boolean
     */
    public static Boolean isAfterNow(String timeStr, String dateFormat) {
        return parse(timeStr, dateFormat).isAfter(LocalDateTime.now());
    }

    /**
     * 判断时间格式字符串是否在当前时间之后
     *
     * @param timeStr 时间字符串 @NotBlack yyyy-MM-dd HH:mm:ss
     * @return boolean
     */
    public static Boolean isAfterNow(String timeStr) {
        return parse(timeStr, DateCommon.DATETIME_PATTERN_DEFAULT).isAfter(LocalDateTime.now());
    }


    /**
     * 解析时间字符串
     *
     * @param time       时间字符串 @NotBlack
     * @param dateFormat 格式化 @NotBlack
     * @return LocalDateTime
     */
    public static LocalDateTime parse(String time, String dateFormat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return LocalDateTime.parse(time, formatter);
    }

    /**
     * @param fromdatetime - 时间格式的字符串”YYYY-MM-DD HH:MM:SS”
     * @param todatetime   - 时间格式的字符串”YYYY-MM-DD HH:MM:SS”
     * @return 返回两个时间之间间隔的秒数
     * <p>
     * 计算fromdatetime和todatetime之间相隔的时间（秒）.
     * 如果fromdatetime或者todatetime不满足时间格式，返回0，如：
     * <p>
     * timeInterval (”2004-07-04 09:24:05”, ”2004-07-04 10:24:05”) 返回3600
     * timeInterval (”2004-07-05 09:24:05”, ”2004-07-04 09:24:05”)  返回 -86400
     * timeInterval (”2004-7-5 09:24:05”, ”2004-7-4 09:24:05”)  返回 0
     *
     */
    public static long timeInterval(String fromdatetime, String todatetime) {
        Calendar fromcalendar = getCalendar(fromdatetime, "yyyy'-'MM'-'dd' 'HH:mm:ss");
        Calendar tocalendar = getCalendar(todatetime, "yyyy'-'MM'-'dd' 'HH:mm:ss");

        if (fromcalendar == null || tocalendar == null) return 0;

        return (tocalendar.getTime().getTime() - fromcalendar.getTime().getTime()) / 1000;
    }

    /**
     * @param datetime - 给定的日期时间
     * @param formart  - 给定的日期时间的格式
     * @return 返回给定日历， 如果格式不正确，返回null
     */
    public static Calendar getCalendar(String datetime, String formart) {
        SimpleDateFormat SDF = new SimpleDateFormat(formart);

        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(SDF.parse(datetime));
        } catch (ParseException e) {
            return null;
        }

        return calendar;
    }

    /**
     * 比较时间，大于返回1，等于返回0，小于返回-1
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int comparing(String date1, String date2, String dateFormat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        LocalDateTime localDateTime1 = LocalDateTime.parse(date1, formatter);
        LocalDateTime localDateTime2 = LocalDateTime.parse(date2, formatter);
        return localDateTime1.compareTo(localDateTime2);
    }

    public static Date getDate(LocalDateTime createTime) {
        return Date.from(createTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
