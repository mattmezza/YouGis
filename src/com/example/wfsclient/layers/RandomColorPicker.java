package com.example.wfsclient.layers;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by simone on 23/06/15.
 */
public class RandomColorPicker {
    private static List<Integer[]> usedColors;
    private static Random random;
    private static int accuracy;

    static {
        usedColors = new ArrayList<Integer[]>();
        usedColors.add(new Integer[] {255, 255, 255});
        random = new Random();
        accuracy = 10;
    }

    public static void setAccuracy(int pAccuracy) {
        accuracy = pAccuracy;
    }

    public static int getColor() {
        Integer[] bestColor = new Integer[] {0, 0, 0};

        double minDistance = Double.POSITIVE_INFINITY;

        for (int i = 0; i < accuracy; i++) {
            Integer[] randomColor = randomColor();
            double distance = distance(randomColor);
            if (distance < minDistance) {
                bestColor = randomColor;
                minDistance = distance;
            }
        }

        usedColors.add(bestColor);

        return Color.rgb(bestColor[0], bestColor[1], bestColor[2]);
    }

    private static Integer[] randomColor() {
        return new Integer[] {random.nextInt(256), random.nextInt(256), random.nextInt(256)};
    }

    private static double distance(Integer[] color1) {
        double totDistance = 0;
        for (Integer[] color : usedColors) {
            totDistance += Math.pow(baseDistance(color, color1), 2.0);
        }

        return totDistance;
    }

    private static double baseDistance(Integer[] color1, Integer[] color2) {
        return Math.sqrt(
                    Math.pow(color1[0]-color2[0], 2.0) +
                    Math.pow(color1[1]-color2[1], 2.0) +
                    Math.pow(color1[2]-color2[2], 2.0)
                );
    }

    private static Integer[] calculateCentroid() {
        Integer[] centroid = new Integer[] {0, 0, 0};
        for (Integer[] color : usedColors) {
            centroid[0] += color[0];
            centroid[1] += color[1];
            centroid[2] += color[2];
        }

        centroid[0] /= usedColors.size();
        centroid[1] /= usedColors.size();
        centroid[2] /= usedColors.size();

        return centroid;
    }
}
