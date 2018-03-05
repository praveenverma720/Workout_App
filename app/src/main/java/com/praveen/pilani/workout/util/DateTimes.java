

package com.praveen.pilani.workout.util;

import com.praveen.pilani.workout.model.DayOfWeek;

import java.util.Calendar;

/**
 * Helper methods for manipulating datetime values expressed as posix timestamps
 */
public class DateTimes {
    private DateTimes() {
        // do not instantiate
    }


    public static long getNextOccurrence(long now, DayOfWeek dayOfWeek, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        if (calendar.getTimeInMillis() <= now) {
            calendar.add(Calendar.DATE, 1);
        }

        // Am I glad that there are only 7 week days.
        while (calendar.get(Calendar.DAY_OF_WEEK) != dayOfWeek.calendarConst) {
            calendar.add(Calendar.DATE, 1);
        }

        return calendar.getTimeInMillis();
    }
}
