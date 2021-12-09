package kth.jjve.moveable.utilities;
/*
Class to change the visibility of views on the UI
 */

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

public class VisibilityChanger {

    //change visibility of three views
    public static void setViewVisibility(ImageView v1, ImageView v2, ImageView v3){
        v1.setVisibility(View.VISIBLE);
        v2.setVisibility(View.INVISIBLE);
        v3.setVisibility(View.INVISIBLE);
    }

    public static void setViewsInvisible(LineChart l1){
        //Todo: also set views for internal things to invisible
        l1.setVisibility(View.INVISIBLE);
    }
}
