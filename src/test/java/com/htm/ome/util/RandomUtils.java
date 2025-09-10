package com.htm.ome.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {

    public static double randomDecimalWith2Precision(double min, double max) {
        double value = ThreadLocalRandom.current().nextDouble(min, max);
        return Math.round(value * 100.0) / 100.0;
    }

}
