package kth.jjve.moveable.datastorage;

import android.util.Log;
import android.widget.Toast;

import java.io.Serializable;

import kth.jjve.moveable.SettingsActivity;

public class Settings implements Serializable {
    private String sFrequency;
    private int sFrequencyInteger;
    private String command;
    private boolean AccOn;
    private boolean GyroOn;

    public Settings(String frequency, boolean acc, boolean gyro){
        sFrequency = frequency;
        freqToInteger();
        setBtType(acc, gyro);
    }

    private void freqToInteger() {
        switch (sFrequency){
            case "13 Hz":
                sFrequencyInteger = 13;
                break;
            case "26 Hz":
                sFrequencyInteger = 26;
                break;
            case "52 Hz":
                sFrequencyInteger = 52;
                break;
            case "104 Hz":
                sFrequencyInteger = 104;
                break;
            case "208 Hz":
                sFrequencyInteger = 208;
                break;
            case "416 Hz":
                sFrequencyInteger = 416;
                break;
            case "833 Hz":
                sFrequencyInteger = 833;
                break;
        }
    }

    public String getFrequency(){return sFrequency;}

    public int getFrequencyInteger() {return sFrequencyInteger;}

    public void setFrequency(String s){
        sFrequency = s;
        freqToInteger();
    }
    public void setBtType(boolean accOn, boolean gyroOn){
        AccOn = accOn;
        GyroOn = gyroOn;

        if (AccOn && GyroOn){
            command = "/Meas/IMU6/";
        } else if (AccOn){
            command = "/Meas/acc/";
        } else if (GyroOn){
            command = "/Meas/gyro/";
        } else{
            Log.i("settings", "No bluetoothtype set");
        }

    }

    public boolean getAcc(){return AccOn;}

    public boolean getGyro(){return GyroOn;}

    public String getCommand(){
        return command + sFrequencyInteger;
    }
}
