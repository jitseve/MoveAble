/*
Class to process the data
Used in MainActivity
Jitse van Esch & Elisa Perini
12.12.21
 */
package kth.jjve.moveable.utils;

import android.util.Log;


public class DataProcess {

    /*--------------------------- LOG -----------------------*/
    private static final String LOG_TAG = DataProcess.class.getSimpleName();
    
    private float acc_x, acc_y, acc_z;
    private float gyro_x, gyro_y, gyro_z;

    private float dT;
    private float[] gyroRot = {0, 0, 0};
    private float[] rotAcc = new float[3];
    private final float alpha = 0.5F;
    private float filtered_value_EMWA = 0;
    private float filtered_value_complimentary = 0;
    
    public void setAcceleration(float ax, float ay, float az) {
        acc_x = ax;
        acc_y = ay;
        acc_z = az;
    }

    public void setGyro(float gx, float gy, float gz, float dt) {
        gyro_x = gx;
        gyro_y = gy;
        gyro_z = gz;
        dT = dt;
    }

    public void rotFromGyroscope() {
        // method to get the rotation (in degrees) from the raw values provided by the gyroscope
        gyroRot[0] = gyroRot[0] + (dT * gyro_x);
        gyroRot[1] = gyroRot[1] + (dT * gyro_y);
        gyroRot[2] = gyroRot[2] + (dT * gyro_z);
    }

    public void rotFromAcc() {
        // method to get the rotation (in degrees) from the values obtained by the accelerometer
        rotAcc[0] = (float) Math.toDegrees(Math.atan(acc_y / (Math.sqrt(Math.pow(acc_x, 2) + Math.pow(acc_x, 2)))));
        rotAcc[1] = (float) Math.toDegrees(Math.atan(acc_x / (Math.sqrt(Math.pow(acc_y, 2) + Math.pow(acc_x, 2)))));
        rotAcc[2] = (float) Math.toDegrees(Math.atan( (Math.sqrt(Math.pow(acc_x, 2) + Math.pow(acc_y, 2)) / acc_z )));
    }

    public float EMWA_filter() {
        // method for exponential moving average filtering
        filtered_value_EMWA =(alpha * filtered_value_EMWA + (1-alpha)*rotAcc[0]);
        return filtered_value_EMWA;
    }

    public float complimentaryFilter() {
        filtered_value_complimentary =alpha * (filtered_value_complimentary + (dT * gyroRot[0])) + (1 - alpha) * rotAcc[0];
        return filtered_value_complimentary;
    }

}
