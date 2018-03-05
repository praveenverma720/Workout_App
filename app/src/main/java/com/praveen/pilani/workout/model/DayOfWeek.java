package com.praveen.pilani.workout.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.praveen.pilani.workout.R;

import java.util.Calendar;


public enum DayOfWeek implements Parcelable {
    MONDAY(Calendar.MONDAY, R.string.monday),
    TUESDAY(Calendar.TUESDAY, R.string.tuesday),
    WEDNESDAY(Calendar.WEDNESDAY, R.string.wednesday),
    THURSDAY(Calendar.THURSDAY, R.string.thursday),
    FRIDAY(Calendar.FRIDAY, R.string.friday),
    SATURDAY(Calendar.SATURDAY, R.string.saturday),
    SUNDAY(Calendar.SUNDAY, R.string.sunday);

    public final int calendarConst;
    public final int fullNameResId;

    DayOfWeek(int calendarConst, @StringRes int fullNameResId) {
        this.calendarConst = calendarConst;
        this.fullNameResId = fullNameResId;
    }

    @NonNull
    public static DayOfWeek[] getWeek() {
        return getWeek(Calendar.getInstance());
    }

    @NonNull
    protected static DayOfWeek[] getWeek(Calendar calendar) {
        int firstDay = calendar.getFirstDayOfWeek();
        DayOfWeek[] week = new DayOfWeek[7];
        for (int i = 0; i < 7; i++) {
            int calendarConst = firstDay + i;
            if (calendarConst > 7) {
                calendarConst -= 7;
            }
            week[i] = getByCalendarConst(calendarConst);
        }
        return week;
    }

    public static DayOfWeek getByCalendarConst(int calendarConst) {
        switch (calendarConst) {
            case Calendar.MONDAY:
                return MONDAY;
            case Calendar.TUESDAY:
                return TUESDAY;
            case Calendar.WEDNESDAY:
                return WEDNESDAY;
            case Calendar.THURSDAY:
                return THURSDAY;
            case Calendar.FRIDAY:
                return FRIDAY;
            case Calendar.SATURDAY:
                return SATURDAY;
            case Calendar.SUNDAY:
                return SUNDAY;
            default:
                throw new IllegalArgumentException("Not a java.util.Calendar weekday constant: " + calendarConst);
        }
    }

    // GENERATED START
    // by parcelable AndroidStudio plugin

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(name());
    }

    public static final Creator<DayOfWeek> CREATOR = new Creator<DayOfWeek>() {
        @Override
        public DayOfWeek createFromParcel(final Parcel source) {
            return DayOfWeek.valueOf(source.readString());
        }

        @Override
        public DayOfWeek[] newArray(final int size) {
            return new DayOfWeek[size];
        }
    };

    // GENERATED END
}
