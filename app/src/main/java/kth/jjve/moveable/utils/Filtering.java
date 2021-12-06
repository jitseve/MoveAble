package kth.jjve.moveable.utils;

public class Filtering {

    private double filtered_value_EMWA;
    private double filtered_value_complimentary;

    //TODO check for recursive implementation?
    //TODO float or double?
    //TODO methods public or private

    public double EMWA_filter(double value, double alpha) {
        filtered_value_EMWA =(alpha * filtered_value_EMWA + (1-alpha)*value);
        return filtered_value_EMWA;
    }

    public double complimentaryFilter(double gyroValue, double accRotValue, double alpha, int dT) {
        filtered_value_complimentary =(alpha * filtered_value_complimentary) + (dT * gyroValue) + ((1 - alpha) * accRotValue);
        return filtered_value_complimentary;
    }

}
