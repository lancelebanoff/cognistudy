package com.cognitutor.cognistudyapp.Custom;

import android.util.Log;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentBlockStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentSubjectBlockStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentTRollingStats;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by Kevin on 2/16/2016.
 */
public class DateUtils {

    static SimpleDateFormat dayOfYearFormatter;
    static SimpleDateFormat dayInMonthFormatter;
    static SimpleDateFormat monthFormatter;
    static SimpleDateFormat monthLetterFormatter;
    static SimpleDateFormat yearFormatter;
    static TimeZone ny = TimeZone.getTimeZone("America/New_York");
    static {

        dayOfYearFormatter = new SimpleDateFormat("D", Locale.US);
        dayOfYearFormatter.setTimeZone(ny);

        dayInMonthFormatter = new SimpleDateFormat("d", Locale.US);
        dayInMonthFormatter.setTimeZone(ny);

        monthFormatter = new SimpleDateFormat("M", Locale.US);
        monthFormatter.setTimeZone(ny);

        monthLetterFormatter = new SimpleDateFormat("MMM", Locale.US);
        monthLetterFormatter.setTimeZone(ny);

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

    public static String getFormattedMonthDate(Date date) {
        return monthFormatter.format(date) + "/" + dayInMonthFormatter.format(date);
    }

    public static String getFormattedMonthLetter(Date date) {
        return monthLetterFormatter.format(date);
    }

    private static int getDayOfYear(Date date) {
        return Integer.parseInt(dayOfYearFormatter.format(date)) - 1; //TODO: Remove -1?
    }

    private static int getCurrentDayOfYear() {
        return getDayOfYear(new Date());
    }

    private static int getDayOfMonth(Date date) {
        return Integer.parseInt(dayInMonthFormatter.format(date));
    }

    private static int getCurrentDayOfMonth() {
        return getDayOfMonth(new Date());
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

    public enum BlockType {
        DAY, TRIDAY, MONTH
    }

    public static void generateRandomStats(BlockType blockType) {
        final String TAG = "randomStats";
        Random rand = new Random();

        float chanceCorrect = 50;
        Calendar calendar = Calendar.getInstance(ny, Locale.US);
        final int N, calendarField, amountToAdd, chancePlayed, chanceMoreThan10Ques;
        if(blockType == BlockType.MONTH) {
            calendarField = Calendar.MONTH;
            amountToAdd = 1;
            chancePlayed = 100;
            chanceMoreThan10Ques = 100;
            N = 12;
        }
        else  {
            calendarField = Calendar.DAY_OF_YEAR;
            N = 10;
            if(blockType == BlockType.TRIDAY) {
                chancePlayed = 90;
                amountToAdd = 3;
                chanceMoreThan10Ques = 95;
            }
            else { //blockType == RollingDateRange.DAY
                chancePlayed = 80;
                amountToAdd = 1;
                chanceMoreThan10Ques = 50;
            }
        }
        for(int n = 0; n < N; n++) {
            Date date = calendar.getTime();
            int[] blockNums = getDayTridayMonthBlockNums(date);
            boolean playedToday = didItHappen(rand, chancePlayed);
            if(!playedToday) {
                Log.d(TAG + " numQues", "=== Skipping " + blockNumsToString(blockNums));
                calendar.add(calendarField, amountToAdd);
                continue;
            }
            int numAnswered;
            boolean answeredMoreThan10Questions = didItHappen(rand, chanceMoreThan10Ques);
            if(!answeredMoreThan10Questions)
                numAnswered = rand.nextInt(10) + 1;
            else
                numAnswered = rand.nextInt(20) + 11;
            Log.d(TAG + " numQues", "=== Answered " + numAnswered + " questions on " + blockNumsToString(blockNums));
            for(int i=0; i<numAnswered; i++) {
                chanceCorrect = answerRandomQuestion(date, chanceCorrect, rand, TAG);
            }
            calendar.add(calendarField, amountToAdd);
        }
    }

    private static float answerRandomQuestion(Date date, float chanceCorrect, Random rand, String TAG) {

//        float improvementFactor = (float) 1.002;
        float improvementFactor = (float) 1.005;
//        float improvementFactor = (float) 1.05;

        int[] blockNums = getDayTridayMonthBlockNums(date);
        String category = Constants.getRandomConstant(Constants.Category.class);
        String subject = StudentSubjectBlockStats.getSubjectFromCategory(category);
        boolean correct = didItHappen(rand, chanceCorrect);

        String randomId = getRandomQuestionId();

        Log.d(TAG + " quesAns", blockNumsToString(blockNums) + " | " + questionToString(subject, category, correct, randomId));

        StudentBlockStats.setTestDate(date);
        try {
            StudentTRollingStats.incrementAllInBackground(randomId, category, correct);
            StudentBlockStats.incrementAll(category, correct)
                    .waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return chanceCorrect * improvementFactor;
    }

    private static String blockNumsToString(int[] blockNums) {
        return "Day: " + blockNums[0] + " | Triday: " + blockNums[1] + " | Month: " + blockNums[2];
    }

    private static String questionToString(String subject, String category, boolean correct, String randomId) {
        return "Subject: " + subject + " | Category: " + category + " | Correct: " + correct + " | QuestionId: " + randomId;
    }

    private static String getRandomQuestionId() {
        return UUID.randomUUID().toString().replace('-', 'd').substring(0,10);
    }

    private static int[] getDayTridayMonthBlockNums(Date date) {
        int[] blockNums = new int[3];
        blockNums[0] = getDayBlockNum(date);
        blockNums[1]  = getTridayBlockNum(date);
        blockNums[2] = getMonthBlockNum(date);
        return blockNums;
    }

    private static boolean didItHappen(Random rand, float percent) {
        int num = rand.nextInt(100) + 1;
        if(num > Math.min(Math.round(percent), 100))
            return false;
        return true;
    }

    public static void test(boolean randomDate) {

        Random rand = new Random();

        for(int i=0; i<1; i++) {
//            int year = rand.nextInt(1) + 2016;
            int year = 2016;
//            int month = rand.nextInt(12) + 1;
            int month = 1;
            int day = getRandomDayInMonth(month, year);
            String dateString = String.valueOf(year) + "." + String.format("%02d", month) + "." + String.format("%02d", day);
            String category = Constants.getRandomConstant(Constants.Category.class);
            String subject = StudentSubjectBlockStats.getSubjectFromCategory(category);
            boolean correct = rand.nextBoolean();

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy.MM.dd");
            Date date = null;

            if(randomDate) {
                try {
                    date = inputFormat.parse(dateString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            else {
                date = new Date();
            }

            int dayBlockNum = getDayBlockNum(date);
            int tridayBlockNum = getTridayBlockNum(date);
            int monthBlockNum = getMonthBlockNum(date);

            String randomId = UUID.randomUUID().toString().replace('-', 'd').substring(0,10);

            Log.d("DateTest", "Day: " + dayBlockNum + " | Triday: " + tridayBlockNum + " | Month: " + monthBlockNum
                    + " | Subject: " + subject + " | Category: " + category + " | Correct: " + correct);

            StudentBlockStats.setTestDate(date);
//            StudentBlockStats.incrementAll(category, correct);
            try {
                StudentTRollingStats.incrementAllInBackground(randomId, category, correct);
                StudentBlockStats.incrementAll(category, correct)
                    .waitForCompletion();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static int getRandomDayInMonth(int month, int year) {
        Random rand = new Random();
        switch (month) {
            case GregorianCalendar.JANUARY:
            case GregorianCalendar.MARCH:
            case GregorianCalendar.MAY:
            case GregorianCalendar.JULY:
            case GregorianCalendar.AUGUST:
            case GregorianCalendar.OCTOBER:
            case GregorianCalendar.DECEMBER: {
                return rand.nextInt(31) + 1;
            }
            case GregorianCalendar.APRIL:
            case GregorianCalendar.JUNE:
            case GregorianCalendar.SEPTEMBER:
            case GregorianCalendar.NOVEMBER: {
                return rand.nextInt(30) + 1;
            }
            case GregorianCalendar.FEBRUARY: {
                if(year % 4 == 0) {
                    return rand.nextInt(29) + 1;
                }
                else {
                    return rand.nextInt(28) + 1;
                }
            }
        }
        return 1;
    }
}
