package com.cognitutor.cognistudyapp.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import java.util.ArrayList;

/**
 * Created by Lance on 12/27/2015.
 */
public class AnalyticsFragment extends CogniFragment {

    private PieChart mPieChart;
    private HorizontalBarChart mHorizBarChart;
    private BarChart mDoubleBarChart;

    public AnalyticsFragment() {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        drawPieChart();
        drawBarChart();
        drawDoubleBarChart();
    }

    private void drawPieChart() {
        mPieChart = (PieChart) getView().findViewById(R.id.pieChart);

        mPieChart.setUsePercentValues(true);
        mPieChart.setDescription("");
        mPieChart.setExtraOffsets(5, 10, 5, 5);

        mPieChart.setDragDecelerationFrictionCoef(0.95f);

        mPieChart.setCenterText(generateCenterSpannableText());

        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleColorTransparent(true);

        mPieChart.setTransparentCircleColor(Color.WHITE);
        mPieChart.setTransparentCircleAlpha(110);

        mPieChart.setHoleRadius(58f);
        mPieChart.setTransparentCircleRadius(61f);

        mPieChart.setDrawCenterText(true);

        mPieChart.setRotationAngle(270);
        // enable rotation of the chart by touch
        mPieChart.setRotationEnabled(true);
        mPieChart.setHighlightPerTapEnabled(true);

        setPieChartData(2);

        mPieChart.animateY(1500, Easing.EasingOption.EaseInOutQuad);
        mPieChart.getLegend().setEnabled(false);
    }

    private void setPieChartData(int count) {
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        for (int i = 0; i < count; i++) {
            yVals1.add(new Entry((float) (i+1), i));
        }

        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("Incorrect");
        xVals.add("Correct");

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

    private SpannableString generateCenterSpannableText() {

        SpannableString s = new SpannableString("Math");
        s.setSpan(new RelativeSizeSpan(1.7f), 0, s.length(), 0);
        return s;
    }

    private void drawBarChart() {
        mHorizBarChart = (HorizontalBarChart) getView().findViewById(R.id.horizontalBarChart);
        mHorizBarChart.setDrawBarShadow(false);
        mHorizBarChart.setDrawValueAboveBar(true);
        mHorizBarChart.setDescription("");
        mHorizBarChart.setDrawGridBackground(false);
        mHorizBarChart.getLegend().setEnabled(false);

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

        setBarChartData();
        mHorizBarChart.animateY(1500);
    }

    private void setBarChartData() {

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        ArrayList<String> xVals = new ArrayList<String>();

        xVals.add("Pre-Algebra");
        xVals.add("Algebra");
        xVals.add("Geometry");
//        xVals.add("Trigonometry");
//        xVals.add("Data Analysis");

        for (int i = 0; i < 3; i++) {
            yVals1.add(new BarEntry((float) ((i+1)*20), i));
        }

        BarDataSet set1 = new BarDataSet(yVals1, "DataSet 1");
        set1.setColor(Color.rgb(150, 200, 255));
        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);
        BarData data = new BarData(xVals, dataSets);
        data.setValueTextSize(10f);

        mHorizBarChart.setData(data);
    }

    private void drawDoubleBarChart() {
        mDoubleBarChart = (BarChart) getView().findViewById(R.id.doubleBarChart);
        mDoubleBarChart.setDrawBarShadow(false);
        mDoubleBarChart.setDrawValueAboveBar(true);
        mDoubleBarChart.setDescription("");
        mDoubleBarChart.setDrawGridBackground(false);
        mDoubleBarChart.getLegend().setEnabled(false);

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

        setDoubleBarChartData();
        mDoubleBarChart.animateY(1500);
    }

    private void setDoubleBarChartData() {

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        ArrayList<String> xVals = new ArrayList<String>();

        xVals.add("2/1");
        xVals.add("2/2");
        xVals.add("2/3");
        xVals.add("2/4");
        xVals.add("2/5");
        xVals.add("2/6");
        xVals.add("2/7");

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();

        for(int d = 0; d < 7; d++) {
            float value1 = (d+1)*10;
            float value2 = (d+1)*20;
            yVals1.add(new BarEntry(new float[] {value1, value2}, d));
        }

        BarDataSet set1 = new BarDataSet(yVals1, "Math");

        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(Color.rgb(255, 150, 150));
        colors.add(Color.rgb(150, 220, 150));
        set1.setColors(colors);

        set1.setStackLabels(new String[] {"Incorrect", "Correct"});
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
}
