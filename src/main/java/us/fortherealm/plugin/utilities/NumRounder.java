package us.fortherealm.plugin.utilities;

public class NumRounder {

    // rounds to 2 decimal places
    public static double round(double value) {
        int scale = (int) Math.pow(10, 2);
        return (double) Math.round(value * scale) / scale;
    }
}
