package kth.jjve.moveable.datastorage;
/*
Class to store the acquired data in two lists
List one has a fixed length, to display in the graph (bluetooth)
List two saves all the data (length is growing).
 */

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataStorage {
    List<Long> xDataGraph;
    List<Long> xData;
    List<Float> yDataGraph;
    List<Float> yData;
    long firstX;
    long currentTime;
    boolean firstRun = true;

    List<Long> timeData;
    List<Float> ewmaData;
    List<Float> complimentaryData;

    public DataStorage() {
        // Initialise the class
        xDataGraph = new ArrayList<>(Collections.nCopies(100, (long) 0));
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
            firstX = (long) x; // save the first timestamp
            firstRun = false;
        }
        currentTime = x - firstX;
        xDataGraph.add(currentTime);      // add x to the end of the list
        xDataGraph.remove(0);      // remove first item of the list
        yDataGraph.add(y);
        yDataGraph.remove(0);

        xData.add(x - firstX);
        yData.add(y);
    }

    public void writeDataForCSV(long time, float ewma, float complimentary) {
        if (firstRun){
            firstX = time;
            firstRun = false;
        }
        currentTime = time - firstX;
        timeData.add(currentTime);
        ewmaData.add(ewma);
        complimentaryData.add(complimentary);
    }

    public void writeCSV(String filename, boolean bluetoothconnected, String command_fragment){
        File directoryDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File logDir = new File (directoryDownload, "data"); //Creates a new folder in DOWNLOAD directory
        logDir.mkdirs();
        File file = new File(logDir, filename);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file, false);
            if (bluetoothconnected){
                if (command_fragment.equals("/Meas/Acc") || command_fragment.equals("/Meas/Gyro")){
                    for (int i = 0; i < xData.size(); i++) {
                        outputStream.write((xData.get(i) + ",").getBytes());
                        outputStream.write((yData.get(i) + "\n").getBytes());}
                } else{
                    for (int i = 0; i < timeData.size(); i++) {
                        outputStream.write((timeData.get(i) + ",").getBytes());
                        outputStream.write((ewmaData.get(i) + ",").getBytes());
                        outputStream.write((complimentaryData.get(i) + "\n").getBytes());
                    }
                }

            }else{
                for (int i = 0; i < timeData.size(); i++) {
                    outputStream.write((timeData.get(i) + ",").getBytes());
                    outputStream.write((ewmaData.get(i) + ",").getBytes());
                    outputStream.write((complimentaryData.get(i) + "\n").getBytes());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Write CSV", "something went wrong" + e);
        }
    }

    public List<Long> getXGraphdata() {
        return xDataGraph;
    }

    public List<Float> getYGraphdata() {
        return yDataGraph;
    }

    public List<Long> getXData() {
        return xData;
    }

    public List<Float> getYData(){return yData;}

    public Long getRunningTime(){
        return currentTime;
    }
}

