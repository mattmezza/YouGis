package com.example.wfsclient.layers;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by simone on 23/06/15.
 */
public class RandomColorPicker {
    private static final float GOLDEN_RATION_CONJUGATE = 0.618033988749895f;
    private static float saturation;
    private static float value;
    private static float lastHue;

    static {
        lastHue = (float)new Random().nextFloat();
        saturation = 0.5f;
        value = 0.95f;
    }

    public static void setSaturation(float pSaturation) {
        saturation = pSaturation;
    }
    public static void setValue(float pValue) {
        value = pValue;
    }

    public static int getColor() {
        lastHue += GOLDEN_RATION_CONJUGATE;
        lastHue %= 1;

        float[] hsv = new float[3];
        hsv[0] = lastHue * 360;
        hsv[1] = saturation;
        hsv[2] = value;

        return Color.HSVToColor(hsv);
    }
}
