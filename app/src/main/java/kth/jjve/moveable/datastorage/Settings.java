package kth.jjve.moveable.datastorage;

import java.io.Serializable;

public class Settings implements Serializable {
    private String sFrequency;
    private int sFrequencyInteger;
    private String sRetriever;
    private String sOldRetriever;
    private boolean sReset;

    public Settings(String frequency){
        sFrequency = frequency;
        setRetriever();
    }

    private void setRetriever() {
        if( sRetriever != null){
            sOldRetriever = sRetriever;
        }
        switch (sFrequency){
            case "13 Hz":
                sRetriever = "47 77 101 97 115 47 73 77 85 54 47 49 51";
                sFrequencyInteger = 13;
                break;
            case "26 Hz":
                sRetriever = "47 77 101 97 115 47 73 77 85 54 47 50 54";
                sFrequencyInteger = 26;
                break;
            case "52 Hz":
                sRetriever = "47 77 101 97 115 47 73 77 85 54 47 53 50";
                sFrequencyInteger = 52;
                break;
            case "104 Hz":
                sRetriever = "47 77 101 97 115 47 73 77 85 54 47 49 48 52";
                sFrequencyInteger = 104;
                break;
            case "208 Hz":
                sRetriever = "47 77 101 97 115 47 73 77 85 54 47 50 48 56";
                sFrequencyInteger = 208;
                break;
            case "416 Hz":
                sRetriever = "47 77 101 97 115 47 73 77 85 54 47 52 49 54";
                sFrequencyInteger = 416;
                break;
            case "833 Hz":
                sRetriever = "47 77 101 97 115 47 73 77 85 54 47 56 51 51";
                sFrequencyInteger = 833;
                break;
        }
    }

    private void checkOldRetriever(){
        // sets the reset to true is the oldRetriever is not
        // equal to the current retriever
        if (sOldRetriever != null) sReset = !sOldRetriever.equals(sRetriever);
    }

    public String getFrequency(){return sFrequency;}

    public int getFrequencyInteger() {return sFrequencyInteger;}

    public boolean getReset(){return sReset;}

    public String getRetriever(){return sRetriever;}

    public void setFrequency(String s){
        sFrequency = s;
        setRetriever();
        checkOldRetriever();
    }
}
