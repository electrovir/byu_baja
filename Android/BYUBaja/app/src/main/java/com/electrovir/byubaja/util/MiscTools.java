package com.electrovir.byubaja.util;

import java.util.Date;

/**
 * Created by Simeon on 2/2/18.
 */

public class MiscTools {

    public static float sumArray(float[] array) {
        float sum = 0;
        // I would prefer a .reduce() here but this isn't JavaScript
        for (float item : array) {
            sum += item;
        }
        return sum;
    }
}
