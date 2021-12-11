package kth.jjve.moveable.datastorage;

import java.io.Serializable;

public class Settings implements Serializable {
    private String sFrequency;
    private int sFrequencyInteger;
    private String sRetriever;
    private String sOldRetriever;

    public Settings(String frequency){
        sFrequency = frequency;
        setRetriever();
    }

    private void setRetriever() {
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
        setRetriever();
    }
}
