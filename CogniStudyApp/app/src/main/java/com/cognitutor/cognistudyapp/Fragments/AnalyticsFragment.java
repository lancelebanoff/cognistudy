package com.cognitutor.cognistudyapp.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.DateUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Student;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentBlockStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentCategoryRollingStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentSubjectMonthStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentSubjectRollingStats;
import com.cognitutor.cognistudyapp.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.parse.ParseObject;
import com.rey.material.widget.Spinner;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Lance on 12/27/2015.
 */
public class AnalyticsFragment extends CogniFragment {

    private Spinner mSpSubjects;
    private Spinner mSpDateRange;
    private PieChart mPieChart;
    private HorizontalBarChart mHorizBarChart;
    private BarChart mDoubleBarChart;

    public static final AnalyticsFragment newInstance() {
        return new AnalyticsFragment();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeSpinners();
        mPieChart = (PieChart) getView().findViewById(R.id.pieChart);
        mHorizBarChart = (HorizontalBarChart) getView().findViewById(R.id.horizontalBarChart);
        mDoubleBarChart = (BarChart) getView().findViewById(R.id.doubleBarChart);

        displayAnalytics();
    }

    private void initializeSpinners() {
        mSpSubjects = (Spinner) getView().findViewById(R.id.spSubjects);
        mSpDateRange = (Spinner) getView().findViewById(R.id.spDateRange);

        String[] subjects = Constants.Subject.getSubjects();
        ArrayAdapter<String> subjectsAdapter =
                new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, subjects);
        subjectsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpSubjects.setAdapter(subjectsAdapter);

