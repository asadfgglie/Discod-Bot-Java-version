package ckcsc.asadfgglie.util;

import org.slf4j.Logger;

import java.util.Arrays;

public class Array {
    private Array(){}

    public static int maxIndex(float[] array){
        if (array == null){
            return -1;
        }

        int index = 0;

        for(int i = 1; i < array.length; i++){
            if(array[index] < array[i]){
                index = i;
            }
        }

        return index;
    }

    public static float[][] copy(float[][] array){
        float[][] tmp = new float[array.length][];

        for(int i = 0; i < array.length; i++){
            tmp[i] = Arrays.copyOf(array[i], array[i].length);
        }

        return tmp;
    }

    public static void printArray(float[][] array, Logger logger, LogLevel level){
        for (float[] floats : array) {
            StringBuilder strBuf = new StringBuilder();
            for (float aFloat : floats) {
                strBuf.append(String.format("%-4d", (int) aFloat));
            }
            log(logger, level, strBuf.toString(), null);
        }
    }

    private static void log(Logger logger, LogLevel level, String msg, Throwable cause){
        switch (level){
            case info:
                if(cause != null) {
                    logger.info(msg, cause);
                }
                else {
                    logger.info(msg);
                }
                break;
            case debug:
                if(cause != null) {
                    logger.debug(msg, cause);
                }
                else {
                    logger.debug(msg);
                }
                break;
            case error:
                if(cause != null) {
                    logger.error(msg, cause);
                }
                else {
                    logger.error(msg);
                }
                break;
            case warn:
                if(cause != null) {
                    logger.warn(msg, cause);
                }
                else {
                    logger.warn(msg);
                }
                break;
        }
    }

    public static void roll(float[][] array, int xRoll, int yRoll) {
        float[][] tmp = Array.copy(array);

        for(int i = 0; i < array.length; i++){
            for(int j = 0; j < array[i].length; j++){
                int y = i + yRoll;
                int x = j + xRoll;

                try {
                    array[i][j] = tmp[y][x];
                }
                catch (ArrayIndexOutOfBoundsException e){
                    if(x < 0){
                        x += array[i].length;
                    }
                    else if(x >= array[i].length){
                        x -= array[i].length;
                    }
                    if(y < 0){
                        y += array.length;
                    }
                    else if(y >= array.length){
                        y -= array.length;
                    }

                    array[i][j] = tmp[y][x];
                }
            }
        }
    }

    public static double[] centerPoint(float[][] array){
        double m = 0;
        double centerX = 0, centerY = 0;

        for(float[] i : array){
            for(double j :i){
                m += j;
            }
        }

        for(int i = 0; i < array.length; i++){
            for(int j = 0; j < array[i].length; j++){
                centerX += i * array[i][j] / m;
                centerY += j * array[i][j] / m;
            }
        }

        return new double[]{centerX, centerY};
    }

    public static void divide(float[][] array, float number){
        for(float[] floats :array){
            for(int i = 0; i < floats.length; i++){
                floats[i] /= number;
            }
        }
    }

    public enum LogLevel{
        info, error, debug, warn
    }
}
