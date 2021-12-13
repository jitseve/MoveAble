package kth.jjve.moveable.utilities;
/*
Class to display the linechart on the ui
 */

import android.graphics.Color;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class DisplayGraph {

    public static void displayTheGraph(List<Integer> xData, List<Float> yData, LineChart lc){

        List<Entry> entries = new ArrayList<>();
        for (int i=0; i<xData.size(); i++){
            float x = xData.get(i);
            float y = yData.get(i);
            entries.add(new Entry(x, y));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Acceleration X");
        dataSet.setColor(Color.BLUE);
        dataSet.setDrawCircles(false);

        Description description = new Description();
        description.setText("This is a description of the graph");

        LineData lineData = new LineData(dataSet);

        XAxis xAxis = lc.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        lc.setData(lineData);
        lc.setDescription(description); // Sets a description in the right bottom corner
        lc.setDrawGridBackground(true);
        lc.invalidate(); //Refreshes the graph
        lc.notifyDataSetChanged(); //Needed when data is added dynamically (as it will be)

    }
}
