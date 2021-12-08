package kth.jjve.moveable.datastorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataStorage {
    List<Integer> xDataGraph;
    List<Integer> xData;
    List<Float> yDataGraph;
    List<Float> yData;
    Integer firstX;
    boolean firstRun = true;


    public DataStorage() {
        // Class to save the bluetooth data
        xDataGraph = new ArrayList<>(Collections.nCopies(100, 0));
        yDataGraph = new ArrayList<>(Collections.nCopies(100, (float) 0));

        xData = new ArrayList<>();
        yData = new ArrayList<>();
    }

    public void writeData(int x, float y) {
        if (firstRun) {
            firstX = x; // save the first timestamp
            firstRun = false;
        }
        xDataGraph.add(x - firstX);               // add x to the end of the list
        xDataGraph.remove(0);      // remove first item of the list
        yDataGraph.add(y);
        yDataGraph.remove(0);

        xData.add(x - firstX);
        yData.add(y);
    }

    public List<Integer> getXGraphdata() {
        return xDataGraph;
    }

    public List<Float> getYGraphdata() {
        return yDataGraph;
    }

    public List<Integer> getXData() {
        return xData;
    }

    public List<Float> getYData(){return yData;}
}

