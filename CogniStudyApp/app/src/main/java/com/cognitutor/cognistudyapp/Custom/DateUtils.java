package com.cognitutor.cognistudyapp.Custom;

import android.util.Log;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentBlockStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentSubjectBlockStats;
import com.parse.ParseObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

/**
 * Created by Kevin on 2/16/2016.
 */
public class DateUtils {

    static SimpleDateFormat dayOfYearFormatter;
    static SimpleDateFormat monthFormatter;
    static SimpleDateFormat yearFormatter;
    static {
        TimeZone ny = TimeZone.getTimeZone("America/New_York");

        dayOfYearFormatter = new SimpleDateFormat("DDD", Locale.US);
        dayOfYearFormatter.setTimeZone(ny);

        monthFormatter = new SimpleDateFormat("MM", Locale.US);
        monthFormatter.setTimeZone(ny);

        yearFormatter = new SimpleDateFormat("yyyy", Locale.US);
        yearFormatter.setTimeZone(ny);
    }

//    private static int getDayBlockNum(Date date) {
    public static int getDayBlockNum(Date date) {
        int day = getDayOfYear(date); //0 - 364 (or 365 in leap year)
        int theYear = getYear(date);
        for(int year = theYear; year > 2016; year--) {
            day += (year % 4 == 1) ? 366 : 365;
        }
        return day;
    }

    public static int getCurrentDayBlockNum() {
        return getDayBlockNum(new Date());
    }

//    private static int getTridayBlockNum(Date date) {
    public static int getTridayBlockNum(Date date) {
        return getDayBlockNum(date) / 3;
    }

    public static int getCurrentTridayBlockNum() {
        return getTridayBlockNum(new Date());
    }

//    private static int getMonthBlockNum(Date date) {
    public static int getMonthBlockNum(Date date) {
        int month = getMonth(date); //0 - 11
        int yearsSince2016 = getYear(date) - 2016;
        return month + yearsSince2016*12;
    }

    public static int getCurrentMonthBlockNum() {
        return getMonthBlockNum(new Date());
    }

    private static int getDayOfYear(Date date) {
        return Integer.parseInt(dayOfYearFormatter.format(date)) - 1;
    }

    private static int getCurrentDayOfYear() {
        return getDayOfYear(new Date());
    }

    private static int getMonth(Date date) {
        return Integer.parseInt(monthFormatter.format(date)) - 1;
    }

    private static int getCurrentMonth() {
        return getMonth(new Date());
    }

    private static int getYear(Date date) {
        return Integer.parseInt(yearFormatter.format(date));
    }

    private static int getCurrentYear() {
        return getYear(new Date());
    }

    public static void test() {

        Random rand = new Random();

        for(int i=0; i<10; i++) {
//            int year = rand.nextInt(1) + 2016;
            int year = 2016;
//            int month = rand.nextInt(12) + 1;
            int month = 1;
            int day = 0;
            switch (month) {
                case GregorianCalendar.JANUARY:
                case GregorianCalendar.MARCH:
                case GregorianCalendar.MAY:
                case GregorianCalendar.JULY:
                case GregorianCalendar.AUGUST:
                case GregorianCalendar.OCTOBER:
                case GregorianCalendar.DECEMBER: {
                    day = rand.nextInt(31) + 1;
                }
                case GregorianCalendar.APRIL:
                case GregorianCalendar.JUNE:
                case GregorianCalendar.SEPTEMBER:
                case GregorianCalendar.NOVEMBER: {
                    day = rand.nextInt(30) + 1;
                }
                case GregorianCalendar.FEBRUARY: {
                    if(year % 4 == 0) {
                        day = rand.nextInt(29) + 1;
                    }
                    else {
                        day = rand.nextInt(28) + 1;
                    }
                }
            }
            String dateString = String.valueOf(year) + "." + String.format("%02d", month) + "." + String.format("%02d", day);
            String category = Constants.getRandomConstant(Constants.Category.class);
            String subject = StudentSubjectBlockStats.getSubjectFromCategory(category);
            boolean correct = rand.nextBoolean();

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy.MM.dd");
            Date date = null;
            try {
                date = inputFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            int dayBlockNum = getDayBlockNum(date);
            int tridayBlockNum = getTridayBlockNum(date);
            int monthBlockNum = getMonthBlockNum(date);

            Log.d("DateTest", "Day: " + dayBlockNum + " | Triday: " + tridayBlockNum + " | Month: " + monthBlockNum
                    + " | Subject: " + subject + " | Category: " + category + " | Correct: " + correct);

            StudentBlockStats.setTestDate(date);
//            StudentBlockStats.incrementAll(category, correct);
            try {
                StudentBlockStats.incrementAll(category, correct)
                    .waitForCompletion();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