        String[] dateRanges = Constants.Analytics.RollingDateRange.getRollingStatsTypes();
        ArrayAdapter<String> dateRangesAdapter =
                new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, dateRanges);
        dateRangesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpDateRange.setAdapter(dateRangesAdapter);

        Spinner.OnItemSelectedListener listener = new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(Spinner parent, View view, int position, long id) {
                displayAnalytics();
            }
        };
        mSpSubjects.setOnItemSelectedListener(listener);
        mSpSubjects.setAnimation(null);
        mSpDateRange.setOnItemSelectedListener(listener);
        mSpDateRange.setAnimation(null);
    }

    private void displayAnalytics() {
        String subject = mSpSubjects.getAdapter().getItem(mSpSubjects.getSelectedItemPosition()).toString();
        String rollingStatsType = mSpDateRange.getAdapter().getItem(mSpDateRange.getSelectedItemPosition()).toString();

        getAnalytics(subject, rollingStatsType).continueWith(new Continuation<AnalyticsData, Void>() {
            @Override
            public Void then(Task<AnalyticsData> task) throws Exception {
                final AnalyticsData analyticsData = task.getResult();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        drawPieChart(analyticsData);
                        drawBarChart(analyticsData);
                        drawDoubleBarChart(analyticsData);
                    }
                });

                return null;
            }
        });
    }

    private Task<AnalyticsData> getAnalytics(final String subject, final String rollingDateRange) {
        if (!subject.equals(Constants.Analytics.OVERALL)) {
            Task<AnalyticsData> task = Student.getStudentInBackground().continueWith(new Continuation<Student, AnalyticsData>() {
                @Override
                public AnalyticsData then(Task<Student> task) throws Exception {
                    Student student = task.getResult();
                    String baseUserId = student.getBaseUserId();

                    // Pie chart values
                    StudentSubjectRollingStats subjectRollingStats =
                            StudentSubjectRollingStats.findBySubjectFromCache(subject, baseUserId);
                    String pieLabel = subject;
                    int[] pieValues = new int[]{
                            subjectRollingStats.getCorrectForRollingStatsType(rollingDateRange),
                            subjectRollingStats.getTotalForRollingStatsType(rollingDateRange)
                    };

                    // Bar chart values
                    String[] barLabels = Constants.SubjectToCategory.get(subject);
                    int[][] barValues = new int[barLabels.length][2];
                    List<StudentCategoryRollingStats> categoryRollingStatsList = student.getStudentCategoryRollingStats();
                    for (int i = 0; i < barLabels.length; i++) {
                        String barLabel = barLabels[i];
                        StudentCategoryRollingStats categoryRollingStats = findStatsObjectByLabel(
                                categoryRollingStatsList, StudentCategoryRollingStats.Columns.category, barLabel);
                        barValues[i][0] = categoryRollingStats.getCorrectAllTime();
                        barValues[i][1] = categoryRollingStats.getTotalAllTime();
                    }

                    // Double bar chart
                    String blockType = Constants.Analytics.RollingDateRangeToBlockType.get(rollingDateRange);
                    int numBlocks = Constants.Analytics.RollingDateRangeToNumSmallerBlocks.get(rollingDateRange);
                    int maxBlockNum = DateUtils.getCurrentMonthBlockNum();
                    int minBlockNum = maxBlockNum - numBlocks + 1;

                    // Double bar chart labels
                    String[] doubleBarLabels = getDoubleBarLabels(blockType, numBlocks);

                    // Double bar chart values
                    int[][] doubleBarValues = new int[numBlocks][2];
                    for (int blockNum = minBlockNum, barIndex = 0; blockNum <= maxBlockNum; blockNum++, barIndex++) {
                        StudentSubjectMonthStats subjectMonthStats = StudentBlockStats.getStudentBlockStatsByBlockNum(
                                StudentSubjectMonthStats.class, baseUserId, blockNum);
                        if (subjectMonthStats != null) {
                            doubleBarValues[barIndex][0] = subjectMonthStats.getCorrect();
                            doubleBarValues[barIndex][1] = subjectMonthStats.getTotal();
                        } else {
                            doubleBarValues[barIndex][0] = 0;
                            doubleBarValues[barIndex][1] = 0;
                        }
                    }

                    AnalyticsData analyticsData = new AnalyticsData(
                            pieLabel, pieValues, barLabels, barValues, doubleBarLabels, doubleBarValues
                    );

                    return analyticsData;
                }
            });
            return task;
        }
        return null;
    }

    private String[] getDoubleBarLabels(String blockType, int numBlocks) {
        String[] doubleBarLabels = new String[numBlocks];

        Calendar calendarDate = Calendar.getInstance();
        calendarDate.setTime(new Date());
        calendarDate.set(Calendar.HOUR_OF_DAY, 0);
        calendarDate.set(Calendar.MINUTE, 0);
        calendarDate.set(Calendar.SECOND, 0);
        calendarDate.set(Calendar.MILLISECOND, 0);

        switch (blockType) {
            case Constants.Analytics.BlockType.MONTH:
                calendarDate.set(Calendar.DAY_OF_MONTH, calendarDate.getActualMinimum(Calendar.DAY_OF_MONTH));
                for (int barIndex = numBlocks - 1; barIndex >= 0; barIndex--) {
                    doubleBarLabels[barIndex] = DateUtils.getFormattedMonthDate(calendarDate.getTime());
                    calendarDate.add(Calendar.MONTH, -1);
                }
                return doubleBarLabels;
            case Constants.Analytics.BlockType.TRIDAY:
                DateTime startDate = new DateTime(2016, 1, 1, 0, 0, 0);
                DateTime today = new DateTime();
                Days daysBetween = Days.daysBetween(startDate, today);
                int numDaysBetween = daysBetween.getDays();
                int numDaysAfterStartOfTriday = numDaysBetween % 3;
                calendarDate.add(Calendar.DATE, -numDaysAfterStartOfTriday);
                for (int barIndex = numBlocks - 1; barIndex >= 0; barIndex--) {
                    doubleBarLabels[barIndex] = DateUtils.getFormattedMonthDate(calendarDate.getTime());
                    calendarDate.add(Calendar.DATE, -3);
                }
                return doubleBarLabels;
            case Constants.Analytics.BlockType.DAY:
                for (int barIndex = numBlocks - 1; barIndex >= 0; barIndex--) {
                    doubleBarLabels[barIndex] = DateUtils.getFormattedMonthDate(calendarDate.getTime());
                    calendarDate.add(Calendar.DATE, -1);
                }
                return doubleBarLabels;
        }

        return doubleBarLabels;
    }

    private <T extends ParseObject> T findStatsObjectByLabel(List<T> statsList, String key, String value) {
        for (T statsObject : statsList) {
            if (statsObject.get(key).equals(value)) {
                return statsObject;
            }
        }
        return null;
    }

    private void drawPieChart(AnalyticsData analyticsData) {
        mPieChart.setUsePercentValues(true);
        mPieChart.setDescription("");
        mPieChart.setExtraOffsets(5, 10, 5, 5);

        mPieChart.setDragDecelerationFrictionCoef(0.95f);

        mPieChart.setCenterText(generateCenterSpannableText(analyticsData));

        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleColorTransparent(true);

        mPieChart.setTransparentCircleColor(Color.WHITE);
        mPieChart.setTransparentCircleAlpha(110);

        mPieChart.setHoleRadius(58f);
        mPieChart.setTransparentCircleRadius(61f);

        mPieChart.setDrawCenterText(true);
        mPieChart.setDrawSliceText(false);

        mPieChart.setRotationAngle(270);
//        mPieChart.setTouchEnabled(false);

        setPieChartData(analyticsData);

        mPieChart.animateY(1500, Easing.EasingOption.EaseInOutQuad);
        mPieChart.getLegend().setEnabled(false);
    }

    private void setPieChartData(AnalyticsData analyticsData) {
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        for (int i = 0; i < analyticsData.pieCorrectAndTotalValues.length; i++) {
            yVals1.add(new Entry((float) analyticsData.pieCorrectAndTotalValues[i], i));
        }

        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("Incorrect" /* Incorrect */);
        xVals.add("Correct" /* Correct */);

        PieDataSet dataSet = new PieDataSet(yVals1, "" /* title */);

        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(Color.rgb(255, 50, 50));
        colors.add(Color.rgb(0, 200, 0));
        dataSet.setColors(colors);

        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        mPieChart.setData(data);

        // undo all highlights
        mPieChart.highlightValues(null);

        mPieChart.invalidate();
    }

    private SpannableString generateCenterSpannableText(AnalyticsData analyticsData) {

        SpannableString s = new SpannableString(analyticsData.pieLabel);
        s.setSpan(new RelativeSizeSpan(1.7f), 0, s.length(), 0);
        return s;
    }

    private void drawBarChart(AnalyticsData analyticsData) {
        mHorizBarChart.setDrawBarShadow(false);
        mHorizBarChart.setDrawValueAboveBar(true);
        mHorizBarChart.setDescription("");
        mHorizBarChart.setDrawGridBackground(false);
        mHorizBarChart.getLegend().setEnabled(false);
        mHorizBarChart.setTouchEnabled(false);

        XAxis xl = mHorizBarChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setDrawAxisLine(false);
        xl.setDrawGridLines(false);
        xl.setGridLineWidth(0.3f);

        YAxis yl = mHorizBarChart.getAxisLeft();
        yl.setDrawAxisLine(false);
        yl.setDrawGridLines(false);
        yl.setGridLineWidth(0.3f);
        yl.setAxisMaxValue(110);
        yl.setEnabled(false);

        YAxis yr = mHorizBarChart.getAxisRight();
        yr.setDrawAxisLine(false);
        yr.setDrawGridLines(false);
        yr.setEnabled(false);

        setBarChartData(analyticsData);
        mHorizBarChart.animateY(1500);
    }

    private void setBarChartData(AnalyticsData analyticsData) {

        ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();
        ArrayList<String> xVals = new ArrayList<String>();

        // Category labels
        for (String barLabel : analyticsData.barLabels) {
            xVals.add(barLabel);
        }

        // Category correct and incorrect values
        for (int i = 0; i < analyticsData.barCorrectAndTotalValues.length; i++) {
            int percentCorrect = (int) ((double) analyticsData.barCorrectAndTotalValues[i][0]
                    / analyticsData.barCorrectAndTotalValues[i][1] * 100);
            yVals.add(new BarEntry((float) percentCorrect, i));
        }

        BarDataSet set1 = new BarDataSet(yVals, "DataSet 1");
        set1.setColor(Color.rgb(150, 200, 255));
        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);
        BarData data = new BarData(xVals, dataSets);
        data.setValueTextSize(10f);

        mHorizBarChart.setData(data);
    }

    private void drawDoubleBarChart(AnalyticsData analyticsData) {
        mDoubleBarChart.setDrawBarShadow(false);
        mDoubleBarChart.setDrawValueAboveBar(true);
        mDoubleBarChart.setDescription("");
        mDoubleBarChart.setDrawGridBackground(false);
        mDoubleBarChart.getLegend().setEnabled(false);
        mDoubleBarChart.setTouchEnabled(false);

        XAxis xl = mDoubleBarChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setDrawAxisLine(false);
        xl.setDrawGridLines(false);
        xl.setGridLineWidth(0.3f);

        YAxis yl = mDoubleBarChart.getAxisLeft();
        yl.setDrawAxisLine(false);
        yl.setDrawGridLines(false);
        yl.setGridLineWidth(0.3f);
        yl.setEnabled(false);

        YAxis yr = mDoubleBarChart.getAxisRight();
        yr.setDrawAxisLine(false);
        yr.setDrawGridLines(false);
        yr.setEnabled(false);

        setDoubleBarChartData(analyticsData);
        mDoubleBarChart.animateY(1500);
    }

    private void setDoubleBarChartData(AnalyticsData analyticsData) {

        ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();
        ArrayList<String> xVals = new ArrayList<String>();

        for (String barLabel : analyticsData.doubleBarLabels) {
            xVals.add(barLabel);
        }

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();

        for (int i = 0; i < analyticsData.doubleBarCorrectAndTotalValues.length; i++) {
            int[] barValues = analyticsData.doubleBarCorrectAndTotalValues[i];
            int correct = barValues[0];
            int incorrect = barValues[1] - barValues[0];
            yVals.add(new BarEntry(new float[]{correct, incorrect}, i));
        }

        BarDataSet set1 = new BarDataSet(yVals, "Data Set 1");

        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(Color.rgb(150, 220, 150));
        colors.add(Color.rgb(255, 150, 150));
        set1.setColors(colors);

        set1.setStackLabels(new String[] {"Correct", "Incorrect"});
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);
        data.setValueTextSize(10f);

        mDoubleBarChart.setData(data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_analytics, container, false);
        return rootView;
    }

    private class AnalyticsData {
        String pieLabel;
        int[] pieCorrectAndTotalValues;
        String[] barLabels;
        int[][] barCorrectAndTotalValues;
        String[] doubleBarLabels;
        int[][] doubleBarCorrectAndTotalValues;

        public AnalyticsData(String pieLabel, int[] pieCorrectAndTotalValues, String[] barLabels, int[][] barCorrectAndTotalValues, String[] doubleBarLabels, int[][] doubleBarCorrectAndTotalValues) {
            this.pieLabel = pieLabel;
            this.pieCorrectAndTotalValues = pieCorrectAndTotalValues;
            this.barLabels = barLabels;
            this.barCorrectAndTotalValues = barCorrectAndTotalValues;
            this.doubleBarLabels = doubleBarLabels;
            this.doubleBarCorrectAndTotalValues = doubleBarCorrectAndTotalValues;
        }
    }
}
