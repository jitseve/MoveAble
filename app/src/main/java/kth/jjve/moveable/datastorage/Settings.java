package kth.jjve.moveable.datastorage;

import java.io.Serializable;

public class Settings implements Serializable {
    private String sFrequency;
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
                break;
            case "26 Hz":
                sRetriever = "47 77 101 97 115 47 73 77 85 54 47 50 54";
                break;
            case "52 Hz":
                sRetriever = "47 77 101 97 115 47 73 77 85 54 47 53 50";
                break;
            case "104 Hz":
                sRetriever = "47 77 101 97 115 47 73 77 85 54 47 49 48 52";
                break;
            case "208 Hz":
                sRetriever = "47 77 101 97 115 47 73 77 85 54 47 50 48 56";
                break;
            case "416 Hz":
                sRetriever = "47 77 101 97 115 47 73 77 85 54 47 52 49 54";
                break;
            case "833 Hz":
                sRetriever = "47 77 101 97 115 47 73 77 85 54 47 56 51 51";
                break;
        }
    }

    private void checkOldRetriever(){
        // sets the reset to true is the oldRetriever is not
        // equal to the current retriever
        if (sOldRetriever != null) sReset = !sOldRetriever.equals(sRetriever);
    }

    public String getFrequency(){return sFrequency;}

    public boolean getReset(){return sReset;}

    public String getRetriever(){return sRetriever;}

    public void setFrequency(String s){
        sFrequency = s;
        setRetriever();
        checkOldRetriever();
    }
}
