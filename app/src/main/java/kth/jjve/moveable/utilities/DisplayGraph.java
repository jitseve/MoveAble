package kth.jjve.moveable.utilities;

import android.graphics.Color;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class DisplayGraph {

    public static void displayTheGraph(ArrayList<Double> xData, ArrayList<Double> yData, LineChart lc){

        // This is all example stuff from here
        double x = 0;
        int numDataPoints = 1000;
        List<Entry> entries = new ArrayList<>();
        for(int i=0; i<numDataPoints; i++){
            float sinFunction = Float.parseFloat(String.valueOf(Math.sin(x)));
            entries.add(new Entry((float) x, sinFunction));
            x = x + 0.1;
        }

        LineDataSet dataSet = new LineDataSet(entries, "sin function (trial)");
        dataSet.setColor(Color.BLUE);
        dataSet.setDrawCircles(false);

        Description description = new Description();
        description.setText("This is a description of the graph");

        LineData lineData = new LineData(dataSet);

        XAxis xAxis = lc.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum((float) 6.7);

        lc.setVisibility(View.VISIBLE);
        lc.setData(lineData);
        lc.setDescription(description); // Sets a description in the right bottom corner
        lc.setDrawGridBackground(true);
        lc.invalidate(); //Refreshes the graph
        lc.notifyDataSetChanged(); //Needed when data is added dynamically (as it will be)


        ArrayList<ILineDataSet> lineDataSet = new ArrayList<>();


        lc.setVisibility(View.VISIBLE);

    }
}
