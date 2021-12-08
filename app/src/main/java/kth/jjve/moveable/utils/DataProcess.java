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

    //TODO check for recursive implementation?
    //TODO methods public or private

    public static float[] rotFromGyroscope(float gx, float gy, float gz, float previous_rot_value_x, float previous_rot_value_y, float previous_rot_value_z, float dT) {
        // method to get the rotation (in degrees) from the raw values provided by the gyroscope
        float[] rot = new float[3];
        rot[0] = previous_rot_value_x + (dT * gx);
        rot[1] = previous_rot_value_y + (dT * gy);
        rot[2] = previous_rot_value_z + (dT * gz);
        return rot;
    }

    public static float[] rotFromAcc(float ax, float ay, float az) {
        // method to get the rotation (in degrees) from the values obtained by the accelerometer
        float[] rot = new float[3];
        rot[0] = (float) Math.toDegrees(Math.atan(ay / (Math.sqrt(Math.pow(ax, 2) + Math.pow(ax, 2)))));
        rot[1] = (float) Math.toDegrees(Math.atan(ax / (Math.sqrt(Math.pow(ay, 2) + Math.pow(ax, 2)))));
        rot[2] = (float) Math.toDegrees(Math.atan( (Math.sqrt(Math.pow(ax, 2) + Math.pow(ay, 2)) / az )));
        return rot;
    }

    public static float EMWA_filter(float value, float alpha, float previous_value) {
        // method for exponential moving average filtering
        float filtered_value_EMWA =(alpha * previous_value + (1-alpha)*value);
        return filtered_value_EMWA;
    }

    public static float complimentaryFilter(float gyroValue, float accRotValue, float alpha, float previous_value, float dT) {
        float filtered_value_complimentary =alpha * (previous_value + (dT * gyroValue)) + (1 - alpha) * accRotValue;
        return filtered_value_complimentary;
    }

}
