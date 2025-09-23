package com.web.back.mappers;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

public final class DaysOfTheWeekMapper {
    private DaysOfTheWeekMapper() {
    }
    public static List<DayOfWeek> mapDaysOfWeek(short daysBitmask) {
        List<DayOfWeek> days = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            // Monday is 2, Tuesday is 4, ..., Sunday is 64
            int bit = 1 << (day.getValue()); // Monday=1<<1=2, Tuesday=1<<2=4, ..., Sunday=1<<7=128
            if ((daysBitmask & bit) != 0) {
                days.add(day);
            }
        }
        return days;
    }

    public static short mapToBitmask(List<DayOfWeek> days) {
        short bitmask = 0;
        for (DayOfWeek day : days) {
            int bit = 1 << day.getValue(); // Monday=1<<1=2, ..., Sunday=1<<7=128
            bitmask |= (short) bit;
        }
        return bitmask;
    }
}
