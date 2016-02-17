package com.cognitutor.cognistudyapp.Custom;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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

    public static int getCurrentDayBlockNum() {
        int day = getCurrentDayOfYear() - 1; //0 - 364 (or 365 in leap year)
        int currentYear = getCurrentYear();
        for(int year = currentYear; year > 2016; year--) {
            day += (year % 4 == 1) ? 366 : 365;
        }
        return day;
    }

    public static int getCurrentTridayBlockNum() {
        return getCurrentDayBlockNum() / 3;
    }

    public static int getCurrentMonthBlockNum() {
        int currentMonth = getCurrentMonth(); //0 - 11
        int yearsSince2016 = getCurrentYear() - 2016;
        return currentMonth + yearsSince2016*12;
    }

    private static int getCurrentDayOfYear() {
        return Integer.parseInt(dayOfYearFormatter.format(new Date()));
    }

    private static int getCurrentMonth() {
        return Integer.parseInt(monthFormatter.format(new Date()));
    }

    private static int getCurrentYear() {
        return Integer.parseInt(yearFormatter.format(new Date()));
    }
}
