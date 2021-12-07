/*
Class to process the data
Used in MainActivity
Jitse van Esch & Elisa Perini
12.12.21
 */
package kth.jjve.moveable.utils;

import android.util.Log;

import kth.jjve.moveable.MainActivity;

public class DataProcess {

    /*--------------------------- LOG -----------------------*/
    private static final String LOG_TAG = DataProcess.class.getSimpleName();

    private double filtered_value_EMWA;
    private double filtered_value_complimentary;

    //TODO check for recursive implementation?
    //TODO methods public or private

    public static float[] rotFromGyroscope(float gx, float gy, float gz, float previous_rot_value_x, float previous_rot_value_y, float previous_rot_value_z, float dT) {
        float[] rot = new float[3];
        rot[0] = previous_rot_value_x + (dT * gx);
        Log.i(LOG_TAG, "rotx = " + ", previous_rot_value = " + previous_rot_value_x + ", dT = " + dT + ", gx = " + gx);
        rot[1] = previous_rot_value_y + (dT * gy);
        rot[2] = previous_rot_value_z + (dT * gz);
        return rot;
    }

    public static float[] rotFromAcc(double ax, double ay, double az) {
        float[] rot = new float[3];
        rot[0] = (float) Math.toDegrees(Math.atan(ay / (Math.sqrt(Math.pow(ax, 2) + Math.pow(ax, 2)))));
        rot[1] = (float) Math.toDegrees(Math.atan(ax / (Math.sqrt(Math.pow(ay, 2) + Math.pow(ax, 2)))));
        rot[2] = (float) Math.toDegrees(Math.atan( (Math.sqrt(Math.pow(ax, 2) + Math.pow(ay, 2)) / az )));
        return rot;
    }

    public double EMWA_filter(double value, double alpha) {
        filtered_value_EMWA =(alpha * filtered_value_EMWA + (1-alpha)*value);
        return filtered_value_EMWA;
    }

    public double complimentaryFilter(double gyroValue, double accRotValue, double alpha, int dT) {
        filtered_value_complimentary =(alpha * filtered_value_complimentary) + (dT * gyroValue) + ((1 - alpha) * accRotValue);
        return filtered_value_complimentary;
    }

}
