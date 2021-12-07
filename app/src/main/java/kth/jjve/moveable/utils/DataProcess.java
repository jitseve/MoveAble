/*
Class to process the data
Used in MainActivity
Jitse van Esch & Elisa Perini
12.12.21
 */
package kth.jjve.moveable.utils;

public class DataProcess {

    public static double[] rotFromAcc;
    private double filtered_value_EMWA;
    private double filtered_value_complimentary;

    //TODO check for recursive implementation?
    //TODO methods public or private

    public static double[] rotFromGyroscope(double gyro_value_x, double gyro_value_y, double gyro_value_z, double previous_rot_value_x, double previous_rot_value_y, double previous_rot_value_z, double dT) {
        double[] rot = new double[3];
        rot[0] = previous_rot_value_x + (dT * gyro_value_x);
        rot[1] = previous_rot_value_y + (dT * gyro_value_y);
        rot[2] = previous_rot_value_z + (dT * gyro_value_z);
        return rot;
    }

    public static double[] rotFromAcc(double ax, double ay, double az) {
        double[] rot = new double[3];
        rot[0] = Math.toDegrees(Math.atan(ay / (Math.sqrt(Math.pow(ax, 2) + Math.pow(ax, 2)))));
        rot[1] = Math.toDegrees(Math.atan(ax / (Math.sqrt(Math.pow(ay, 2) + Math.pow(ax, 2)))));
        rot[2] = Math.toDegrees(Math.atan( (Math.sqrt(Math.pow(ax, 2) + Math.pow(ay, 2)) / az )));
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
