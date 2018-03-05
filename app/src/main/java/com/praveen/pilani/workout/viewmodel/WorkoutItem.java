

package com.praveen.pilani.workout.viewmodel;


import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.praveen.pilani.workout.R;
import com.praveen.pilani.workout.model.DayOfWeek;
import com.praveen.pilani.workout.model.FitActivity;
import com.praveen.pilani.workout.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.praveen.pilani.workout.util.Lists.map;

public class WorkoutItem {
    final public long id;
    final public int durationInMinutes;
    final public int calories;
    final public String label;
    final public String scheduleDisplay;
    public final FitActivity activityType;

    private WorkoutItem(long id, FitActivity activityType, int durationInMinutes, int calories, @Nullable String label, @NonNull String scheduleDisplay) {
        this.id = id;
        this.activityType = activityType;
        this.durationInMinutes = durationInMinutes;
        this.calories = calories;
        this.label = label;
        this.scheduleDisplay = scheduleDisplay;
    }

    public static WorkoutItem getForIdHack(long id) {
        return new WorkoutItem(id, null, 0, 0, "", "");
    }

    @Override
    public String toString() {
        return "WorkoutItem{" + "id=" + id +
                ", activityType='" + activityType + '\'' +
                ", durationInMinutes=" + durationInMinutes +
                ", calories=" + calories +
                ", label='" + label + '\'' +
                ", scheduleDisplay='" + scheduleDisplay + '\'' +
                '}';
    }

    public static class Builder {
        private final Context context;
        private final List<ScheduleItem> scheduleItems = new ArrayList<>();
        private long workoutId;
        private String activityTypeKey;
        private int durationInMinutes;
        private int calories;
        private String label;

        public Builder(Context context) {
            this.context = context;
        }


        public WorkoutItem build(DayOfWeek[] week) {
            FitActivity fitActivity = FitActivity.fromKey(activityTypeKey, context.getResources());

            Collections.sort(scheduleItems, new ScheduleItem.ByCalendar(week));

            String schedulesDisplay = Strings.join(", ", map(scheduleItems, this::formatScheduleShort));
            return new WorkoutItem(workoutId, fitActivity, durationInMinutes, calories, label, schedulesDisplay);
        }

        private String formatScheduleShort(ScheduleItem scheduleItem) {
            Resources resources = context.getResources();
            String dayOfWeek = resources.getString(scheduleItem.dayOfWeek.fullNameResId);
            return resources.getString(R.string.weekday_time_format, dayOfWeek, scheduleItem.time);
        }

        public void addSchedule(ScheduleItem scheduleItem) {
            this.scheduleItems.add(scheduleItem);
        }

        public Builder withWorkoutId(long workoutId) {
            this.workoutId = workoutId;
            return this;
        }

        public Builder withActivityTypeKey(String activityTypeKey) {
            this.activityTypeKey = activityTypeKey;
            return this;
        }

        public Builder withDurationInMinutes(int durationInMinutes) {
            this.durationInMinutes = durationInMinutes;
            return this;
        }

        public Builder withCalories(int calories) {
            this.calories = calories;
            return this;
        }

        public Builder withLabel(String label) {
            this.label = label;
            return this;
        }
    }
}
