

package com.praveen.pilani.workout.util.ui;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;


public class BackgroundTintListAnimator {
    public static ObjectAnimator create(@NonNull Context context, @NonNull Object target, @ColorRes int startColor, @ColorRes int endColor, long duration) {
        ColorStateList startColorStateList = ColorStateList.valueOf(ContextCompat.getColor(context, startColor));
        ColorStateList endColorStateList = ColorStateList.valueOf(ContextCompat.getColor(context, endColor));
        ObjectAnimator animator = ObjectAnimator.ofObject(target, "backgroundTintList", ColorStateListEvaluator.INSTANCE, startColorStateList, endColorStateList);
        animator.setDuration(duration);
        return animator;
    }

    private static class ColorStateListEvaluator implements TypeEvaluator<ColorStateList> {
        static final ColorStateListEvaluator INSTANCE = new ColorStateListEvaluator();

        private final ArgbEvaluator argbEvaluator = new ArgbEvaluator();

        @Override
        public ColorStateList evaluate(float fraction, ColorStateList startValue, ColorStateList endValue) {
            return ColorStateList.valueOf((Integer) argbEvaluator.evaluate(fraction, startValue.getDefaultColor(), endValue.getDefaultColor()));
        }
    }
}
