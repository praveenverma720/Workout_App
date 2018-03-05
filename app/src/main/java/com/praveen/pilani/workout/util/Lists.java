
package com.praveen.pilani.workout.util;

import android.support.annotation.NonNull;

import java.util.AbstractList;
import java.util.Comparator;
import java.util.List;

/**
 * Helper methods for {@link java.util.List} instances.
 */
public class Lists {
    private Lists() {
        // do not instantiate
    }


    @NonNull
    public static <S, T> List<T> map(@NonNull List<S> source, @NonNull Function<S, T> transformation) {
        return new AbstractList<T>() {
            @Override
            public T get(int location) {
                return transformation.apply(source.get(location));
            }

            @Override
            public int size() {
                return source.size();
            }
        };
    }


    public static <T> int bisectLeft(@NonNull List<T> list, @NonNull Comparator<T> ordering, @NonNull T item) {
        int low = 0;
        int high = list.size();
        int mid;

        while (low < high) {
            mid = (low + high) / 2;
            if (ordering.compare(item, list.get(mid)) <= 0) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }
        return low;
    }
}
