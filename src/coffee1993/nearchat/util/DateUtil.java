package coffee1993.nearchat.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.R.integer;
import android.content.Context;
/**
 * 日期工具类
 * @author zhangyi
 *
 */
public class DateUtil {

    public DateUtil(){

    }

    public static final String hhmmFormat="HH:mm";
    public static final String MMddFormat="MM-dd";
    public static final String yyyyFormat="yyyy";
    public static final String yyyyChineseFormat="yyyy年";
    public static final String yyyyMMddFormat="yyyy-MM-dd";
    public static final String fullFormat="yyyy-MM-dd HH:mm:ss";
    public static final String MMddChineseFormat="MM月dd日";
    public static final String yyyyMMddChineseFormat="yyyy年MM月dd日";
    public static final String fullChineseFormat="yyyy年MM月dd日 HH时mm分ss秒";
    public static final String [] WEEKS={"星期日","星期一","星期二","星期三","星期四","星期五","星期六"};
    public static String FORMATTIMESTR = "yyyy年MM月dd日 HH:mm:ss"; 

    /**
     * 获取yyyyMMdd格式日期
     * 
     * @param time
     * @return
     */
    public static Date getDate(String time) {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        try {
            date = format.parse(time);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String formatDate(Context context, long date) {
        @SuppressWarnings("deprecation")
        int format_flags = android.text.format.DateUtils.FORMAT_NO_NOON_MIDNIGHT
                | android.text.format.DateUtils.FORMAT_ABBREV_ALL
                | android.text.format.DateUtils.FORMAT_CAP_AMPM
                | android.text.format.DateUtils.FORMAT_SHOW_DATE
                | android.text.format.DateUtils.FORMAT_SHOW_DATE
                | android.text.format.DateUtils.FORMAT_SHOW_TIME;
        return android.text.format.DateUtils.formatDateTime(context, date, format_flags);
    }

    /**
     * 返回此时时间
     * 
     * @return String: XXX年XX月XX日 XX:XX:XX
     */
    public static String getNowtime() {
        return new SimpleDateFormat(FORMATTIMESTR).format(new Date());
    }

    /**
     * 格式化输出指定时间点与现在的差
     * 
     * @param paramTime
     *            指定的时间点
     * @return 格式化后的时间差，类似 X秒前、X小时前、X年前
     */
    public static String getBetweentime(String paramTime) {
        String returnStr = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(FORMATTIMESTR);
        try {
            Date nowData = new Date();
            Date mDate = dateFormat.parse(paramTime);
            long betweenForSec = Math.abs(mDate.getTime() - nowData.getTime()) / 1000; // 秒
            if (betweenForSec < 60) {
                returnStr = betweenForSec + "秒前";
            }
            else if (betweenForSec < (60 * 60)) {
                returnStr = betweenForSec / 60 + "分钟前";
            }
            else if (betweenForSec < (60 * 60 * 24)) {
                returnStr = betweenForSec / (60 * 60) + "小时前";
            }
            else if (betweenForSec < (60 * 60 * 24 * 30)) {
                returnStr = betweenForSec / (60 * 60 * 24) + "天前";
            }
            else if (betweenForSec < (60 * 60 * 24 * 30 * 12)) {
                returnStr = betweenForSec / (60 * 60 * 24 * 30) + "个月前";
            }
            else
                returnStr = betweenForSec / (60 * 60 * 24 * 30 * 12) + "年前";
        }
        catch (ParseException e) {
            returnStr = "TimeError"; // 错误提示
        }
        return returnStr;
    }
    
    /**
     * 得到指定时间的时间日期格式
     * @param date 指定的时间
     * @param format 时间日期格式
     * @return
     */
    public static String getFormatDateTimeString(Date date,String format){
        DateFormat df=new SimpleDateFormat(format);
        return df.format(date);
    }
    
    public static Date getFormatDateTime(String time,String format){
        Date date = new Date(System.currentTimeMillis());
    	DateFormat df=new SimpleDateFormat(format);
    	try {
			date = df.parse(time);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return date;
    }
    
    
    /**
     * 取得指定时间毫秒数
     * @return 指定时间毫秒数字符串
     * 
     */
    public static String getDateTime(Date date){
    	
    	return Long.toString(date.getTime());
    
    }
    
    
    /**
     * 获取当前时间的年份
     * @return year int
     */
    public static int getIntYear(){
    	  Calendar cal=Calendar.getInstance();
          cal.setTime(new Date());
          return cal.get(Calendar.YEAR);
    }
    
    /**
     * 获取当前时间的月份
     * @return month int
     */
    public static int getIntMonth(){
    	  Calendar cal=Calendar.getInstance();
          cal.setTime(new Date());
          return cal.get(Calendar.MONTH);
    }
    
    /**
     * 获取当前时间的当月第几天
     * @return day day_of_month
     */
    public static int getIntday(){
    	  Calendar cal=Calendar.getInstance();
          cal.setTime(new Date());
          return cal.get(Calendar.DAY_OF_MONTH);
    }
    
    
    
    
    /**
     * 判断是否是润年
     * @param date 指定的时间
     * @return true:是润年,false:不是润年
     */
    public static boolean isLeapYear(Date date) {
        Calendar cal=Calendar.getInstance();
        cal.setTime(date);
        return isLeapYear(cal.get(Calendar.YEAR));
    }

    /**
     * 判断是否是润年
     * @param date 指定的年
     * @return true:是润年,false:不是润年
     */
    public static boolean isLeapYear(int year) {
        GregorianCalendar calendar = new GregorianCalendar();
        return calendar.isLeapYear(year);
    }

    /**
     * 判断指定的时间是否是今天
     * @param date 指定的时间
     * @return true:是今天,false:非今天
     */
    public static boolean isInToday(Date date){
        boolean flag=false;
        Date now=new Date();
        String fullFormat=getFormatDateTimeString(now,DateUtil.yyyyMMddFormat);
        String beginString=fullFormat+" 00:00:00";
        String endString=fullFormat+" 23:59:59";
        DateFormat df=new SimpleDateFormat(DateUtil.fullFormat);
        try {
            Date beginTime=df.parse(beginString);
            Date endTime=df.parse(endString);
            flag=date.before(endTime)&&date.after(beginTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 判断两时间是否是同一天
     * @param from 第一个时间点
     * @param to 第二个时间点
     * @return true:是同一天,false:非同一天
     */
    public static boolean isSameDay(Date from,Date to){
        boolean isSameDay=false;
        DateFormat df=new SimpleDateFormat(DateUtil.yyyyMMddFormat);
        String firstDate=df.format(from);
        String secondDate=df.format(to);
        isSameDay=firstDate.equals(secondDate);
        return isSameDay;
    }

    /**
     * 求出指定的时间那天是星期几
     * @param date 指定的时间
     * @return 星期X
     */
    public static String getWeekString(Date date){
        return DateUtil.WEEKS[getWeek(date)-1];
    }

    /**
     * 求出指定时间那天是星期几
     * @param date 指定的时间
     * @return 1-7
     */
    public static int getWeek(Date date){
        int week=0;
        Calendar cal=Calendar.getInstance();
        cal.setTime(date);
        week=cal.get(Calendar.DAY_OF_WEEK);
        return week;
    }

    /**
     * 制定时间离现在的时长
     * @param date 已有的指定时间
     * @return 时间段描述
     */
    public static String getAgoTimeString(Date date){
        Date now=new Date();
        Calendar cal=Calendar.getInstance();
        cal.setTime(date);
        Date agoTime=cal.getTime();
        long mtime=now.getTime()-agoTime.getTime();
        String str="";
        long stime=mtime/1000;
        long minute=60;
        long hour=60*60;
        long day=24*60*60;
        long weeks=7*24*60*60;
        long months=100*24*60*60;
        if(stime<minute){
            long time_value=stime;
            if(time_value<=0){
                time_value=1;
            }
            str=time_value+"秒前";
        }else if(stime>=minute && stime<hour){
            long time_value=stime/minute;
            if(time_value<=0){
                time_value=1;
            }
            str=time_value+"分前";
        }else if(stime>=hour && stime<day){
            long time_value=stime/hour;
            if(time_value<=0){
                time_value=1;
            }
            str=time_value+"小时前";
        }else if(stime>=day&&stime<weeks){
            long time_value=stime/day;
            if(time_value<=0){
                time_value=1;
            }
            str=time_value+"天前";
        }else if(stime>=weeks&&stime<months){
            DateFormat df=new SimpleDateFormat(DateUtil.MMddFormat);
            str=df.format(date);
        }else{
            DateFormat df=new SimpleDateFormat(DateUtil.yyyyMMddFormat);
            str=df.format(date);
        }
        return str;
    }

    /**
     * 判断指定时间是否是周末
     * @param date 指定的时间
     * @return true:是周末,false:非周末
     */
    public static boolean isWeeks(Date date){
        boolean isWeek=false;
        isWeek=(getWeek(date)-1==0||getWeek(date)-1==6);
        return isWeek;
    }

    /**
     * 今天启动时间
     * @return 今天的最开始时间
     */
    public static Date getTodayBeginTime(){
        String beginString=DateUtil.yyyyMMddFormat+" 00:00:00";
        DateFormat df=new SimpleDateFormat(DateUtil.fullFormat);
        Date beginTime=new Date();
        try {
            beginTime=df.parse(beginString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return beginTime;
    }

    /**
     * 得到今天的最后结束时间
     * @return 今天的最后时间
     */
    public static Date getTodayEndTime(){
        String endString=DateUtil.yyyyMMddFormat+" 23:59:59";
        DateFormat df=new SimpleDateFormat(DateUtil.fullFormat);
        Date endTime=new Date();
        try {
            endTime=df.parse(endString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return endTime;
    }

    /**
     * 取得本周的开始时间
     * @return 本周的开始时间
     */
    public static Date getThisWeekBeginTime(){
        Date beginTime=null;
        Calendar cal=Calendar.getInstance();
        int week=getWeek(cal.getTime());
        week=week-1;
        int days=0;
        if(week==0){
            days=6;
        }else{
            days=week-1;
        }
        cal.add(Calendar.DAY_OF_MONTH, -days);
        beginTime=cal.getTime();
        return beginTime;
    }

    /**
     * 取得本周的开始日期
     * @param format 时间的格式
     * @return 指定格式的本周最开始时间
     */
    public static String getThisWeekBeginTimeString(String format){
        DateFormat df=new SimpleDateFormat(format);
        return df.format(getThisWeekBeginTime());
    }


    /**
     * 取得本周的结束时间
     * @return 本周的结束时间
     */
    public static Date getThisWeekEndTime(){
        Date endTime=null;
        Calendar cal=Calendar.getInstance();
        int week=getWeek(cal.getTime());
        week=week-1;
        int days=0;
        if(week!=0){
            days=7-week;
        }
        cal.add(Calendar.DAY_OF_MONTH, days);
        endTime=cal.getTime();
        return endTime;
    }


    /**
     * 取得本周的结束日期
     * @param format 时间的格式
     * @return 指定格式的本周结束时间
     */
    public static String getThisWeekEndTimeString(String format){
        DateFormat df=new SimpleDateFormat(format);
        return df.format(getThisWeekEndTime());
    }

    /**
     * 取得两时间相差的天数
     * @param from 第一个时间
     * @param to 第二个时间
     * @return 相差的天数
     */
    public static long getBetweenDays(Date from, Date to){
        long days=0;
        long dayTime=24*60*60*1000;
        long fromTime=from.getTime();
        long toTime=to.getTime();
        long times=Math.abs(fromTime-toTime);
        days=times/dayTime;
        return days;
    }

    /**
     * 取得两时间相差的小时数
     * @param from 第一个时间
     * @param to 第二个时间
     * @return 相差的小时数
     */
    public static long getBetweenHours(Date from,Date to){
        long hours=0;
        long hourTime=60*60*1000;
        long fromTime=from.getTime();
        long toTime=to.getTime();
        long times=Math.abs(fromTime-toTime);
        hours=times/hourTime;
        return hours;
    }

    /**
     * 取得在指定时间上加减days天后的时间
     * @param date 指定的时间
     * @param days 天数,正为加，负为减
     * @return 在指定时间上加减days天后的时间
     */
    public static Date addDays(Date date,int days){
        Date time=null;
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, days);
        time=cal.getTime();
        return time;
    }

    /**
     * 取得在指定时间上加减months月后的时间
     * @param date 指定时间
     * @param months 月数，正为加，负为减
     * @return 在指定时间上加减months月后的时间
     */
    public static Date addMonths(Date date,int months){
        Date time=null;
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.MONTH, months);
        time=cal.getTime();
        return time;
    }

    /**
     * 取得在指定时间上加减years年后的时间
     * @param date 指定时间
     * @param years 年数，正为加，负为减
     * @return 在指定时间上加减years年后的时间
     */
    public static Date addYears(Date date,int years){
        Date time=null;
        Calendar cal=Calendar.getInstance();
        cal.add(Calendar.YEAR, years);
        time=cal.getTime();
        return time;
    }
    
    /**
     * 根据月日获取星座
     * 
     * @param month
     *            月
     * @param day
     *            日
     * @return
     */
    public static String getConstellation(int month, int day) {
        /**
         * 12.22-1.19 摩羯   0
         * 1.20-2.18 水瓶 1
         * 2.19-3.20 双鱼 2
         * 3.21-4.19 白羊 3 
         * 4.20-5.20 金牛 4
         * 5.21-6.21 双子 5 
         * 6.22-7.22 巨蟹座 6
         * 7.23-8.22 狮子座 7
         * 8.23-9.22 处女座 8
         * 9.23-10.23 天秤座 9
         * 10.24-11.22 天蝎座 10
         * 11.23-12.21 射手 11
         */
        String[] constellationArr = { "摩羯座","水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座",
                "天秤座", "天蝎座", "射手座", "摩羯座" };
        //星座改变节点数组
        int[] constellationChangeLastDay = { 19, 18, 20, 19, 20, 21, 22, 22, 22, 23, 22, 21 };
        /*
         * day <= cCLD[month-1] -->  cons = const[month-1]
         * day > cCLD[month-1] --> cons = const[month] 摩羯座 month 12 不能取 month[12] 所以添加一个month [12] 摩羯
         * 
         * 6<
         */
        
    	if(!(month>0 && day > 0)){
    		return constellationArr[0];//默认摩羯
    	}
        if (day <= constellationChangeLastDay[month-1]) {
        	
            return constellationArr[month-1];
        }        
        // 默认返回摩羯座 因为我是12月28
        return constellationArr[month];
    }
    
    /**
     * 根据日期返回年龄
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static int getAgeByDate(int year, int month, int day){
    	int age = 0;
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(new Date());//当前时间
    	age = (calendar.get(Calendar.YEAR))-year;    	
    	int now_month = calendar.get(Calendar.MONTH)+1;
    	//已经过了生日 年龄加1 
    	if(now_month > month){
    		age = age + 1;
    	}else if(now_month == month && calendar.get(Calendar.DAY_OF_MONTH)> day ){
    		age = age + 1;
    	}
    	return age;
    }

}