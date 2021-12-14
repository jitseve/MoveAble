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
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class DisplayGraph {

    public static void displayTheGraph(List<Long> xData, List<Float> yData1,
                                       List<Float> yData2, List<Float> yData3,
                                       String name, LineChart lc){

        List<Entry> entries1 = new ArrayList<>();
        for (int i=0; i<xData.size(); i++){
            float x = xData.get(i);
            float y = yData1.get(i);
            entries1.add(new Entry(x, y));
        }

        List<Entry> entries2 = new ArrayList<>();
        for (int i=0; i<xData.size(); i++){
            float x = xData.get(i);
            float y = yData2.get(i);
            entries2.add(new Entry(x, y));
        }

        List<Entry> entries3 = new ArrayList<>();
        for (int i=0; i<xData.size(); i++){
            float x = xData.get(i);
            float y = yData3.get(i);
            entries3.add(new Entry(x, y));
        }

        LineDataSet dataSet1 = new LineDataSet(entries1, name + " X");
        dataSet1.setColor(Color.BLUE);
        dataSet1.setDrawCircles(false);

        LineDataSet dataSet2 = new LineDataSet(entries2, name + " Y");
        dataSet2.setColor(Color.RED);
        dataSet2.setDrawCircles(false);

        LineDataSet dataSet3 = new LineDataSet(entries3, name + " Z");
        dataSet3.setColor(Color.GREEN);
        dataSet3.setDrawCircles(false);

        Description description = new Description();
        description.setText(name + " is being displayed");

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet1);
        dataSets.add(dataSet2);
        dataSets.add(dataSet3);

        LineData data = new LineData(dataSets);

        XAxis xAxis = lc.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        lc.setData(data);
        lc.setDescription(description); // Sets a description in the right bottom corner
        lc.setDrawGridBackground(true);
        lc.invalidate(); //Refreshes the graph
        lc.notifyDataSetChanged(); //Needed when data is added dynamically (as it will be)

    }
}
