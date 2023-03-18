package de.greensurvivors.findme.dataObjects;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeHelper {
    public static final byte TICKS_PER_SECOND = 20;

    private final Pattern
            PATTERN_TICK = Pattern.compile("(\\d+)t"),
            PATTERN_SECOND = Pattern.compile("(\\d+)s"),
            PATTERN_MINUTE = Pattern.compile("(\\d+)m");

    private final long ticks;
    private final long minutes;
    private final long seconds;

    private long convertToTicks_Implement(long minutes, long seconds){
        return (TimeUnit.MINUTES.toSeconds(minutes) + seconds) * TICKS_PER_SECOND;
    }

    private long[] convertFromTicks_Implement(long ticks){
        long[] result = new long[2];
        long temp = ticks / TICKS_PER_SECOND;

        result[0] = TimeUnit.SECONDS.toMinutes(temp);
        result[1] = temp % 60;

        return result;
    }

    public TimeHelper(long ticks){
        long[] temp = convertFromTicks_Implement(ticks);
        this.ticks = ticks;
        this.minutes = temp[0];
        this.seconds = temp[1];
    }

    public TimeHelper(long minutes, long seconds){
        this.minutes = minutes;
        this.seconds = seconds;
        this.ticks = convertToTicks_Implement(minutes, seconds);
    }

    public TimeHelper(long minutes, long seconds, long ticks){
        this.ticks = ticks + convertToTicks_Implement(minutes, seconds);

        long[] temp = convertFromTicks_Implement(this.ticks);
        this.minutes = temp[0];
        this.seconds = temp[1];
    }

    public TimeHelper(String string) {
        Matcher matcher = PATTERN_TICK.matcher(string);
        long tempTicks;
        long tempSeconds;
        long tempMinutes;

        if (matcher.find()){
            tempTicks = Integer.parseInt(matcher.group(1));
        } else {
            tempTicks = 0;
        }

        matcher = PATTERN_SECOND.matcher(string);
        if (matcher.find()){
            tempSeconds = Integer.parseInt(matcher.group(1));
        } else {
            tempSeconds = 0;
        }

        matcher = PATTERN_MINUTE.matcher(string);
        if (matcher.find()){
            tempMinutes = Integer.parseInt(matcher.group(1));
        } else {
            tempMinutes = 0;
        }

        this.ticks = tempTicks + convertToTicks_Implement(tempMinutes, tempSeconds);

        long[] temp = convertFromTicks_Implement(this.ticks);
        this.minutes = temp[0];
        this.seconds = temp[1];
    }

    public long getTicks() {
        return ticks;
    }

    public long getMinutes() {
        return minutes;
    }

    public long getSeconds() {
        return seconds;
    }
}
