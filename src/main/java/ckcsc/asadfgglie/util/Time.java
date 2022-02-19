package ckcsc.asadfgglie.util;

public class Time {
    public static final long second = 1000;
    public static final long minute = second * 60;
    public static final long hour = minute * 60;

    private Time(){}

    public static String toTimeString(long milliseconds){
        long h = milliseconds / hour;
        long min = (milliseconds % hour) / minute;
        long s = (milliseconds % hour) % minute / second;
        if(h != 0){
            return String.format("%d:%02d:%02d", h, min, s);
        }
        else {
            return String.format("%02d:%02d", min, s);
        }
    }
}
