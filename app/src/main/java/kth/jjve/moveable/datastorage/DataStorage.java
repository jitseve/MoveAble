package kth.jjve.moveable.datastorage;
/*
Class to store the acquired data in two lists
List one has a fixed length, to display in the graph (bluetooth)
List two saves all the data (length is growing).
 */

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

    List<Long> timeData;
    List<Float> ewmaData;
    List<Float> complimentaryData;

    public DataStorage() {
        // Initialise the class
        xDataGraph = new ArrayList<>(Collections.nCopies(100, 0));
        yDataGraph = new ArrayList<>(Collections.nCopies(100, (float) 0));

        xData = new ArrayList<>();
        yData = new ArrayList<>();

        timeData = new ArrayList<>();
        ewmaData = new ArrayList<>();
        complimentaryData = new ArrayList<>();
    }

    public void writeData(int x, float y) {
        // Method to write the data into the lists
        if (firstRun) {
            firstX = x; // save the first timestamp
            firstRun = false;
        }
        xDataGraph.add(x - firstX);      // add x to the end of the list
        xDataGraph.remove(0);      // remove first item of the list
        yDataGraph.add(y);
        yDataGraph.remove(0);

        xData.add(x - firstX);
        yData.add(y);
    }

    public void writeDataForCSV(long time, float ewma, float complimentary) {
        timeData.add(time);
        ewmaData.add(ewma);
        complimentaryData.add(complimentary);
    }

    public void writeCSV(){
        String filename = "data.csv";
        File directoryDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File logDir = new File (directoryDownload, "data"); //Creates a new folder in DOWNLOAD directory
        logDir.mkdirs();
        File file = new File(logDir, filename);

        FileOutputStream outputStream = null;
        try {
            try {
                outputStream = new FileOutputStream(file, true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < timeData.size(); i += 3) {
                outputStream.write((timeData.get(i) + ",").getBytes());
                outputStream.write((ewmaData.get(i + 1) + ",").getBytes());
                outputStream.write((complimentaryData.get(i + 2) + "\n").getBytes());
            }
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public Integer getRunningTime(){
        return xDataGraph.get(99);
    }
}

