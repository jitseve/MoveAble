package kth.jjve.moveable.utilities;

import android.view.View;
import android.widget.ImageView;

public class VisibilityChanger {

    //change visibility of three views
    public static void setViewVisibility(ImageView v1, ImageView v2, ImageView v3){
        v1.setVisibility(View.VISIBLE);
        v2.setVisibility(View.INVISIBLE);
        v3.setVisibility(View.INVISIBLE);
    }
}
